import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Common configuration for feature modules.
 */
class FeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      ensureNotDependingOnOtherFeatureModule()
    }
  }
}

private fun Project.ensureNotDependingOnOtherFeatureModule() {
  val thisModuleName = this.name
  configurations.configureEach {
    resolutionStrategy {
      eachDependency {
        if (requested.group != "hedvigandroid") return@eachDependency // Only check for our own modules
        if (requested.name == thisModuleName) return@eachDependency // Only check deps to other modules
        // feature-foo-ui is allowed to depend on feature-foo-data
        if (thisModuleName.withoutUiOrDataSuffix() == requested.name.withoutUiOrDataSuffix()) {
          // Only allow -ui module to depend on the -data module, not vice versa
          require(thisModuleName.endsWith("-ui")) {
            "Hedvig build error on FeatureConventionPlugin." +
              "\nDo not depend on a -ui feature module from a -data feature module."
            "\nIn particular, ${thisModuleName} is trying to depend on ${requested.name}." +
              "\nDid you mean to have the -ui module depend on the -data module instead?"
          }
          return@eachDependency
        }
        val requestedModuleIsAFeatureModule =
          requested.name.startsWith("feature-") &&
            !requested.name.startsWith("feature-flags")
        require(!requestedModuleIsAFeatureModule) {
          "Hedvig build error on FeatureConventionPlugin." +
            "\nYou are trying to depend on another feature module from a feature module." +
            "\nThis is not allowed as it breaks our ability to properly share code between modules." +
            "\nIn particular, ${thisModuleName} is trying to depend on ${requested.name}." +
            "\nIf you need to share code between feature modules, consider moving the shared code to a library module."
        }
      }
    }
  }
}

private fun String.withoutUiOrDataSuffix(): String = this.removeSuffix("-ui").removeSuffix("-data")
