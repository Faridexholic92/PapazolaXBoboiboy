import java.util.Base64

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "my.papazola.tv"
    compileSdk = 35
    defaultConfig {
        applicationId = "my.papazola.tv"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0-weather-preview"
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

// Versioned text assets are decoded into native Android drawable resources.
val generatedWeather = layout.buildDirectory.dir("generated/res/weather")
val prepareWeather by tasks.registering {
    inputs.files(fileTree("src/main/artwork") { include("*.jpg.base64") })
    outputs.dir(generatedWeather)
    doLast {
        fileTree("src/main/artwork").matching { include("*.jpg.base64") }.forEach { source ->
            val name = source.name.removeSuffix(".jpg.base64")
            val out = generatedWeather.get().file("drawable-nodpi/weather_${name}.jpg").asFile
            out.parentFile.mkdirs()
            out.writeBytes(Base64.getMimeDecoder().decode(source.readText()))
        }
    }
}
android.sourceSets.getByName("main").res.srcDir(generatedWeather)
tasks.named("preBuild").configure { dependsOn(prepareWeather) }
