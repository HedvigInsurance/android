package com.hedvig.android.app.navigation

import com.hedvig.android.navigation.common.AnalyticsNamed
import io.github.classgraph.ClassGraph

/**
 * The analytics names pinned by nav keys on the classpath, read from each key's `ANALYTICS_NAME` constant
 * without constructing the key.
 *
 * A classpath scan is expensive, so it runs once and every test reads the same map. It is scoped to the
 * feature packages so the fake keys in this package are not picked up.
 */
internal object PinnedAnalyticsNames {
  private const val CONSTANT_NAME = "ANALYTICS_NAME"
  private const val FEATURE_PACKAGE = "com.hedvig.android.feature"

  /** Key class name to its pinned name. A key that implements [AnalyticsNamed] without the constant maps to "". */
  val byClassName: Map<String, String> by lazy {
    ClassGraph()
      .enableClassInfo()
      .enableFieldInfo()
      .enableStaticFinalFieldConstantInitializerValues()
      .acceptPackages(FEATURE_PACKAGE)
      .scan()
      .use { scan ->
        scan.getClassesImplementing(AnalyticsNamed::class.java.name)
          .filter { !it.isInterface && !it.isAbstract }
          .associate { classInfo ->
            classInfo.name to (classInfo.getFieldInfo(CONSTANT_NAME)?.constantInitializerValue as? String).orEmpty()
          }
      }
  }
}
