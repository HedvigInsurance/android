package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import com.hedvig.android.navigation.common.HedvigNavKey
import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass
import org.junit.Test

/**
 * Guards the analytics screen name of every nav key.
 *
 * A `screen_view` is keyed by its name, so two keys resolving to the same name are reported as a single screen and
 * their metrics silently merge. Nothing else in the build catches that: the keys live in different feature modules
 * and never see each other. This test scans the classpath instead of a hand-maintained list, so a newly added key
 * that collides fails here rather than in a dashboard months later.
 */
internal class ScreenNameTest {
  @Test
  fun `every nav key on the classpath maps to a distinct screen name`() {
    val keys = concreteNavKeysOnClasspath()
    assertThat(keys.size).isGreaterThan(50)

    val collisions = keys
      .groupBy { screenNameFor(it) }
      .filterValues { it.size > 1 }
      .mapValues { (_, colliding) -> colliding.mapNotNull { it.qualifiedName }.sorted() }

    assertThat(collisions).isEmpty()
  }

  @Test
  fun `keys sharing a simple name across features are qualified by their feature`() {
    assertThat(screenNameFor(keyClass("com.hedvig.android.feature.change.tier.navigation.SubmitSuccessKey")))
      .isEqualTo("ChangeTier.SubmitSuccess")
    assertThat(screenNameFor(keyClass("com.hedvig.android.feature.addon.purchase.navigation.SubmitSuccessKey")))
      .isEqualTo("AddonPurchase.SubmitSuccess")
  }

  @Test
  fun `a feature qualifier that only repeats the key name is dropped`() {
    assertThat(screenNameFor(keyClass("com.hedvig.android.feature.forever.navigation.ForeverKey")))
      .isEqualTo("Forever")
    assertThat(screenNameFor(keyClass("com.hedvig.android.feature.payments.navigation.ForeverKey")))
      .isEqualTo("Payments.Forever")
  }

  @Test
  fun `repeated package segments collapse`() {
    assertThat(screenNameFor(keyClass("com.hedvig.android.feature.home.home.navigation.HomeKey")))
      .isEqualTo("Home")
  }

  private fun keyClass(qualifiedName: String): KClass<*> = Class.forName(qualifiedName).kotlin

  private fun concreteNavKeysOnClasspath(): Set<KClass<*>> = ClassGraph()
    .enableClassInfo()
    .enableAnnotationInfo()
    .acceptPackages("com.hedvig")
    .scan()
    .use { scan ->
      scan.getClassesImplementing(HedvigNavKey::class.java.name)
        .filter { !it.isInterface && !it.isAbstract }
        .filter { it.hasAnnotation("kotlinx.serialization.Serializable") }
        .map { it.loadClass().kotlin }
        .toSet()
    }
}
