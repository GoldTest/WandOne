import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {

    kotlin("jvm")
    //todo refactor
    alias(libs.plugins.jetbrainsCompose)
//    kotlin("multiplatform")
//    kotlin("plugin.compose")

//    alias(libs.plugins.jetbrainsCompose) apply false
//    alias(libs.plugins.composeCompiler) apply false
//    alias(libs.plugins.kotlinMultiplatform) apply false
//    alias(libs.plugins.compose.compiler) apply false

//    id("java-platform")
//    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose") version ("2.1.0")
    id("org.jetbrains.kotlin.plugin.serialization") version ("2.0.0-RC1")
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

//val os: org.gradle.internal.os.OperatingSystem = org.gradle.internal.os.OperatingSystem.current()
//val arch: String = System.getProperty("os.arch")
//val isAarch64: Boolean = arch.contains("aarch64")
//val platform =
//    when {
//        os.isWindows -> "win"
//        os.isMacOsX -> "mac"
//        else -> "linux"
//    } + if (isAarch64) "-aarch64" else ""

dependencies {
//    api(platform(project(":platform")))

    //versions
    val voyagerVersion = "1.0.0"
    val exposedVersion = "0.52.0"
    val sqliteVersion = "+" //0.44.1
    val h2Version = "+"
    val serializeVersion = "1.2.0" //
    val reorderVersion = "+" //0.9.6

    //desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.material3)
    implementation(kotlin("stdlib"))
    //navigator
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")

//    implementation("org.jetbrains.compose.animation:animation-desktop:1.4.1")

    //Dao
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    //sql driver
    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    implementation("com.h2database:h2:$h2Version")

    //log
//    implementation("ch.qos.logback:logback-classic:$logbackVersion")

//    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
//    implementation("org.apache.logging.log4j:log4j-api:2.14.0")
//    implementation("org.apache.logging.log4j:log4j-core:2.14.0")

    //serialize
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializeVersion")

    //reorder 排序
    implementation("org.burnoutcrew.composereorderable:reorderable:$reorderVersion")



    implementation("com.alibaba:dashscope-sdk-java:+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx3:1.6.0")


//    implementation("org.commonmark:commonmark:0.24.0")

    implementation("com.mikepenz:multiplatform-markdown-renderer:0.27.0")
    // Offers Material 2 defaults for Material 2 themed apps (com.mikepenz.markdown.m2.Markdown)
    implementation("com.mikepenz:multiplatform-markdown-renderer-m2:0.27.0")
    // Offers Material 3 defaults for Material 3 themed apps (com.mikepenz.markdown.m3.Markdown)
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.27.0")

    //require by coil3
    implementation(libs.coil.network.ktor)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.kotlinx.coroutines.swing)

    implementation("com.mikepenz:multiplatform-markdown-renderer-coil3:0.27.0")
    implementation("com.mikepenz:multiplatform-markdown-renderer-code:0.27.0")
    implementation("com.mikepenz:multiplatform-markdown-renderer-jvm:0.27.0")

    implementation(compose.components.resources)

    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")

    //webview
    api("io.github.kevinnzou:compose-webview-multiplatform:1.9.40")


//    implementation("org.openjfx:javafx-base:19:$platform")
//    implementation("org.openjfx:javafx-graphics:19:$platform")
//    implementation("org.openjfx:javafx-controls:19:$platform")
//    implementation("org.openjfx:javafx-media:19:$platform")
//    implementation("org.openjfx:javafx-web:19:$platform")
//    implementation("org.openjfx:javafx-swing:19:$platform")

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

