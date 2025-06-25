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
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest
import octopus.BottomSheetCrossSellsQuery
import octopus.fragment.CrossSellFragment
import octopus.type.CrossSellSource

internal class CrossSellSheetViewModel(
  getCrossSellSheetDataUseCaseProvider: Provider<GetCrossSellSheetDataUseCase>,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) : MoleculeViewModel<CrossSellSheetEvent, CrossSellSheetState>(
    CrossSellSheetState.Loading,
    CrossSellSheetPresenter(getCrossSellSheetDataUseCaseProvider, crossSellAfterFlowRepository),
  )

sealed interface CrossSellSheetEvent {
  data object CrossSellSheetShown : CrossSellSheetEvent
}

sealed interface CrossSellSheetState {
  data object Loading : CrossSellSheetState

  data object DontShow : CrossSellSheetState

  data class Error(val errorMessage: ErrorMessage) : CrossSellSheetState

  data class Content(val crossSellSheetData: CrossSellSheetData, val infoType: CrossSellInfoType) : CrossSellSheetState
}

private class CrossSellSheetPresenter(
  private val getCrossSellSheetDataUseCaseProvider: Provider<GetCrossSellSheetDataUseCase>,
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
          getCrossSellSheetDataUseCaseProvider.provide().invoke(infoType.toCrossSellSource())
            .mapLatest { result ->
              result.fold(
                ifLeft = { error -> CrossSellSheetState.Error(error) },
                ifRight = { data -> CrossSellSheetState.Content(data, infoType) },
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

internal fun CrossSellInfoType.toCrossSellSource(): CrossSellSource {
  return when (this) {
    CrossSellInfoType.Addon -> CrossSellSource.ADDON
    CrossSellInfoType.ChangeTier -> CrossSellSource.CHANGE_TIER
    is CrossSellInfoType.ClosedClaim -> CrossSellSource.CLOSED_CLAIM
    CrossSellInfoType.EditCoInsured -> CrossSellSource.EDIT_COINSURED
    CrossSellInfoType.MovingFlow -> CrossSellSource.MOVING_FLOW
  }
}

internal class GetCrossSellSheetDataUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetCrossSellSheetDataUseCase,
  override val prodImpl: GetCrossSellSheetDataUseCase,
) : ProdOrDemoProvider<GetCrossSellSheetDataUseCase>

internal interface GetCrossSellSheetDataUseCase {
  suspend fun invoke(source: CrossSellSource): Flow<Either<ErrorMessage, CrossSellSheetData>>
}

internal class GetCrossSellSheetDataUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellSheetDataUseCase {
  override suspend fun invoke(source: CrossSellSource): Flow<Either<ErrorMessage, CrossSellSheetData>> {
    return apolloClient
      .query(BottomSheetCrossSellsQuery(source))
      .safeFlow(::ErrorMessage)
      .map { response ->
        either {
          val allData = response
            .bind()
            .currentMember.crossSell
          val recommendedData = allData.recommendedCrossSell?.let {
            RecommendedCrossSell(
              crossSell = it.crossSell.toCrossSell(),
              bannerText = it.bannerText,
              buttonText = it.buttonText,
              discountText = it.discountText,
              buttonDescription = it.buttonDescription,
            )
          }
          val otherCrossSellsData = allData.otherCrossSells.map {
            it.toCrossSell()
          }
          CrossSellSheetData(
            recommendedCrossSell = recommendedData,
            otherCrossSells = otherCrossSellsData,
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

internal class DemoGetCrossSellSheetDataUseCase() : GetCrossSellSheetDataUseCase {
  override suspend fun invoke(source: CrossSellSource): Flow<Either<ErrorMessage, CrossSellSheetData>> {
    return flowOf(ErrorMessage("Ineligible for demo mode").left())
  }
}
