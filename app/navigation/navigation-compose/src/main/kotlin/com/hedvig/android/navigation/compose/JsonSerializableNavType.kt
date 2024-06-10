package com.hedvig.android.navigation.compose

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Use like:
 * ```
 * private val typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = mapOf(
 * 	typeOf<InsuranceData>() to JsonSerializableNavType<InsuranceData>(),
 * )
 * ```
 */
@Suppress("FunctionName")
inline fun <reified T : Any> JsonSerializableNavType(): NavType<T> = JsonSerializableNavType(serializer())

@Suppress("NOTHING_TO_INLINE")
class JsonSerializableNavType<T : Any>(
  private val serializer: KSerializer<T>,
) : NavType<T>(isNullableAllowed = false) {
  override fun put(bundle: Bundle, key: String, value: T) {
    bundle.putString(key, value.encodedAsString())
  }

  override fun get(bundle: Bundle, key: String): T {
    return parseValue(bundle.getString(key)!!)
  }

  override fun serializeAsValue(value: T): String {
    return Uri.encode(value.encodedAsString())
  }

  override fun parseValue(value: String): T {
    return value.decodedFromString()
  }

  private inline fun T.encodedAsString(): String = Json.encodeToString(serializer, this)

  private inline fun String.decodedFromString(): T = Json.decodeFromString(serializer, this)
}

/**
 * Use like:
 * ```
 * private val typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = mapOf(
 * 	typeOf<InsuranceData?>() to JsonSerializableNullableNavType<InsuranceData?>(),
 * )
 * ```
 */
@Suppress("FunctionName")
inline fun <reified T : Any?> JsonSerializableNullableNavType(): NavType<T?> =
  JsonSerializableNullableNavType(serializer())

@Suppress("NOTHING_TO_INLINE")
class JsonSerializableNullableNavType<T : Any?>(
  private val serializer: KSerializer<T?>,
) : NavType<T?>(isNullableAllowed = true) {
  override fun put(bundle: Bundle, key: String, value: T?) {
    bundle.putString(key, value?.encodedAsString())
  }

  override fun get(bundle: Bundle, key: String): T? {
    val data = bundle.getString(key) ?: return null
    return parseValue(data)
  }

  override fun serializeAsValue(value: T?): String {
    if (value == null) return "null"
    return Uri.encode(value.encodedAsString())
  }

  override fun parseValue(value: String): T? {
    if (value == "null") return null
    return value.decodedFromString()
  }

  private inline fun T.encodedAsString(): String = Json.encodeToString(serializer, this)

  private inline fun String.decodedFromString(): T? = Json.decodeFromString(serializer, this)
}
