package com.hedvig.android.viewmodel.processor

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isNotNull
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import java.io.File
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Exercises the KSP processor end-to-end: real symbols in, real generated files out.
 *
 * Note on assertions: we deliberately assert on the *generated source* and the processor's own
 * diagnostics rather than the overall compilation [JvmCompilationResult.exitCode]. The kctfork
 * harness embeds an older Kotlin compiler than this repo's toolchain, so the post-KSP `kotlinc`
 * stage always fails on stdlib metadata-version mismatch — that is a harness artifact, not the
 * processor. KSP itself runs to completion (files are written, diagnostics fire), which is exactly
 * what we verify here. That generated code actually compiles is covered by the real app build.
 */
@OptIn(ExperimentalCompilerApi::class)
class HedvigViewModelProcessorTest {
  @get:Rule
  val temporaryFolder: TemporaryFolder = TemporaryFolder()

  @Test
  fun `no-arg view model generates a ViewModelKey module contributed into the scope`() {
    val text = compile(
      vm(
        """
        @HedvigViewModel
        class HomeViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
    ).generatedText("HomeViewModelModule.kt")

    assertThat(text).contains("interface HomeViewModelModule")
    assertThat(text).contains("@ContributesTo(ActivityRetainedScope::class)")
    assertThat(text).contains("@ViewModelKey(HomeViewModel::class)")
    assertThat(text).contains("@Provides")
    assertThat(text).contains("@IntoMap")
    assertThat(text).contains("public fun provide(viewModel: HomeViewModel): ViewModel = viewModel")
  }

  @Test
  fun `assisted view model generates a manual assisted factory contributed into the scope`() {
    val text = compile(
      vm(
        """
        @HedvigViewModel
        class ContractViewModel @AssistedInject constructor(
          @Assisted val contractId: String,
          val dep: String,
        ) : ViewModel()
        """.trimIndent(),
      ),
    ).generatedText("ContractViewModelFactory.kt")

    assertThat(text).contains("interface ContractViewModelFactory : ManualViewModelAssistedFactory")
    assertThat(text).contains("@AssistedFactory")
    assertThat(text).contains("@ManualViewModelAssistedFactoryKey")
    assertThat(text).contains("@ContributesIntoMap(ActivityRetainedScope::class)")
    assertThat(text).contains("public fun create(@Assisted contractId: String): ContractViewModel")
    // Only the @Assisted constructor params should reach the factory, not the injected ones.
    assertThat(text).doesNotContain("dep")
    // Assisted factories use a key, never the no-arg ViewModelKey path.
    assertThat(text).doesNotContain("@ViewModelKey")
  }

  @Test
  fun `assisted qualifier value is carried onto the generated factory parameter`() {
    val text = compile(
      vm(
        """
        @HedvigViewModel
        class TopicViewModel @AssistedInject constructor(
          @Assisted("topicId") val id: String,
        ) : ViewModel()
        """.trimIndent(),
      ),
    ).generatedText("TopicViewModelFactory.kt")

    assertThat(text).contains("""@Assisted("topicId")""")
  }

  @Test
  fun `saved state handle view model generates a CreationExtras-backed factory`() {
    val text = compile(
      vm(
        """
        @HedvigViewModel
        class LoginViewModel @AssistedInject constructor(
          @Assisted val savedStateHandle: SavedStateHandle,
          val dep: String,
        ) : ViewModel()
        """.trimIndent(),
      ),
    ).generatedText("LoginViewModelFactory.kt")

    assertThat(text).contains("interface LoginViewModelFactory : ViewModelAssistedFactory")
    assertThat(text).contains("@ViewModelAssistedFactoryKey(LoginViewModel::class)")
    assertThat(text).contains("@ContributesIntoMap(ActivityRetainedScope::class)")
    // The handle can't be passed at a call site; it is pulled out of CreationExtras instead.
    assertThat(text).contains("createSavedStateHandle")
    assertThat(text).contains("public fun create(@Assisted savedStateHandle: SavedStateHandle): LoginViewModel")
  }

  @Test
  fun `mixing saved state handle with a nav arg generates a manual factory carrying both`() {
    val text = compile(
      vm(
        """
        @HedvigViewModel
        class CombinedViewModel @AssistedInject constructor(
          @Assisted val contractId: String,
          @Assisted val savedStateHandle: SavedStateHandle,
          val dep: String,
        ) : ViewModel()
        """.trimIndent(),
      ),
    ).generatedText("CombinedViewModelFactory.kt")

    assertThat(text).contains("interface CombinedViewModelFactory : ManualViewModelAssistedFactory")
    assertThat(text).contains("@ManualViewModelAssistedFactoryKey")
    assertThat(text).contains("@ContributesIntoMap(ActivityRetainedScope::class)")
    // Both assisted params reach the factory in declared order — the handle is just another @Assisted
    // param. The caller supplies it from the lambda's CreationExtras via createSavedStateHandle().
    assertThat(text).contains(
      "public fun create(@Assisted contractId: String, @Assisted savedStateHandle: SavedStateHandle): CombinedViewModel",
    )
    // The mix uses the call-site (manual) path, not the CreationExtras-only ViewModelAssistedFactory path.
    assertThat(text).doesNotContain("createSavedStateHandle")
    // Injected (non-assisted) params never reach the factory.
    assertThat(text).doesNotContain("dep")
  }

  @Test
  fun `scope argument overrides the default scope in the generated code`() {
    val text = compile(
      vm(
        """
        @HedvigViewModel(AppScope::class)
        class ReceiverViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
    ).generatedText("ReceiverViewModelModule.kt")

    assertThat(text).contains("@ContributesTo(AppScope::class)")
    assertThat(text).doesNotContain("ActivityRetainedScope")
  }

  @Test
  fun `internal view model produces an internal generated declaration`() {
    val text = compile(
      vm(
        """
        @HedvigViewModel
        internal class SecretViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
    ).generatedText("SecretViewModelModule.kt")

    assertThat(text).contains("internal interface SecretViewModelModule")
  }

  private fun vm(body: String): SourceFile = SourceFile.kotlin(
    "ViewModels.kt",
    """
    package com.hedvig.test

    import androidx.lifecycle.SavedStateHandle
    import androidx.lifecycle.ViewModel
    import com.hedvig.android.core.common.di.HedvigViewModel
    import dev.zacsweers.metro.AppScope
    import dev.zacsweers.metro.Assisted
    import dev.zacsweers.metro.AssistedInject
    import dev.zacsweers.metro.Inject

    $body
    """.trimIndent(),
  )

  private fun compile(vararg sources: SourceFile): JvmCompilationResult {
    val compilation = KotlinCompilation().apply {
      workingDir = temporaryFolder.root
      this.sources = stubs + sources.toList()
      inheritClassPath = true
      messageOutputStream = System.out
      configureKsp(useKsp2 = true) {
        symbolProcessorProviders.add(HedvigViewModelProcessorProvider())
      }
    }
    return compilation.compile()
  }

  private fun JvmCompilationResult.generatedFile(name: String): File? =
    temporaryFolder.root.walkTopDown().firstOrNull { it.isFile && it.name == name }

  private fun JvmCompilationResult.generatedText(name: String): String {
    val file = generatedFile(name)
    assertThat(file).isNotNull()
    return file!!.readText()
  }

  // Minimal stubs so the input ViewModels resolve and validate under KSP without dragging in the
  // real (heavyweight, KMP) Metro/lifecycle artifacts. The processor matches these by fully
  // qualified name, so the package + simple name are all that matter.
  private val stubs: List<SourceFile> = listOf(
    SourceFile.kotlin(
      "DiStubs.kt",
      """
      package com.hedvig.android.core.common.di

      import kotlin.reflect.KClass

      class ActivityRetainedScope

      annotation class HedvigViewModel(val scope: KClass<*> = ActivityRetainedScope::class)
      """.trimIndent(),
    ),
    SourceFile.kotlin(
      "MetroStubs.kt",
      """
      package dev.zacsweers.metro

      class AppScope

      annotation class Inject

      annotation class AssistedInject

      annotation class Assisted(val value: String = "")
      """.trimIndent(),
    ),
    SourceFile.kotlin(
      "LifecycleStubs.kt",
      """
      package androidx.lifecycle

      open class ViewModel

      class SavedStateHandle
      """.trimIndent(),
    ),
  )
}
