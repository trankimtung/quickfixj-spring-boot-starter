plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
    signing
    `maven-publish`
}

group = property("masterGroup") as String
version = property("masterVersion") as String

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.quickfixj:quickfixj-core:${property("quickfixjVersion")}")
    implementation("org.quickfixj:quickfixj-messages-all:${property("quickfixjVersion")}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("junit:junit:${property("junitVersion")}")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junitJupiterVersion")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junitJupiterVersion")}")
}

the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "quickfixj-spring-boot-starter"
            from(components["java"])
            pom {
                name.set(property("projectName") as String)
                description.set(property("projectDescription") as String)
                url.set(property("projectUrl") as String)
                licenses {
                    license {
                        name.set(property("license") as String)
                        url.set(property("licenseUrl") as String)
                    }
                }
                developers {
                    developer {
                        id.set(property("authorId") as String)
                        name.set(property("authorName") as String)
                        email.set(property("authorEmail") as String)
                    }
                }
                scm {
                    connection.set(property("scmConnection") as String)
                    developerConnection.set(property("scmDeveloperConnection") as String)
                    url.set(property("scmUrl") as String)
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri(property("releasesRepoUrl") as String)
            val snapshotsRepoUrl = uri(property("snapshotsRepoUrl") as String)
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = findProperty("sonatypeUsername") as String
                password = findProperty("sonatypePassword") as String
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
