package com.hedvig.android.viewmodel.processor

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isNotNull
import assertk.assertions.isNull
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
        @HedvigViewModel(ActivityRetainedScope::class)
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
        @HedvigViewModel(ActivityRetainedScope::class)
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
        @HedvigViewModel(ActivityRetainedScope::class)
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
        @HedvigViewModel(ActivityRetainedScope::class)
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
        @HedvigViewModel(ActivityRetainedScope::class)
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
  fun `scope argument selects the contribution scope in the generated code`() {
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
  fun `commonMain view model is skipped in a single-target leaf pass while non-common VMs are emitted`() {
    // A single-target (e.g. kspAndroid) pass re-sees a commonMain VM that the multi-target metadata
    // pass already generated; it must NOT re-emit it, or the VM's Metro contribution is declared twice.
    // A VM outside commonMain (here under androidMain) in the same pass is still generated.
    val result = compile(
      vmAt(
        "commonMain/CommonViewModels.kt",
        """
        @HedvigViewModel(ActivityRetainedScope::class)
        class SharedViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
      vmAt(
        "androidMain/AndroidViewModels.kt",
        """
        @HedvigViewModel(ActivityRetainedScope::class)
        class AndroidOnlyViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
      options = mapOf(PASS_OPTION to "leaf"),
    )

    assertThat(result.generatedFile("SharedViewModelModule.kt")).isNull()
    assertThat(result.generatedFile("AndroidOnlyViewModelModule.kt")).isNotNull()
  }

  @Test
  fun `commonMain view model is emitted in a single-platform pass with the metadata role`() {
    // KMP modules without native targets run their metadata pass as a single JVM platform, so the
    // platform count cannot identify it and the build sets the role instead.
    val result = compile(
      vmAt(
        "commonMain/CommonViewModels.kt",
        """
        @HedvigViewModel(ActivityRetainedScope::class)
        class SharedViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
      options = mapOf(PASS_OPTION to "metadata"),
    )

    assertThat(result.generatedFile("SharedViewModelModule.kt")).isNotNull()
  }

  @Test
  fun `commonMain view model in a single-platform pass without a role fails the build`() {
    // Without the role, a single-platform pass would skip the VM as a leaf pass and the build would pass
    // with the VM missing from the graph, so the processor reports it instead.
    val result = compile(
      vmAt(
        "commonMain/CommonViewModels.kt",
        """
        @HedvigViewModel(ActivityRetainedScope::class)
        class SharedViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
    )

    val processorErrors = result.messages.lines().filter { it.startsWith("e:") && "SharedViewModel" in it }
    assertThat(processorErrors.joinToString("\n")).contains(PASS_OPTION)
    assertThat(result.generatedFile("SharedViewModelModule.kt")).isNull()
  }

  @Test
  fun `an unknown pass role fails the build`() {
    val result = compile(
      vm(
        """
        @HedvigViewModel(ActivityRetainedScope::class)
        class PlainViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
      options = mapOf(PASS_OPTION to "metdata"),
    )

    val processorErrors = result.messages.lines().filter { it.startsWith("e:") && PASS_OPTION in it }
    assertThat(processorErrors.joinToString("\n")).contains("metdata")
  }

  @Test
  fun `internal view model still produces a public generated declaration`() {
    // The generated contribution must always be public so Metro emits a public `metro/hints` marker,
    // which is what makes the contribution discoverable from the merging `:app` graph. An internal
    // declaration emits an internal hint that is invisible across module boundaries, silently dropping
    // the VM from the graph (runtime "Unknown model class"). Exposing the internal VM through the public
    // wrapper is allowed via the file-level @Suppress the processor emits.
    val text = compile(
      vm(
        """
        @HedvigViewModel(ActivityRetainedScope::class)
        internal class SecretViewModel @Inject constructor(val dep: String) : ViewModel()
        """.trimIndent(),
      ),
    ).generatedText("SecretViewModelModule.kt")

    assertThat(text).contains("public interface SecretViewModelModule")
    assertThat(text).contains("EXPOSED_PARAMETER_TYPE")
  }

  @Test
  fun `view model whose constructor type never resolves fails with an error naming it`() {
    // An unresolved constructor type stands in for a dependency missing from the commonMain metadata
    // pass's classpath. Such a VM is deferred every round and never generated, so the
    // processor must report it rather than let the build pass with the VM missing from the graph.
    // The message is matched on processor-specific wording, since the harness's kotlinc stage always
    // adds unrelated errors of its own (see the class comment).
    val result = compile(
      vm(
        """
        @HedvigViewModel(ActivityRetainedScope::class)
        class StrandedViewModel @Inject constructor(val repository: MissingRepository) : ViewModel()
        """.trimIndent(),
      ),
    )

    val processorErrors = result.messages.lines().filter { it.startsWith("e:") && "StrandedViewModel" in it }
    assertThat(processorErrors.joinToString("\n")).contains("MissingRepository")
    assertThat(result.generatedFile("StrandedViewModelModule.kt")).isNull()
  }

  private fun vm(body: String): SourceFile = vmAt("ViewModels.kt", body)

  // [fileName] may include directory segments (e.g. "commonMain/X.kt"); the processor's leaf-dedupe
  // keys off "/commonMain/" appearing in the symbol's file path, so the path is part of the fixture.
  private fun vmAt(fileName: String, body: String): SourceFile = SourceFile.kotlin(
    fileName,
    """
    package com.hedvig.test

    import androidx.lifecycle.SavedStateHandle
    import androidx.lifecycle.ViewModel
    import com.hedvig.android.core.common.di.ActivityRetainedScope
    import com.hedvig.android.core.common.di.HedvigViewModel
    import dev.zacsweers.metro.AppScope
    import dev.zacsweers.metro.Assisted
    import dev.zacsweers.metro.AssistedInject
    import dev.zacsweers.metro.Inject

    $body
    """.trimIndent(),
  )

  private fun compile(vararg sources: SourceFile, options: Map<String, String> = emptyMap()): JvmCompilationResult {
    val compilation = KotlinCompilation().apply {
      workingDir = temporaryFolder.root
      this.sources = stubs + sources.toList()
      inheritClassPath = true
      messageOutputStream = System.out
      configureKsp {
        symbolProcessorProviders.add(HedvigViewModelProcessorProvider())
        processorOptions.putAll(options)
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

      annotation class HedvigViewModel(val scope: KClass<*>)
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

  private companion object {
    // Spelled out rather than taken from the processor, so renaming the option on one side fails this
    // test. ViewModelsHandler in build-logic sets the same name.
    const val PASS_OPTION = "hedvig.viewmodel.pass"
  }
}
