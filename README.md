# Installation
1. Ensure Java JDK is installed (Java 8). If it's installed, you'll have the commands `java` and `javac`
2. Type: ./gradlew build
3. Run the jar file: build/libs/ipa-api-0.1.0.jar

# Running with docker
1. docker build -t ipa-web .
2. docker run --env-file=ipa-web.env -p 8080:8080 -t ipa-web

# When running tests
docker build -t ipa-web -f Dockerfile-build --build-arg IPA_DATASOURCE_URL="jdbc:mysql://host:3306/schema_name?autoReconnect=true&useSSL=false" --build-arg IPA_DATASOURCE_USERNAME="username" --build-arg IPA_DATASOURCE_PASSWORD="password" .

Note that you'll need to correct the three variables being passed in to be correct. If you wish
to use your host computer's MySQL installation, Docker provides special hostnames to access it:
"host.docker.internal".

