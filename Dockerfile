FROM amazoncorretto:17 AS backend-build

WORKDIR /app
COPY backend /app/
RUN curl -L https://services.gradle.org/distributions/gradle-7.6-bin.zip -o gradle.zip \
    && unzip gradle.zip \
    && rm gradle.zip
ENV PATH=$PATH:/app/gradle-7.6/bin
RUN if [ -f "./gradlew" ]; then \
        chmod +x ./gradlew && ./gradlew build --no-daemon; \
    else \
        gradle build --no-daemon; \
    fi

FROM node:18 AS frontend-build

WORKDIR /app
COPY frontend /app/
RUN npm install
RUN npm run build

FROM nginx:1.26

COPY --from=frontend-build /app/dist/frontend/browser /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=backend-build /app/build/libs/*.jar /app/backend.jar

RUN apt-get update && \
    apt-get install -y openjdk-17-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN echo '#!/bin/bash\n\
java -jar /app/backend.jar &\n\
nginx -g "daemon off;"\n' > /start.sh && \
    chmod +x /start.sh

EXPOSE $PORT

CMD sed -i "s/listen       80/listen       $PORT/" /etc/nginx/conf.d/default.conf && /start.sh