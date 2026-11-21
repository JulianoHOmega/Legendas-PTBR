plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.omegasub.android"; compileSdk = 35
    defaultConfig { applicationId = "com.omegasub.android"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies { implementation("org.jsoup:jsoup:1.18.3") }
