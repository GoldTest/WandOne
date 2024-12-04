import org.gradle.kotlin.dsl.mavenCentral

pluginManagement {
    //声明查找位置
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    //声明可用 plugin 不是应用依赖 只是声明

    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}
//三方依赖
dependencyResolutionManagement{
    repositories{
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jogamp.org/deployment/maven") //require by webview
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "WandOne"
