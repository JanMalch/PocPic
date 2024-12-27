@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.com.google.protobuf)
    id("kotlin-parcelize")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "io.github.janmalch.pocpic"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.janmalch.pocpic"
        minSdk = 26 // v26 for LocalDateTime
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        val release = getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("releaseCi") {
            initWith(release)
            signingConfig = signingConfigs.create("releaseCi") {
                storeFile = file("keystore/android_keystore.jks")
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }
    compileOptions {
        // Java 17 for landscapist: https://github.com/skydoves/landscapist/issues/298#issuecomment-1569903376
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // https://github.com/Kotlin/kotlinx.coroutines#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            excludes += "DebugProbesKt.bin"
        }
    }
}

protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
    // https://github.com/google/protobuf-gradle-plugin/issues/518#issuecomment-1273099797
    // https://github.com/zhaobozhen/LibChecker/blob/c0c3bc7c661fe45cc44d5c6ab0202764652e0b7e/app/build.gradle.kts#L197
    generateProtoTasks {
        all().forEach {
            it.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}
dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.ui)
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation(libs.bundles.ui.widget)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.bundles.ui.debug)

    implementation(libs.coil)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.datetime)

    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.testing)
    kspAndroidTest(libs.hilt.compiler)

    implementation(libs.androidx.documentfile)

    implementation(libs.bundles.datastore)
    implementation(libs.androidx.datastore.preferences)

    // material components (for dark mode theme)
    implementation(libs.material)

    implementation(libs.google.oss.licenses) {
        exclude(group = "androidx.appcompat")
    }
}
