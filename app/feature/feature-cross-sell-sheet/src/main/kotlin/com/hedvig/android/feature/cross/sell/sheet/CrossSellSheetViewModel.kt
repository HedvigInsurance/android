package com.hedvig.android.feature.cross.sell.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.crosssells.BundleProgress
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedAddon
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest
import octopus.BottomSheetCrossSellsQuery
import octopus.fragment.CrossSellFragment
import octopus.type.CrossSellInput
import octopus.type.CrossSellSource
import octopus.type.FlowSource
import octopus.type.UserFlow

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class CrossSellSheetViewModel(
  getCrossSellSheetDataUseCase: GetCrossSellSheetDataUseCase,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) : MoleculeViewModel<CrossSellSheetEvent, CrossSellSheetState>(
    CrossSellSheetState.Loading,
    CrossSellSheetPresenter(getCrossSellSheetDataUseCase, crossSellAfterFlowRepository),
  )

internal sealed interface CrossSellSheetEvent {
  data object CrossSellSheetShown : CrossSellSheetEvent
}

internal sealed interface CrossSellSheetState {
  data object Loading : CrossSellSheetState

  data object DontShow : CrossSellSheetState

  data class Error(val errorMessage: ErrorMessage) : CrossSellSheetState

  data class Content(val crossSellSheetData: CrossSellSheetData, val infoType: CrossSellInfoType) : CrossSellSheetState
}

internal class CrossSellSheetPresenter(
  private val getCrossSellSheetDataUseCase: GetCrossSellSheetDataUseCase,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) : MoleculePresenter<CrossSellSheetEvent, CrossSellSheetState> {
  @Composable
  override fun MoleculePresenterScope<CrossSellSheetEvent>.present(
    lastState: CrossSellSheetState,
  ): CrossSellSheetState {
    var state by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        is CrossSellSheetEvent.CrossSellSheetShown -> {
          crossSellAfterFlowRepository.showedCrossSellSheet((state as? CrossSellSheetState.Content)?.infoType)
        }
      }
    }

    LaunchedEffect(Unit) {
      crossSellAfterFlowRepository.shouldShowCrossSellSheetWithInfo().transformLatest { infoType ->
        if (infoType == null) {
          emit(CrossSellSheetState.DontShow)
          return@transformLatest
        }
        emitAll(
          getCrossSellSheetDataUseCase.invoke(infoType.toCrossSellSource())
            .mapLatest { result ->
              result.fold(
                ifLeft = { error -> CrossSellSheetState.Error(error) },
                ifRight = { data ->
                  if (data.isEmpty) {
                    CrossSellSheetState.DontShow
                  } else {
                    CrossSellSheetState.Content(data, infoType)
                  }
                },
              )
            },
        )
      }.collectLatest {
        state = it
      }
    }
    return state
  }
}

internal fun CrossSellInfoType.toCrossSellSource(): CrossSellInput {
  val smartCrossSellInput: (FlowSource) -> CrossSellInput = { flowSource ->
    CrossSellInput(
      userFlow = UserFlow.SMART_X_SELL,
      flowSource = Optional.present(flowSource),
      experiments = emptyList(),
      contractId = Optional.presentIfNotNull(this.contractId),
    )
  }
  return when (this) {
    CrossSellInfoType.Addon -> smartCrossSellInput(FlowSource.ADDON)
    is CrossSellInfoType.ChangeTier -> smartCrossSellInput(FlowSource.CHANGE_TIER)
    is CrossSellInfoType.ClosedClaim -> smartCrossSellInput(FlowSource.CLOSED_CLAIM)
    CrossSellInfoType.EditCoInsured -> smartCrossSellInput(FlowSource.EDIT_COINSURED)
    is CrossSellInfoType.MovingFlow -> smartCrossSellInput(FlowSource.MOVING)
  }
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetCrossSellSheetDataUseCase>())
internal class SwitchingGetCrossSellSheetDataUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetCrossSellSheetDataUseCaseImpl,
  override val demoImpl: DemoGetCrossSellSheetDataUseCase,
) : GetCrossSellSheetDataUseCase, DemoSwitcher<GetCrossSellSheetDataUseCase>() {
  override suspend fun invoke(source: CrossSellInput) = pickFlow { it.invoke(source) }
}

internal interface GetCrossSellSheetDataUseCase {
  suspend fun invoke(source: CrossSellInput): Flow<Either<ErrorMessage, CrossSellSheetData>>
}

@Inject
internal class GetCrossSellSheetDataUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellSheetDataUseCase {
  override suspend fun invoke(source: CrossSellInput): Flow<Either<ErrorMessage, CrossSellSheetData>> {
    return apolloClient
      .query(BottomSheetCrossSellsQuery(source))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeFlow(::ErrorMessage)
      .map { response ->
        either {
          val allData = response
            .bind()
            .currentMember.crossSellV2
          val recommendedData = allData.recommendedCrossSell?.let {
            val bundleProgress = if (it.numberOfEligibleContracts > 0 && it.discountPercent != null) {
              BundleProgress(it.numberOfEligibleContracts, it.discountPercent)
            } else {
              null
            }
            RecommendedCrossSell(
              crossSell = it.crossSell.toCrossSell(),
              bannerText = it.bannerText,
              buttonText = it.buttonText,
              discountText = it.discountText,
              buttonDescription = it.buttonDescription,
              backgroundPillowImages = it.backgroundPillowImages?.let { images ->
                images.leftImage.src to images.rightImage.src
              },
              bundleProgress = bundleProgress,
            )
          }
          val otherCrossSellsData = allData.otherCrossSells.map {
            it.toCrossSell()
          }
          val recommendedAddon = allData.recommendedAddon?.let {
            RecommendedAddon(
              id = it.id,
              title = it.title,
              buttonTitle = it.buttonTitle,
              description = it.description,
              deepLink = it.deepLink,
              banner = it.banner,
              benefits = it.benefits,
              pillowImageSmall = it.pillowImageSmall.src,
              pillowImageLarge = it.pillowImageLarge.src,
            )
          }
          CrossSellSheetData(
            recommendedCrossSell = recommendedData,
            otherCrossSells = otherCrossSellsData,
            recommendedAddon = recommendedAddon,
          )
        }
      }
  }
}

internal fun CrossSellFragment.toCrossSell(): CrossSell {
  return with(this) {
    CrossSell(
      id = id,
      title = title,
      subtitle = description,
      storeUrl = storeUrl,
      pillowImage = ImageAsset(
        id = pillowImageLarge.id,
        src = pillowImageLarge.src,
        description = pillowImageLarge.alt,
      ),
    )
  }
}

@Inject
internal class DemoGetCrossSellSheetDataUseCase : GetCrossSellSheetDataUseCase {
  override suspend fun invoke(source: CrossSellInput): Flow<Either<ErrorMessage, CrossSellSheetData>> {
    return flowOf(ErrorMessage("Ineligible for demo mode").left())
  }
}
