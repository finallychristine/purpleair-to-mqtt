plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
}

group = "fyi.hellochristine.purpleairtomqtt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
//    implementation(libs.bundles.kmqtt)
//    implementation(libs.bundles.ktoml)

    implementation("io.github.davidepianca98:kmqtt-common-jvm:1.0.0")
    implementation("io.github.davidepianca98:kmqtt-client-jvm:1.0.0")
    implementation("com.akuleshov7:ktoml-core:0.7.1")
    implementation("com.akuleshov7:ktoml-file:0.7.1")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")
    implementation("com.google.inject:guice:7.0.0")
    implementation("io.reactivex.rxjava3:rxjava:3.1.12")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:6.0.0-M1"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")

    testImplementation("org.assertj:assertj-core:4.0.0-M1")

    // testCompileOnly(libs.mockito.inline)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    mockitoAgent(libs.mockito.core) { isTransitive = false }

}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    useJUnitPlatform()
}
