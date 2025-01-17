FROM gradle:8.7.0-jdk11

COPY . /usr/src/server

RUN cd /usr/src/server && tr -d '\015' <build_e2e.sh >build_e2e.tmp && mv build_e2e.tmp build_e2e.sh && chmod +x build_e2e.sh && ./build_e2e.sh

WORKDIR "/usr/src/server"

CMD ["java", "-jar", "e2e/build/libs/e2e.jar"]
