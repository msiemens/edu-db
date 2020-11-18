import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junitVersion = "5.6.2"

plugins {
    kotlin("jvm") version "1.4.10"
    application
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("info.solidsoft.pitest") version "1.5.1"
    jacoco
}
group = "de.msiemens"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
dependencies {
    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "13"
}
tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}
application {
    mainClassName = "MainKt"
}
