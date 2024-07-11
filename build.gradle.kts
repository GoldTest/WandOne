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
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    //versions
    val voyagerVersion = "+"
    val exposedVersion = "0.52.0"
    val sqliteVersion = "+" //0.44.1
    val h2Version = "+"
    val logbackVersion = "+" //1.2.11
    val serializeVersion = "1.1.0" //
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
