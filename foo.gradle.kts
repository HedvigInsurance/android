/*
plugins {
    id("com.github.konifar.gradle.unused-resources-remover") version "0.3.3"
}
*/

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies { classpath("gradle.plugin.com.github.konifar.gradle:plugin:0.3.3") }
}
apply<com.github.konifar.gradle.remover.UnusedResourcesRemoverPlugin>()
