plugins {
    kotlin("jvm") version "1.8.10"
    `java-library`
    `maven-publish`
    signing
}

group = "io.github.fkohl04"
version = project.version
description = "Monitoring of artifacts that are able to expire in a JVM service using Micrometer."

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.9.5")

    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
    // cryptography
    testImplementation("org.bouncycastle:bcprov-jdk15on:1.70")
    testImplementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

apply("../publishing.gradle.kts")

signing {
    sign(publishing.publications["maven"])
}