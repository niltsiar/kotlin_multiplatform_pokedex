plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktor) apply false
}

/**
 * Ben Manes Versions Plugin Configuration
 * 
 * Checks for dependency and Gradle wrapper updates.
 * 
 * Stability Rules:
 * - Stable versions (e.g., 2.8.4) will NOT upgrade to unstable versions (e.g., 2.9.0-alpha01)
 * - Unstable versions (e.g., 2.9.0-alpha01) WILL upgrade to higher unstable versions ONLY in the same major.minor version:
 *   - 2.9.0-alpha01 → 2.9.0-alpha03 ✅ (same major.minor)
 *   - 2.9.0-alpha01 → 2.9.0-beta01 ✅ (same major.minor)
 *   - 2.9.0-alpha01 → 2.10.0-alpha01 ❌ (different minor)
 *   - 2.9.0-alpha01 → 3.0.0-alpha01 ❌ (different major)
 *   - 2.9.0-alpha01 → 3.9.0-alpha01 ❌ (different major)
 * - Unstable versions WILL upgrade to ANY stable version (major version bumps are safe when going stable):
 *   - 2.9.0-rc03 → 2.9.0 ✅
 *   - 2.9.0-alpha02 → 3.1.1 ✅ (stable release, any version)
 *   - 1.0.0-rc02 → 1.0.0 ✅
 * 
 * Usage: ./gradlew dependencyUpdates
 * Report: build/dependencyUpdates/report.html
 */
tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    checkForGradleUpdate = true
    outputFormatter = "plain"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
    
    rejectVersionIf {
        val currentVersion = currentVersion
        val candidateVersion = candidate.version
        
        val isCurrentStable = isStable(currentVersion)
        val isCandidateStable = isStable(candidateVersion)
        
        when {
            // Reject unstable candidates when current is stable
            isCurrentStable && !isCandidateStable -> true
            
            // For unstable→unstable: only accept candidates in the same major.minor version
            !isCurrentStable && !isCandidateStable -> {
                val currentParts = parseVersion(currentVersion)
                val candidateParts = parseVersion(candidateVersion)
                
                // Reject if different major or minor version
                currentParts.major != candidateParts.major || 
                currentParts.minor != candidateParts.minor
            }
            
            // For unstable→stable: accept ANY stable version (major version bump is safe)
            !isCurrentStable && isCandidateStable -> false
            
            // Accept stable→stable (always safe)
            else -> false
        }
    }
}

/**
 * Determines if a version string represents a stable release.
 * 
 * Stable: No alpha, beta, rc, snapshot, dev, or similar qualifiers
 * Unstable: Contains alpha, beta, rc, snapshot, dev, m (milestone), etc.
 */
fun isStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val unstableKeyword = listOf("ALPHA", "BETA", "RC", "SNAPSHOT", "DEV", "M", "PREVIEW", "EAP")
        .any { version.uppercase().contains(it) }
    
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return (stableKeyword || regex.matches(version)) && !unstableKeyword
}

/**
 * Parses a version string into major, minor, patch components.
 */
data class VersionParts(val major: Int, val minor: Int, val patch: Int)

fun parseVersion(version: String): VersionParts {
    val numericPart = version.split("-", ".").take(3)
    val major = numericPart.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = numericPart.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = numericPart.getOrNull(2)?.toIntOrNull() ?: 0
    return VersionParts(major, minor, patch)
}