import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec

tasks.withType<JavaExec>().configureEach {
    if (name.startsWith("runServer")) {
        doFirst("stripClientOnlyRuntimeMods") {
            classpath = classpath.filter { file ->
                !file.name.contains("angelica", ignoreCase = true)
            }
        }
    }
}

val standardTest = tasks.named<Test>("test")
val javaToolchains = extensions.getByType<JavaToolchainService>()

// Local visual fixtures live in the ignored dev/ tree and never enter the published jar.
val projectSourceSets = extensions.getByType<SourceSetContainer>()
val mainSourceSet = projectSourceSets.getByName("main")
val devSourceSet = projectSourceSets.create("dev") {
    java.srcDir("dev/src/main/java")
    resources.srcDir("dev/src/main/resources")
    compileClasspath += mainSourceSet.output + mainSourceSet.compileClasspath
}

// Forge 1.7.10 creates a mod resource pack from the directory containing @Mod classes.
// Stage a second ignored copy of fixture resources there after compiling the fixture.
val compileDevJava = tasks.named<JavaCompile>(devSourceSet.compileJavaTaskName)
compileDevJava.configure {
    options.release.set(8)
}
val stageDevResources = tasks.register<Copy>("stageDevResources") {
    from("dev/src/main/resources")
    into(compileDevJava.map { it.destinationDirectory.get().asFile })
    mustRunAfter(compileDevJava)
}

tasks.named(devSourceSet.classesTaskName).configure {
    dependsOn(stageDevResources)
}

tasks.withType<JavaExec>().configureEach {
    if (name.startsWith("runClient") || name.startsWith("runServer")) {
        dependsOn(devSourceSet.classesTaskName)
        classpath(devSourceSet.output)
    }
}

val testJava8 = tasks.register<Test>("testJava8") {
    description = "Runs the downgraded unit tests on Java 8."
    group = "verification"
    dependsOn("downgradeMainClasses", "downgradeTestClasses")
    testClassesDirs = standardTest.get().testClassesDirs
    classpath = standardTest.get().classpath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.AZUL)
    })
    shouldRunAfter(standardTest)
}

tasks.named("check").configure {
    dependsOn(testJava8)
}
