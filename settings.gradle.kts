import org.gradle.kotlin.dsl.mavenCentral

pluginManagement {
    //声明查找位置 plugin
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    //声明可用 plugin 不是应用依赖 只是声明
    plugins {
    }
}
dependencyResolutionManagement {
    //三方依赖 libs
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jogamp.org/deployment/maven") //require by webview
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "WandOne"
