package com.hedvig.android.desktop.walkthrough

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure
import kotlinx.datetime.LocalDate

/** A key the walkthrough can push, or why it cannot build one. */
internal sealed interface KeyInstance {
  val keyClass: KClass<*>

  /** [fakedArguments] lists each argument that was not left to its default, as `name=value`. */
  data class Built(
    override val keyClass: KClass<*>,
    val key: HedvigNavKey,
    val fakedArguments: List<String>,
  ) : KeyInstance

  data class NeedsData(override val keyClass: KClass<*>, val reason: String) : KeyInstance
}

/**
 * Ids that demo data answers to. Demo insurances hold one contract with id "1"; the demo chat repository accepts any
 * conversation id.
 */
internal val demoIds: Map<String, String> = mapOf(
  "contractId" to "1",
  "conversationId" to "00000000-0000-4000-8000-000000000001",
)

/** A fixed date for date arguments, so two runs push the same keys. */
private val fixedDate = LocalDate(2026, 11, 12)

/**
 * Builds an instance of [keyClass]: the object itself, or the primary constructor called with its defaults, null for
 * nullable arguments, and trivial values (false, 0, an enum's first entry, an empty list, a fixed date) for the rest.
 * A string argument named like an id is filled from [ids] or makes the key [KeyInstance.NeedsData]; any other string
 * becomes "walkthrough". An argument of any other type makes the key [KeyInstance.NeedsData].
 */
internal fun buildKey(keyClass: KClass<*>, ids: Map<String, String>): KeyInstance {
  keyClass.objectInstance?.let { return KeyInstance.Built(keyClass, it as HedvigNavKey, emptyList()) }
  val constructor = keyClass.primaryConstructor
    ?: return KeyInstance.NeedsData(keyClass, "no primary constructor")
  constructor.isAccessible = true
  val arguments = mutableMapOf<KParameter, Any?>()
  val faked = mutableListOf<String>()
  for (parameter in constructor.parameters) {
    if (parameter.isOptional) continue
    val value = when (val fake = fakeArgument(parameter, ids)) {
      is FakeArgument.Value -> fake.value
      is FakeArgument.Missing -> return KeyInstance.NeedsData(keyClass, fake.reason)
    }
    arguments[parameter] = value
    faked += "${parameter.name}=$value"
  }
  val key = try {
    constructor.callBy(arguments) as HedvigNavKey
  } catch (e: Exception) {
    return KeyInstance.NeedsData(keyClass, "constructor threw ${(e.cause ?: e)::class.simpleName}")
  }
  return KeyInstance.Built(keyClass, key, faked)
}

private sealed interface FakeArgument {
  data class Value(val value: Any?) : FakeArgument

  data class Missing(val reason: String) : FakeArgument
}

/** A class that only wraps one id string, such as a contract id, built from [ids] by the parameter's [name]. */
private fun idWrapper(type: KClass<*>, name: String, ids: Map<String, String>): FakeArgument? {
  val id = ids[name] ?: return null
  val constructor = type.primaryConstructor ?: return null
  val parameter = constructor.parameters.singleOrNull() ?: return null
  if (parameter.type.jvmErasure != String::class) return null
  constructor.isAccessible = true
  return FakeArgument.Value(constructor.call(id))
}

private fun fakeArgument(parameter: KParameter, ids: Map<String, String>): FakeArgument {
  val name = parameter.name.orEmpty()
  if (parameter.type.isMarkedNullable) return FakeArgument.Value(null)
  val type = parameter.type.jvmErasure
  return when {
    type == String::class -> if (name == "id" || name.endsWith("Id") || name.endsWith("Ids")) {
      ids[name]?.let { FakeArgument.Value(it) } ?: FakeArgument.Missing("needs a real $name")
    } else {
      FakeArgument.Value("walkthrough")
    }

    type == Boolean::class -> FakeArgument.Value(false)

    type == Int::class -> FakeArgument.Value(0)

    type == Long::class -> FakeArgument.Value(0L)

    type == LocalDate::class -> FakeArgument.Value(fixedDate)

    type == List::class -> FakeArgument.Value(emptyList<Any>())

    type == Set::class -> FakeArgument.Value(emptySet<Any>())

    type.java.isEnum -> FakeArgument.Value(type.java.enumConstants.first())

    else -> FakeArgument.Missing("needs a ${type.simpleName} for $name")
  }
}
