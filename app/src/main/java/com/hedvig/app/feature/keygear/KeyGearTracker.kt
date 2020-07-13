package com.hedvig.app.feature.keygear

import com.mixpanel.android.mpmetrics.MixpanelAPI

class KeyGearTracker(
    private val mixpanel: MixpanelAPI
) {
    fun createItem() = mixpanel.track("KEY_GEAR_ADD_BUTTON")
    fun saveItem() = mixpanel.track("KEY_GEAR_ADD_ITEM_SAVE_BUTTON")
    fun addPhoto() = mixpanel.track("KEY_GEAR_ADD_ITEM_ADD_PHOTO_BUTTON")
    fun deletePhoto() = mixpanel.track("KEY_GEAR_CREATE_DELETE_PHOTO")
    fun addPurchaseInfo() = mixpanel.track("KEY_GEAR_ITEM_VIEW_VALUATION_EMPTY")
    fun valuationMoreInfo() = mixpanel.track("KEY_GEAR_VALUATION_MORE_INFO")
    fun saveName() = mixpanel.track("KEY_GEAR_ITEM_VIEW_ITEM_NAME_SAVE_BUTTON")
    fun editName() = mixpanel.track("KEY_GEAR_ITEM_VIEW_ITEM_NAME_EDIT_BUTTON")
    fun showReceipt() = mixpanel.track("KEY_GEAR_ITEM_VIEW_RECEIPT_SHOW")
    fun addReceipt() = mixpanel.track("KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_ADD_BTN")
    fun shareReceipt() = mixpanel.track("KEY_GEAR_RECCEIPT_VIEW_SHARE_BUTTON")
    fun downloadReceipt() = mixpanel.track("KEY_GEAR_RECEIPT_DOWNLOAD")
}
