FROM node:18 AS build

WORKDIR /app

COPY . .

RUN npm install
RUN npm run build

FROM nginx:1.26

COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

CMD sed -i -e 's/$PORT/'"$PORT"'/g' /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'
