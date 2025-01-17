FROM gradle:8.7.0-jdk11

COPY . /usr/src/server

RUN cd /usr/src/server && rm -rf /usr/src/server/build

WORKDIR "/usr/src/server"

CMD ["gradle", ":test", "--tests", "com.github.romanqed.course.units.*"]
