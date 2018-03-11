FROM ubuntu:17.04

MAINTAINER bassages

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get clean

RUN mkdir -p /opt/home-server
ADD build/libs/home-server-1.0.0.jar /opt/home-server
RUN chmod -R 777 /opt/home-server

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/home-server/home-server-1.0.0.jar"]
