plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.academy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.academy"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.1")
    implementation("androidx.navigation:navigation-ui:2.8.5")
    implementation("androidx.navigation:navigation-fragment:2.8.5")
    implementation("com.google.android.material:material:1.12.0")
}