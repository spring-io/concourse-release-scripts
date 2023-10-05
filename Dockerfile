FROM ubuntu:jammy-20230916

ARG root=.
ARG jar=build/concourse-release-scripts.jar

COPY ${jar} /concourse-release-scripts.jar

RUN export DEBIAN_FRONTEND=noninteractive
RUN apt-get update
RUN apt-get install --no-install-recommends -y tzdata ca-certificates curl jq
RUN ln -fs /usr/share/zoneinfo/UTC /etc/localtime
RUN dpkg-reconfigure --frontend noninteractive tzdata
RUN rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /opt/jdk
ENV PATH $JAVA_HOME/bin:$PATH
RUN mkdir -p /opt/jdk && \
    cd /opt/jdk && \
    curl -L https://download.bell-sw.com/java/17.0.8.1+1/bellsoft-jdk17.0.8.1+1-linux-amd64.tar.gz | tar xz --strip-components=1
