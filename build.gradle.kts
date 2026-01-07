import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.tbread"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jogamp.org/deployment/maven")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation ("org.pcap4j:pcap4j-core:1.8.2")
    implementation ("org.pcap4j:pcap4j-packetfactory-static:1.8.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation("io.github.kevinnzou:compose-webview-multiplatform:1.9.20")
    //cef 래퍼 포함 라이브러리

    implementation ("net.java.dev.jna:jna:5.18.1")
    implementation ("net.java.dev.jna:jna-platform:5.18.1")
    //jna


}

compose.desktop {
    application {
        mainClass = "com.tbread.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "aion2meter4j"
            packageVersion = "1.0.0"
        }
    }
}