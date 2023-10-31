import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
//            modules("java.instrument", "java.prefs", "jdk.unsupported")
//            buildTypes.release.proguard {
//                configurationFiles.from(project.file("compose-desktop.pro"))
//            }
            windows {
                shortcut = false
                iconFile.set(File("icon.ico"))
//                installationPath = "C:\\Programs\\CLI"
                dirChooser = true
                menuGroup = "--startup"
            }
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "WandToolOne"
            packageVersion = "1.0.2"
        }
    }
}
