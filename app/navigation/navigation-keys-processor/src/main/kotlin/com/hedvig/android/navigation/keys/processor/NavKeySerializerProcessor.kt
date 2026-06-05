package com.hedvig.android.navigation.keys.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

private const val NAV_KEY_FQ_NAME = "com.hedvig.android.navigation.common.HedvigNavKey"
private const val SERIALIZABLE_FQ_NAME = "kotlinx.serialization.Serializable"

private val appScope = ClassName("com.hedvig.android.core.common.di", "AppScope")
private val hedvigNavKey = ClassName("com.hedvig.android.navigation.common", "HedvigNavKey")
private val contributesTo = ClassName("dev.zacsweers.metro", "ContributesTo")
private val provides = ClassName("dev.zacsweers.metro", "Provides")
private val intoSet = ClassName("dev.zacsweers.metro", "IntoSet")
private val serializersModuleType = ClassName("kotlinx.serialization.modules", "SerializersModule")
private val serializersModuleFun = MemberName("kotlinx.serialization.modules", "SerializersModule")
private val polymorphic = MemberName("kotlinx.serialization.modules", "polymorphic")
private val subclass = MemberName("kotlinx.serialization.modules", "subclass")

/**
 * Generates the kotlinx.serialization polymorphic registration that lets a Nav3 back stack of
 * HedvigNavKeys survive process death, so no module has to hand-write that boilerplate.
 *
 * For every concrete `@Serializable` type that implements `HedvigNavKey`, this processor emits one
 * provider interface per package. Metro then merges all of those `@IntoSet` modules into a single
 * app-wide `SerializersModule` used to (de)serialize the saved back stack.
 *
 * ```text
 *   :feature-foo            (any module that opts in with `hedvig { navKeys() }`)
 *   ----------------------------------------------------------------------------
 *     @Serializable object     FooKey        : HedvigNavKey
 *     @Serializable data class BarKey(...)   : HedvigNavKey
 *
 *                    |
 *                    |   NavKeySerializerProcessor (this KSP processor)
 *                    v
 *
 *   generated: one provider interface per package, names derived from the package
 *   ----------------------------------------------------------------------------
 *     @ContributesTo(AppScope::class)
 *     interface FeatureFooGeneratedNavKeySerializersModuleProvider {
 *       @Provides
 *       @IntoSet
 *       fun provideFeatureFooNavKeySerializersModule(): SerializersModule =
 *         SerializersModule {
 *           polymorphic(HedvigNavKey::class) {
 *             subclass(FooKey::class)
 *             subclass(BarKey::class)
 *           }
 *         }
 *     }
 *
 *                    |
 *                    |   Metro merges every @IntoSet SerializersModule across all modules
 *                    v
 *
 *   one app-wide SerializersModule  ->  (de)serializes the Nav3 back stack on process death
 * ```
 */
class NavKeySerializerProcessor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val keys = resolver
      .getSymbolsWithAnnotation(SERIALIZABLE_FQ_NAME)
      .filterIsInstance<KSClassDeclaration>()
      .filter { it.classKind == ClassKind.CLASS || it.classKind == ClassKind.OBJECT }
      .filterNot { it.isAbstract() }
      .filter { decl ->
        decl.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == NAV_KEY_FQ_NAME }
      }
      .sortedBy { it.qualifiedName?.asString() }
      .toList()
    if (keys.isEmpty()) return emptyList()

    val packageName = keys.first().packageName.asString()
    // The generated interface is merged into the app-wide Metro graph alongside every other module's
    // generated provider. A shared method name would make the graph inherit conflicting same-signature
    // default methods (a diamond), so derive a per-package token to keep the interface and provide
    // method names unique across modules.
    val uniqueToken = packageName.toUniqueToken()

    val provideFunction = FunSpec.builder("provide${uniqueToken}NavKeySerializersModule")
      .addAnnotation(provides)
      .addAnnotation(intoSet)
      .returns(serializersModuleType)
      .addCode(
        CodeBlock.builder()
          .beginControlFlow("return %M", serializersModuleFun)
          .beginControlFlow("%M(%T::class)", polymorphic, hedvigNavKey)
          .apply {
            keys.forEach { key -> addStatement("%M(%T::class)", subclass, key.toClassName()) }
          }
          .endControlFlow()
          .endControlFlow()
          .build(),
      )
      .build()

    val providerInterface = TypeSpec.interfaceBuilder("${uniqueToken}GeneratedNavKeySerializersModuleProvider")
      .addAnnotation(
        AnnotationSpec.builder(contributesTo)
          .addMember("%T::class", appScope)
          .build(),
      )
      .addFunction(provideFunction)
      .build()

    FileSpec.builder(packageName, "${uniqueToken}GeneratedNavKeySerializersModuleProvider")
      .addType(providerInterface)
      .build()
      .writeTo(
        codeGenerator = environment.codeGenerator,
        aggregating = true,
        originatingKSFiles = keys.mapNotNull { it.containingFile },
      )

    return emptyList()
  }
}

private fun String.toUniqueToken(): String = split('.')
  .dropWhile { it == "com" || it == "hedvig" }
  .joinToString("") { segment -> segment.replaceFirstChar { it.uppercaseChar() } }

class NavKeySerializerProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    NavKeySerializerProcessor(environment)
}
