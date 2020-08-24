package com.hedvig.app.feature.profile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ProfileFragmentBinding
import com.hedvig.app.databinding.ProfileRowBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppActivity
import com.hedvig.app.feature.profile.ui.charity.CharityActivity
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
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

        model.data.observe(viewLifecycleOwner) { data ->
            (binding.recycler.adapter as? ProfileAdapter)?.items = listOf(
                ProfileModel.Title,
                ProfileModel.Row(
                    getString(R.string.PROFILE_MY_INFO_ROW_TITLE),
                    "${data.member.firstName} ${data.member.lastName}",
                    R.drawable.ic_contact_information
                ) {
                    startActivity(Intent(requireContext(), MyInfoActivity::class.java))
                },
                ProfileModel.Row(
                    getString(R.string.PROFILE_MY_CHARITY_ROW_TITLE),
                    data.cashback?.fragments?.cashbackFragment?.name ?: "",
                    R.drawable.ic_charity
                ) {
                    startActivity(Intent(requireContext(), CharityActivity::class.java))
                },
                ProfileModel.Row(
                    getString(R.string.PROFILE_ROW_PAYMENT_TITLE),
                    data.insuranceCost?.fragments?.costFragment?.monthlyNet?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                        ?.let { monthlyNet ->
                            getString(
                                R.string.PROFILE_ROW_PAYMENT_DESCRIPTION,
                                monthlyNet.format(requireContext())
                            )
                        } ?: "",
                    R.drawable.ic_payment
                ) {
                    startActivity(Intent(requireContext(), PaymentActivity::class.java))
                },
                ProfileModel.Subtitle,
                ProfileModel.Row(
                    getString(R.string.profile_appSettingsSection_row_headline),
                    getString(R.string.profile_appSettingsSection_row_subheadline),
                    R.drawable.ic_profile_settings
                ) {
                    startActivity(Intent(requireContext(), AboutAppActivity::class.java))
                }
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
        R.layout.profile_row -> ViewHolder.Row(parent)
        R.layout.profile_subtitle -> ViewHolder.Subtitle(parent)
        else -> throw Error("Invalid viewType")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        ProfileModel.Title -> R.layout.profile_title
        is ProfileModel.Row -> R.layout.profile_row
        ProfileModel.Subtitle -> R.layout.profile_subtitle
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

        class Row(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.profile_row)) {
            private val binding by viewBinding(ProfileRowBinding::bind)
            override fun bind(data: ProfileModel) = with(binding) {
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
            override fun bind(data: ProfileModel) = Unit
        }
    }
}

sealed class ProfileModel {
    object Title : ProfileModel()

    data class Row(
        val title: String,
        val caption: String,
        @DrawableRes val icon: Int,
        val action: () -> Unit
    ) : ProfileModel()

    object Subtitle : ProfileModel()
}
