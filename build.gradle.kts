import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_18.toString()
    targetCompatibility = JavaVersion.VERSION_18.toString()
}
kotlin.compilerOptions {
    jvmTarget.set(JvmTarget.JVM_18)
}

group = "arc.mage.wandone"
version = "1.0-SNAPSHOT"

dependencies {
    //desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.material3)
    implementation(libs.kotlinStdlib)
    implementation(compose.components.resources)
    //navigator
    implementation(libs.nav)
    implementation(libs.navTab)
    implementation(libs.navBottom)
    implementation(libs.navTransition)
    //dao
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.sqlite)
    implementation(libs.h2database)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.reorder)
    implementation(libs.dashscope)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.rx3)
    //md
    implementation(libs.markdownx)
    implementation(libs.markdownx.m2)
    implementation(libs.markdownx.m3)
    implementation(libs.markdownx.coil3)
    implementation(libs.markdownx.code)
    implementation(libs.markdownx.jvm)
    //require by coil3
    implementation(libs.coil.network.ktor)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.kotlinx.coroutines.swing)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization)
    //webview
    api(libs.webviewx)

}
compose.resources {
    customDirectory(
        sourceSetName = "desktopMain",
        directoryProvider = provider { layout.projectDirectory.dir("desktopResources") }
    )
}
compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED") // recommended but not necessary

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
        nativeDistributions {
            modules("java.sql") //db
            modules("java.naming") //path
            //includeAllModules = true
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
        buildTypes.release.proguard {
            configurationFiles.from("compose-desktop.pro")
        }
    }
}

