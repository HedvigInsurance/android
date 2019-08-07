package com.hedvig.app.feature.claims.ui.commonclaim.bulletpoint

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.commonclaim.BulletPoint
import kotlinx.android.synthetic.main.claim_bulletpoint_row.view.*

class BulletPointsAdapter(
    private val bulletPoints: List<BulletPoint>,
    private val baseUrl: String,
    private val requestBuilder: RequestBuilder<PictureDrawable>
) : RecyclerView.Adapter<BulletPointsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.claim_bulletpoint_row,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = bulletPoints.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val bulletPoint = bulletPoints[position]
        viewHolder.apply {
            requestBuilder.load(Uri.parse(baseUrl + bulletPoint.iconUrl)).into(bulletPointIcon)
            bulletPointTitle.text = bulletPoint.title
            bulletPointDescription.text = bulletPoint.description
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bulletPointIcon: ImageView = itemView.bulletPointIcon
        val bulletPointTitle: TextView = itemView.bulletPointTitle
        val bulletPointDescription: TextView = itemView.bulletPointDescription
    }
}
