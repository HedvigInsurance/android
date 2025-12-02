package com.hedvig.android.lokalise.extension

import com.hedvig.android.lokalise.config.DownloadConfig
import javax.inject.Inject
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class LokalisePluginExtension @Inject constructor(
  objectFactory: ObjectFactory,
) {
  val lokaliseProjectId: Property<String> = objectFactory.property(String::class.java)
  val lokaliseToken: Property<String> = objectFactory.property(String::class.java)
  val downloadConfig: Property<DownloadConfig> = objectFactory
    .property(DownloadConfig::class.java)
    .convention(DownloadConfig())
  val outputDirectories: ConfigurableFileCollection = objectFactory.fileCollection()

  companion object {
    const val NAME = "lokalise"
  }
}
