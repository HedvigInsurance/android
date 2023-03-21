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
        apply(libs.plugins.apollo.get().pluginId)
      }

      tasks.withType<com.apollographql.apollo3.gradle.internal.ApolloDownloadSchemaTask>().configureEach {
        doLast {
          val schemaPath = schema.get()
          val schemaFile = file(schemaPath)
          val schemaText = schemaFile.readText()
          val convertedSchema = schemaText.performClientSideChanges()
          schemaFile.writeText(convertedSchema)
        }
      }

      tasks.register("downloadApolloSchemasFromIntrospection") {
        tasks.findByName("downloadGiraffeApolloSchemaFromIntrospection")?.let { downloadTask ->
          dependsOn(downloadTask)
        }
        tasks.findByName("downloadOctopusApolloSchemaFromIntrospection")?.let { downloadTask ->
          dependsOn(downloadTask)
        }
      }
    }
  }
}

private fun String.performClientSideChanges(): String {
  return this.withoutDoubleLineBreaks()
}

/**
 * Just so the schema looks more neat and is easier to navigate without all the extra whitespace
 */
private fun String.withoutDoubleLineBreaks(): String {
  return replace("\n\n", "\n")
}
