package io.github.drumber.plugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class CustomPlugin : Plugin<Project> {

    companion object {
        const val DEFAULT_PACKAGE = "io.github.drumber.kitsune"
    }

    override fun apply(project: Project) {
        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                // extract locales task
                val langCodesProvider = project.tasks.register(
                    "${variant.name}ExtractLocales",
                    ExtractLocalesTask::class.java
                ) { task ->
                    variant.sources.res?.let { resFiles ->
                        task.packageName.set(variant.namespace)
                        task.inputFile.setFrom(resFiles.all)
                        task.outputDir.set(project.layout.buildDirectory.dir("generated/source/appLocales/${variant.name}"))
                    }
                }
                variant.sources.java?.addGeneratedSourceDirectory(
                    langCodesProvider,
                    ExtractLocalesTask::outputDir
                )

                // replace shortcuts package task
                val replacePackageInShortcutsProvider = project.tasks.register(
                    "${variant.name}ReplaceShortcutsPackage",
                    ReplaceShortcutsPackageTask::class.java
                ) { task ->
                    variant.sources.res?.let { resFiles ->
                        task.packageName.set(variant.applicationId)
                        task.inputFiles.setFrom(resFiles.static)
                        task.outputDir.set(project.layout.buildDirectory.dir("generated/res/shortcuts/${variant.name}"))
                    }
                }
                variant.sources.res?.addGeneratedSourceDirectory(
                    replacePackageInShortcutsProvider,
                    ReplaceShortcutsPackageTask::outputDir
                )
            }
        }
    }
}