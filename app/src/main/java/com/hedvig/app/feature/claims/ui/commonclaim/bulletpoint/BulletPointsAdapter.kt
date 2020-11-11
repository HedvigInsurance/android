package com.hedvig.app.feature.claims.ui.commonclaim.bulletpoint

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.R
import com.hedvig.app.databinding.ClaimBulletpointRowBinding
import com.hedvig.app.feature.claims.ui.commonclaim.BulletPoint
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class BulletPointsAdapter(
    private val baseUrl: String,
    private val requestBuilder: RequestBuilder<PictureDrawable>
) : ListAdapter<BulletPoint, BulletPointsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position), baseUrl, requestBuilder)
        viewHolder.apply {
        }
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.claim_bulletpoint_row)) {
        private val binding by viewBinding(ClaimBulletpointRowBinding::bind)
        fun bind(
            item: BulletPoint,
            baseUrl: String,
            requestBuilder: RequestBuilder<PictureDrawable>
        ) {
            binding.apply {
                requestBuilder.load(Uri.parse(baseUrl + item.iconUrls.iconByTheme(bulletPointIcon.context)))
                    .into(bulletPointIcon)
                bulletPointTitle.text = item.title
                bulletPointDescription.text = item.description
            }
        }
    }
}
