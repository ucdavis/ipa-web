FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /usr/src

# cache dependencies
COPY build.gradle ./
RUN gradle dependencies --no-daemon

COPY . .
RUN gradle build --no-daemon -x check -x test

FROM eclipse-temurin:21-jre
RUN apt-get update && \
    apt-get install -y \
    fontconfig libfreetype6 fonts-dejavu openssl && \
    rm -rf /var/lib/apt/lists/*

EXPOSE 8080

LABEL maintainer="UC Davis DSS IT http://it.dss.ucdavis.edu"

WORKDIR /usr/src/app

COPY import-rds-certs.sh .
RUN ./import-rds-certs.sh

COPY --from=builder /usr/src/build/libs/ipa-api-0.1.0.jar ipa-api-0.1.0.jar

CMD ["java", "-jar", "ipa-api-0.1.0.jar"]
