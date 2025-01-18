FROM gradle:8.7.0-jdk11

RUN mkdir /app

COPY . /app

WORKDIR /app

RUN gradle javalin:shadowJar

CMD ["java", "-jar", "javalin/build/libs/javalin.jar"]
