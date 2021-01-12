package com.hedvig.app.feature.profile.ui.tab

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.app.R
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.databinding.ProfileLogoutBinding
import com.hedvig.app.databinding.ProfileRowBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.extensions.triggerRestartActivity
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val retry: () -> Unit
) : ListAdapter<ProfileModel, ProfileAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.profile_title -> ViewHolder.Title(parent)
        R.layout.profile_row -> ViewHolder.Row(parent)
        R.layout.profile_subtitle -> ViewHolder.Subtitle(parent)
        R.layout.profile_logout -> ViewHolder.Logout(parent)
        R.layout.generic_error -> ViewHolder.Error(parent)
        else -> throw Error("Invalid viewType")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        ProfileModel.Title -> R.layout.profile_title
        is ProfileModel.Row -> R.layout.profile_row
        ProfileModel.Subtitle -> R.layout.profile_subtitle
        ProfileModel.Logout -> R.layout.profile_logout
        ProfileModel.Error -> R.layout.generic_error
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), lifecycleOwner, retry)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner, retry: () -> Unit): Any?

        fun invalid(data: ProfileModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Title(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_title)) {
            override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner, retry: () -> Unit) = Unit
        }

        class Row(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_row)) {
            private val binding by viewBinding(ProfileRowBinding::bind)
            override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner, retry: () -> Unit) = with(binding) {
                if (data !is ProfileModel.Row) {
                    return invalid(data)
                }

                title.text = data.title
                caption.text = data.caption

                icon.setImageResource(data.icon)
                root.setHapticClickListener { data.action() }
            }
        }

        class Subtitle(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_subtitle)) {
            override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner, retry: () -> Unit) = Unit
        }

        class Logout(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_logout)) {
            private val binding by viewBinding(ProfileLogoutBinding::bind)
            override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner, retry: () -> Unit) = with(binding) {
                root.setHapticClickListener {
                    root.context.apply {
                        setAuthenticationToken(null)
                        setIsLoggedIn(false)
                        lifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                runCatching { FirebaseInstanceId.getInstance().deleteInstanceId() }
                                withContext(Dispatchers.Main) {
                                    triggerRestartActivity()
                                }
                            }
                        }
                    }
                }
            }
        }

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.generic_error)) {
            private val binding by viewBinding(GenericErrorBinding::bind)
            override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner, retry: () -> Unit) = with(binding) {
                this.retry.setHapticClickListener {
                    retry()
                }
            }
        }
    }
}
