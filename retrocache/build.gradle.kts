plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") // todo alias
}

android {
    namespace = "io.github.hylkeb.retrocache"
    compileSdk = 34

    defaultConfig {
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api("com.squareup.retrofit2:retrofit:2.11.0") // todo catalog
    api("io.github.hylkeb:susstatemachine:1.0.0") // todo catalog
    api("com.squareup.okhttp3:okhttp:4.12.0") // todo catalog
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1") // todo catalog
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // todo catalog
//    implementation(libs.core.ktx)
//    implementation(libs.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.test.ext.junit)
//    androidTestImplementation(libs.espresso.core)
}