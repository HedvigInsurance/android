package com.hedvig.app.feature.claims.ui.commonclaim

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.R
import kotlinx.android.synthetic.main.claims_common_claim_cell.view.*
import org.jetbrains.annotations.NotNull
import timber.log.Timber

class CommonClaimsAdapter(
    private val commonClaims: @NotNull MutableList<CommonClaimQuery.CommonClaim>,
    private val requestBuilder: RequestBuilder<PictureDrawable>,
    private val baseUrl: String,
    private val navigateToCommonClaimFragment: (CommonClaimQuery.CommonClaim) -> Unit,
    private val navigateToEmergencyFragment: (CommonClaimQuery.CommonClaim) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<CommonClaimsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.claims_common_claim_cell,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = commonClaims.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.apply {
            val commonClaim = commonClaims[position]

            when (commonClaim.layout) {
                is CommonClaimQuery.AsTitleAndBulletPoints ->
                    view.setOnClickListener { navigateToCommonClaimFragment.invoke(commonClaim) }
                is CommonClaimQuery.AsEmergency ->
                    view.setOnClickListener { navigateToEmergencyFragment.invoke(commonClaim) }
                else ->
                    view.setOnClickListener { Timber.i("Not a recognized view") }
            }

            requestBuilder.load(Uri.parse(baseUrl + commonClaim.icon.svgUrl)).into(commonClaimIcon)
            commonClaimLabel.text = commonClaim.title
        }
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val view = itemView
        val commonClaimIcon: ImageView = itemView.commonClaimCellIcon
        val commonClaimLabel: TextView = itemView.commonClaimCellLabel
    }
}
