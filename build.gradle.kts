import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version ("1.9.21")
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
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    //versions
    val voyagerVersion = "1.0.0"
    val exposedVersion = "0.52.0"
    val sqliteVersion = "+" //0.44.1
    val h2Version = "+"
    val logbackVersion = "+" //1.2.11
    val serializeVersion = "1.2.0" //
    val reorderVersion = "+" //0.9.6

    //desktop
    implementation(compose.desktop.currentOs)
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
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

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

    implementation("com.mikepenz:multiplatform-markdown-renderer-jvm:0.27.0")

}

compose.desktop {

    application {
        mainClass = "MainKt"

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
    }
}
