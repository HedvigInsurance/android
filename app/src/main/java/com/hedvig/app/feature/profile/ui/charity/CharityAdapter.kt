package com.hedvig.app.feature.profile.ui.charity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.CashbackOptionBinding
import com.hedvig.app.feature.profile.ui.tab.CharityOption
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class CharityAdapter(
  val context: Context,
  private val clickListener: (id: String) -> Unit,
) : ListAdapter<CharityOption, CharityAdapter.CashbackOptionViewHolder>(
  GenericDiffUtilItemCallback(),
) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CashbackOptionViewHolder =
    CashbackOptionViewHolder(
      LayoutInflater.from(context).inflate(
        R.layout.cashback_option,
        parent,
        false,
      ),
    )

  override fun onBindViewHolder(holder: CashbackOptionViewHolder, position: Int) {
    holder.bind(getItem(position), clickListener)
  }

  class CashbackOptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding by viewBinding(CashbackOptionBinding::bind)
    val title: TextView = binding.cashbackOptionTitle
    val paragraph: TextView = binding.cashbackOptionParagraph
    val button: Button = binding.cashbackSelect

    fun bind(charityOption: CharityOption, clickListener: (id: String) -> Unit) {
      binding.apply {
        cashbackOptionTitle.text = charityOption.name
        cashbackOptionParagraph.text = charityOption.description

        cashbackSelect.text = cashbackSelect.resources.getString(
          hedvig.resources.R.string.PROFILE_CHARITY_SELECT_BUTTON,
          charityOption.name,
        )
        cashbackSelect.setHapticClickListener {
          charityOption.id?.let { id ->
            clickListener(id)
          }
        }
      }
    }
  }
}
