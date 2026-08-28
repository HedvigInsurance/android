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
 * Two properties have to hold, and neither is checked anywhere else in the build. Names must be unique,
 * because a `screen_view` is keyed by its name and duplicates report two screens as one. And they must fit
 * the 100 character analytics parameter limit, because the longest key name is already 91 characters before
 * the shared package prefix is stripped.
 *
 * The keys live in different feature modules and never see each other, so a collision or an over-long name
 * is invisible at compile time. This scans the classpath rather than a hand-maintained list, so a newly
 * added key is covered automatically.
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
  fun `a screen name is the class name with only the shared feature prefix removed`() {
    assertThat(screenNameFor(keyClass("com.hedvig.android.feature.change.tier.navigation.SubmitSuccessKey")))
      .isEqualTo("change.tier.navigation.SubmitSuccessKey")
    assertThat(screenNameFor(keyClass("com.hedvig.android.feature.claim.chat.navigation.ClaimOutcomeNewClaimKey")))
      .isEqualTo("claim.chat.navigation.ClaimOutcomeNewClaimKey")
  }

  @Test
  fun `prepending the feature prefix recovers the declaring class`() {
    val keys = concreteNavKeysOnClasspath()
    assertThat(keys.size).isGreaterThan(50)

    val unrecoverable = keys.filter { keyClass ->
      val name = screenNameFor(keyClass)
      val candidates = listOf("com.hedvig.android.feature.$name", name)
      keyClass.qualifiedName !in candidates
    }.mapNotNull { it.qualifiedName }

    assertThat(unrecoverable).isEmpty()
  }

  @Test
  fun `every screen name fits the analytics parameter limit`() {
    val tooLong = concreteNavKeysOnClasspath()
      .map { screenNameFor(it) }
      .filter { it.length > ANALYTICS_PARAMETER_LIMIT }

    assertThat(tooLong).isEmpty()
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

  private companion object {
    /** GA4 truncates event parameter values, and `screen_name` is one. */
    const val ANALYTICS_PARAMETER_LIMIT = 100
  }
}
