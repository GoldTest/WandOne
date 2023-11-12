import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_18.toString()
    targetCompatibility = JavaVersion.VERSION_18.toString()
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    kotlinOptions.jvmTarget = "18"
}

group = "arc.mage.wandone"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    //versions
    val voyagerVersion = "1.0.0-rc10"
    //desktop
    implementation(compose.desktop.currentOs)
    //navigator
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")

}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            packageName = "WandOne"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)

            windows {
                shortcut = false
                iconFile.set(File("icon.ico"))
                installationPath = "D:\\Program Files\\WandOne"
                dirChooser = true
                menu = true
                menuGroup = "Startup"
                packageVersion = "0.0.1"
                perUserInstall = false
                shortcut = true
            }
        }
    }
}
