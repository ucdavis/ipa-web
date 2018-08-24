FROM openjdk:8

EXPOSE 8080

MAINTAINER UC Davis DSS IT "http://it.dss.ucdavis.edu"

ENV IPA_DATASOURCE_URL=""
ENV IPA_DATASOURCE_USERNAME=""
ENV IPA_DATASOURCE_PASSWORD=""
ENV IPA_SPRING_PROFILE="development"
ENV IPA_JWT_SIGNING_KEY=""
ENV IPA_JWT_TIMEOUT=""
ENV IPA_URL_API=""
ENV DW_URL=""
ENV DW_TOKEN=""
ENV DW_PORT=""
ENV SMTP_HOST=""
ENV SMTP_EMAIL_FROM=""
ENV IPA_URL_FRONTEND=""
ENV CAS_URL=""

ADD ./dw.dss.ucdavis.edu.cer dw.dss.ucdavis.edu.cer
RUN keytool -import -noprompt -trustcacerts -alias dss_dw -file dw.dss.ucdavis.edu.cer -storepass changeit -keystore "$JAVA_HOME/jre/lib/security/cacerts"

ADD ./build/libs/ipa-api-0.1.0.jar app.jar

CMD java -Djava.security.egd=file:/dev/./urandom -jar /app.jar
