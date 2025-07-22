package com.hedvig.android.lokalise

import com.hedvig.android.lokalise.extension.LokalisePluginExtension
import com.hedvig.android.lokalise.task.DownloadStringsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused") // Used in ./lokalise-gradle-plugin/lokalise/build.gradle.kts
class HedvigLokalisePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.create(LokalisePluginExtension.NAME, LokalisePluginExtension::class.java, project.objects)

    project.afterEvaluate {
      val extension = project.extensions.findByType(LokalisePluginExtension::class.java)
        ?: error(
          """
          |${LokalisePluginExtension.NAME} extension not specified.
          |Try adding something like:
          |```
          |lokalise {
          |  lokaliseProjectId.set("...")
          |  lokaliseToken.set("...")
          |  outputDirectory.set(file("src/main/res"))
          |  // Optionally
          |  downloadConfig.set(com.hedvig.android.lokalise.config.DownloadConfig()) 
          |}
          |```
          """.trimMargin(),
        )
      project.tasks.register("downloadStrings", DownloadStringsTask::class.java) { task ->
        task.lokaliseProjectId.set(extension.lokaliseProjectId)
        task.lokaliseToken.set(extension.lokaliseToken)
        task.downloadConfig.set(extension.downloadConfig)
        task.outputDirectory.from(extension.outputDirectory)
      }
    }
  }
}
