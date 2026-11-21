plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("com.chaquo.python") }
android {
    namespace = "com.omegasub.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.omegasub.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0-experimental"
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
chaquopy {
    defaultConfig {
        version = "3.11"
        pip { install("subliminal==2.2.1") }
    }
}
