plugins {
    kotlin("jvm") version "1.8.0"
    `java-library`
    `maven-publish`
}

group = "io.github.fkohl04"
version = project.version
description = ""

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.9.5")
    implementation(kotlin("stdlib-jdk8"))

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "expiration-monitoring-core"

            from(components["java"])

            pom {
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("fkohl04")
                        name.set("Fabian Kohlmann")
                        email.set("todo")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/simpligility/ossrh-demo.git")
                    developerConnection.set("scm:git:ssh://github.com:simpligility/ossrh-demo.git")
                    url.set("http://github.com/simpligility/ossrh-demo/tree/master")
                }
            }
        }
    }
}
