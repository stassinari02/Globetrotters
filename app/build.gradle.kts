// Top-level Gradle file
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.globetrotters"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.globetrotters"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.filament.android) // Usa Material 3 invece di Material 1.9.0

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Splash screen Android 12+
    implementation("androidx.core:core-splashscreen:1.0.1")

// Material Design 3 (Compose + Material)
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("com.google.android.material:material:1.12.0")

// Room database
    val room_version = "2.7.1"
    implementation("androidx.room:room-runtime:$room_version")

// Room compiler (Kotlin)
    ksp("androidx.room:room-compiler:$room_version")
// Room compiler (Java)
    annotationProcessor("androidx.room:room-compiler:$room_version")

// Room extra features
    implementation("androidx.room:room-ktx:$room_version")       // Kotlin coroutines
    implementation("androidx.room:room-rxjava2:$room_version")    // RxJava2 support
    implementation("androidx.room:room-rxjava3:$room_version")    // RxJava3 support
    implementation("androidx.room:room-guava:$room_version")      // Guava support
    testImplementation("androidx.room:room-testing:$room_version")// Testing
    implementation("androidx.room:room-paging:$room_version")     // Paging 3 support

// Android Preferences (Kotlin)
    implementation("androidx.preference:preference-ktx:1.2.1")

// Glide for images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.14.2")

// Retrofit + Gson for network and JSON
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Kotlin Coroutines core and Android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
// Retrofit adapter for coroutines
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

// OkHttp logging for HTTP requests
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

}