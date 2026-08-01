tasks.withType<JavaExec>().configureEach {
    if (name.startsWith("runServer")) {
        doFirst("stripClientOnlyRuntimeMods") {
            classpath = classpath.filter { file ->
                !file.name.contains("angelica", ignoreCase = true)
            }
        }
    }
}
