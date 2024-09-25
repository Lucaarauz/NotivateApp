// Project-level build.gradle.kts
plugins {
    // Plugin versions are defined in the 'libs' block if you're using version catalog
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

buildscript {
    // Dependencies for the build script
    dependencies {
        // Add the Google services classpath
        classpath(libs.google.services)
    }
}