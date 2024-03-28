plugins {
    kotlin("jvm") version "1.7.10"
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.jpa") version "1.7.10"
    id("jacoco")
}

subprojects {
    apply(plugin = "java")

    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring") //all-open

    apply {
        plugin("org.jetbrains.kotlin.kapt")
        version = "1.7.10"
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}

allprojects {
    group = "team.msg"
    version = "0.0.1-SNAPSHOT"

    tasks {
        compileKotlin {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "11"
            }
        }

        compileJava {
            sourceCompatibility = JavaVersion.VERSION_11.majorVersion
        }

        test {
            useJUnitPlatform()
        }
    }

    repositories {
        mavenCentral()
    }

}

jacoco {
    toolVersion = "0.8.5"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        xml.outputLocation.set(File("$buildDir/reports/jacoco/test/jacocoTestReport.xml"))
    }

    var excludes = mutableListOf<String>()
    excludes.add("some/path/for/exclude")

    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude(excludes)
        }
    )
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
