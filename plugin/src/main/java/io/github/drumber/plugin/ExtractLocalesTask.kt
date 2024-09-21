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

abstract class ExtractLocalesTask : DefaultTask() {

    companion object {
        const val DEFAULT_LANG_CODE = "en-US"
        const val CLASS_NAME = "AppLocales"
    }

    @get:Input
    abstract val packageName: Property<String>

    @get:InputFiles
    abstract val inputFile: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        packageName.convention(CustomPlugin.DEFAULT_PACKAGE)
    }

    @TaskAction
    fun taskAction() {
        // extract language codes from 'values' directories containing strings
        val languageCodes = inputFile.asFileTree
            .matching { it.include("**/values-*/strings.xml") }
            .mapNotNull { it.parentFile }
            .map { it.name.substringAfter("values-") }
            .mapNotNull { it.resourceQualifierToLanguageCode() }
            .sorted()

        val appLanguages = listOf(DEFAULT_LANG_CODE) + languageCodes

        // generate java class containing a string array with all supported app languages
        val outputFile = File(outputDir.get().asFile, "${packageName.get().replace('.', '/')}/$CLASS_NAME.java")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package ${packageName.get()};
            public class $CLASS_NAME {
                public static final String[] SUPPORTED_LOCALES = {${
                appLanguages.joinToString(
                    prefix = "\"",
                    separator = "\", \"",
                    postfix = "\""
                )
            }};
            }
        """.trimIndent()
        )
    }

    private fun String.resourceQualifierToLanguageCode(): String? {
        val segments = split("-").toMutableList()
        if (segments.isEmpty()) return null
        if (segments.size == 1) return segments.first()
        if (segments[1].startsWith('r')) {
            segments[1] = segments[1].substring(1)
        }
        return segments.joinToString("-")
    }

}