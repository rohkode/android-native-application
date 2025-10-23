plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") version "4.4.0"
}

android {
    namespace = "com.example.androidnativeclevertap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.androidnativeclevertap"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation("androidx.core:core:1.13.0")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("com.android.installreferrer:installreferrer:2.2")

    implementation ("androidx.compose.ui:ui:1.5.0") // Jetpack Compose UI
    implementation ("androidx.compose.material3:material3:1.1.0") // Material 3
    implementation ("androidx.compose.ui:ui-tooling-preview:1.5.0") // Compose tooling for previews
    implementation ("androidx.compose.runtime:runtime:1.5.0") // Compose runtime
    implementation ("androidx.activity:activity-compose:1.7.0")

    //MANDATORY for App Inbox
    implementation ("androidx.appcompat:appcompat:1.3.1")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("androidx.viewpager:viewpager:1.0.0")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    //Optional AndroidX Media3 Libraries for Audio/Video Inbox Messages. Audio/Video messages will be dropped without these dependencies
    implementation ("androidx.media3:media3-exoplayer:1.1.1")
    implementation ("androidx.media3:media3-exoplayer-hls:1.1.1")
    implementation ("androidx.media3:media3-ui:1.1.1")

    //Out-of-the-Box Android Push Templates
    implementation ("com.clevertap.android:push-templates:2.1.0") // 2.1.0 and above
    implementation ("com.clevertap.android:clevertap-android-sdk:7.4.1") // 7.4.1 and above

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.protolite.well.known.types)
    implementation(libs.androidx.monitor)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}