package com.hedvig.app.feature.swedishbankid.sign

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.hedvig.android.designsystem.theme.HedvigTheme
import com.hedvig.app.R
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.viewLifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SwedishBankIdSignDialog : DialogFragment() {
  private val viewModel: SwedishBankIdSignViewModel by viewModel {
    parametersOf(
      requireArguments().getParcelable(QUOTE_CART_ID),
    )
  }
  private val marketManager: MarketManager by inject()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ) = ComposeView(requireContext()).apply {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

    var viewState: BankIdSignViewState by mutableStateOf(viewModel.viewState.value)
    viewLifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.viewState.collect { state ->
          viewState = state
          dialog?.setCanceledOnTouchOutside(viewState.isDialogDismissible)
          if (state is BankIdSignViewState.StartBankId) {
            val bankIdUri = bankIdUri()
            if (requireActivity().canOpenUri(bankIdUri)) {
              startActivity(
                Intent(
                  Intent.ACTION_VIEW,
                  bankIdUri,
                ),
              )
              viewModel.bankIdStarted()
            }
          } else if (state is BankIdSignViewState.StartDirectDebit) {
            val market = marketManager.market ?: return@collect
            startActivity(
              connectPayinIntent(
                requireContext(),
                state.payinType,
                market,
                true,
              ),
            )
            viewModel.directDebitStarted()
          }
        }
      }
    }
    setContent {
      HedvigTheme {
        SwedishBankIdSignDialog(
          text = textFromViewState(viewState),
        )
      }
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?) =
    super.onCreateDialog(savedInstanceState).apply {
      window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      setCanceledOnTouchOutside(false)
    }

  @Composable
  private fun textFromViewState(viewState: BankIdSignViewState): String {
    return when (viewState) {
      is BankIdSignViewState.StartBankId -> stringResource(hedvig.resources.R.string.SIGN_START_BANKID)
      BankIdSignViewState.Cancelled -> stringResource(hedvig.resources.R.string.SIGN_CANCELED)
      BankIdSignViewState.SignInProgress -> stringResource(hedvig.resources.R.string.SIGN_IN_PROGRESS)
      is BankIdSignViewState.Error -> {
        if (viewState.message != null) return viewState.message
        stringResource(hedvig.resources.R.string.SIGN_FAILED_REASON_UNKNOWN)
      }
      BankIdSignViewState.BankIdSuccess -> stringResource(hedvig.resources.R.string.SIGN_IN_PROGRESS)
      is BankIdSignViewState.StartDirectDebit -> stringResource(hedvig.resources.R.string.SIGN_IN_PROGRESS)
      BankIdSignViewState.Success -> stringResource(hedvig.resources.R.string.SIGN_SUCCESSFUL)
    }
  }

  companion object {
    private fun bankIdUri() = Uri.parse("bankid:///?redirect=hedvig://")

    private const val QUOTE_CART_ID = "QUOTE_CART_ID"
    const val TAG = "OfferSignDialog"
    fun newInstance(
      quoteCartId: QuoteCartId,
    ) = SwedishBankIdSignDialog().apply {
      arguments = bundleOf(
        QUOTE_CART_ID to quoteCartId,
      )
    }
  }
}

@Composable
fun SwedishBankIdSignDialog(text: String) {
  Surface(
    shape = RoundedCornerShape(8.dp),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .padding(16.dp),
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.subtitle1,
      )
      Image(
        painter = painterResource(id = R.drawable.ic_bank_id),
        contentDescription = null,
        modifier = Modifier.padding(top = 8.dp),
      )
    }
  }
}

@Composable
@Preview(
  name = "Swedish BankID Sign-Dialog",
  group = "Offer Screen",
)
fun Preview() {
  HedvigTheme {
    SwedishBankIdSignDialog(text = stringResource(id = hedvig.resources.R.string.SIGN_IN_PROGRESS))
  }
}
