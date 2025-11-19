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

// Updated by roar
val vName = "0.1.1"

// Compute versionCode based on versionName, by padding the segments in thousands groups,
// allowing for up to 1000 patch versions per major/minor.
// v0 will be codes less than 1_000_000.
// https://pl.kotl.in/LUdtQtZh6
val (vMajor, vMinor, vPatch) = vName
    .split(".")
    .map { s -> s.toInt(radix = 10).also { require(it < 1000) } }

val vCode = "%d%03d%03d".format(vMajor, vMinor, vPatch)
    .toInt(radix = 10)
    .also { require(it < 2100000000) { "Exceeded greatest value for Google Play: $it" } }

android {
    namespace = "io.github.janmalch.pocpic"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.janmalch.pocpic"
        minSdk = 26 // v26 for LocalDateTime
        targetSdk = 36
        versionName = vName
        versionCode = vCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        val release = getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.create("release") {
                storeFile = file("keystore/android_keystore.jks")
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("releaseSignDebug") {
            initWith(release)
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
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
    implementation(libs.timber)
    implementation(libs.shed)
    implementation(libs.shed.autoload)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.ui)
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
