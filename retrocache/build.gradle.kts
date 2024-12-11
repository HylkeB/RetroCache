import com.vanniktech.maven.publish.SonatypeHost
import dev.mokkery.MockMode
import dev.mokkery.verify.VerifyMode

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.allopen)
    alias(libs.plugins.mokkery)
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

mokkery {
    defaultVerifyMode.set(VerifyMode.exhaustiveOrder)
    defaultMockMode.set(MockMode.autoUnit)
}

allOpen {
    annotation("io.github.hylkeb.retrocache.utility.OpenForMocking")
}


dependencies {
    api("com.squareup.retrofit2:retrofit:2.11.0") // todo catalog
    api("io.github.hylkeb:susstatemachine:1.1.0") // todo catalog
    api("com.squareup.okhttp3:okhttp:4.12.0") // todo catalog
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1") // todo catalog
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // todo catalog
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.assertions)
//    implementation(libs.core.ktx)
//    implementation(libs.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.test.ext.junit)
//    androidTestImplementation(libs.espresso.core)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    val version = System.getenv("GITHUB_REF_NAME")?.drop(1) ?: "0.0.0" // remove v from vX.Y.
    println("Resolved version: $version")
    coordinates("io.github.hylkeb", "retrocache", version)

    pom {
        name.set("RetroCache")
        description.set("Extension library on retrofit to configure cacheable requests")
        inceptionYear.set("2024")
        url.set("https://github.com/HylkeB/RetroCache")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://opensource.org/licenses/Apache-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("HylkeB")
                name.set("Hylke Bron")
                email.set("hylke.bron@gmail.com")
            }
        }
        scm {
            url.set("https://github.com/HylkeB/RetroCache")
            connection.set("scm:git:git://github.com/HylkeB/RetroCache.git")
            developerConnection.set("scm:git:ssh://git@github.com/HylkeB/RetroCache.git")
        }
    }
}