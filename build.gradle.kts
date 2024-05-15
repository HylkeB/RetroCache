plugins {
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.mockmp).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    id("com.google.dagger.hilt.android") version "2.51.1" apply false // TODO catalog
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false // todo catalog
}