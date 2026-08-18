package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TrackedScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test

internal class ScreenParameterExtractorTest {
  private val extractor = ScreenParameterExtractor(
    setOf(
      SerializersModule {
        polymorphic(HedvigNavKey::class) {
          subclass(SimpleKey::class)
          subclass(EmptyKey::class)
          subclass(NullableKey::class)
          subclass(NestedKey::class)
          subclass(OverridingKey::class)
        }
      },
    ),
  )

  @Test
  fun `primitive properties are flattened and coerced to Firebase-compatible types`() {
    val params = extractor.parametersFor(SimpleKey(id = "abc", count = 42, enabled = true))

    assertThat(params).isEqualTo(
      mapOf<String, Any?>(
        "id" to "abc",
        "count" to 42L, // Int coerces to Long
        "enabled" to true,
      ),
    )
  }

  @Test
  fun `the polymorphic type discriminator is dropped`() {
    val params = extractor.parametersFor(EmptyKey)

    assertThat(params).isEmpty()
  }

  @Test
  fun `null-valued properties are preserved as null`() {
    val params = extractor.parametersFor(NullableKey(maybe = null))

    assertThat(params).isEqualTo(mapOf<String, Any?>("maybe" to null))
  }

  @Test
  fun `nested objects fall back to their JSON string`() {
    val params = extractor.parametersFor(NestedKey(inner = Inner(a = "x")))

    assertThat(params).isEqualTo(mapOf<String, Any?>("inner" to """{"a":"x"}"""))
  }

  @Test
  fun `a TrackedScreen takes over with its own parameters, ignoring serialized shape`() {
    val params = extractor.parametersFor(OverridingKey(secret = "should-not-leak"))

    assertThat(params).isEqualTo(mapOf<String, Any?>("custom" to "value"))
  }
}

@Serializable
private data class SimpleKey(val id: String, val count: Int, val enabled: Boolean) : HedvigNavKey

@Serializable
private data object EmptyKey : HedvigNavKey

@Serializable
private data class NullableKey(val maybe: String?) : HedvigNavKey

@Serializable
private data class NestedKey(val inner: Inner) : HedvigNavKey

@Serializable
private data class Inner(val a: String)

@Serializable
private data class OverridingKey(val secret: String) : HedvigNavKey, TrackedScreen {
  override val screenParameters: Map<String, Any?>
    get() = mapOf("custom" to "value")
}
