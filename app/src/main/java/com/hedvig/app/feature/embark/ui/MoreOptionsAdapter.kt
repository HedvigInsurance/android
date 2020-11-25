package com.hedvig.app.feature.embark.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.SettingsBinding
import com.hedvig.app.databinding.UserIdBinding
import com.hedvig.app.databinding.VersionBinding
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class MoreOptionsAdapter :
    ListAdapter<MoreOptionsModel, MoreOptionsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.more_options_header -> ViewHolder.Header(parent)
        R.layout.user_id -> ViewHolder.UserId(parent)
        R.layout.version -> ViewHolder.Version(parent)
        R.layout.settings -> ViewHolder.Settings(parent)
        R.layout.copyright -> ViewHolder.Copyright(parent)
        else -> throw Error("Unreachable")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        MoreOptionsModel.Header -> R.layout.more_options_header
        is MoreOptionsModel.UserId -> R.layout.user_id
        MoreOptionsModel.Version -> R.layout.version
        MoreOptionsModel.Settings -> R.layout.settings
        MoreOptionsModel.Copyright -> R.layout.copyright
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: MoreOptionsModel)

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.more_options_header)) {
            override fun bind(item: MoreOptionsModel) {
            }
        }

        class UserId(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.user_id)) {
            private val binding by viewBinding(UserIdBinding::bind)
            override fun bind(item: MoreOptionsModel) {
                if (item !is MoreOptionsModel.UserId) {
                    invalid(item)
                    return
                }
                binding.memberId.text = item.id
            }
        }

        class Version(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.version)) {
            private val binding by viewBinding(VersionBinding::bind)
            override fun bind(item: MoreOptionsModel) {
                binding.version.text = BuildConfig.VERSION_NAME
            }
        }

        class Settings(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.settings)) {
            private val binding by viewBinding(SettingsBinding::bind)
            override fun bind(item: MoreOptionsModel) {
                binding.root.setHapticClickListener { view ->
                    view.context.startActivity(SettingsActivity.newInstance(view.context))
                }
            }
        }

        class Copyright(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.copyright)) {
            override fun bind(item: MoreOptionsModel) {
            }
        }
    }
}

sealed class MoreOptionsModel {
    object Header : MoreOptionsModel()
    data class UserId(val id: String) : MoreOptionsModel()
    object Version : MoreOptionsModel()
    object Settings : MoreOptionsModel()
    object Copyright : MoreOptionsModel()
}
