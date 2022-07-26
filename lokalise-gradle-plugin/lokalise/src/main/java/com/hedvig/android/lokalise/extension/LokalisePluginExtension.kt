@file:Suppress("MISSING_DEPENDENCY_CLASS")

package com.hedvig.android.lokalise.extension

import com.hedvig.android.lokalise.config.DownloadConfig
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class LokalisePluginExtension @Inject constructor(
  objectFactory: ObjectFactory,
) {
  val lokaliseProjectId: Property<String> = objectFactory.property(String::class.java)
  val lokaliseToken: Property<String> = objectFactory.property(String::class.java)
  val downloadConfig: Property<DownloadConfig> = objectFactory
    .property(DownloadConfig::class.java)
    .convention(DownloadConfig())
  val outputDirectory: RegularFileProperty = objectFactory.fileProperty()

  companion object {
    const val NAME = "lokalise"
  }
}
