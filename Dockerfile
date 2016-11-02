FROM openjdk:8

MAINTAINER UC Davis DSS IT "http://it.dss.ucdavis.edu"

VOLUME /tmp
ADD ./build/libs/ipa-api-0.1.0.jar app.jar
RUN sh -c 'touch /app.jar'

ADD ./ipa-web.env
RUN /bin/bash -c 'source ipa-web.env'

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
