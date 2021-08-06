package com.hedvig.app.feature.keygear

import com.hedvig.app.feature.tracking.TrackingFacade

class KeyGearTracker(
    private val trackingFacade: TrackingFacade
) {
    fun createItem() = trackingFacade.track("KEY_GEAR_ADD_BUTTON")
    fun saveItem() = trackingFacade.track("KEY_GEAR_ADD_ITEM_SAVE_BUTTON")
    fun addPhoto() = trackingFacade.track("KEY_GEAR_ADD_ITEM_ADD_PHOTO_BUTTON")
    fun deletePhoto() = trackingFacade.track("KEY_GEAR_CREATE_DELETE_PHOTO")
    fun addPurchaseInfo() = trackingFacade.track("KEY_GEAR_ITEM_VIEW_VALUATION_EMPTY")
    fun valuationMoreInfo() = trackingFacade.track("KEY_GEAR_VALUATION_MORE_INFO")
    fun saveName() = trackingFacade.track("KEY_GEAR_ITEM_VIEW_ITEM_NAME_SAVE_BUTTON")
    fun editName() = trackingFacade.track("KEY_GEAR_ITEM_VIEW_ITEM_NAME_EDIT_BUTTON")
    fun showReceipt() = trackingFacade.track("KEY_GEAR_ITEM_VIEW_RECEIPT_SHOW")
    fun addReceipt() = trackingFacade.track("KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_ADD_BTN")
    fun shareReceipt() = trackingFacade.track("KEY_GEAR_RECCEIPT_VIEW_SHARE_BUTTON")
    fun downloadReceipt() = trackingFacade.track("KEY_GEAR_RECEIPT_DOWNLOAD")
}
