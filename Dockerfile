############################################################
# Dockerfile to build score container images
# Based on Ubuntu with preinstalled java
############################################################

FROM java:7

MAINTAINER Bonczidai Levente

RUN apt-get update

RUN apt-get install maven -y

ADD * /app/

WORKDIR /app/

RUN mvn package

EXPOSE  8080

WORKDIR /score-samples/score-webapp/target/

CMD ["java","-jar","score-webapp-0.1.221-SNAPSHOT.jar"]