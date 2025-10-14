import java.util.Properties

import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
}

val geminiApiKey: String by lazy {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        Properties().apply {
            propertiesFile.inputStream().use { load(it) }
        }.getProperty("GEMINI_API_KEY") ?: ""
    } else ""
}

val cloudinaryCloudName: String by lazy {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        Properties().apply {
            propertiesFile.inputStream().use { load(it) }
        }.getProperty("CLOUDINARY_CLOUD_NAME") ?: ""
    } else ""
}

val cloudinaryUploadPreset: String by lazy {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        Properties().apply {
            propertiesFile.inputStream().use { load(it) }
        }.getProperty("CLOUDINARY_UPLOAD_PRESET") ?: ""
    } else ""
}

android {
    namespace = "com.example.final_project"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.final_project"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Tạo biến BuildConfig để dùng trong Java
    buildConfigField("String", "GEMINI_API_KEY", "\"${geminiApiKey}\"")
    buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${cloudinaryCloudName}\"")
    buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${cloudinaryUploadPreset}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.mysql.connector.java)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // CameraX dependencies
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
}