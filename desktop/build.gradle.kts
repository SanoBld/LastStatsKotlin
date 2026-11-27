import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}
val appVersionName: String =
    providers.gradleProperty("versionName").orNull ?: versionProps.getProperty("VERSION_NAME")

// Installers need a plain x.y.z with a major version of 1 or more.
val packageVersionName: String = appVersionName.substringBefore('-').let { v ->
    if (Regex("\\d+\\.\\d+\\.\\d+").matches(v) && !v.startsWith("0.")) v else "1.0.0"
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.sanobld.laststats.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "LastStats"
            packageVersion = packageVersionName
            description = "Last.fm statistics companion"
            vendor = "SanoBld"
            copyright = "SanoBld"
            licenseFile.set(rootProject.file("LICENSE"))
            modules("java.instrument", "java.management", "java.naming", "java.sql", "jdk.unsupported")

            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "LastStats"
                shortcut = true
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "6f1c3a52-8d1e-4b7a-9a52-3d6c0a9b7e11"
            }
            macOS {
                iconFile.set(project.file("icons/icon.icns"))
                bundleID = "com.sanobld.laststats"
                packageName = "LastStats"
            }
            linux {
                iconFile.set(project.file("icons/icon.png"))
                packageName = "laststats"
                debMaintainer = "SanoBld"
                appCategory = "Audio"
            }
        }
    }
}
