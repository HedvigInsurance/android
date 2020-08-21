package com.hedvig.app.feature.profile.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ProfileFragmentBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import e
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ProfileFragment : Fragment(R.layout.profile_fragment) {
    private val binding by viewBinding(ProfileFragmentBinding::bind)
    private val model: ProfileViewModel by sharedViewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()

    private var scrollInitialBottomPadding = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recycler.apply {
            scrollInitialBottomPadding = paddingBottom

            loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bottomTabInset ->
                updatePadding(bottom = scrollInitialBottomPadding + bottomTabInset)
            }

            adapter = ProfileAdapter()
        }

        model.data.observe(viewLifecycleOwner) {
            (binding.recycler.adapter as? ProfileAdapter)?.items = listOf(
                ProfileModel.Title
            )
        }
    }
}

class ProfileAdapter : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {
    var items: List<ProfileModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(GenericDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.profile_title -> ViewHolder.Title(parent)
        else -> throw Error("Invalid viewType")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        ProfileModel.Title -> R.layout.profile_title
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: ProfileModel): Any?

        fun invalid(data: ProfileModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Title(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_title)) {
            override fun bind(data: ProfileModel) = Unit
        }
    }
}

sealed class ProfileModel {
    object Title : ProfileModel()
}
