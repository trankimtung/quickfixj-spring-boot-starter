rootProject.name = "quickfixj"

pluginManagement {
    val kotlinJvmVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinJvmVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinJvmVersion
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinJvmVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
    }
}

include("quickfixj-spring-boot-starter")
include("quickfixj-spring-boot-starter-example")
