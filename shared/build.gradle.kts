import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqlDelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
        iosTarget.binaries.all {
            linkerOpts += "-lsqlite3"
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            // SQLDelight
            implementation(libs.sqlDelight.runtime)
            implementation(libs.sqlDelight.coroutines.extensions)
            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            // Koin
            implementation(libs.koin.core)
            // ViewModel
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            // Logging - Napier (KMM-friendly logging library)
            implementation(libs.napier)
        }
        androidMain.dependencies {
            implementation(libs.sqlDelight.android.driver)
            implementation(libs.ktor.client.android)
            implementation(libs.koin.android)
        }
        
        val androidUnitTest by getting {
            dependencies {
                // Use JVM driver for unit tests since they run on JVM
                implementation(libs.sqlDelight.driver.jvm)
                // MockK and JUnit need to be explicitly added for Android unit tests
                implementation(libs.mockk)
                implementation(libs.junit)
            }
        }
        iosMain.dependencies {
            implementation(libs.sqlDelight.native.driver)
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
        }
    }
}

android {
    namespace = "com.bpn.comics.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}

sqldelight {
    databases {
        create("ComicsDatabase") {
            packageName.set("com.bpn.comics.database")
        }
    }
}

