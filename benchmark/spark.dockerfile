FROM gradle:8.7.0-jdk11

RUN mkdir /app

COPY . /app

WORKDIR /app

RUN gradle spark:shadowJar

CMD ["java", "-jar", "spark/build/libs/spark.jar"]
