plugins {
    java
    application
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.20"
    id("com.google.devtools.ksp") version "2.3.6"
    id("de.jensklingenberg.ktorfit") version "2.7.2"
}

group = "fyi.hellochristine.purpleairtomqtt"
version = file("VERSION").readText().trim()

application {
    mainClass = "fyi.hellochristine.purpleairtomqtt.MainKt"
    applicationDefaultJvmArgs = listOf("-Dname=purpleair-to-mqtt")
}


repositories {
    mavenCentral()
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
}

// https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation("com.hivemq:hivemq-mqtt-client:1.3.13")
    implementation("com.akuleshov7:ktoml-core:0.7.1")
    implementation("com.akuleshov7:ktoml-file:0.7.1")

    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")
    implementation("com.google.dagger:dagger:2.59.2")
    implementation("io.ktor:ktor-client-logging:3.4.2")
    ksp("com.google.dagger:dagger-compiler:2.59.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx3:1.10.2")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.32")

    implementation("io.ktor:ktor-client-core-jvm:3.4.2")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:3.4.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.4.2")
    implementation("io.ktor:ktor-client-logging-jvm:3.4.2")
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:2.7.2")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:2.7.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("io.github.hakky54:ayza:10.0.4")
    implementation("com.google.guava:guava:33.6.0-jre")

    implementation("com.github.ajalt.clikt:clikt:5.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:6.1.0-M1"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    val mockito = testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("org.assertj:assertj-core:4.0.0-M1")

    mockitoAgent(mockito.toString()) { isTransitive = false }

    testImplementation("org.testcontainers:testcontainers:2.0.4")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.4")
    testImplementation("org.testcontainers:testcontainers-hivemq:2.0.4")
    testImplementation("org.testcontainers:testcontainers-mockserver:2.0.4")
    testImplementation("org.antlr:ST4:4.3.4")
    testImplementation("org.mock-server:mockserver-client-java:5.15.0")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    useJUnitPlatform()
}
