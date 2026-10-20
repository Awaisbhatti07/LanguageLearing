plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin { jvmToolchain(17) }

sourceSets.test { resources.srcDir(rootProject.file("app/src/main/assets/content")) }

dependencies {
    implementation(libs.serialization.json)
    testImplementation(libs.junit)
}
