import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.hedvig.android.configureKotlinAndroid
import com.hedvig.android.targetSdkVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<BaseAppModuleExtension> {
                configureKotlinAndroid(this, addStandardBuildTypes = false)
                defaultConfig.targetSdk = libs.targetSdkVersion
            }
        }
    }
}
