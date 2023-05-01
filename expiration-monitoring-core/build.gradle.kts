import java.util.Base64

plugins {
    kotlin("jvm") version "1.8.20"
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
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.6")

    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    // cryptography
    testImplementation("org.bouncycastle:bcprov-jdk15on:1.70")
    testImplementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
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

fun base64Decode(prop: String): String? {
    return project.findProperty(prop)?.let {
        String(Base64.getDecoder().decode(it.toString())).trim()
    }
}

signing {
    val signingKey = base64Decode("signingKey")
    val signingPassword = findProperty("signingPassword")
    useInMemoryPgpKeys(signingKey, signingPassword as String?)
    sign(publishing.publications["maven"])
}