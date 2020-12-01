package com.hedvig.app.feature.onbarding.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.MoreOptionsRowBinding
import com.hedvig.app.databinding.SettingsBinding
import com.hedvig.app.feature.onbarding.MoreOptionsViewModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.setCompoundDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class MoreOptionsAdapter(private val viewModel: MoreOptionsViewModel) :
    ListAdapter<MoreOptionsModel, MoreOptionsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        HEADER -> ViewHolder.Header(parent)
        MEMBER_ID_SUCCESS -> ViewHolder.UserIdSuccess(parent)
        MEMBER_ID_ERROR -> ViewHolder.UserIdError(parent)
        VERSION -> ViewHolder.Version(parent)
        SETTINGS -> ViewHolder.Settings(parent)
        COPYRIGHT -> ViewHolder.Copyright(parent)
        else -> throw Error("Unreachable")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        MoreOptionsModel.Header -> HEADER
        is MoreOptionsModel.UserId.Success -> MEMBER_ID_SUCCESS
        MoreOptionsModel.UserId.Error -> MEMBER_ID_ERROR
        MoreOptionsModel.Version -> VERSION
        MoreOptionsModel.Settings -> SETTINGS
        MoreOptionsModel.Copyright -> COPYRIGHT
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel)

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.more_options_header)) {
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) = Unit
        }

        class UserIdSuccess(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.more_options_row)) {
            private val binding by viewBinding(MoreOptionsRowBinding::bind)
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
                if (item !is MoreOptionsModel.UserId.Success) {
                    invalid(item)
                    return
                }
                binding.apply {
                    label.apply {
                        setText(R.string.embark_onboarding_more_options_user_id_label)
                        compoundDrawablePadding = 16.dp
                        setCompoundDrawable(start = R.drawable.ic_contact_information)
                    }
                    info.text = item.id
                }
            }
        }

        class UserIdError(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.more_options_row)) {
            private val binding by viewBinding(MoreOptionsRowBinding::bind)
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
                if (item !is MoreOptionsModel.UserId.Error) {
                    invalid(item)
                    return
                }
                binding.apply {
                    label.apply {
                        setText(R.string.embark_onboarding_more_options_user_id_label)
                        compoundDrawablePadding = 16.dp
                        setCompoundDrawable(start = R.drawable.ic_contact_information)
                    }
                    info.apply {
                        setText(R.string.embark_onboarding_more_options_loading_error_reload_label)
                        compoundDrawablePadding = 8.dp
                        setCompoundDrawable(end = R.drawable.ic_refresh)
                        setHapticClickListener {
                            viewModel.load()
                        }
                    }
                }
            }
        }

        class Version(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.more_options_row)) {
            private val binding by viewBinding(MoreOptionsRowBinding::bind)
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
                binding.apply {
                    label.apply {
                        setText(R.string.embark_onboarding_more_options_version_label)
                        compoundDrawablePadding = 16.dp
                        setCompoundDrawable(start = R.drawable.ic_info_more_options)
                    }
                    info.text = BuildConfig.VERSION_NAME
                }
            }
        }

        class Settings(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.settings)) {
            private val binding by viewBinding(SettingsBinding::bind)
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
                binding.root.setHapticClickListener { view ->
                    view.context.startActivity(SettingsActivity.newInstance(view.context))
                }
            }
        }

        class Copyright(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.copyright)) {
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) = Unit
        }
    }

    companion object {
        private const val HEADER = 1
        private const val MEMBER_ID_SUCCESS = 2
        private const val MEMBER_ID_ERROR = 3
        private const val VERSION = 4
        private const val SETTINGS = 5
        private const val COPYRIGHT = 6
    }
}

sealed class MoreOptionsModel {
    object Header : MoreOptionsModel()
    sealed class UserId : MoreOptionsModel() {
        data class Success(val id: String) : UserId()
        object Error : UserId()
    }

    object Version : MoreOptionsModel()
    object Settings : MoreOptionsModel()
    object Copyright : MoreOptionsModel()
}
