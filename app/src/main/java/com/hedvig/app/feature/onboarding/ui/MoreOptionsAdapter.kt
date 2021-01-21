package com.hedvig.app.feature.onboarding.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.MoreOptionsRowBinding
import com.hedvig.app.feature.onboarding.MoreOptionsModel
import com.hedvig.app.feature.onboarding.MoreOptionsViewModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.putCompoundDrawablesRelativeWithIntrinsicBounds
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding

class MoreOptionsAdapter(private val viewModel: MoreOptionsViewModel) :
    ListAdapter<MoreOptionsModel, MoreOptionsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.more_options_header -> ViewHolder.Header(parent)
        R.layout.more_options_row -> ViewHolder.Row(parent)
        R.layout.copyright -> ViewHolder.Copyright(parent)
        else -> throw Error("Unreachable")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        MoreOptionsModel.Header -> R.layout.more_options_header
        is MoreOptionsModel.Row.UserId.Success -> R.layout.more_options_row
        MoreOptionsModel.Row.UserId.Error -> R.layout.more_options_row
        MoreOptionsModel.Row.Version -> R.layout.more_options_row
        MoreOptionsModel.Row.Settings -> R.layout.more_options_row
        MoreOptionsModel.Copyright -> R.layout.copyright
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel)

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.more_options_header)) {
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) = Unit
        }

        class Row(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.more_options_row)) {
            private val binding by viewBinding(MoreOptionsRowBinding::bind)
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
                if (item !is MoreOptionsModel.Row) {
                    invalid(item)
                    return
                }
                binding.apply {

                    when (item) {
                        is MoreOptionsModel.Row.UserId.Success -> {
                            label.apply {
                                setText(R.string.embark_onboarding_more_options_user_id_label)
                                putCompoundDrawablesRelativeWithIntrinsicBounds(
                                    start = R.drawable.ic_contact_information
                                )
                            }
                            info.text = item.id
                        }
                        MoreOptionsModel.Row.UserId.Error -> {
                            label.apply {
                                setText(R.string.embark_onboarding_more_options_user_id_label)
                                putCompoundDrawablesRelativeWithIntrinsicBounds(
                                    start = R.drawable.ic_contact_information
                                )
                            }
                            info.apply {
                                setText(R.string.embark_onboarding_more_options_loading_error_reload_label)
                                compoundDrawablePadding = 8.dp
                                putCompoundDrawablesRelativeWithIntrinsicBounds(end = R.drawable.ic_refresh)
                                setHapticClickListener {
                                    viewModel.load()
                                }
                            }
                        }
                        MoreOptionsModel.Row.Version -> {
                            label.apply {
                                setText(R.string.embark_onboarding_more_options_version_label)
                                putCompoundDrawablesRelativeWithIntrinsicBounds(start = R.drawable.ic_info_more_options)
                            }
                            info.text = BuildConfig.VERSION_NAME
                        }
                        MoreOptionsModel.Row.Settings -> {
                            label.apply {
                                setText(R.string.embark_onboarding_more_options_settings_label)
                                putCompoundDrawablesRelativeWithIntrinsicBounds(start = R.drawable.ic_profile_settings)
                            }
                            settingsArrow.show()
                            info.remove()
                            binding.root.setHapticClickListener { view ->
                                view.context.startActivity(SettingsActivity.newInstance(view.context))
                            }
                        }
                    }
                    label.compoundDrawablePadding = 16.dp
                }
            }
        }

        class Copyright(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.copyright)) {
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) = Unit
        }
    }
}
