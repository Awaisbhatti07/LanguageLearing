plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.lingomate.ai"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.lingomate.ai"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
    lint { abortOnError = true }
}

room { schemaDirectory("$projectDir/schemas") }

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    systemProperty("lingomate.schemas", file("schemas").absolutePath)
    // Prevent Android/Compose test classes from sharing process-level sandbox state.
    forkEvery = 1
    // Mitigate observed Robolectric worker spins on ARM64 macOS; the JVM cause is unconfirmed.
    // This limits host-test optimization only and does not change Android runtime behavior.
    if (System.getProperty("os.name") == "Mac OS X" && System.getProperty("os.arch") == "aarch64") {
        jvmArgs("-XX:TieredStopAtLevel=1")
    }
}

dependencies {
    implementation(project(":core:learning"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.serialization.json)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.test.runner)
}
