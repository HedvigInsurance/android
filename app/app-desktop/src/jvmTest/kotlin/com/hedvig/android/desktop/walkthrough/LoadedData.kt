package com.hedvig.android.desktop.walkthrough

import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.apolloStore
import kotlinx.coroutines.runBlocking

/** What the app has already loaded into its Apollo cache, read without a network call. */
internal class LoadedData(private val records: List<Map<String, Any?>>) {
  /** The `id` of every cached object whose `__typename` is [typename], in cache order. */
  fun idsOf(typename: String): List<String> = records
    .filter { it["__typename"] == typename }
    .mapNotNull { it["id"] as? String }
    .distinct()

  /** The `id` of the first cached object with a string field [field] that contains [needle]. */
  fun idWhere(field: String, needle: String): String? = records
    .firstOrNull { (it[field] as? String)?.contains(needle) == true && it["id"] is String }
    ?.get("id") as? String

  /** The `id` and [field] of every cached object whose string [field] contains [needle]. */
  fun idsAndFieldWhere(field: String, needle: String): List<Pair<String, String>> = records
    .mapNotNull { record ->
      val id = record["id"] as? String ?: return@mapNotNull null
      val value = record[field] as? String ?: return@mapNotNull null
      (id to value).takeIf { value.contains(needle) }
    }
    .distinct()

  /** [keyField] and [field] of every cached object whose string [field] contains [needle]. */
  fun pairsWhere(keyField: String, field: String, needle: String): List<Pair<String, String>> = records
    .mapNotNull { record ->
      val key = record[keyField] as? String ?: return@mapNotNull null
      val value = record[field] as? String ?: return@mapNotNull null
      (key to value).takeIf { value.contains(needle) }
    }
    .distinct()

  /** The string [field] of the first cached object that also has a [requiredField]. */
  fun firstStringWhereAlso(field: String, requiredField: String): String? = records
    .firstOrNull { it[field] is String && it.containsKey(requiredField) }
    ?.get(field) as? String

  /** Every markdown link (`[text](url)`) in a cached string, with the cached field name it was found in. */
  fun markdownLinks(): List<Triple<String, String, String>> = records.flatMap { record ->
    record.flatMap { (field, value) ->
      val text = value as? String ?: return@flatMap emptyList()
      markdownLink.findAll(text).map { Triple(field, it.groupValues[1], it.groupValues[2]) }.toList()
    }
  }.distinct()

  companion object {
    private val markdownLink = Regex("""\[([^\]]+)]\(([^)\s]+)\)""")

    fun read(apolloClient: ApolloClient): LoadedData = runBlocking {
      val dump = apolloClient.apolloStore.dump()
      LoadedData(dump.values.flatMap { byKey -> byKey.values.map { record -> record.fields } })
    }
  }
}
