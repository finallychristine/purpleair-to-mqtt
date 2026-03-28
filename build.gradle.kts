plugins {
    java
    application
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
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

// https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation(libs.bundles.mqtt)
    implementation(libs.bundles.ktoml)

    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")
    implementation("com.google.inject:guice:7.0.0")
    implementation("io.reactivex.rxjava3:rxjava:3.1.12")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.32")

    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")

    // transitive dependency has security vulnerabilities
    implementation("com.google.guava:guava:33.5.0-jre")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("com.github.ajalt.clikt:clikt:5.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:6.0.0-M1"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("org.assertj:assertj-core:4.0.0-M1")

    testImplementation(libs.bundles.mockito)
    mockitoAgent(libs.mockito.core) { isTransitive = false }

}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    useJUnitPlatform()
}
