plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.chaquo.python")
}

android {
    namespace = "com.example.resqapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.resqapp"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

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
chaquopy {
    defaultConfig {
        buildPython("C:/Program Files/Python312/python.exe")
        version = "3.12"
    }
    productFlavors { }
    sourceSets {
        getByName("main") {
            srcDir("some/other/main/python")
        }
    }
}


    dependencies {


        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.10.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("com.google.firebase:firebase-auth-ktx:22.1.2")
        implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")
        implementation("com.google.firebase:firebase-storage:20.3.0")
        implementation("com.google.firebase:firebase-database:20.3.0")
        implementation("com.google.android.gms:play-services-cast-framework:21.4.0")
        implementation("com.google.firebase:firebase-ml-vision:24.1.0")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
        implementation("com.google.firebase:firebase-analytics-ktx")
        implementation("com.google.android.gms:play-services-maps:17.0.1")
        implementation("com.google.android.gms:play-services-location:21.0.1")
        implementation("com.karumi:dexter:6.2.1")
        implementation("com.google.firebase:firebase-core:21.1.1")
        implementation("com.android.volley:volley:1.2.0")
        implementation("com.squareup.picasso:picasso:2.5.2")
        implementation("com.github.dhaval2404:imagepicker:2.1")
        implementation("com.google.android.gms:play-services-mlkit-text-recognition:18.0.2")
    }

