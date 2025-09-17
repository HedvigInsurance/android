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
        if (!extension.lokaliseToken.isPresent) {
          error(
            """
            |The lokalise extension for task "downloadStrings" does not look to be properly setup.
            |Are you sure the `lokalise.properties` file exists in your directory?
            |Hint: Look at the `lokalise {...` plugin extension setup in the resources gradle project.
            """.trimMargin(),
          )
        }
        task.lokaliseProjectId.set(extension.lokaliseProjectId)
        task.lokaliseToken.set(extension.lokaliseToken)
        task.downloadConfig.set(extension.downloadConfig)
        task.outputDirectory.from(extension.outputDirectory)
      }
    }
  }
}
