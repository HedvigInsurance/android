package com.hedvig.android.feature.inapppurchase.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.inapppurchase.data.GetInAppPurchaseFormUseCase
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseEligibility
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFieldInput
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseForm
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseIneligibilityReason
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseProduct
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseQuote
import com.hedvig.android.feature.inapppurchase.data.QuoteInAppPurchaseUseCase
import com.hedvig.android.feature.inapppurchase.navigation.InAppProduct
import com.hedvig.android.feature.inapppurchase.navigation.navigateToOffers
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class InAppPurchaseFormViewModel(
  @Assisted product: InAppProduct,
  getInAppPurchaseFormUseCase: GetInAppPurchaseFormUseCase,
  quoteInAppPurchaseUseCase: QuoteInAppPurchaseUseCase,
  backstack: Backstack,
) : MoleculeViewModel<InAppPurchaseFormEvent, InAppPurchaseFormUiState>(
    initialState = InAppPurchaseFormUiState.Loading,
    presenter = InAppPurchaseFormPresenter(product, getInAppPurchaseFormUseCase, quoteInAppPurchaseUseCase, backstack),
  )

internal class InAppPurchaseFormPresenter(
  private val product: InAppProduct,
  private val getInAppPurchaseFormUseCase: GetInAppPurchaseFormUseCase,
  private val quoteInAppPurchaseUseCase: QuoteInAppPurchaseUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<InAppPurchaseFormEvent, InAppPurchaseFormUiState> {
  @Composable
  override fun MoleculePresenterScope<InAppPurchaseFormEvent>.present(
    lastState: InAppPurchaseFormUiState,
  ): InAppPurchaseFormUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var form: LoadedForm by remember { mutableStateOf(LoadedForm.Loading) }
    var values: Map<String, String?> by remember { mutableStateOf(emptyMap()) }
    var fieldErrors: Map<String, String> by remember { mutableStateOf(emptyMap()) }
    var isQuoting by remember { mutableStateOf(false) }
    var quoteError: QuoteError? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        InAppPurchaseFormEvent.Reload -> {
          loadIteration++
        }

        is InAppPurchaseFormEvent.UpdateField -> {
          values = values + (event.fieldId to event.value)
          fieldErrors = fieldErrors - event.fieldId
        }

        InAppPurchaseFormEvent.Submit -> {
          val fields = (form as? LoadedForm.Loaded)?.form?.fields.orEmpty()
          val blocked = fields.any { field ->
            FormFieldUiState(field = field, value = values[field.id], errorMessage = null).blocksSubmission
          }
          if (!isQuoting && !blocked) submitIteration++
        }

        InAppPurchaseFormEvent.DismissQuoteError -> {
          quoteError = null
        }
      }
    }

    LaunchedEffect(loadIteration) {
      form = LoadedForm.Loading
      getInAppPurchaseFormUseCase.invoke(product).fold(
        ifLeft = { form = LoadedForm.Failure },
        ifRight = { loadedForm ->
          values = loadedForm.fields.associate { it.id to it.defaultValueOrNull() }
          fieldErrors = emptyMap()
          form = LoadedForm.Loaded(loadedForm)
        },
      )
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration == 0) return@LaunchedEffect
      val loadedForm = (form as? LoadedForm.Loaded)?.form ?: return@LaunchedEffect
      isQuoting = true
      val fieldInputs = loadedForm.fields.map { field ->
        InAppPurchaseFieldInput(fieldId = field.id, values = listOfNotNull(values[field.id]))
      }
      quoteInAppPurchaseUseCase.invoke(product, fieldInputs).fold(
        ifLeft = { quoteError = QuoteError(it.message) },
        ifRight = { quote ->
          when (quote) {
            is InAppPurchaseQuote.InvalidFields -> {
              fieldErrors = quote.errors.associate { it.fieldId to it.message }
            }

            is InAppPurchaseQuote.Quoted -> {
              backstack.navigateToOffers(loadedForm.product.displayNameFull, quote)
            }
          }
        },
      )
      isQuoting = false
    }

    return when (val currentForm = form) {
      LoadedForm.Loading -> InAppPurchaseFormUiState.Loading

      LoadedForm.Failure -> InAppPurchaseFormUiState.Failure

      is LoadedForm.Loaded -> when (val eligibility = currentForm.form.eligibility) {
        is InAppPurchaseEligibility.NotEligible -> InAppPurchaseFormUiState.NotEligible(
          product = currentForm.form.product,
          reason = eligibility.reason,
          existingContractId = eligibility.existingContractId,
        )

        InAppPurchaseEligibility.Eligible -> InAppPurchaseFormUiState.Content(
          product = currentForm.form.product,
          fields = currentForm.form.fields.map { field ->
            FormFieldUiState(field = field, value = values[field.id], errorMessage = fieldErrors[field.id])
          },
          isQuoting = isQuoting,
          quoteError = quoteError,
        )
      }
    }
  }
}

private sealed interface LoadedForm {
  data object Loading : LoadedForm

  data object Failure : LoadedForm

  data class Loaded(val form: InAppPurchaseForm) : LoadedForm
}

/**
 * The value as sent in [InAppPurchaseFieldInput.values]. A single select without a default starts on its first option.
 */
internal fun InAppPurchaseFormField.defaultValueOrNull(): String? = when (this) {
  is InAppPurchaseFormField.Number -> defaultValue?.toString()
  is InAppPurchaseFormField.Date -> defaultValue?.toString()
  is InAppPurchaseFormField.SingleSelect -> defaultValue ?: options.firstOrNull()?.value
}

internal sealed interface InAppPurchaseFormEvent {
  data object Reload : InAppPurchaseFormEvent

  data class UpdateField(val fieldId: String, val value: String?) : InAppPurchaseFormEvent

  data object Submit : InAppPurchaseFormEvent

  data object DismissQuoteError : InAppPurchaseFormEvent
}

@Stable
internal sealed interface InAppPurchaseFormUiState {
  data object Loading : InAppPurchaseFormUiState

  data object Failure : InAppPurchaseFormUiState

  data class NotEligible(
    val product: InAppPurchaseProduct,
    val reason: InAppPurchaseIneligibilityReason,
    val existingContractId: String?,
  ) : InAppPurchaseFormUiState

  data class Content(
    val product: InAppPurchaseProduct,
    val fields: List<FormFieldUiState>,
    val isQuoting: Boolean,
    val quoteError: QuoteError?,
  ) : InAppPurchaseFormUiState {
    val canSubmit: Boolean = !isQuoting && fields.none { it.blocksSubmission }
  }
}

internal data class FormFieldUiState(
  val field: InAppPurchaseFormField,
  val value: String?,
  val errorMessage: String?,
) {
  val selectedOption: InAppPurchaseFormField.SingleSelect.Option? =
    (field as? InAppPurchaseFormField.SingleSelect)?.options?.firstOrNull { it.value == value }

  /**
   * An option that isn't selectable can still be chosen, but the purchase can't continue with it.
   */
  val blocksSubmission: Boolean = selectedOption?.isSelectable == false
}

/**
 * [message] is the backend's user-facing explanation, null for a network failure.
 */
internal data class QuoteError(val message: String?)
