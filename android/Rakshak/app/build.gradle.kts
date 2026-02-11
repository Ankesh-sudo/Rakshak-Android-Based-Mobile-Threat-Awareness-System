plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.rakshak.security"

    compileSdk {
        version = release(36)
    }

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

    // Core UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // RecyclerView (for Risk History screen)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Lifecycle (future LiveData / ViewModel support)
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")

    // Room Database (Risk History)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // JSON
    implementation("org.json:json:20231013")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
