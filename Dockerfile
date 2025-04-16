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

RUN echo 'server {\n\
    listen PORT;\n\
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
}\n' > /etc/nginx/conf.d/default.conf.template

RUN echo '#!/bin/bash\n\
echo "window.GOOGLE_CLIENT_ID = \"${GOOGLE_CLIENT_ID}\";" > /usr/share/nginx/html/runtime-config.js\n\
\n\
cat > /usr/share/nginx/html/env-config.js << EOF\n\
window.env = {\n\
  GOOGLE_CLIENT_ID: "${GOOGLE_CLIENT_ID}"\n\
};\n\
EOF\n\
\n\
if [ -n "$JAWSDB_MARIA_URL" ]; then\n\
  DB_URL=$(echo $JAWSDB_MARIA_URL | sed "s|mysql://||g")\n\
  DB_USERNAME=$(echo $DB_URL | cut -d: -f1)\n\
  DB_PASSWORD=$(echo $DB_URL | cut -d: -f2 | cut -d@ -f1)\n\
  DB_HOST_PORT=$(echo $DB_URL | cut -d@ -f2 | cut -d/ -f1)\n\
  DB_NAME=$(echo $DB_URL | cut -d/ -f2)\n\
  \n\
  export SPRING_DATASOURCE_URL="jdbc:mariadb://$DB_HOST_PORT/$DB_NAME"\n\
  export SPRING_DATASOURCE_USERNAME=$DB_USERNAME\n\
  export SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD\n\
fi\n\
\n\
java -Dserver.port=8081 \
-Dspring.profiles.active=prod \
-Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
-Dspring.jpa.hibernate.ddl-auto=update \
-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect \
-Dspring.security.user.name=${SPRING_SECURITY_USER_NAME:-admin} \
-Dspring.security.user.password=${SPRING_SECURITY_USER_PASSWORD:-password} \
-Dgoogle.client.id=${GOOGLE_CLIENT_ID} \
-Dgoogle.oauth.password.prefix=${GOOGLE_OAUTH_PASSWORD_PREFIX:-google_auth_} \
-Dgoogle.oauth.password.suffix=${GOOGLE_OAUTH_PASSWORD_SUFFIX:-default_suffix} \
-jar /app/backend.jar &\n\
\n\
sed -i "s/PORT/$PORT/g" /etc/nginx/conf.d/default.conf.template\n\
cp /etc/nginx/conf.d/default.conf.template /etc/nginx/conf.d/default.conf\n\
\n\
nginx -g "daemon off;"\n' > /start.sh && chmod +x /start.sh

EXPOSE $PORT

CMD /start.sh