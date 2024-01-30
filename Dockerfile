FROM gradle:6.9-jdk8 AS builder
USER root
WORKDIR /usr/src
COPY . .
RUN gradle build --no-daemon -x check -x test

FROM amazoncorretto:8-alpine-jre
RUN apk --no-cache add fontconfig freetype
RUN apk --no-cache add openssl

EXPOSE 8080

LABEL maintainer="UC Davis DSS IT http://it.dss.ucdavis.edu"

WORKDIR /usr/src/app

COPY import-rds-certs.sh .
RUN ./import-rds-certs.sh

COPY --from=builder /usr/src/build/libs/ipa-api-0.1.0.jar ipa-api-0.1.0.jar

CMD java -Djava.security.egd=file:/dev/./urandom -jar ./ipa-api-0.1.0.jar
