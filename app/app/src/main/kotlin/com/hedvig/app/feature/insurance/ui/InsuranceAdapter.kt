package com.hedvig.app.feature.insurance.ui

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.app.R
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.getActivity
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class InsuranceAdapter(
  private val retry: () -> Unit,
  private val imageLoader: ImageLoader,
) : ListAdapter<InsuranceModel, InsuranceAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    R.layout.insurance_contract_card -> ViewHolder.ContractViewHolder(parent, imageLoader)
    ERROR -> ViewHolder.Error(ComposeView(parent.context), retry)
    else -> {
      throw Error("Unreachable")
    }
  }

  override fun getItemViewType(position: Int) = when (getItem(position)) {
    is InsuranceModel.Contract -> R.layout.insurance_contract_card
    is InsuranceModel.Error -> ERROR
    else -> error("Not applicable")
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(data: InsuranceModel)

    class ContractViewHolder(
      parent: ViewGroup,
      private val imageLoader: ImageLoader,
    ) : ViewHolder(
      parent.inflate(R.layout.insurance_contract_card),
    ) {
      private val binding by viewBinding(InsuranceContractCardBinding::bind)

      override fun bind(data: InsuranceModel) = with(binding) {
        if (data !is InsuranceModel.Contract) {
          return invalid(data)
        }
        data.contractCardViewState.bindTo(binding, imageLoader)
        card.setHapticClickListener {
          card.context.getActivity()?.let { activity ->
            card.context.startActivity(
              ContractDetailActivity.newInstance(
                card.context,
                data.contractCardViewState.id,
              ),
            )
          }
        }
      }
    }

    class Error(
      private val composeView: ComposeView,
      private val retry: () -> Unit,
    ) : ViewHolder(composeView) {
      override fun bind(data: InsuranceModel) {
        if (data !is InsuranceModel.Error) {
          return invalid(data)
        }
        composeView.setContent {
          HedvigTheme {
            GenericErrorScreen(
              description = composeView.context.getString(hedvig.resources.R.string.home_tab_error_body),
              onRetryButtonClick = { retry() },
              modifier = Modifier
                .padding(16.dp)
                .padding(top = (40 - 16).dp),
            )
          }
        }
      }
    }
  }

  companion object {
    private const val ERROR = 4
  }
}
