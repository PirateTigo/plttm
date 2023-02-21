plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.5.0"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "ru.sibsutis.piratetigo.plttm"
version = "1.0.0"
description = "Теория языков программирования и методы трансляции"

val plttmMainClass = "$group.GUIStarter"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.24")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.google.guava:guava:31.1-jre")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

javafx {
    version = "19"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainModule.set("plttm")
    mainClass.set(plttmMainClass)
}

jlink {
    options.set(listOf(
            "--compress", "2",
            "--no-header-files",
            "--no-man-pages",
            "--verbose"
    ))
    launcher {
        name = "plttm"
        mainClass.set(plttmMainClass)
    }
    jpackage {
        imageOptions = listOf(
                "--icon", "src/main/resources/icons/compiler.ico"
        )
        installerOptions = listOf(
                "--win-dir-chooser",
                "--win-menu",
                "--win-shortcut",
                "--win-shortcut-prompt",
                "--vendor", "SibSUTIs",
                "--description", "PLTTM",
                "--copyright", "Artem Tarakanovskiy",
                "--verbose"
        )
        installerType = "exe"
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
