buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.4.0")
        classpath ("com.android.tools.build:gradle:8.2.1")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("com.chaquo.python") version "15.0.1" apply false
}
