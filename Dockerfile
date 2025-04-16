
FROM amazoncorretto:17 as backend

WORKDIR /app
COPY backend/build/libs/backend-0.0.1-SNAPSHOT.jar /app/

FROM nginx:1.26

COPY frontend/dist/frontend/browser /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=backend /app/backend-0.0.1-SNAPSHOT.jar /app/

RUN apt-get update && \
    apt-get install -y openjdk-17-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN echo '#!/bin/bash\n\
java -jar /app/backend-0.0.1-SNAPSHOT.jar &\n\
nginx -g "daemon off;"\n' > /start.sh && \
    chmod +x /start.sh

EXPOSE $PORT

CMD sed -i "s/listen       80/listen       $PORT/" /etc/nginx/conf.d/default.conf && /start.sh