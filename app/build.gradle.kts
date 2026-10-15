plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.fcl.plugin.mobileglues"
    compileSdk = 36

    ndkVersion = "27.3.13750724"

    defaultConfig {
        applicationId = "com.fcl.plugin.mobileglues"
        minSdk = 26
        targetSdk = 36
        versionCode = 2000
        versionName = "2.0.0"

        // Ship only the 64-bit ARM native libraries.
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        // Keep the default English resources in the APK.
        resourceConfigurations += listOf("en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD") ?: project.findProperty("SIGNING_STORE_PASSWORD") as String?
            keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: project.findProperty("SIGNING_KEY_ALIAS") as String?
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: project.findProperty("SIGNING_KEY_PASSWORD") as String?
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        configureEach {
            resValue("string","app_name","MobileGlues")

            manifestPlaceholders["des"] = "MobileGlues (OpenGL 4.0, 1.17+)"
            manifestPlaceholders["renderer"] = "MobileGlues:libmobileglues.so:libmobileglues.so"

            manifestPlaceholders["minMCVer"] = "1.17"
            manifestPlaceholders["maxMCVer"] = "" // No restriction if empty

            // Count only launches initiated by a launcher.
            manifestPlaceholders["boatEnv"] = mutableMapOf<String,String>().apply {
                put("LIBGL_ES", "3")
                put("MG_COUNT_LAUNCH", "1")
            }.run {
                var env = ""
                forEach { (key, value) ->
                    env += "$key=$value:"
                }
                env.dropLast(1)
            }
            manifestPlaceholders["pojavEnv"] = mutableMapOf<String,String>().apply {
                put("LIBGL_ES", "3")
                put("POJAV_RENDERER", "opengles3")
                put("POJAVEXEC_EGL", "libmobileglues.so")
                put("LIBGL_EGL", "libmobileglues.so")
                put("MG_COUNT_LAUNCH", "1")
            }.run {
                var env = ""
                forEach { (key, value) ->
                    env += "$key=$value:"
                }
                env.dropLast(1)
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        buildConfig = true
        aidl = true
        compose = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.gson)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.documentfile)
    implementation(libs.miuix.ui)
    // Explicit coroutine and lifecycle dependencies.
    implementation(libs.coroutines.android)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(project(":MobileGlues"))

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
