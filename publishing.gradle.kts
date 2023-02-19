allprojects {
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "SonaType"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = "${project.properties["ossrhUsername"]}"
                    password = "${project.properties["ossrhPassword"]}"
                }
            }
            maven {
                name = "SonaTypeSnapshot"
                url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                credentials {
                    username = "${project.properties["ossrhUsername"]}"
                    password = "${project.properties["ossrhPassword"]}"
                }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    name.set("${project.group}:${project.name}")
                    description.set(project.description)
                    url.set("http://github.com/fkohl04/expiration-monitoring/tree/master")
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
                            email.set("fkohl04@googlemail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://https://github.com/fkohl04/expiration-monitoring.git")
                        developerConnection.set("scm:git:ssh://github.com:fkohl04/expiration-monitoring.git")
                        url.set("http://github.com/fkohl04/expiration-monitoring/tree/master")
                    }
                }
            }
        }
    }
}