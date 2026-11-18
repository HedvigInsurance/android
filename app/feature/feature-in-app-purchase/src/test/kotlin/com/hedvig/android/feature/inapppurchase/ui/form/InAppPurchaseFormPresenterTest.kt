package com.hedvig.android.feature.inapppurchase.ui.form

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.InAppProduct
import com.hedvig.android.feature.inapppurchase.data.GetInAppPurchaseFormUseCase
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseEligibility
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFieldError
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFieldInput
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseForm
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseImage
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseProduct
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseQuote
import com.hedvig.android.feature.inapppurchase.data.QuoteInAppPurchaseUseCase
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class InAppPurchaseFormPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val getForm = FakeGetInAppPurchaseFormUseCase()
  private val quote = FakeQuoteInAppPurchaseUseCase()
  private val presenter = InAppPurchaseFormPresenter(InAppProduct.DOG, getForm, quote, TestBackstack())

  @Test
  fun `loaded form starts with each field's default values`() = runTest {
    presenter.test(InAppPurchaseFormUiState.Loading) {
      skipItems(1)
      getForm.results.add(form.right())

      val content = awaitItem() as InAppPurchaseFormUiState.Content
      assertThat(content.valuesOf("name")).isEqualTo(emptyList())
      assertThat(content.valuesOf("breeds")).containsExactly("LABRADOR")
      assertThat(content.valuesOf("extraBuildings")).isEqualTo(emptyList())
    }
  }

  @Test
  fun `a question without a default starts unanswered`() = runTest {
    val question = InAppPurchaseFormField.SingleSelect(
      id = "isNeutered",
      title = "Neutered",
      options = listOf("true", "false").map { InAppPurchaseFormField.SingleSelect.Option(it, it, true, null) },
      defaultValue = null,
    )

    presenter.test(InAppPurchaseFormUiState.Loading) {
      skipItems(1)
      getForm.results.add(form.copy(fields = listOf(question)).right())

      val content = awaitItem() as InAppPurchaseFormUiState.Content
      assertThat(content.valuesOf("isNeutered")).isEqualTo(emptyList())
    }
  }

  @Test
  fun `updating a field replaces its values and clears its error`() = runTest {
    presenter.test(InAppPurchaseFormUiState.Loading) {
      skipItems(1)
      getForm.results.add(form.right())
      skipItems(1)

      sendEvent(InAppPurchaseFormEvent.Submit)
      quote.results.add(
        InAppPurchaseQuote.InvalidFields(listOf(InAppPurchaseFieldError("name", "Required")), null).right(),
      )
      val withError = awaitUntil { it.fieldState("name").errorMessage != null }
      assertThat(withError.fieldState("name").errorMessage).isEqualTo("Required")

      sendEvent(InAppPurchaseFormEvent.UpdateField("name", listOf("Fido")))
      val updated = awaitUntil { it.valuesOf("name") == listOf("Fido") }
      assertThat(updated.fieldState("name").errorMessage).isNull()
    }
  }

  @Test
  fun `submit sends every field with its values, including empty ones`() = runTest {
    presenter.test(InAppPurchaseFormUiState.Loading) {
      skipItems(1)
      getForm.results.add(form.right())
      skipItems(1)
      sendEvent(InAppPurchaseFormEvent.UpdateField("name", listOf("Fido")))
      sendEvent(InAppPurchaseFormEvent.UpdateField("breeds", listOf("LABRADOR", "POODLE")))

      sendEvent(InAppPurchaseFormEvent.Submit)

      assertThat(quote.requests.awaitItem()).containsExactly(
        InAppPurchaseFieldInput("name", listOf("Fido")),
        InAppPurchaseFieldInput("breeds", listOf("LABRADOR", "POODLE")),
        InAppPurchaseFieldInput("extraBuildings", emptyList()),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `field errors for shown fields go under the field and others to the error dialog`() = runTest {
    presenter.test(InAppPurchaseFormUiState.Loading) {
      skipItems(1)
      getForm.results.add(form.right())
      skipItems(1)

      sendEvent(InAppPurchaseFormEvent.Submit)
      quote.results.add(
        InAppPurchaseQuote.InvalidFields(
          listOf(InAppPurchaseFieldError("breeds", "Pick a breed"), InAppPurchaseFieldError("street", "No address")),
          null,
        ).right(),
      )

      val content = awaitUntil { it.quoteError != null }
      assertThat(content.fieldState("breeds").errorMessage).isEqualTo("Pick a breed")
      assertThat(content.quoteError?.message).isEqualTo("No address")
    }
  }

  @Test
  fun `a restarted presenter keeps the member's multi-value input`() = runTest {
    val lastState = InAppPurchaseFormUiState.Content(
      product = product,
      fields = listOf(
        FormFieldUiState(form.fields[0], listOf("Fido"), null),
        FormFieldUiState(form.fields[1], listOf("LABRADOR", "POODLE"), null),
        FormFieldUiState(form.fields[2], listOf(GARAGE), null),
      ),
      campaignCode = "",
      campaignCodeError = null,
      isQuoting = false,
      quoteError = null,
    )

    presenter.test(lastState) {
      val content = awaitItem() as InAppPurchaseFormUiState.Content
      assertThat(content.valuesOf("breeds")).containsExactly("LABRADOR", "POODLE")
      assertThat(content.valuesOf("extraBuildings")).containsExactly(GARAGE)
    }
  }

  @Test
  fun `removing the last extra building submits an empty list`() = runTest {
    presenter.test(InAppPurchaseFormUiState.Loading) {
      skipItems(1)
      getForm.results.add(form.right())
      skipItems(1)
      sendEvent(InAppPurchaseFormEvent.UpdateField("extraBuildings", listOf(GARAGE)))
      sendEvent(InAppPurchaseFormEvent.UpdateField("extraBuildings", emptyList()))

      sendEvent(InAppPurchaseFormEvent.Submit)

      assertThat(quote.requests.awaitItem().single { it.fieldId == "extraBuildings" }.values).isEqualTo(emptyList())
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun app.cash.turbine.ReceiveTurbine<InAppPurchaseFormUiState>.awaitUntil(
    predicate: (InAppPurchaseFormUiState.Content) -> Boolean,
  ): InAppPurchaseFormUiState.Content {
    while (true) {
      val item = awaitItem()
      if (item is InAppPurchaseFormUiState.Content && predicate(item)) return item
    }
  }

  private fun InAppPurchaseFormUiState.Content.fieldState(id: String) = fields.single { it.field.id == id }

  private fun InAppPurchaseFormUiState.Content.valuesOf(id: String) = fieldState(id).values

  private companion object {
    const val GARAGE = """{"type":"GARAGE","area":25,"hasWaterConnected":false}"""

    val product = InAppPurchaseProduct(
      name = "SE_PET_DOG",
      displayNameShort = "Dog",
      displayNameFull = "Dog insurance",
      tagline = "",
      pillowImage = InAppPurchaseImage(src = "", alt = null),
      featuredImage = null,
      variants = emptyList(),
    )

    val form = InAppPurchaseForm(
      product = product,
      eligibility = InAppPurchaseEligibility.Eligible,
      fields = listOf(
        InAppPurchaseFormField.Text(id = "name", title = "Name", defaultValue = null),
        InAppPurchaseFormField.SearchableSelect(
          id = "breeds",
          title = "Breed",
          options = listOf("LABRADOR", "POODLE").map { InAppPurchaseFormField.SingleSelect.Option(it, it, true, null) },
          defaultValues = listOf("LABRADOR"),
          maxSelections = 2,
        ),
        InAppPurchaseFormField.ExtraBuildings(
          id = "extraBuildings",
          title = "Extra buildings",
          buildingTypes = listOf(InAppPurchaseFormField.SingleSelect.Option("GARAGE", "Garage", true, null)),
        ),
      ),
    )
  }
}

private class FakeGetInAppPurchaseFormUseCase : GetInAppPurchaseFormUseCase {
  val results = Turbine<Either<ErrorMessage, InAppPurchaseForm>>()

  override suspend fun invoke(product: InAppProduct) = results.awaitItem()
}

private class FakeQuoteInAppPurchaseUseCase : QuoteInAppPurchaseUseCase {
  val requests = Turbine<List<InAppPurchaseFieldInput>>()
  val results = Turbine<Either<ErrorMessage, InAppPurchaseQuote>>()

  override suspend fun invoke(
    product: InAppProduct,
    fields: List<InAppPurchaseFieldInput>,
    campaignCode: String?,
  ): Either<ErrorMessage, InAppPurchaseQuote> {
    requests.add(fields)
    return results.awaitItem()
  }
}

private class TestBackstack(override val entries: MutableList<HedvigNavKey> = mutableListOf()) : Backstack
