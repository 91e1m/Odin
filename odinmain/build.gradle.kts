import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

blossom {
    replaceToken("@VER@", version)
}

group = "me.odinmain"

val lwjgl: Configuration by configurations.creating
val lwjglNative: Configuration by configurations.creating {
    isTransitive =true
}

val lwjglJar = tasks.create<ShadowJar>("lwjglJar") {
    group = "shadow"
    destinationDirectory.set(layout.buildDirectory.dir("archiveJars"))
    archiveClassifier.set("lwjgl")
    configurations = listOf(lwjgl)
    exclude("META-INF/versions/**")
    exclude("**/module-info.class")
    exclude("**/package-info.class")

    // Relocate LWJGL packages
    relocate("org.lwjgl", "me.odinmain.lwjgl") {
        include("**")
    }

    // Optionally relocate other dependencies you want to avoid conflicts with
    relocate("me.odinmain.lwjgl.nanovg", "me.odinmain.nanovg")
    relocate("org.lwjgl.stb", "me.odinmain.stb")
    relocate("org.lwjgl.tinyfd", "me.odinmain.tinyfd")
}


sourceSets.main {
    java.srcDir(file("$projectDir/src/main/kotlin"))
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
    runtimeClasspath += configurations.getByName("lwjglNative")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    compileOnly("com.github.NotEnoughUpdates:NotEnoughUpdates:2.4.0:all")

    shadowImpl("com.github.Stivais:Commodore:3f4a14b1cf") {
        exclude(module = "kotlin-stdlib-jdk8")
    }

    lwjgl("org.lwjgl:lwjgl:3.3.1")
    lwjgl("org.lwjgl:lwjgl-stb:3.3.1")
    lwjgl("org.lwjgl:lwjgl-tinyfd:3.3.1")
    lwjgl("org.lwjgl:lwjgl-nanovg:3.3.1")

    lwjglNative("org.lwjgl:lwjgl:3.3.1:natives-windows")
    lwjglNative("org.lwjgl:lwjgl-stb:3.3.1:natives-windows")
    lwjglNative("org.lwjgl:lwjgl-tinyfd:3.3.1:natives-windows")
    lwjglNative("org.lwjgl:lwjgl-nanovg:3.3.1:natives-windows")
    lwjglNative("org.lwjgl:lwjgl:3.3.1:natives-linux")
    lwjglNative("org.lwjgl:lwjgl-stb:3.3.1:natives-linux")
    lwjglNative("org.lwjgl:lwjgl-tinyfd:3.3.1:natives-linux")
    lwjglNative("org.lwjgl:lwjgl-nanovg:3.3.1:natives-linux")
    lwjglNative("org.lwjgl:lwjgl:3.3.1:natives-macos")
    lwjglNative("org.lwjgl:lwjgl-stb:3.3.1:natives-macos")
    lwjglNative("org.lwjgl:lwjgl-tinyfd:3.3.1:natives-macos")
    lwjglNative("org.lwjgl:lwjgl-nanovg:3.3.1:natives-macos")
    shadowImpl(lwjglJar.outputs.files)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
kotlin.jvmToolchain(8)
