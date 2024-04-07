plugins {
    id("java")
    id("application")
}

application {
    mainClass = "net.protsenko.Main"
}

group = "net.protsenko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}