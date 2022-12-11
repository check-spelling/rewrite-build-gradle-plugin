import nl.javadude.gradle.plugins.license.LicenseExtension
import java.util.*

plugins {
    id("com.gradle.plugin-publish") version "1.0.0"
    id("com.github.hierynomus.license") version "0.16.1"
}

pluginBundle {
    website = "https://github.com/openrewrite/rewrite-build-gradle-plugin"
    vcsUrl = "https://github.com/openrewrite/rewrite-build-gradle-plugin.git"
    tags = listOf("rewrite", "refactoring", "java", "gradle")
}

gradlePlugin {
    plugins {
        create("build-language-library") {
            id = "org.openrewrite.rewrite.build-language-library"
            displayName = "Rewrite language library"
            description = "Core language module"
            implementationClass = "org.openrewrite.gradle.RewriteLanguageLibraryPlugin"
        }
        create("build-shadow") {
            id = "org.openrewrite.rewrite.build-shadow"
            displayName = "Rewrite shadow configuration"
            description = "Configures the Gradle Shadow plugin to replace the normal jar task output with " +
                    "the shaded jar without a classifier"
            implementationClass = "org.openrewrite.gradle.RewriteShadowPlugin"
        }
        create("build-root") {
            id = "org.openrewrite.rewrite.build-root"
            displayName = "Rewrite root"
            description = "Configures the root project"
            implementationClass = "org.openrewrite.gradle.RewriteRootPlugin"
        }
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
        cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
        if(name.startsWith("test")) {
            eachDependency {
                if(requested.name == "groovy-xml") {
                    useVersion("3.0.9")
                }
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.named<JavaCompile>("compileJava") {
    options.release.set(8)
}

dependencies {
    implementation("gradle.plugin.com.hierynomus.gradle.plugins:license-gradle-plugin:latest.release")
    implementation("com.github.jk1:gradle-license-report:latest.release")
    implementation("org.owasp:dependency-check-gradle:latest.release")
    implementation("com.netflix.nebula:gradle-contacts-plugin:latest.release")
    implementation("com.netflix.nebula:gradle-info-plugin:latest.release")
    implementation("com.netflix.nebula:nebula-release-plugin:latest.release")
    implementation("com.netflix.nebula:nebula-publishing-plugin:latest.release")
    implementation("com.netflix.nebula:nebula-project-plugin:latest.release")
    implementation("io.github.gradle-nexus:publish-plugin:latest.release")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:latest.release")
    implementation("org.gradle:test-retry-gradle-plugin:latest.release")

    // TODO remove this once Rewrite core modules no longer use Kotlin for tests
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:latest.release")

    testImplementation(platform("org.junit:junit-bom:latest.release"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core:latest.release")
}

project.rootProject.tasks.getByName("postRelease").dependsOn(project.tasks.getByName("publishPlugins"))

tasks.withType<Test>() {
    useJUnitPlatform()
}

configure<LicenseExtension> {
    ext.set("year", Calendar.getInstance().get(Calendar.YEAR))
    skipExistingHeaders = true
    header = project.rootProject.file("gradle/licenseHeader.txt")
    mapping(mapOf("kt" to "SLASHSTAR_STYLE", "java" to "SLASHSTAR_STYLE"))
    strictCheck = true
    exclude("**/versions.properties")
    exclude("**/*.txt")
}
