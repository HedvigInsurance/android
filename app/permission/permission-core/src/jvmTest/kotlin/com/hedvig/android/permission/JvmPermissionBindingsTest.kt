package com.hedvig.android.permission

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame

@DependencyGraph(AppScope::class)
internal interface PermissionTestGraph {
  val permissionManager: PermissionManager
  val otherPermissionManager: PermissionManager
}

class JvmPermissionBindingsTest {
  @Test
  fun `the permission manager resolves to one JVM instance that grants nothing`() {
    val graph = createGraph<PermissionTestGraph>()

    assertIs<JvmPermissionManager>(graph.permissionManager)
    assertSame(graph.permissionManager, graph.otherPermissionManager)
    assertFalse(graph.permissionManager.isPermissionGranted(Permission.PostNotifications))
  }
}
