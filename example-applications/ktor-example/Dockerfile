FROM registry.access.redhat.com/ubi8/openjdk-17
EXPOSE 8080

ENV JAVA_OPTS="-Xmx256m"

ADD build/libs/ktor-example-all.jar /opt/ktor.jar

CMD ["java", "-jar", "/opt/ktor.jar"]
