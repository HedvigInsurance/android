package com.hedvig.android.app

import android.app.Activity
import android.content.Intent
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.merge
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * The Intent-based handoff that carries a backstack across an Activity relaunch.
 *
 * When Up is pressed from a deep link hosted inside the launching app's task, [escapeToOwnTask]
 * re-roots us in our own task (NEW_TASK|CLEAR_TASK + finish) and serializes the target ancestry into
 * the launch intent. The fresh instance reads it back with [readFrom] to seed its initial backstack.
 * Both sides share the same extra key and codec here so the write/read contract can't drift.
 */
internal object RestoredBackstackTransfer {
  private const val EXTRA_RESTORE_STACK = "com.hedvig.android.app.RESTORE_STACK"

  private val serializer = ListSerializer(PolymorphicSerializer(HedvigNavKey::class))

  /** Finishes this foreign-hosted instance and relaunches MainActivity in its own task, seeded with [parentStack]. */
  fun escapeToOwnTask(
    activity: Activity,
    parentStack: List<HedvigNavKey>,
    serializersModules: Set<SerializersModule>,
  ) {
    val relaunch = Intent(activity, MainActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
      putExtra(EXTRA_RESTORE_STACK, json(serializersModules).encodeToString(serializer, parentStack))
    }
    activity.finish()
    activity.startActivity(relaunch)
  }

  /** The ancestry seeded by a prior [escapeToOwnTask], or null when [intent] carries no handoff. */
  fun readFrom(intent: Intent, serializersModules: Set<SerializersModule>): List<HedvigNavKey>? {
    val encoded = intent.getStringExtra(EXTRA_RESTORE_STACK) ?: return null
    return json(serializersModules).decodeFromString(serializer, encoded)
  }

  private fun json(serializersModules: Set<SerializersModule>): Json = Json {
    serializersModule = serializersModules.merge()
  }
}
