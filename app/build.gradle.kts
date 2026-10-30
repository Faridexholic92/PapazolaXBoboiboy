import java.util.Base64

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "my.papazola.tv"
    compileSdk = 35
    defaultConfig {
        applicationId = "my.papazola.tv"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-preview"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    lint { abortOnError = true }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}

// Text-encoded asset permits MCP-only repository edits; decode before resource processing.
val generatedHero = layout.buildDirectory.dir("generated/res/hero")
val prepareHero by tasks.registering {
    inputs.file("src/main/artwork/hero.jpg.base64")
    outputs.dir(generatedHero)
    doLast {
        val out = generatedHero.get().file("drawable-nodpi/hero_art.jpg").asFile
        out.parentFile.mkdirs()
        out.writeBytes(Base64.getMimeDecoder().decode(file("src/main/artwork/hero.jpg.base64").readText()))
    }
}
android.sourceSets.getByName("main").res.srcDir(generatedHero)
tasks.named("preBuild").configure { dependsOn(prepareHero) }
