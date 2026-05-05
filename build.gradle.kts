plugins {
    java
    application
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
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

val prereleaseVersion = Regex(""".*(alpha|beta|rc|cr|m|dev|snapshot|eap|preview)[\.\-\d]*$""", RegexOption.IGNORE_CASE)
val ignoredCandidates = setOf<String>("org.jetbrains.kotlin:kotlin-stdlib")
configurations.all {
    resolutionStrategy.componentSelection.all {
        if (ignoredCandidates.contains(candidate.moduleIdentifier.toString())) {
            return@all
        }

        if (candidate.version.matches(prereleaseVersion)) {
            reject("Pre-release version: ${candidate.version}")
        }
    }
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
}

dependencyLocking {
    lockAllConfigurations()
}

// https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation("com.hivemq:hivemq-mqtt-client:1.+")
    implementation("com.akuleshov7:ktoml-core:0.+")
    implementation("com.akuleshov7:ktoml-file:0.+")

    implementation("io.github.oshai:kotlin-logging-jvm:8.+")
    implementation("com.google.dagger:dagger:2.+")
    implementation("io.ktor:ktor-client-logging:3.+")
    ksp("com.google.dagger:dagger-compiler:2.+")
    implementation("io.reactivex.rxjava3:rxjava:3.+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx3:1.+")

    implementation("org.slf4j:slf4j-api:2.+")
    implementation("ch.qos.logback:logback-classic:1.+")

    implementation("io.ktor:ktor-client-core-jvm:3.+")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:3.+")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.+")
    implementation("io.ktor:ktor-client-logging-jvm:3.+")
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:2.+")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:2.+")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.+")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.+")

    implementation("io.github.hakky54:ayza:10.+")
    implementation("com.google.guava:guava:33.+")

    implementation("com.github.ajalt.clikt:clikt:5.+")

    testImplementation("org.junit.jupiter:junit-jupiter:6.+")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.+")

    val mockito = testImplementation("org.mockito:mockito-core:5.+")
    testImplementation("org.mockito:mockito-junit-jupiter:5.+")
    testImplementation("org.assertj:assertj-core:3.+")

    mockitoAgent(mockito.toString()) { isTransitive = false }

    testImplementation("org.testcontainers:testcontainers:2.+")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.+")
    testImplementation("org.testcontainers:testcontainers-hivemq:2.+")
    testImplementation("org.testcontainers:testcontainers-mockserver:2.+")
    testImplementation("org.antlr:ST4:4.+")
    testImplementation("org.mock-server:mockserver-client-java:5.+")

    // Testcontainers has a *lot* of transitive dependencies with vulns
    constraints {
        testImplementation("org.xmlunit:xmlunit-core:2.+")
        testImplementation("org.bouncycastle:bcprov-jdk18on:1.+")
        testImplementation("org.bouncycastle:bcpkix-jdk18on:1.+")
        testImplementation("com.nimbusds:nimbus-jose-jwt:10.+")
        testImplementation("commons-beanutils:commons-beanutils:1.+")
        testImplementation("io.swagger.parser.v3:swagger-parser:2.+")
        testImplementation("io.netty:netty-common:4.+")
        testImplementation("io.netty:netty-codec:4.+")
        testImplementation("com.jayway.jsonpath:json-path:3.+")
        testImplementation("io.netty:netty-codec-http:4.+")
        testImplementation("io.netty:netty-codec-http2:4.+")
    }
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    useJUnitPlatform()
}
