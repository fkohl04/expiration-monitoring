FROM registry.access.redhat.com/ubi8/openjdk-17
EXPOSE 8080

ENV JAVA_OPTS="-Xmx256m"

ADD build/libs/spring-example-0.0.1-SNAPSHOT.jar /opt/spring.jar

ADD src/main/resources/keystore.pfx /res/keystore.pfx
ADD src/main/resources/x509Certificate.crt /res/x509Certificate.crt

ENV PKCS12_LOCATION="/res/keystore.pfx"
ENV X509_LOCATION="/res/x509Certificate.crt"

CMD ["java", "-jar", "/opt/spring.jar"]
