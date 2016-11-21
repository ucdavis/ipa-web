#!/bin/bash

# Import the dw.dss.ucdavis.edu cert into the container JVM keystore
keytool -import -noprompt -trustcacerts -alias dss_dw -file dw.dss.ucdavis.edu.cer -storepass changeit # -keystore "$JAVAHOME/jre/lib/security/cacerts"

# Ensure MySQL is ready -- it may not be when the container first starts up
# Solution credit: http://stackoverflow.com/questions/25503412/how-do-i-know-when-my-docker-mysql-container-is-up-and-mysql-is-ready-for-taking
while ! mysqladmin ping -h "db" --silent; do
    sleep 1
done

java -Djava.security.egd=file:/dev/./urandom \
                    -Djava.security.egd=file:/dev/./urandom \
                    -Dipa.datasource.url=${DATASOURCE_URL} \
                    -Dipa.datasource.username=${DATASOURCE_USERNAME} \
                    -Dipa.datasource.password=${DATASOURCE_PASSWORD} \
                    -Dipa.spring.profile=${SPRING_PROFILE} \
                    -Dipa.jwt.signingkey=${JWT_SIGNING_KEY} \
                    -Dipa.logging.level=${LOGGING_LEVEL} \
                    -Dipa.url.api=${BACKEND_URL} \
                    -Dipa.url.frontend=${FRONTEND_URL} \
                    -Djava.security.egd=file:/dev/./urandom \
                    -Ddw.url=${DATAWAREHOUSE_URL} \
                    -Ddw.token=${DATAWAREHOUSE_TOKEN} \
                    -Ddw.port=${DATAWAREHOUSE_PORT} \
                    -Dipa.email.protocol=${EMAIL_PROTOCOL} \
                    -Dipa.email.auth=${EMAIL_AUTH} \
                    -Dipa.email.debug=${EMAIL_DEBUG} \
                    -Dipa.email.host=${EMAIL_HOST} \
                    -Dipa.email.port=${EMAIL_PORT} \
                    -Dipa.email.from=${EMAIL_FROM} \
                    -jar /app.jar
