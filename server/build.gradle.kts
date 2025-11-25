plugins {
    id("convention.server")
}

group = "com.minddistrict.multiplatformpoc"
version = "1.0.0"

application {
    mainClass.set("com.minddistrict.multiplatformpoc.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    // Ktor engine choice (architectural decision)
    implementation(libs.ktor.serverNetty)
    
    // Future feature-specific dependencies go here
    // implementation(libs.ktor.serverContentNegotiation)
    // implementation(libs.ktor.serializationJson)
    // implementation(libs.exposed)
}

