val ktorVersion="2.3.9"
val kotlinVersion="1.9.23"
val prometheusVersion="1.12.3"
val logbackVersion="1.5.3"

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.9"
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
