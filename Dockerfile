FROM openjdk:8

MAINTAINER UC Davis DSS IT "http://it.dss.ucdavis.edu"

# Set up nginx (used for SSL proxy)
ENV NGINX_VERSION 1.11.5-1~jessie

RUN apt-key adv --keyserver hkp://pgp.mit.edu:80 --recv-keys 573BFD6B3D8FBC641079A6ABABF5BD827BD9BF62 \
	&& echo "deb http://nginx.org/packages/mainline/debian/ jessie nginx" >> /etc/apt/sources.list \
	&& apt-get update \
	&& apt-get install --no-install-recommends --no-install-suggests -y \
						ca-certificates \
						nginx=${NGINX_VERSION} \
						nginx-module-xslt \
						nginx-module-geoip \
						nginx-module-image-filter \
						nginx-module-perl \
						nginx-module-njs \
						gettext-base \
	&& rm -rf /var/lib/apt/lists/*

# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access.log \
	&& ln -sf /dev/stderr /var/log/nginx/error.log

EXPOSE 80 443

RUN ["nginx", "-g", "daemon off;"]

# Set up Java Spring Boot application
VOLUME /tmp
ADD ./build/libs/ipa-api-0.1.0.jar app.jar
RUN sh -c 'touch /app.jar'

ADD ./ipa-web.env
RUN /bin/bash -c 'source ipa-web.env'

# -javaagent:/home/dssadmin/srv/newrelic/newrelic.jar

RUN ["java", "-Djava.security.egd=file:/dev/./urandom", \
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
