# Installation
1. Ensure Java JDK is installed (Java 8). If it's installed, you'll have the commands `java` and `javac`
2. Type: ./gradlew build
3. Run the jar file: build/libs/ipa-api-0.1.0.jar

# Building with Docker
You can build ipa-web with Docker.

Run the following commands:

0. cd into your ipa-web folder
1. (include the final period) docker build -t build-ipa-web -f Dockerfile-build .
2. Make a directory in your ipa-web called 'build'
3. docker run -v $(pwd)/build:/app/build build-ipa-web
4. Your build/ directory will contain the ipa-web .jar file in build/libs.

# Running with docker
1. docker build -t ipa-web .
2. docker run --env-file=ipa-web.env -p 8080:8080 -t ipa-web

# Misc Notes
docker build -t ipa-web -f Dockerfile-build --build-arg IPA_DATASOURCE_URL="jdbc:mysql://host:3306/schema_name?autoReconnect=true&useSSL=false" --build-arg IPA_DATASOURCE_USERNAME="username" --build-arg IPA_DATASOURCE_PASSWORD="password" .

Note that you'll need to correct the three variables being passed in to be correct. If you wish
to use your host computer's MySQL installation, Docker provides special hostnames to access it:
"host.docker.internal".
