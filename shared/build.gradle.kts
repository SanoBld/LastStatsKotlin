import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}
val appVersionName: String =
    providers.gradleProperty("versionName").orNull ?: versionProps.getProperty("VERSION_NAME")
val appVersionCode: Int =
    providers.gradleProperty("versionCode").orNull?.toInt() ?: versionProps.getProperty("VERSION_CODE").toInt()
val internalKeys: String =
    (providers.gradleProperty("LS_KEYS").orNull ?: System.getenv("LS_KEYS") ?: "")
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("$", "\\$")

// Version and internal keys for every platform, written as a Kotlin file.
val buildKeysDir = layout.buildDirectory.dir("generated/buildkeys")
val generateBuildKeys = tasks.register("generateBuildKeys") {
    inputs.property("versionName", appVersionName)
    inputs.property("versionCode", appVersionCode)
    inputs.property("keys", internalKeys)
    outputs.dir(buildKeysDir)
    doLast {
        val dir = buildKeysDir.get().asFile.resolve("com/sanobld/laststats/core")
        dir.mkdirs()
        dir.resolve("BuildKeys.kt").writeText(
            "package com.sanobld.laststats.core\n\n" +
                "object BuildKeys {\n" +
                "    const val VERSION_NAME = \"$appVersionName\"\n" +
                "    const val VERSION_CODE = $appVersionCode\n" +
                "    const val LS_KEYS = \"$internalKeys\"\n" +
                "}\n",
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(files(buildKeysDir).builtBy(generateBuildKeys))
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.ui)
                api(compose.components.resources)
                api(compose.materialIconsExtended)
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                api(libs.material.kolor)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.okio)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            api(libs.androidx.appcompat)
            api(libs.androidx.core.ktx)
            api(libs.kotlinx.coroutines.android)
        }
        val desktopMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

android {
    namespace = "com.sanobld.laststats.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.resources {
    packageOfResClass = "com.sanobld.laststats.resources"
}
