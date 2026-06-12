package com.hedvig.android.viewmodel.processor

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Visibility
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

private const val HEDVIG_VIEW_MODEL_FQ_NAME = "com.hedvig.android.core.common.di.HedvigViewModel"
private const val METRO_ASSISTED_FQ_NAME = "dev.zacsweers.metro.Assisted"
private const val SAVED_STATE_HANDLE_FQ_NAME = "androidx.lifecycle.SavedStateHandle"

private val defaultScope = ClassName("com.hedvig.android.core.common.di", "ActivityRetainedScope")
private val viewModel = ClassName("androidx.lifecycle", "ViewModel")
private val creationExtras = ClassName("androidx.lifecycle.viewmodel", "CreationExtras")
private val createSavedStateHandle = MemberName("androidx.lifecycle", "createSavedStateHandle")

private val provides = ClassName("dev.zacsweers.metro", "Provides")
private val intoMap = ClassName("dev.zacsweers.metro", "IntoMap")
private val contributesTo = ClassName("dev.zacsweers.metro", "ContributesTo")
private val contributesIntoMap = ClassName("dev.zacsweers.metro", "ContributesIntoMap")
private val assistedFactory = ClassName("dev.zacsweers.metro", "AssistedFactory")
private val assisted = ClassName("dev.zacsweers.metro", "Assisted")

private val viewModelKey = ClassName("dev.zacsweers.metrox.viewmodel", "ViewModelKey")
private val manualViewModelAssistedFactory =
  ClassName("dev.zacsweers.metrox.viewmodel", "ManualViewModelAssistedFactory")
private val manualViewModelAssistedFactoryKey =
  ClassName("dev.zacsweers.metrox.viewmodel", "ManualViewModelAssistedFactoryKey")
private val viewModelAssistedFactory =
  ClassName("dev.zacsweers.metrox.viewmodel", "ViewModelAssistedFactory")
private val viewModelAssistedFactoryKey =
  ClassName("dev.zacsweers.metrox.viewmodel", "ViewModelAssistedFactoryKey")

/**
 * Generates the Metro DI wiring for a ViewModel marked `@HedvigViewModel`, so no VM hand-writes
 * `@ViewModelKey` / `@ContributesIntoMap` / a nested assisted factory. One generated file per VM,
 * named by the VM's own simple name, so names are unique by construction.
 *
 * Branches, chosen by the primary constructor's `@Assisted` params:
 *  - none                       -> a `@ContributesTo` module with `@Provides @IntoMap @ViewModelKey(VM::class)`.
 *  - a single SavedStateHandle  -> a `<VM>Factory : ViewModelAssistedFactory` that pulls the handle out of
 *    CreationExtras, so the call site stays the zero-arg `assistedMetroViewModel<VM>()`.
 *  - anything else (nav args, optionally alongside a SavedStateHandle) -> a top-level
 *    `<VM>Factory : ManualViewModelAssistedFactory`. Nav args come from the call-site `create(...)`; a
 *    SavedStateHandle in the mix is just another `@Assisted` param, supplied at the call site via
 *    `extras.createSavedStateHandle()` (the manual-factory lambda receives the CreationExtras).
 */
class HedvigViewModelProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : SymbolProcessor {
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val (ready, deferred) = resolver
      .getSymbolsWithAnnotation(HEDVIG_VIEW_MODEL_FQ_NAME)
      .filterIsInstance<KSClassDeclaration>()
      .partition { it.validate() }
    ready.forEach { generate(it) }
    return deferred
  }

  private fun generate(vm: KSClassDeclaration) {
    val constructor = vm.primaryConstructor
    if (constructor == null) {
      logger.error("@HedvigViewModel requires a primary constructor", vm)
      return
    }
    val scope = vm.hedvigViewModelScope()
    val assistedParams = constructor.parameters.filter { it.hasAnnotation(METRO_ASSISTED_FQ_NAME) }
    val savedStateParams = assistedParams.filter { it.type.resolve().isSavedStateHandle() }

    when {
      assistedParams.isEmpty() -> generateNoArgModule(vm, scope)
      assistedParams.size == 1 && savedStateParams.size == 1 -> generateSavedStateFactory(vm, scope)
      else -> generateManualFactory(vm, scope, assistedParams)
    }
  }

  private fun generateNoArgModule(vm: KSClassDeclaration, scope: TypeName) {
    val vmClass = vm.toClassName()
    val moduleName = "${vmClass.simpleName}Module"
    val provide = FunSpec.builder("provide")
      .addAnnotation(provides)
      .addAnnotation(intoMap)
      .addAnnotation(AnnotationSpec.builder(viewModelKey).addMember("%T::class", vmClass).build())
      .addParameter("viewModel", vmClass)
      .returns(viewModel)
      .addStatement("return viewModel")
      .build()
    val module = TypeSpec.interfaceBuilder(moduleName)
      .addModifiers(vm.visibilityModifiers())
      .addAnnotation(contributesAnnotation(contributesTo, scope))
      .addFunction(provide)
      .build()
    write(vm, moduleName, module)
  }

  private fun generateManualFactory(
    vm: KSClassDeclaration,
    scope: TypeName,
    assistedParams: List<KSValueParameter>,
  ) {
    val vmClass = vm.toClassName()
    val factoryName = "${vmClass.simpleName}Factory"
    val create = FunSpec.builder("create")
      .addModifiers(KModifier.ABSTRACT)
      .returns(vmClass)
      .apply { assistedParams.forEach { addParameter(it.toAssistedParameter()) } }
      .build()
    val factory = TypeSpec.funInterfaceBuilder(factoryName)
      .addModifiers(vm.visibilityModifiers())
      .addSuperinterface(manualViewModelAssistedFactory)
      .addAnnotation(assistedFactory)
      .addAnnotation(manualViewModelAssistedFactoryKey)
      .addAnnotation(contributesAnnotation(contributesIntoMap, scope))
      .addFunction(create)
      .build()
    write(vm, factoryName, factory)
  }

  private fun generateSavedStateFactory(vm: KSClassDeclaration, scope: TypeName) {
    val vmClass = vm.toClassName()
    val factoryName = "${vmClass.simpleName}Factory"
    val fromExtras = FunSpec.builder("create")
      .addModifiers(KModifier.OVERRIDE)
      .addParameter("extras", creationExtras)
      .returns(vmClass)
      .addStatement("return create(extras.%M())", createSavedStateHandle)
      .build()
    val typed = FunSpec.builder("create")
      .addModifiers(KModifier.ABSTRACT)
      .addParameter(
        ParameterSpec.builder("savedStateHandle", ClassName("androidx.lifecycle", "SavedStateHandle"))
          .addAnnotation(assisted)
          .build(),
      )
      .returns(vmClass)
      .build()
    val factory = TypeSpec.funInterfaceBuilder(factoryName)
      .addModifiers(vm.visibilityModifiers())
      .addSuperinterface(viewModelAssistedFactory)
      .addAnnotation(assistedFactory)
      .addAnnotation(AnnotationSpec.builder(viewModelAssistedFactoryKey).addMember("%T::class", vmClass).build())
      .addAnnotation(contributesAnnotation(contributesIntoMap, scope))
      .addFunction(fromExtras)
      .addFunction(typed)
      .build()
    write(vm, factoryName, factory)
  }

  private fun write(vm: KSClassDeclaration, fileName: String, type: TypeSpec) {
    FileSpec.builder(vm.packageName.asString(), fileName)
      .addType(type)
      .build()
      .writeTo(
        codeGenerator = codeGenerator,
        aggregating = true,
        originatingKSFiles = listOfNotNull(vm.containingFile),
      )
  }
}

private fun contributesAnnotation(annotation: ClassName, scope: TypeName): AnnotationSpec =
  AnnotationSpec.builder(annotation).addMember("%T::class", scope).build()

private fun KSValueParameter.toAssistedParameter(): ParameterSpec {
  val builder = ParameterSpec.builder(name!!.asString(), type.toTypeName())
  val assistedAnnotation = annotations.firstOrNull {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == METRO_ASSISTED_FQ_NAME
  }
  val qualifier = assistedAnnotation
    ?.arguments
    ?.firstOrNull { it.name?.asString() == "value" }
    ?.value as? String
  builder.addAnnotation(
    if (qualifier.isNullOrEmpty()) {
      AnnotationSpec.builder(assisted).build()
    } else {
      AnnotationSpec.builder(assisted).addMember("%S", qualifier).build()
    },
  )
  return builder.build()
}

private fun KSClassDeclaration.hedvigViewModelScope(): TypeName {
  val annotation = annotations.firstOrNull {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == HEDVIG_VIEW_MODEL_FQ_NAME
  } ?: return defaultScope
  val scope = annotation.arguments.firstOrNull { it.name?.asString() == "scope" }?.value as? KSType
    ?: return defaultScope
  return scope.toClassName()
}

private fun KSClassDeclaration.visibilityModifiers(): List<KModifier> = when (getVisibility()) {
  Visibility.PUBLIC -> emptyList()
  Visibility.INTERNAL -> listOf(KModifier.INTERNAL)
  else -> listOf(KModifier.INTERNAL)
}

private fun KSValueParameter.hasAnnotation(fqName: String): Boolean = annotations.any {
  it.annotationType.resolve().declaration.qualifiedName?.asString() == fqName
}

private fun KSType.isSavedStateHandle(): Boolean =
  declaration.qualifiedName?.asString() == SAVED_STATE_HANDLE_FQ_NAME

class HedvigViewModelProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    HedvigViewModelProcessor(environment.codeGenerator, environment.logger)
}
