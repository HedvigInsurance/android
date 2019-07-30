package com.hedvig.app.feature.profile.ui.referral

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.util.LightClass
import com.hedvig.app.util.extensions.*
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.getLightness
import com.hedvig.app.util.hashColor
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.referral_header.view.*
import kotlinx.android.synthetic.main.referral_invite_row.view.*
import kotlinx.android.synthetic.main.referral_small_header_row.view.*
import kotlin.math.ceil
import kotlin.math.min

class InvitesAdapter(
    private val monthlyCost: Int,
    private val data: ProfileQuery.ReferralInformation
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder = when (position) {
        HEADER -> {
            HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.referral_header, parent, false)
            )
        }
        REFERRED_BY_HEADER,
        INVITE_HEADER -> SmallHeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.referral_small_header_row, parent, false)
        )
        REFERRED_BY_ITEM,
        INVITE_ITEM -> ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.referral_invite_row, parent, false)
        )
        else -> EmptyViewHolder(View(parent.context))
    }

    override fun getItemViewType(position: Int) = when {
        position == 0 -> HEADER
        data.referredBy != null && position == itemCount - 2 -> REFERRED_BY_HEADER
        data.referredBy != null && position == itemCount - 1 -> REFERRED_BY_ITEM
        !data.invitations.isNullOrEmpty() && position == 1 -> INVITE_HEADER
        !data.invitations.isNullOrEmpty() -> INVITE_ITEM
        else -> UNKNOWN_ITEM // Should never happen
    }

    override fun getItemCount(): Int {
        var count = 1 //start of with header
        data.invitations?.let { count += it.size + 1 } //add title
        data.referredBy?.let { count += 2 } //add title and item

        return count
    }

    override fun onBindViewHolder(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            HEADER -> (viewHolder as? HeaderViewHolder)?.apply {
                val incentive =
                    data.campaign.monthlyCostDeductionIncentive()?.amount?.amount?.toBigDecimal()?.toInt()
                        ?: return@apply
                if (monthlyCost / incentive <= PROGRESS_TANK_MAX_SEGMENTS) {
                    progressTankView.initialize(
                        monthlyCost,
                        calculateDiscount(),
                        incentive
                    )
                    progressTankView.show()
                    referralProgressHighPremiumContainer.remove()
                } else {
                    referralProgressHighPremiumContainer.show()
                    val highPremiumStringKey = if (calculateDiscount() == 0) {
                        R.string.REFERRAL_PROGRESS_HIGH_PREMIUM_DISCOUNT_NO_MINUS
                    } else {
                        R.string.REFERRAL_PROGRESS_HIGH_PREMIUM_DISCOUNT
                    }
                    referralProgressHighPremiumDiscount.text = interpolateTextKey(
                        referralProgressHighPremiumDiscount.resources.getString(highPremiumStringKey),
                        "DISCOUNT_VALUE" to calculateDiscount().toString()
                    )
                    referralProgressHighPremiumCurrentPrice.text = interpolateTextKey(
                        referralProgressHighPremiumDiscount.resources.getString(R.string.REFERRAL_PROGRESS_HIGH_PREMIUM_DESCRIPTION),
                        "MONTHLY_COST" to monthlyCost.toString()
                    )
                    progressTankView.remove()
                }
                val campaignCode = data.campaign.code
                code.text = campaignCode
                code.setOnLongClickListener {
                    code.context.copyToClipboard(campaignCode)
                    code.context.makeToast(R.string.REFERRAL_INVITE_CODE_COPIED_MESSAGE)
                    true
                }
                subtitle.text = interpolateTextKey(
                    subtitle.resources.getString(R.string.REFERRAL_PROGRESS_HEADLINE),
                    "NUMBER_OF_FRIENDS_LEFT" to calculateInvitesLeftToFree().toString()
                )
                explainer.text = interpolateTextKey(
                    explainer.resources.getString(R.string.REFERRAL_PROGRESS_BODY),
                    "REFERRAL_VALUE" to incentive.toString()
                )

                if (shouldShowEmptyState()) {
                    avatar.setImageDrawable(avatar.context.compatDrawable(R.drawable.ic_ghost))
                    name.text = name.resources.getString(R.string.REFERRAL_INVITE_EMPTYSTATE_TITLE)
                    statusText.text = statusText.resources.getString(R.string.REFERRAL_INVITE_EMPTYSTATE_DESCRIPTION)
                    statusIconContainer.remove()
                    emptyStateRow.show()
                    emptyStateTitle.text = emptyStateTitle.resources.getString(R.string.REFERRAL_INVITE_TITLE)
                    emptyStateTitle.show()
                } else {
                    emptyStateRow.remove()
                    emptyStateTitle.remove()
                }
            }
            INVITE_HEADER -> (viewHolder as? SmallHeaderViewHolder)?.apply {
                headerTextView.text = headerTextView.resources.getString(R.string.REFERRAL_INVITE_TITLE)
            }
            INVITE_ITEM -> (viewHolder as? ItemViewHolder)?.apply {
                data.invitations?.getOrNull(position - 2)?.let { invite ->
                    when (invite) {
                        is ProfileQuery.AsActiveReferral1 -> bindActiveRow(
                            this,
                            invite.name,
                            invite.discount.amount.toBigDecimal().toInt().toString(),
                            false
                        )
                        is ProfileQuery.AsInProgressReferral1 -> bindInProgress(this, invite.name)
                        is ProfileQuery.AsAcceptedReferral1 -> bindNotInitiated(
                            this, invite.quantity
                                ?: 0
                        )
                        is ProfileQuery.AsTerminatedReferral1 -> bindTerminated(this, invite.name)
                        else -> { /* Should never happen */
                        }
                    }
                }

            }
            REFERRED_BY_HEADER -> (viewHolder as? SmallHeaderViewHolder)?.apply {
                headerTextView.text = headerTextView.resources.getString(R.string.REFERRAL_REFERRED_BY_TITLE)
            }
            REFERRED_BY_ITEM -> (viewHolder as? ItemViewHolder)?.apply {
                when (val referredBy = data.referredBy) {
                    is ProfileQuery.AsActiveReferral -> bindActiveRow(
                        this,
                        referredBy.name,
                        referredBy.discount.amount.toBigDecimal().toInt().toString(),
                        true
                    )
                    is ProfileQuery.AsInProgressReferral -> bindInProgress(this, referredBy.name)
                    is ProfileQuery.AsTerminatedReferral -> bindTerminated(this, referredBy.name)
                    is ProfileQuery.AsAcceptedReferral -> bindNotInitiated(
                        this, referredBy.quantity
                            ?: 0
                    )
                    else -> { /* Should never happen */
                    }
                }
            }
        }
    }

    private fun bindActiveRow(
        viewHolder: ItemViewHolder,
        nameString: String?,
        discountString: String?,
        referredBy: Boolean
    ) =
        viewHolder.apply {
            setupAvatarWithLetter(this, nameString)

            name.text = nameString
            statusText.text =
                if (referredBy) statusText.resources.getString(R.string.REFERRAL_INVITE_INVITEDYOUSTATE) else statusText.resources.getString(
                    R.string.REFERRAL_INVITE_NEWSTATE
                )

            statusIconContainer.setBackgroundResource(R.drawable.background_rounded_corners)
            statusIconContainer.background.setTint(
                statusIconContainer.context.compatColor(
                    R.color.light_gray
                )
            )
            discount.text = interpolateTextKey(
                discount.resources.getString(R.string.REFERRAL_INVITE_ACTIVE_VALUE),
                "REFERRAL_VALUE" to discountString
            )
            statusIcon.setImageDrawable(statusIcon.context.compatDrawable(R.drawable.ic_filled_checkmark))
        }

    private fun bindInProgress(viewHolder: ItemViewHolder, nameString: String?) = viewHolder.apply {
        setupAvatarWithLetter(this, nameString)

        name.text = nameString
        statusText.text = statusText.resources.getString(R.string.REFERRAL_INVITE_STARTEDSTATE)

        statusIcon.setImageDrawable(statusIcon.context.compatDrawable(R.drawable.ic_clock))
    }

    private fun bindNotInitiated(viewHolder: ItemViewHolder, quantity: Int) = viewHolder.apply {
        avatar.setImageDrawable(avatar.context.compatDrawable(R.drawable.ic_ghost))
        avatar.scaleType = ImageView.ScaleType.CENTER
        name.text = name.resources.getString(R.string.REFERRAL_INVITE_ANON)
        statusText.text = if (quantity <= 1) {
            statusText.resources.getString(R.string.REFERRAL_INVITE_OPENEDSTATE)
        } else {
            interpolateTextKey(
                statusText.resources.getString(R.string.REFERRAL_INVITE_OPENEDSTATE_MULTIPLE),
                "NUMBER_OF_INVITES" to quantity
            )
        }
        statusIcon.setImageDrawable(statusIcon.context.compatDrawable(R.drawable.ic_clock))
    }

    private fun bindTerminated(viewHolder: ItemViewHolder, nameString: String?) = viewHolder.apply {
        setupAvatarWithLetter(this, nameString)
        name.text = nameString
        statusText.text = statusText.resources.getString(R.string.REFERRAL_INVITE_QUITSTATE)
        statusIcon.setImageDrawable(statusIcon.context.compatDrawable(R.drawable.ic_cross))
    }

    private fun setupAvatarWithLetter(viewHolder: ItemViewHolder, name: String?) {
        viewHolder.apply {
            if (!name.isNullOrBlank()) {
                avatar.setImageDrawable(avatar.context.compatDrawable(R.drawable.sphere))
                val hashedColor = avatar.context.compatColor(hashColor(name))
                avatar.drawable.mutate().setTint(hashedColor)
                avatarLetter.text = name[0].toString().capitalize()
                avatarLetter.setTextColor(
                    avatarLetter.context.compatColor(
                        when (getLightness(hashedColor)) {
                            LightClass.DARK -> R.color.off_white
                            LightClass.LIGHT -> R.color.off_black_dark
                        }
                    )
                )
            }
        }
    }

    private fun shouldShowEmptyState() =
        data.invitations.isNullOrEmpty() && data.referredBy == null

    //TODO: Let's get the data from backend
    private fun calculateDiscount(): Int {
        var totalDiscount = 0
        (data.referredBy as? ProfileQuery.AsActiveReferral?)?.let {
            totalDiscount += it.discount.amount.toBigDecimal().toInt()
        }
        data.invitations?.filterIsInstance(ProfileQuery.AsActiveReferral1::class.java)
            ?.forEach { receiver -> totalDiscount += receiver.discount.amount.toBigDecimal().toInt() }
        return min(totalDiscount, monthlyCost)
    }

    //TODO: Let's get the data from backend
    private fun calculateInvitesLeftToFree(): Int {
        val amount = monthlyCost - calculateDiscount()
        val incentive =
            (data.campaign.incentive as? ProfileQuery.AsMonthlyCostDeduction)?.amount?.amount?.toBigDecimal()?.toDouble()
                ?: 0.0
        return ceil((amount / incentive)).toInt()
    }

    companion object {
        private const val HEADER = 0
        private const val INVITE_HEADER = 1
        private const val INVITE_ITEM = 2
        private const val REFERRED_BY_HEADER = 3
        private const val REFERRED_BY_ITEM = 4
        private const val UNKNOWN_ITEM = 5

        private const val PROGRESS_TANK_MAX_SEGMENTS = 20
    }

    inner class HeaderViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val progressTankView: ProgressTankView = view.discountView
        val referralProgressHighPremiumContainer: LinearLayout = view.referralProgressHighPremiumContainer
        val referralProgressHighPremiumDiscount: TextView = view.referralProgressHighPremiumDiscount
        val referralProgressHighPremiumCurrentPrice: TextView = view.referralProgressHighPremiumCurrentPrice
        val subtitle: TextView = view.subtitle
        val explainer: TextView = view.explainer
        val code: TextView = view.code
        val emptyStateRow: View = view.emptyStateRow

        val avatar: ImageView = view.avatar
        val name: TextView = view.name
        val statusText: TextView = view.statusText
        val statusIconContainer: LinearLayout = view.statusIconContainer

        val emptyStateTitle: TextView = view.referralSmallHeader
    }

    inner class SmallHeaderViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val headerTextView: TextView = view as TextView
    }

    inner class ItemViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.avatar
        val avatarLetter: TextView = view.avatarLetter
        val name: TextView = view.name
        val statusText: TextView = view.statusText
        val statusIconContainer: LinearLayout = view.statusIconContainer
        val discount: TextView = view.discount
        val statusIcon: ImageView = view.statusIcon
    }

    inner class EmptyViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}
