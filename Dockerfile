FROM openjdk:8
VOLUME /tmp
ADD ./build/libs/ipa-api-0.1.0.jar app.jar
RUN sh -c 'touch /app.jar'

ARG DATASOURCE_URL
ENV DATASOURCE_URL=$DATASOURCE_URL

ARG DATASOURCE_USERNAME
ENV DATASOURCE_USERNAME=$DATASOURCE_USERNAME

ARG DATASOURCE_PASSWORD
ENV DATASOURCE_PASSWORD=$DATASOURCE_PASSWORD

ARG SPRING_PROFILE="production"
ENV SPRING_PROFILE=$SPRING_PROFILE

ARG JWT_SIGNING_KEY
ENV JWT_SIGNING_KEY=$JWT_SIGNING_KEY

ARG LOGGING_LEVEL="INFO"
ENV LOGGING_LEVEL=$LOGGING_LEVEL

# (This container's URL) Form of https://website (no trailing slash)
ARG BACKEND_URL
ENV BACKEND_URL=$BACKEND_URL

# Form of https://website (no trailing slash)
ARG FRONTEND_URL
ENV FRONTEND_URL=$FRONTEND_URL

ARG DATAWAREHOUSE_URL
ENV DATAWAREHOUSE_URL=$DATAWAREHOUSE_URL

ARG DATAWAREHOUSE_TOKEN
ENV DATAWAREHOUSE_TOKEN=$DATAWAREHOUSE_TOKEN

ARG DATAWAREHOUSE_PORT="443"
ENV DATAWAREHOUSE_PORT=$DATAWAREHOUSE_PORT

ARG EMAIL_PROTOCOL="smtp"
ENV EMAIL_PROTOCOL=$EMAIL_PROTOCOL

ARG EMAIL_AUTH="false"
ENV EMAIL_AUTH=$EMAIL_AUTH

ARG EMAIL_DEBUG="false"
ENV EMAIL_DEBUG=$EMAIL_DEBUG

ARG EMAIL_HOST
ENV EMAIL_HOST=$EMAIL_HOST

ARG EMAIL_PORT="25"
ENV EMAIL_PORT=$EMAIL_PORT

ARG EMAIL_FROM
ENV EMAIL_FROM=$EMAIL_FROM

# -javaagent:/home/dssadmin/srv/newrelic/newrelic.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", \
                    "-Djava.security.egd=file:/dev/./urandom", \
                    "-Dipa.datasource.url=${DATASOURCE_URL}", \
                    "-Dipa.datasource.username=${DATASOURCE_USERNAME}", \
                    "-Dipa.datasource.password=${DATASOURCE_PASSWORD}", \
                    "-Dipa.spring.profile=${SPRING_PROFILE}", \
                    "-Dipa.jwt.signingkey=${JWT_SIGNING_KEY}", \
                    "-Dipa.logging.level=${LOGGING_LEVEL}", \
                    "-Dipa.url.api=${BACKEND_URL}", \
                    "-Dipa.url.frontend=${FRONTEND_URL}", \
                    "-Djava.security.egd=file:/dev/./urandom", \
                    "-Ddw.url=${DATAWAREHOUSE_URL}", \
                    "-Ddw.token=${DATAWAREHOUSE_TOKEN}", \
                    "-Ddw.port=${DATAWAREHOUSE_PORT}", \
                    "-Dipa.email.protocol=${EMAIL_PROTOCOL}", \
                    "-Dipa.email.auth=${EMAIL_AUTH}", \
                    "-Dipa.email.debug=${EMAIL_DEBUG}", \
                    "-Dipa.email.host=${EMAIL_HOST}", \
                    "-Dipa.email.port=${EMAIL_PORT}", \
                    "-Dipa.email.from=${EMAIL_FROM}", \
                    "-jar", "/app.jar"]
