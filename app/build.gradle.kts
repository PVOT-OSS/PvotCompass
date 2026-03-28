import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val toml = rootProject.file("gradle/libs.versions.toml").readText()
val playServicesLocationVersion: String? = if (toml.contains("play-services-location")) {
    toml.lines()
        .firstOrNull { it.trimStart().startsWith("playServicesLocation") }
        ?.substringAfter('"')?.substringBefore('"')
} else null

val versionMajor: Int by rootProject.extra
val versionMinor: Int by rootProject.extra
val versionPatch: Int by rootProject.extra
val computedVersionCode: Int by rootProject.extra
val computedVersionName: String by rootProject.extra

android {
    namespace = "org.prauga.compass"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "org.prauga.compass"
        minSdk = 30
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
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

    applicationVariants.all {
        val variant = this
        val flavorName = when (variant.flavorName) {
            "foss" -> "fdroid"
            else -> variant.flavorName
        }
        outputs.all {
            val output = this as BaseVariantOutputImpl
            output.outputFileName =
                "Compass-v${variant.versionName}-${variant.buildType.name}-${flavorName}.apk"
        }
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}