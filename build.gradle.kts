plugins {
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.allopen).apply(false)
    alias(libs.plugins.mokkery).apply(false)
    alias(libs.plugins.kotlin.compose).apply(false)
    id("com.google.dagger.hilt.android") version "2.51.1" apply false // TODO catalog
}