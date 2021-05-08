FROM docker.io/java:openjdk-8-jdk
MAINTAINER Dara

ENV TZ=America/Los_Angeles
ENV LANG en_US.UTF-8

WORKDIR /app
COPY . .
RUN chmod +x bin/run.sh

CMD [ "sh", "bin/run.sh" ]
