import com.hedvig.android.ktlintVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.konan.file.File
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<KtlintExtension> {
                version.set(libs.ktlintVersion)
                enableExperimentalRules.set(true)
                additionalEditorconfigFile.set(file("${project.rootDir}${File.separator}.editorconfig"))
                outputToConsole.set(true)
                filter {
                    exclude("**/generated/**")
                    include("**/src/**/*.kt")
                    include("**/src/**/*.kts")
                    include("**/build.gradle.kts")
                }
                reporters {
                    reporter(ReporterType.CHECKSTYLE)
                }
            }

            tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask> {
                reportsOutputDirectory.set(
                    project.layout.buildDirectory.dir("ktlint-report-in-checkstyle-format"),
                )
            }
        }
    }
}
