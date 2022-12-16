FROM registry.access.redhat.com/ubi8/openjdk-17
EXPOSE 8080

ENV JAVA_OPTS="-Xmx256m"

ADD build/libs/spring-example-0.0.1-SNAPSHOT.jar /opt/spring.jar

CMD ["java", "-jar", "/opt/spring.jar"]
