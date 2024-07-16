import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class ApolloConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(
          libs.plugins.apollo
            .get()
            .pluginId,
        )
      }

      tasks.withType<com.apollographql.apollo.gradle.internal.ApolloDownloadSchemaTask>().configureEach {
        doLast {
          val schemaFile = outputFile.get().asFile
          val schemaText = schemaFile.readText()
          val convertedSchema = schemaText.performClientSideChanges()
          schemaFile.writeText(convertedSchema)
        }
      }
    }
  }
}

private fun String.performClientSideChanges(): String {
  return this
    .withoutDoubleLineBreaks()
    .makeClaimConversationNullable()
}

/**
 * Just so the schema looks more neat and is easier to navigate without all the extra whitespace
 */
private fun String.withoutDoubleLineBreaks(): String {
  return replace("\n\n", "\n")
}

/**
 * Sometimes the backend wrongly sends null for the conversation field
 * Context: https://hedviginsurance.slack.com/archives/C075NGQ600Z/p1721134166246409
 */
private fun String.makeClaimConversationNullable(): String {
  val oldValue = """
|  ${'"'}""
|  Return the relevant conversation for this claim.
|  These conversations can be one of two kinds:
|  - either it will be a dedicated conversation for this specific claim
|  - or it will be the "legacy conversation", containing the entire old chat history
|  The first one will be given for claims that are newer, created after the release of conversations.
|  The second one for all other (older) claims.
|  ${'"'}""
|  conversation: Conversation!
  """.trimMargin()
  val newValue = """
|  ${'"'}""
|  Return the relevant conversation for this claim.
|  These conversations can be one of two kinds:
|  - either it will be a dedicated conversation for this specific claim
|  - or it will be the "legacy conversation", containing the entire old chat history
|  The first one will be given for claims that are newer, created after the release of conversations.
|  The second one for all other (older) claims.
|  ${'"'}""
|  conversation: Conversation
  """.trimMargin()
  return replace(
    oldValue,
    newValue,
  )
}
