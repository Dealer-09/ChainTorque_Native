import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

// ─── Load local.properties secrets into BuildConfig ──────────────────────────
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.example.chaintorquenative"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.chaintorquenative"
        minSdk = 33
        targetSdk = 36
        versionCode = 2
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ── Secrets from local.properties (never hardcode these) ────────────
        buildConfigField("String", "WALLETCONNECT_PROJECT_ID",
            "\"${localProperties.getProperty("WALLETCONNECT_PROJECT_ID", "\"\"")}\""
        )
        buildConfigField("String", "CONTRACT_ADDRESS",
            "\"${localProperties.getProperty("CONTRACT_ADDRESS", "0x0000000000000000000000000000000000000000")}\""
        )
        buildConfigField("String", "RPC_URL",
            "\"${localProperties.getProperty("RPC_URL", "https://rpc.sepolia.org")}\""
        )
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)

    // Jetpack Compose BOM (updated for PullToRefreshBox support)
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")  // LocalLifecycleOwner
    implementation("androidx.compose.runtime:runtime-livedata")

    // Accompanist Navigation Material (required by Reown AppKit modal)
    implementation("com.google.accompanist:accompanist-navigation-material:0.36.0")
    // Compose Material (M2) needed for Accompanist ModalBottomSheetLayout
    implementation("androidx.compose.material:material")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Hilt for Dependency Injection (using KSP instead of KAPT)
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-compiler:2.59.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Retrofit & Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.compose.material:material-icons-extended-android")

    // Coil for image loading in Compose
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Reown AppKit (WalletConnect v2) - replaces MetaMask native SDK
    implementation(platform("com.reown:android-bom:1.6.7"))
    implementation("com.reown:android-core")
    implementation("com.reown:appkit")
}
