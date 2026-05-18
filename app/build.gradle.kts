plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.rust.plugin)
}

android {
    namespace = "io.github.neerajshdev.nativerust"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "io.github.neerajshdev.nativerust"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    ndkVersion = "30.0.14904198"

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

val rustJniLibsDir = layout.buildDirectory.dir("rustJniLibs/android").get()
tasks.matching { it.name.matches(Regex("merge.*JniLibFolders")) }.configureEach {
    inputs.dir(rustJniLibsDir)
    dependsOn("cargoBuild")
}

cargo {
    module = "./rust"
    libname = "rust"

    // 1. Default targets jo release/final build me jayengi
    var activeTargets = listOf("x86_64", "arm64", "arm", "x86")

    // 2. Agar aap local run/debug kar rahe hain, toh sirf active device ki ABI select hogi
    if (project.hasProperty("android.injected.build.abi")) {
        val injectedAbi = project.property("android.injected.build.abi").toString()

        // Android ABI names ko Rust target mapping ke hisab se convert karna
        val target = when {
            injectedAbi.contains("arm64-v8a") -> "arm64"
            injectedAbi.contains("armeabi-v7a") -> "arm"
            injectedAbi.contains("x86_64") -> "x86_64"
            injectedAbi.contains("x86") -> "x86"
            else -> null
        }

        if (target != null) {
            activeTargets = listOf(target)
        }
    }

    targets = activeTargets
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}