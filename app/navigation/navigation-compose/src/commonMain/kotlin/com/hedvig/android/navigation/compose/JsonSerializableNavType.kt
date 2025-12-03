@file:Suppress("NOTHING_TO_INLINE")

package com.hedvig.android.navigation.compose

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@PublishedApi
internal fun typeMapOf(ktypes: List<KType>): Map<KType, @JvmSuppressWildcards NavType<*>> {
  return ktypes.associate {
    if (it.isMarkedNullable) {
      it to JsonSerializableNullableNavType(serializer(it))
    } else {
      it to JsonSerializableNavType(serializer(it).cast())
    }
  }
}

@Suppress("UNCHECKED_CAST")
private inline fun <T> KSerializer<*>.cast(): KSerializer<T> = this as KSerializer<T>

/**
 * Expect/actual for URL encoding since it's platform-specific
 */
internal expect fun urlEncode(value: String): String

/**
 * Expect/actual for URL decoding since it's platform-specific
 */
internal expect fun urlDecode(value: String): String

@Suppress("NOTHING_TO_INLINE")
internal data class JsonSerializableNavType<T : Any>(
  private val serializer: KSerializer<T>,
) : NavType<T>(isNullableAllowed = false) {
  override fun put(bundle: SavedState, key: String, value: T) {
    bundle.write {
      this.putString(key, value.encodedAsString())
    }
  }

  override fun get(bundle: SavedState, key: String): T {
    return bundle.read {
      parseValue(this.getString(key))
    }
  }

  override fun serializeAsValue(value: T): String {
    return urlEncode(value.encodedAsString())
  }

  override fun parseValue(value: String): T {
    return urlDecode(value).decodedFromString()
  }

  private inline fun T.encodedAsString(): String = Json.encodeToString(serializer, this)

  private inline fun String.decodedFromString(): T = Json.decodeFromString(serializer, this)
}

@Suppress("NOTHING_TO_INLINE")
internal data class JsonSerializableNullableNavType<T : Any?>(
  private val serializer: KSerializer<T?>,
) : NavType<T?>(isNullableAllowed = true) {
  override fun put(bundle: SavedState, key: String, value: T?) {
    bundle.write {
      if (value == null) {
        this.putNull(key)
      } else {
        this.putString(key, value.encodedAsString())
      }
    }
  }

  override fun get(bundle: SavedState, key: String): T? {
    return bundle.read {
      val value = this.getStringOrNull(key) ?: return@read null
      parseValue(value)
    }
  }

  override fun serializeAsValue(value: T?): String {
    if (value == null) return "null"
    return urlEncode(value.encodedAsString())
  }

  override fun parseValue(value: String): T? {
    if (value == "null") return null
    return urlDecode(value).decodedFromString()
  }

  private inline fun T.encodedAsString(): String = Json.encodeToString(serializer, this)

  private inline fun String.decodedFromString(): T? = Json.decodeFromString(serializer, this)
}
