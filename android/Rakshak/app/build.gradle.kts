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

        // 🔐 API KEY (Replace with your NEW key)
        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${project.property("GROQ_API_KEY")}\""
        )

    }

    // ✅ IMPORTANT: Enable BuildConfig generation
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 🌐 Networking
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // 📦 JSON
    implementation("org.json:json:20231013")
}
