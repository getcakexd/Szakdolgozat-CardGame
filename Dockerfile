FROM amazoncorretto:17 AS backend-build

WORKDIR /app
COPY backend /app/
RUN chmod +x ./gradlew && ./gradlew build -x test --no-daemon

FROM node:18 AS frontend-build

WORKDIR /app
COPY frontend /app/

RUN npm install

RUN npm run build

FROM nginx:1.26

COPY --from=frontend-build /app/dist/frontend/browser /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=backend-build /app/build/libs/backend-0.0.1-SNAPSHOT.jar /app/backend.jar


RUN apt-get update && \
    apt-get install -y openjdk-17-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN echo '#!/bin/bash\n\
cat > /usr/share/nginx/html/env-config.js << EOF\n\
window.env = {\n\
  GOOGLE_CLIENT_ID: "${GOOGLE_CLIENT_ID}"\n\
};\n\
EOF\n' > /generate-env-config.sh && \
    chmod +x /generate-env-config.sh


RUN echo '#!/bin/bash\n\
# Generate runtime environment config\n\
/generate-env-config.sh\n\
# Start Spring Boot application\n\
java -Dspring.profiles.active=prod \
-Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
-Dspring.security.user.name=${SPRING_SECURITY_USER_NAME} \
-Dspring.security.user.password=${SPRING_SECURITY_USER_PASSWORD} \
-Dgoogle.client.id=${GOOGLE_CLIENT_ID} \
-Dgoogle.oauth.password.prefix=${GOOGLE_OAUTH_PASSWORD_PREFIX} \
-Dgoogle.oauth.password.suffix=${GOOGLE_OAUTH_PASSWORD_SUFFIX} \
-jar /app/backend.jar &\n\
# Start Nginx\n\
nginx -g "daemon off;"\n' > /start.sh && \
    chmod +x /start.sh

EXPOSE $PORT

CMD sed -i "s/listen       80/listen       $PORT/" /etc/nginx/conf.d/default.conf && /start.sh