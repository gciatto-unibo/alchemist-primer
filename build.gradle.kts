/*
 * DEFAULT GRADLE BUILD FOR ALCHEMIST SIMULATOR
 */

plugins {
    application
}

repositories {
    mavenCentral()
    /* 
     * The following repositories contain beta features and should be added for experimental mode only
     * 
     * maven("https://dl.bintray.com/alchemist-simulator/Alchemist/")
     * maven("https://dl.bintray.com/protelis/Protelis/")
     */
}
/*
 * Only required if you plan to use Protelis, remove otherwise
 */
sourceSets {
    main {
        resources {
            srcDir("src/main/protelis")
        }
    }
}
dependencies {
    // it is highly recommended to replace the '+' symbol with a fixed version
    implementation("it.unibo.alchemist:alchemist:+")
}

val batch: String by project
val maxTime: String by project

val alchemistGroup = "Run Alchemist"
/*
 * This task is used to run all experiments in sequence
 */
val runAll by tasks.register<DefaultTask>("runAll") {
    group = alchemistGroup
    description = "Launches all simulations"
}
/*
 * Scan the folder with the simulation files, and create a task for each one of them.
 */
File(rootProject.rootDir.path + "/src/main/yaml").listFiles()
    .filter { it.extension == "yml" }
    .sortedBy { it.nameWithoutExtension }
    .forEach {
        val task by tasks.register<JavaExec>("run${it.nameWithoutExtension.capitalize()}") {
            group = alchemistGroup
            description = "Launches simulation ${it.nameWithoutExtension}"
            main = "it.unibo.alchemist.Alchemist"
            classpath = sourceSets["main"].runtimeClasspath
            val exportsDir = File("${projectDir.path}/build/exports/${it.nameWithoutExtension}")
            doFirst {
                if (!exportsDir.exists()) {
                    exportsDir.mkdirs()
                }
            }
            args("-y", it.absolutePath, "-e", "$exportsDir/${it.nameWithoutExtension}-${System.currentTimeMillis()}")
            if (System.getenv("CI") == "true" || batch == "true") {
                args("-hl", "-t", maxTime)
            } else {
                args("-g", "effects/${it.nameWithoutExtension}.aes")
            }
            outputs.dir(exportsDir)
        }
        // task.dependsOn(classpathJar) // Uncomment to switch to jar-based cp resolution
        runAll.dependsOn(task)
    }

