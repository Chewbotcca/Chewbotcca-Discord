/*
 * Copyright (C) 2022 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    java
    `maven-publish`
    application
    kotlin("jvm") version "1.6.10"
}

repositories {
    maven {
        url = uri("https://jenkins.chew.pw/plugin/repository/everything/")
        content {
            includeGroup("me.memerator.api")
            includeGroup("pro.chew.api")
        }
    }

    maven {
        url = uri("https://m2.chew.pro/snapshots/")
        content {
            includeGroup("pw.chew")
        }
    }

    mavenCentral()
}

dependencies {
    implementation("net.dv8tion", "JDA", "5.0.0-alpha.22")
    implementation("pw.chew", "jda-chewtils", "2.0-SNAPSHOT")
    implementation("ch.qos.logback", "logback-classic", "1.2.11")
    implementation("com.squareup.okhttp3", "okhttp", "4.9.3")
    implementation("org.json", "json", "20211205")
    implementation("io.sentry", "sentry", "5.6.3")
    implementation("org.kohsuke", "github-api", "1.303")
    implementation("org.jsoup", "jsoup", "1.14.3")
    implementation("pro.chew.api", "ChewAPI", "1.0-b5")
    implementation("org.reflections", "reflections", "0.10.2")
    implementation("me.memerator.api", "MemeratorAPI", "2.0.0_74")
    implementation("org.hibernate", "hibernate-core", "5.6.5.Final")
    implementation("mysql", "mysql-connector-java", "8.0.28")
    implementation("org.codehaus.groovy", "groovy", "3.0.10")
    implementation("org.knowm.xchart", "xchart", "3.8.1")
}

group = "pw.chew.chewbotcca"
version = "2.0-SNAPSHOT"
description = "Chewbotcca"
java.sourceCompatibility = JavaVersion.VERSION_17

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        groupId = project.group.toString()
        artifactId = "Chewbotcca"
        version = project.version.toString()

        from(components["java"])
    }

    repositories {
        maven {
            url = uri("https://m2.chew.pro/snapshots")
            credentials {
                username = properties["mchew-username"].toString()
                password = properties["mchew-password"].toString()
            }
        }
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.apiVersion = "1.6"
}

application {
    mainClass.set("pw.chew.chewbotcca.Chewbotcca")
}
