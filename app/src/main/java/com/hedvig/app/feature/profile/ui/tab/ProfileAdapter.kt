package com.hedvig.app.feature.profile.ui.tab

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isGone
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ProfileLogoutBinding
import com.hedvig.app.databinding.ProfileRowBinding
import com.hedvig.app.ui.compose.composables.screens.GenericErrorScreen
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class ProfileAdapter(
  private val lifecycleOwner: LifecycleOwner,
  private val retry: () -> Unit,
  private val onLogoutListener: () -> Unit,
) : ListAdapter<ProfileModel, ProfileAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    R.layout.profile_title -> ViewHolder.Title(parent)
    R.layout.profile_row -> ViewHolder.Row(parent)
    R.layout.profile_subtitle -> ViewHolder.Subtitle(parent)
    R.layout.profile_logout -> ViewHolder.Logout(parent, onLogoutListener)
    ERROR -> ViewHolder.Error(ComposeView(parent.context), retry)
    else -> throw Error("Invalid viewType")
  }

  override fun getItemViewType(position: Int) = when (getItem(position)) {
    ProfileModel.Title -> R.layout.profile_title
    is ProfileModel.Row -> R.layout.profile_row
    ProfileModel.Subtitle -> R.layout.profile_subtitle
    ProfileModel.Logout -> R.layout.profile_logout
    ProfileModel.Error -> ERROR
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position), lifecycleOwner)
  }

  sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner): Any?

    fun invalid(data: ProfileModel) {
      e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
    }

    class Title(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_title)) {
      override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner) = Unit
    }

    class Row(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_row)) {
      private val binding by viewBinding(ProfileRowBinding::bind)
      override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner) = with(binding) {
        if (data !is ProfileModel.Row) {
          return invalid(data)
        }

        title.text = data.title
        caption.isGone = data.caption == null
        caption.text = data.caption

        icon.setImageResource(data.icon)
        root.setHapticClickListener { data.onClick() }
      }
    }

    class Subtitle(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_subtitle)) {
      override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner) = Unit
    }

    class Logout(
      parent: ViewGroup,
      private val onLogoutListener: () -> Unit,
    ) : ViewHolder(parent.inflate(R.layout.profile_logout)) {
      private val binding by viewBinding(ProfileLogoutBinding::bind)
      override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner) = with(binding) {
        root.setHapticClickListener {
          onLogoutListener()
        }
      }
    }

    class Error(
      val composeView: ComposeView,
      private val retry: () -> Unit,
    ) : ViewHolder(composeView) {
      override fun bind(data: ProfileModel, lifecycleOwner: LifecycleOwner) {
        composeView.setContent {
          HedvigTheme {
            GenericErrorScreen(onRetryButtonClicked = { retry() })
          }
        }
      }
    }
  }

  companion object {
    const val ERROR = 1
  }
}
