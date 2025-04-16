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

RUN mkdir -p /app

COPY --from=frontend-build /app/dist/frontend/browser /usr/share/nginx/html

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

RUN echo 'server {\n\
    listen 8080;\n\
    server_name localhost;\n\
    \n\
    location / {\n\
        root /usr/share/nginx/html;\n\
        index index.html index.htm;\n\
        try_files $uri $uri/ /index.html;\n\
    }\n\
    \n\
    location /api {\n\
        proxy_pass http://localhost:8081;\n\
        proxy_set_header Host $host;\n\
        proxy_set_header X-Real-IP $remote_addr;\n\
    }\n\
    \n\
    error_page 500 502 503 504 /50x.html;\n\
    location = /50x.html {\n\
        root /usr/share/nginx/html;\n\
    }\n\
}\n' > /etc/nginx/conf.d/default.conf

RUN echo '#!/bin/bash\n\
# Generate runtime environment config\n\
/generate-env-config.sh\n\
# Start Spring Boot application on port 8081\n\
java -Dserver.port=8081 -Dspring.profiles.active=prod \
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

RUN echo '#!/bin/bash\n\
# Start the application\n\
PORT=${PORT:-8080}\n\
if [ "$PORT" != "8080" ]; then\n\
  # If PORT is not 8080, use socat to forward from $PORT to 8080\n\
  apt-get update && apt-get install -y socat\n\
  socat TCP-LISTEN:$PORT,fork TCP:localhost:8080 &\n\
fi\n\
# Start the main services\n\
/start.sh\n' > /wrapper.sh && \
    chmod +x /wrapper.sh

EXPOSE $PORT

CMD /wrapper.sh