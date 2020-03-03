package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.spring
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
                selectMarket(holder, position)
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
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(v.context)
            when (position) {
                SV -> {
                    model.updateMarket(Market.SV)
                    sharedPreferences.edit().putInt(Market.MARKET_SHARED_PREF, Market.SV.ordinal)
                        .commit()
                }
                NO -> {
                    model.updateMarket(Market.NO)
                    sharedPreferences.edit().putInt(Market.MARKET_SHARED_PREF, Market.NO.ordinal)
                        .commit()
                }
            }
            selectMarket(holder, position)
        }
    }

    private fun selectMarket(holder: ViewHolder, position: Int) {
        val rb = holder.parent.radioButton as RadioButton
        rb.isChecked = true
        rb.background = rb.context.getDrawable(R.drawable.ic_radiob_button_checked)
        animateRadioButton(holder)
        if (rb.isChecked) {
            if (lastChecked != null) {
                //TODO fixa !!
                if (lastCheckedPos != position) {
                    lastChecked!!.background =
                        rb.context.getDrawable(R.drawable.ic_radio_button_unchecked)
                    lastChecked!!.isChecked = false
                    animateRadioButton(holder)
                }
            }
            lastChecked = rb
            lastCheckedPos = position
        } else lastChecked = null
    }

    private fun animateRadioButton(holder: ViewHolder) {
        val button = holder.button
        button.scaleX = 0f
        button.scaleY = 0f
        button.spring(
            SpringAnimation.SCALE_X,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(1f)
        button.spring(
            SpringAnimation.SCALE_Y,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(1f)
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
