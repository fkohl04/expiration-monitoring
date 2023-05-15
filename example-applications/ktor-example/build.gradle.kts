val ktorVersion="2.3.0"
val kotlinVersion="1.8.20"
val prometheusVersion="1.11.0"
val logbackVersion="1.4.7"

plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.3.0"
}

group = "fkohl04.expiration.monitoring"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":expiration-monitoring-core"))
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
