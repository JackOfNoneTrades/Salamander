import org.gradle.api.tasks.testing.Test
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
