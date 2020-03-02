package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import kotlinx.android.synthetic.main.market_item.view.*

class MarketAdapter :
    RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.market_item, parent, false)
    )

    override fun getItemCount() = 2

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            SV -> holder.apply {
                flag.setImageDrawable(flag.context.compatDrawable(R.drawable.ic_flag_no))
                //TODO textkey
                country.text = "Norige"
            }
            NO -> holder.apply {
                flag.setImageDrawable(flag.context.compatDrawable(R.drawable.ic_flag_se))
                //TODO textkey
                country.text = "Sverige"
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parent = view
        val flag: ImageView = view.flag
        val country: TextView = view.country
    }

    companion object {
        private const val SV = 0
        private const val NO = 1
    }
}
