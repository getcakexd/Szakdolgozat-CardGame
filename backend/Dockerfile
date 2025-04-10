FROM amazoncorretto:17

WORKDIR /app

COPY ./build/libs/backend-0.0.1-SNAPSHOT.jar /app/

ENV PORT=8080
EXPOSE $PORT

CMD ["sh", "-c", "java -jar /app/backend-0.0.1-SNAPSHOT.jar"]
