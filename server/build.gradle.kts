plugins {
    id("java")
    id("application")
    id("com.google.cloud.tools.jib") version "3.4.2"
}

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.google.cloud.tools:jib-gradle-plugin:3.4.2")
    }
}

apply(plugin = "com.google.cloud.tools.jib")

application {
    mainClass = "net.protsenko.Main"
}

group = "net.protsenko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.flywaydb:flyway-core:10.10.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.10.0")
    implementation ("com.typesafe:config:1.4.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation(project(":common"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.h2database:h2:2.2.224")
}

tasks.test {
    useJUnitPlatform()
}