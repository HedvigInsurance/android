package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.market_item.view.*

class MarketAdapter(private val model: MarketPickerViewModel) :
    RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

    private var lastChecked: RadioButton? = null
    private var lastCheckedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.market_item, parent, false)
    )

    override fun getItemCount() = 2

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            SV -> holder.apply {
                flag.setImageDrawable(flag.context.compatDrawable(R.drawable.ic_flag_se))
                //TODO textkey
                country.text = "Sverige"
            }
            NO -> holder.apply {
                flag.setImageDrawable(flag.context.compatDrawable(R.drawable.ic_flag_no))
                //TODO textkey
                country.text = "Norge"
            }
        }

        if (position == 0 && holder.button.isChecked) {
            lastChecked = holder.button
            lastCheckedPos = 0
        }

        holder.parent.setHapticClickListener { v ->
            when (position) {
                SV -> model.updateMarket(Country.SV)
                NO -> model.updateMarket(Country.NO)
            }
            val rb = v.radioButton as RadioButton
            rb.isChecked = true
            if (rb.isChecked) {
                if (lastChecked != null) {
                    //TODO fixa !!
                    if (lastCheckedPos != position) {
                        lastChecked!!.isChecked = false
                    }
                }
                lastChecked = rb
                lastCheckedPos = position
            } else lastChecked = null
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parent = view
        val flag: ImageView = view.flag
        val country: TextView = view.country
        val button: RadioButton = view.radioButton
    }

    companion object {
        private const val SV = 0
        private const val NO = 1
    }
}
