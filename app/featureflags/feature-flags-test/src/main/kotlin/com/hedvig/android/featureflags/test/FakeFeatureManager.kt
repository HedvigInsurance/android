package com.hedvig.android.featureflags.test

import app.cash.turbine.Turbine
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeFeatureManager(
  private val featureMap: (() -> Map<Feature, Boolean>)? = null,
) : FeatureManager {
  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> {
    val featureMap = featureMap?.invoke() ?: error("Set the featureMap returned from FakeFeatureManager")
    return flowOf(featureMap[feature] ?: error("Set a return value for feature:$feature on FakeFeatureManager"))
  }

  companion object {
    /**
     * A FeatureManager which just returns some value for each feature, should be used when the result doesn't matter
     * but the class being tested does need a FeatureManager.
     */
    operator fun invoke(noopFeatureManager: Boolean): FakeFeatureManager {
      return if (noopFeatureManager) {
        FakeFeatureManager { Feature.entries.associateWith { false } }
      } else {
        error("Use the normal constructor instead")
      }
    }
  }
}

/**
 * Uses a Turbine for the feature, to have more control over what and when it returns a value during a test.
 * Should probably delete `FakeFeatureManager` asap in favor of this, while keeping same API maybe.
 */
class FakeFeatureManager2(
  private val fixedMap: Map<Feature, Boolean> = emptyMap(),
) : FeatureManager {
  /**
   * Allow the feature manager to return [fixedReturnForAll] for all features
   */
  constructor(fixedReturnForAll: Boolean) : this(Feature.entries.associateWith { fixedReturnForAll })

  val featureTurbine = Turbine<Pair<Feature, Boolean>>(name = "FeatureManagerTurbine")

  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> {
    return flow {
      if (fixedMap.containsKey(feature)) {
        emit(fixedMap[feature]!!)
        return@flow
      }
      val pair = featureTurbine.awaitItem()
      require(feature == pair.first)
      emit(pair.second)
    }
  }
}
