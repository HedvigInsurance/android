package com.hedvig.android.lokalise.task

import com.hedvig.android.lokalise.config.DownloadConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.io.File
import kotlinx.coroutines.runBlocking
import java.net.URI
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.sink
import okio.source
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class DownloadStringsTask @Inject constructor(
  private val fileSystemOperations: FileSystemOperations,
  private val archiveOperations: ArchiveOperations,
) : DefaultTask() {
  init {
    description = "Tasks which downloads the lokalise strings into strings.xml"
    group = "lokalise"
  }

  @get:Input
  @get:Option(option = "lokaliseProjectId", description = "The ID of the lokalise project")
  abstract val lokaliseProjectId: Property<String>

  @get:Input
  @get:Option(option = "lokaliseToken", description = "Token to authenticate with Lokalise")
  abstract val lokaliseToken: Property<String>

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val outputDirectory: ConfigurableFileCollection

  @get:Input
  @get:Option(option = "downloadConfig", description = "Configuration of how to download the strings")
  abstract val downloadConfig: Property<DownloadConfig>

  private val tag = "[Hedvig Lokalise Plugin]"

  @TaskAction
  fun handle() {
    val dirRes = outputDirectory
    logger.debug("{} strings will be put at path:{}", tag, outputDirectory.asPath)

    val bucketUrl = fetchBucketUrl()
    val tempFileForZipFile = File.createTempFile("lang-file", ".zip")
    tempFileForZipFile.fillContentsByDownloadingFromUrl(bucketUrl)
    logger.debug("{} zip file path:{}", tag, tempFileForZipFile.absolutePath)
    dirRes.fillContentsByCopyingFromZipFile(tempFileForZipFile)
    logger.debug("{} dirRes:{}", tag, dirRes.asFileTree.map { it.absolutePath })
    dirRes.editTranslations { resourcesType ->
      fixPercentageSigns()
        .removeDotsFromStringIds()
        .convertSimpleFormatToNumberedFormat(resourcesType)
        .removeEscapesFromMultiplatformStrings(resourcesType)
        .addUntranslatableStrings()
    }
    tempFileForZipFile.delete()
  }

  private fun fetchBucketUrl(): String {
    return runBlocking {
      val httpClient = HttpClient(CIO)
      httpClient.use { client ->
        val processId = initiateAsyncDownloadAndReturnProcessId(client)
        val amazonDownloadUrl = pollForDownloadUrl(client, processId)
        logger.debug("{} amazonBucketUrl:{}", tag, amazonDownloadUrl)
        amazonDownloadUrl
      }
    }
  }

  private suspend fun initiateAsyncDownloadAndReturnProcessId(httpClient: HttpClient): String {
    val requestBody = buildJsonObject {
      put("format", "xml")
      put("export_sort", downloadConfig.get().stringsOrder.value)
      put("export_empty_as", downloadConfig.get().emptyTranslationStrategy.value)
      put("replace_breaks", true)
      put("escape_percent", true)
      put(
        "filter_langs",
        buildJsonArray {
          add("en")
          add("sv_SE")
        },
      )
    }
    logger.debug("{} asyncDownloadRequest body:{}", tag, requestBody)
    val response = httpClient.post("https://api.lokalise.com/api2/projects/${lokaliseProjectId.get()}/files/async-download") {
      commonLokaliseHeaders()
      setBody(requestBody.toString())
    }.bodyAsText()
    logger.debug("{} post response:{}", tag, response)
    val processId = Json.parseToJsonElement(response).jsonObject["process_id"]?.jsonPrimitive?.content
      ?: error("Lokalise responded with a null processId")
    return processId
  }

  private suspend fun pollForDownloadUrl(httpClient: HttpClient, processId: String): String {
    var iteration = 0
    while (true) {
      iteration++
      logger.debug("{} iteration:{}", tag, iteration)
      if (iteration >= 20) {
        error("Lokalise failed after 20 retries")
      }
      Thread.sleep(3000L)
      val url = "https://api.lokalise.com/api2/projects/${lokaliseProjectId.get()}/processes/$processId"
      logger.debug("{} getProcessStatusRequest url:{}", tag, url)
      val response = httpClient.get(url) {
        commonLokaliseHeaders()
      }.bodyAsText()
      logger.debug("{} get response:{}", tag, response)
      val process = Json
        .parseToJsonElement(response)
        .jsonObject["process"]
        ?: error("Lokalise responded with a null process")
      val status = process
        .jsonObject["status"]
        ?.jsonPrimitive
        ?.content
        ?.toProcessStatus()
        ?: error("Lokalise responded with a null process status")
      logger.info("{} process status for iteration#{}: {}", tag, iteration, status)
      when (status) {
        is ProcessStatus.Other -> error("Lokalise responded with an unknown process status: ${status.status}")
        ProcessStatus.Queued, ProcessStatus.Running -> continue
        ProcessStatus.Finished -> {
          val downloadUrl = process
            .jsonObject["details"]
            ?.jsonObject["download_url"]
            ?.jsonPrimitive
            ?.content
            ?: error("Lokalise responded with a null download_url")
          return downloadUrl
        }
      }
    }
  }

  private fun HttpRequestBuilder.commonLokaliseHeaders() {
    header("x-api-token", lokaliseToken.get())
    contentType(ContentType.Application.Json)
  }

  private fun ConfigurableFileCollection.editTranslations(
    block: String.(ResourcesType) -> String,
  ) {
    val allTranslationXmlFilePaths: List<okio.Path> = asFileTree
      .map { it.path.toPath() }
      .filter { it.name == """strings.xml""" }
      .toList()

    val fileSystem = FileSystem.SYSTEM
    for (currentLanguageTranslationXmlFilePath in allTranslationXmlFilePaths) {
      val resourcesType = when {
        currentLanguageTranslationXmlFilePath.segments.any { it == "commonMain" } -> ResourcesType.KMP
        else -> ResourcesType.Android
      }
      val content = fileSystem.read(currentLanguageTranslationXmlFilePath) { readUtf8() }
      val updatedContent = content.block(resourcesType)
      fileSystem.write(currentLanguageTranslationXmlFilePath) {
        writeUtf8(updatedContent)
      }
    }
  }

  /**
   * We fetch all raw percentage signs as `%%` from lokalise due to to `put("escape_percent", true)`.
   * For this to work in all scenarios, we simply replace all those cases with \u0025 which is unicode for `%`.
   */
  private fun String.fixPercentageSigns(): String {
    return replace(
      oldValue = """%%""",
      newValue = """\u0025""",
    )
  }

  /**
   * Replace all `.` from string keys with `_`. They get translated to `_` anyway in order to be able to access them
   * from Kotlin code so this has no change on the call sites.
   * Keeping the `.` also breaks the code generation for CMP resources, this fixes it.
   */
  private fun String.removeDotsFromStringIds(): String {
    return this.replace(
      // Regex("""name="([^"]*)")""" - Matches the pattern name="..." where:
      //    - name=" - literal text
      //    - ([^"]*) - captures any characters that are not quotes (this is group 1)
      //    - " - closing quote
      Regex(
        """
        name="([^"]*)"
        """.trimIndent(),
      ),
    ) { matchResult ->
      val nameValue = matchResult.groupValues[1]
      val nameWithoutDots = nameValue.replace(".", "_")
      """name="$nameWithoutDots""""
    }
  }

  /**
   * Compose Multiplatform resources only support numbered format arguments like %1$s, %2$d
   * but NOT simple %s, %d. Convert simple placeholders to numbered ones for KMP resources.
   * We process each <string> and <item> tag separately to reset the counter for each resource.
   * Reference: StringResourcesUtils uses regex pattern that matches %1$s, %2$d, etc.
   * Source: https://youtrack.jetbrains.com/projects/CMP/issues/CMP-8385/Resources-Support-for-d-and-s-in-StringResourcesUtils-for-single-argument-string-resource
   */
  private fun String.convertSimpleFormatToNumberedFormat(resourcesType: ResourcesType): String {
    return when (resourcesType) {
      ResourcesType.Android -> this
      ResourcesType.KMP -> {
        val resourceTagRegex = Regex("""<(string|item)[^>]*>([^<]*)</(string|item)>""")
        val simpleFormatRegex = Regex("""%(?!\d+\$)([sidf])""")

        resourceTagRegex.replace(this) { tagMatch ->
          val tagContent = tagMatch.groupValues[2]
          var counter = 0
          val updatedContent = simpleFormatRegex.replace(tagContent) { formatMatch ->
            counter++
            val formatType = formatMatch.groupValues[1]
            "%$counter\$$formatType"
          }
          tagMatch.value.replace(tagContent, updatedContent)
        }
      }
    }
  }

  private fun String.removeEscapesFromMultiplatformStrings(resourcesType: ResourcesType): String {
    return when (resourcesType) {
      ResourcesType.Android -> this
      ResourcesType.KMP -> {
        replace("""\'""", """'""")
          .replace("""\"""", """"""")
      }
    }
  }
  private fun String.addUntranslatableStrings(): String {
    return this.replace(
      """<resources>""",
      """
      |<resources>
      |  <string name="swedish">Svenska</string>
      |  <string name="english_swedish">English</string>
      |  <string name="swish">Swish</string>
      |  <string name="trustly">"Trustly"</string>
      """.trimMargin("|")
    )
  }

  private fun File.fillContentsByDownloadingFromUrl(bucketUrl: String) {
    URI.create(bucketUrl).toURL().openStream().source().buffer().use { zipSource ->
      this.sink().buffer().use { localFileSink ->
        localFileSink.writeAll(zipSource)
      }
    }
  }

  private fun ConfigurableFileCollection.fillContentsByCopyingFromZipFile(zipFile: File) {
    files.forEach { file ->
      fileSystemOperations.copy {
        it.from(archiveOperations.zipTree(zipFile))
        it.into(file.path)
      }
    }
  }
}

private sealed interface ProcessStatus {
  data object Queued : ProcessStatus

  data object Finished : ProcessStatus

  data object Running : ProcessStatus

  data class Other(val status: String) : ProcessStatus
}

private fun String.toProcessStatus(): ProcessStatus {
  return when (this) {
    "queued" -> ProcessStatus.Queued
    "running" -> ProcessStatus.Running
    "finished" -> ProcessStatus.Finished
    else -> ProcessStatus.Other(this)
  }
}

private enum class ResourcesType {
  KMP,
  Android,
}
