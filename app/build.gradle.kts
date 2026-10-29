/*
 * SPDX-FileCopyrightText: Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val toml = rootProject.file("gradle/libs.versions.toml").readText()
val playServicesLocationVersion: String? = if (toml.contains("play-services-location")) {
    toml.lines()
        .firstOrNull { it.trimStart().startsWith("playServicesLocation") }
        ?.substringAfter('"')?.substringBefore('"')
} else null

val versionMajor = rootProject.extra["versionMajor"] as Int
val versionMinor = rootProject.extra["versionMinor"] as Int
val versionPatch = rootProject.extra["versionPatch"] as Int
val computedVersionCode = rootProject.extra["computedVersionCode"] as Int
val computedVersionName = rootProject.extra["computedVersionName"] as String

android {
    namespace = "org.prauga.compass"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.prauga.compass"
        minSdk = 30
        targetSdk = 37
        versionCode = computedVersionCode
        versionName = computedVersionName

        buildConfigField("int", "VERSION_MAJOR", "$versionMajor")
        buildConfigField("int", "VERSION_MINOR", "$versionMinor")
        buildConfigField("int", "VERSION_PATCH", "$versionPatch")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    flavorDimensions += "distribution"
    productFlavors {
        create("gms") {
            dimension = "distribution"
            buildConfigField("boolean", "SHOW_LOCATION_INFO", "true")
        }
        create("foss") {
            dimension = "distribution"
            buildConfigField("boolean", "SHOW_LOCATION_INFO", "false")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

abstract class CopyApk : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val input: DirectoryProperty

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @get:Input
    abstract val baseName: Property<String>

    @TaskAction
    fun taskAction() {
        val outputDir = output.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val builtArtifacts = builtArtifactsLoader.get().load(input.get())
            ?: throw GradleException("Cannot load APKs")
        builtArtifacts.elements.forEach { artifact ->
            File(artifact.outputFile).copyTo(File(outputDir, "${baseName.get()}.apk"))
        }
    }
}

androidComponents {
    onVariants { variant ->
        val flavorName = when (variant.flavorName) {
            "foss" -> "fdroid"
            else -> variant.flavorName
        }
        val copyApk = tasks.register<CopyApk>("copy${variant.name.replaceFirstChar { it.uppercase() }}Apk") {
            output.set(layout.buildDirectory.dir("outputs/apk/renamed/${variant.name}"))
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            baseName.set("PvotCompass-v$computedVersionName-${variant.buildType}-$flavorName")
        }
        variant.artifacts.use(copyApk).wiredWith { it.input }.toListenTo(SingleArtifact.APK)
    }
}

dependencies {
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Location
    playServicesLocationVersion?.let {
        add("gmsImplementation", "com.google.android.gms:play-services-location:$it")
    }


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.glance.appwidget)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}