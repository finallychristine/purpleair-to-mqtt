plugins {
    java
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

kotlin { jvmToolchain(libs.versions.jdk.get().toInt()) }

group = "fyi.hellochristine.purpleairtomqtt"
version = file("VERSION").readText().trim()

application {
    mainClass = "fyi.hellochristine.purpleairtomqtt.MainKt"
    applicationDefaultJvmArgs = listOf("-Dname=purpleair-to-mqtt")
}

repositories { mavenCentral() }

ktorfit { compilerPluginVersion.set(libs.versions.ktorfit.compiler) }


// https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    ksp(libs.dagger.compiler)
    ksp(libs.ktorfit.ksp)

    implementation(libs.bundles.kotlinx.serialization)
    implementation(libs.bundles.kotlinx.coroutines)

    implementation(libs.dagger)
    implementation(libs.rxjava)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.logging)

    implementation(libs.hivemq.mqtt.client)
    implementation(libs.bundles.ktoml)
    implementation(libs.ayza)
    implementation(libs.guava)
    implementation(libs.clikt)

    mockitoAgent(libs.mockito.core) { isTransitive = false }

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bundles.mockito)
    testImplementation(libs.assertj.core)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.st4)
    testImplementation(libs.mockserver.client)

    constraints {
        testImplementation(libs.bundles.vulns.transitive.testcontainers)
    }
}

// Enable gradle.lockfile
dependencyLocking {
    lockAllConfigurations()
}

// Ensure fuzzy matched gradle versions don't include beta versions
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

tasks.test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    useJUnitPlatform()
}
