import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// The version comes from the release tag (v0.1.0 becomes 0.1.0), which the release workflow
// passes in as -PappVersion. A build without one is a test build and says so.
val appVersion: String = (findProperty("appVersion") as String?)?.removePrefix("v") ?: "0.0.0-dev"
val appVersionCode: Int = appVersion.substringBefore('-').split('.')
    .map { it.toIntOrNull() ?: 0 }
    .plus(listOf(0, 0, 0))
    .let { (major, minor, patch) -> major * 10000 + minor * 100 + patch }
    .coerceAtLeast(1)

android {
    namespace = "io.github.michael412133.zmanim"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.michael412133.zmanim"
        // The Kompakt runs Android 12 (API 31), and the app targets exactly that.
        minSdk = 31
        targetSdk = 31
        versionCode = appVersionCode
        versionName = appVersion
    }

    // The release key lives only in a GitHub secret. The workflow unpacks it into signing/
    // (which git ignores) before building, so a release is always signed with the same key
    // and an update installs over the version already on the phone.
    val signingProperties = rootProject.file("signing/signing.properties")
    val releaseSigning = if (signingProperties.isFile) {
        val properties = Properties().apply { signingProperties.inputStream().use(::load) }
        signingConfigs.create("release") {
            storeFile = rootProject.file("signing/signing.keystore")
            storePassword = properties.getProperty("STORE_PASSWORD")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASSWORD")
        }
    } else {
        null
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            releaseSigning?.let { signingConfig = it }
        }
    }

    lint {
        // Sideloaded onto a Kompakt, never sent to Google Play, so its newer target rule
        // does not apply. Targeting the Android version the phone runs is on purpose.
        disable += "ExpiredTargetSdkVersion"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        named("main") { kotlin.srcDir("src/main/kotlin") }
        named("test") { kotlin.srcDir("src/test/kotlin") }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.mmd)
    implementation(libs.zmanim)

    testImplementation(libs.junit)
}
