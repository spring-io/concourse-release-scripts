FROM ubuntu:jammy-20230916

RUN apt-get update
RUN apt-get install --no-install-recommends -y ca-certificates net-tools git curl jq
RUN rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /opt/jdk
ENV PATH $JAVA_HOME/bin:$PATH
RUN mkdir -p /opt/jdk && \
    cd /opt/jdk && \
    curl -L https://download.bell-sw.com/java/17.0.8.1+1/bellsoft-jdk17.0.8.1+1-linux-amd64.tar.gz | tar xz --strip-components=1

ADD https://raw.githubusercontent.com/spring-io/concourse-java-scripts/v0.0.4/concourse-java.sh /opt/