FROM gradle:8.14.7-jdk21-corretto-al2023 AS builder
WORKDIR /usr/src

# cache dependencies
COPY build.gradle ./
RUN gradle dependencies --no-daemon

COPY . .
RUN gradle build --no-daemon -x check -x test

FROM amazoncorretto:21-al2023-headless
RUN dnf install -y fontconfig freetype dejavu-sans-fonts openssl wget && \
    dnf clean all

EXPOSE 8080

LABEL maintainer="UC Davis DSS IT http://it.dss.ucdavis.edu"

WORKDIR /usr/src/app

COPY import-rds-certs.sh .
RUN ./import-rds-certs.sh

COPY --from=builder /usr/src/build/libs/ipa-api-0.1.0.jar ipa-api-0.1.0.jar

CMD ["java", "-jar", "ipa-api-0.1.0.jar"]
