// Top-level build file where you can add configuration options common to all
// sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.room) apply false
}

// The generated type-safe `libs` accessor is not reliably bound when read
// from inside a subprojects {} closure (it's resolved against whichever
// subproject is currently being configured, not the root project's own
// classpath) - so look the catalog up through the stable
// VersionCatalogsExtension API instead, per Gradle's documented pattern for
// consuming catalogs outside of a project's own build script.
val libsCatalog = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        toolVersion = libsCatalog.findVersion("detekt").get().requiredVersion
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }

    dependencies {
        add("detektPlugins", libsCatalog.findLibrary("detekt-rules-compose").get())
    }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        val ktlintVersion = libsCatalog.findVersion("ktlint").get().requiredVersion
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint(ktlintVersion)
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(ktlintVersion)
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
