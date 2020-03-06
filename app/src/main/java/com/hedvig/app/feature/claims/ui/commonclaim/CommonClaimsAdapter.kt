package com.hedvig.app.feature.claims.ui.commonclaim

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.R
import com.hedvig.app.util.apollo.ThemedIconUrls
import kotlinx.android.synthetic.main.claims_common_claim_cell.view.*

class CommonClaimsAdapter(
    private val commonClaims: List<CommonClaimQuery.CommonClaim>,
    private val requestBuilder: RequestBuilder<PictureDrawable>,
    private val baseUrl: String,
    private val navigateToCommonClaimFragment: (CommonClaimQuery.CommonClaim) -> Unit,
    private val navigateToEmergencyFragment: (CommonClaimQuery.CommonClaim) -> Unit
) : RecyclerView.Adapter<CommonClaimsAdapter.ViewHolder>() {

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

            commonClaim.layout.asEmergency?.let {
                view.setOnClickListener { navigateToEmergencyFragment.invoke(commonClaim) }
            }

            commonClaim.layout.asTitleAndBulletPoints?.let {
                view.setOnClickListener { navigateToCommonClaimFragment.invoke(commonClaim) }
            }

            requestBuilder.load(
                Uri.parse(
                    baseUrl + ThemedIconUrls.from(commonClaim.icon.variants.fragments.iconVariantsFragment).iconByTheme(
                        commonClaimIcon.context
                    )
                )
            ).into(commonClaimIcon)
            commonClaimLabel.text = commonClaim.title
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val view = itemView
        val commonClaimIcon: ImageView = itemView.commonClaimCellIcon
        val commonClaimLabel: TextView = itemView.commonClaimCellLabel
    }
}
