FROM openjdk:8@sha256:75739abc3852736798b0f0f897c98d8f587368e8e6cf9a2580289e3390b6432b

EXPOSE 8080

LABEL maintainer="UC Davis DSS IT http://it.dss.ucdavis.edu"

WORKDIR /usr/src/app

ARG IPA_DATASOURCE_URL
ENV IPA_DATASOURCE_URL $IPA_DATASOURCE_URL
ARG IPA_DATASOURCE_USERNAME
ENV IPA_DATASOURCE_USERNAME $IPA_DATASOURCE_USERNAME
ARG IPA_DATASOURCE_PASSWORD
ENV IPA_DATASOURCE_PASSWORD $IPA_DATASOURCE_PASSWORD
ARG IPA_SPRING_PROFILE="development"
ENV IPA_SPRING_PROFILE $IPA_SPRING_PROFILE
ARG IPA_JWT_SIGNING_KEY
ENV IPA_JWT_SIGNING_KEY $IPA_JWT_SIGNING_KEY
ARG IPA_JWT_TIMEOUT
ENV IPA_JWT_TIMEOUT $IPA_JWT_TIMEOUT
ARG IPA_URL_API
ENV IPA_URL_API $IPA_URL_API
ARG DW_URL
ENV DW_URL $DW_URL
ARG DW_TOKEN
ENV DW_TOKEN $DW_TOKEN
ARG DW_PORT
ENV DW_PORT $DW_PORT
ARG SMTP_HOST
ENV SMTP_HOST $SMTP_HOST
ARG SMTP_EMAIL_FROM
ENV SMTP_EMAIL_FROM $SMTP_EMAIL_FROM
ARG IPA_URL_FRONTEND
ENV IPA_URL_FRONTEND $IPA_URL_FRONTEND
ARG CAS_URL
ENV CAS_URL $CAS_URL


COPY ./rds-ca-2019-root.pem rds-ca-2019-root.pem
COPY ./rds-ca-2019-us-west-2.pem rds-ca-2019-us-west-2.pem
RUN keytool -import -noprompt -trustcacerts -alias aws_rds_ca_2019_root -file rds-ca-2019-root.pem -storepass changeit -keystore "$JAVA_HOME/jre/lib/security/cacerts"
RUN keytool -import -noprompt -trustcacerts -alias aws_rds_ca_us_west_2 -file rds-ca-2019-us-west-2.pem -storepass changeit -keystore "$JAVA_HOME/jre/lib/security/cacerts"


COPY ./gradle gradle
COPY ./gradlew gradlew
COPY ./build.gradle build.gradle
COPY ./checkstyle.xml checkstyle.xml

# RUN ./gradlew resolveDependencies

COPY ./src src

RUN ./gradlew build -x test

CMD java -Djava.security.egd=file:/dev/./urandom -jar build/libs/ipa-api-0.1.0.jar
