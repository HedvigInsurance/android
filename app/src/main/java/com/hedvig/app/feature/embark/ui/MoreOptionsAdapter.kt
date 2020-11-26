package com.hedvig.app.feature.embark.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.SettingsBinding
import com.hedvig.app.databinding.UserIdBinding
import com.hedvig.app.databinding.UserIdErrorBinding
import com.hedvig.app.databinding.VersionBinding
import com.hedvig.app.feature.embark.MoreOptionsViewModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class MoreOptionsAdapter(private val viewModel: MoreOptionsViewModel) :
    ListAdapter<MoreOptionsModel, MoreOptionsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.more_options_header -> ViewHolder.Header(parent)
        R.layout.user_id -> ViewHolder.UserIdSuccess(parent)
        R.layout.user_id_error -> ViewHolder.UserIdError(parent)
        R.layout.version -> ViewHolder.Version(parent)
        R.layout.settings -> ViewHolder.Settings(parent)
        R.layout.copyright -> ViewHolder.Copyright(parent)
        else -> throw Error("Unreachable")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        MoreOptionsModel.Header -> R.layout.more_options_header
        is MoreOptionsModel.UserId.Success -> R.layout.user_id
        MoreOptionsModel.UserId.Error -> R.layout.user_id_error
        MoreOptionsModel.Version -> R.layout.version
        MoreOptionsModel.Settings -> R.layout.settings
        MoreOptionsModel.Copyright -> R.layout.copyright
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel)

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.more_options_header)) {
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
            }
        }

        class UserIdSuccess(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.user_id)) {
            private val binding by viewBinding(UserIdBinding::bind)
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
                if (item !is MoreOptionsModel.UserId.Success) {
                    invalid(item)
                    return
                }
                binding.memberId.text = item.id
            }
        }

        class UserIdError(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.user_id_error)) {
            private val binding by viewBinding(UserIdErrorBinding::bind)
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
                if (item !is MoreOptionsModel.UserId.Error) {
                    invalid(item)
                    return
                }
                binding.memberId.setHapticClickListener {
                    viewModel.load()
                }
            }
        }

        class Version(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.version)) {
            private val binding by viewBinding(VersionBinding::bind)
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
                binding.version.text = BuildConfig.VERSION_NAME
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
            override fun bind(item: MoreOptionsModel, viewModel: MoreOptionsViewModel) {
            }
        }
    }
}

