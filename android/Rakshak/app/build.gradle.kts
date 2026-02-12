plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.rakshak.security"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.rakshak.security"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${project.property("GROQ_API_KEY")}\""
        )
    }

    buildFeatures {
        buildConfig = true
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
}

dependencies {

    // ===============================
    // Core Android UI
    // ===============================
    implementation("androidx.appcompat:appcompat:1.6.1")

    // 🔥 FORCE MATERIAL 3 (IMPORTANT FIX)
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")

    // ===============================
    // Room Database
    // ===============================
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // ===============================
    // Networking (ML API)
    // ===============================
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // JSON Parsing
    implementation("org.json:json:20231013")

    // ===============================
    // Testing
    // ===============================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
