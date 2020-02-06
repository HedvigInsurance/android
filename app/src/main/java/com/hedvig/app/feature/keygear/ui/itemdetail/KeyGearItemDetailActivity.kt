package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import com.hedvig.app.BaseActivity
import com.hedvig.app.R

// TODO: Exit animation. Should be a shared element transition to the newly created item on the tab screen
class KeyGearItemDetailActivity : BaseActivity(R.layout.activity_key_gear_item_detail) {

    companion object {
        fun newInstance(context: Context) = Intent(context, KeyGearItemDetailActivity::class.java)
    }
}
