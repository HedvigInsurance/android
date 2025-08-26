package com.hedvig.android.lokalise.task

import com.hedvig.android.lokalise.config.DownloadConfig
import java.io.File
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    dirRes.fixPercentageSigns()
    tempFileForZipFile.delete()
  }

  private fun fetchBucketUrl(): String {
    val okHttpClient = OkHttpClient()
    val processId = initiateAsyncDownloadAndReturnProcessId(okHttpClient)
    val amazonDownloadUrl = pollForDownloadUrl(okHttpClient, processId)
    logger.debug("{} amazonBucketUrl:{}", tag, amazonDownloadUrl)
    return amazonDownloadUrl
  }

  private fun initiateAsyncDownloadAndReturnProcessId(okHttpClient: OkHttpClient): String {
    val asyncDownloadRequest = Request.Builder()
      .url("https://api.lokalise.com/api2/projects/${lokaliseProjectId.get()}/files/async-download")
      .commonLokaliseHeaders()
      .post(
        buildJsonObject {
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
        }.toRequestBody(),
      )
      .build()
    logger.debug("{} asyncDownloadRequest:{}", tag, asyncDownloadRequest)
    val response = okHttpClient.newCall(asyncDownloadRequest).execute().body?.string()
      ?: error("Lokalise responded with a null body")
    logger.debug("{} post response:{}", tag, response)
    val processId = Json.parseToJsonElement(response).jsonObject["process_id"]?.jsonPrimitive?.content
      ?: error("Lokalise responded with a null processId")
    return processId
  }

  private fun pollForDownloadUrl(okHttpClient: OkHttpClient, processId: String): String {
    var iteration = 0
    while (true) {
      iteration++
      logger.debug("{} iteration:{}", tag, iteration)
      if (iteration >= 5) {
        error("Lokalise failed after 10 retries")
      }
      Thread.sleep(3000L)
      val getProcessStatusRequest = Request.Builder()
        .url("https://api.lokalise.com/api2/projects/${lokaliseProjectId.get()}/processes/$processId")
        .commonLokaliseHeaders()
        .get()
        .build()
      logger.debug("{} getProcessStatusRequest:{}", tag, getProcessStatusRequest)
      val response = okHttpClient.newCall(getProcessStatusRequest).execute().body?.string()
        ?: error("Lokalise responded with a null body")
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
      logger.debug("{} process status: {}", tag, status)
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

  private fun Request.Builder.commonLokaliseHeaders(): Request.Builder {
    return header("x-api-token", lokaliseToken.get())
      .header("content-type", "application/json")
  }

  private fun JsonObject.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
  }

  /**
   * We fetch all raw percentage signs as `%%` from lokalise due to to `put("escape_percent", true)`.
   * For this to work in all scenarios, we simply replace all those cases with \u0025 which is unicode for `%`.
   */
  private fun ConfigurableFileCollection.fixPercentageSigns() {
    val allTranslationXmlFilePaths: List<okio.Path> = asFileTree
      .map { it.path.toPath() }
      .filter { it.name == """strings.xml""" }
      .toList()

    val fileSystem = FileSystem.SYSTEM
    for (currentLanguageTranslationXmlFilePath in allTranslationXmlFilePaths) {
      val content = fileSystem.read(currentLanguageTranslationXmlFilePath) { readUtf8() }
      val updatedContent = content
        .replace(
          oldValue = """%%""",
          newValue = """\u0025""",
        )
      fileSystem.write(currentLanguageTranslationXmlFilePath) {
        writeUtf8(updatedContent)
      }
    }
  }

  private fun File.fillContentsByDownloadingFromUrl(bucketUrl: String) {
    URI.create(bucketUrl).toURL().openStream().source().buffer().use { zipSource ->
      this.sink().buffer().use { localFileSink ->
        localFileSink.writeAll(zipSource)
      }
    }
  }

  private fun ConfigurableFileCollection.fillContentsByCopyingFromZipFile(zipFile: File) {
    fileSystemOperations.copy {
      it.from(archiveOperations.zipTree(zipFile))
      it.into(this.asPath)
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
  when (this) {
    "queued" -> return ProcessStatus.Queued
    "running" -> return ProcessStatus.Running
    "finished" -> return ProcessStatus.Finished
    else -> return ProcessStatus.Other(this)
  }
}
