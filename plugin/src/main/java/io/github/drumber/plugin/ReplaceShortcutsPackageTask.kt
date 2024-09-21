package io.github.drumber.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ReplaceShortcutsPackageTask : DefaultTask() {

    @get:Input
    abstract val packageName: Property<String>

    @get:InputFiles
    abstract val inputFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val packageNameValue = packageName.get()

        val shortcutsFile = inputFiles.asFileTree
            .matching { it.include("**/shortcuts.xml") }
            .singleFile

        val content = shortcutsFile.readText()
        val updatedContent = content.replace(
            "android:targetPackage=\"${CustomPlugin.DEFAULT_PACKAGE}\"",
            "android:targetPackage=\"$packageNameValue\""
        )

        val outputFile = File(outputDir.get().asFile, "xml-v25/shortcuts.xml")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(updatedContent)
    }
}