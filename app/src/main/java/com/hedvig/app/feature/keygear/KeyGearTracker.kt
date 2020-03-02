package com.hedvig.app.feature.keygear

import com.google.firebase.analytics.FirebaseAnalytics

class KeyGearTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun createItem() = firebaseAnalytics.logEvent("KEY_GEAR_ADD_BUTTON", null)
    fun openItem() = firebaseAnalytics.logEvent("key_gear_view_item", null)
    fun saveItem() = firebaseAnalytics.logEvent("KEY_GEAR_ADD_ITEM_SAVE_BUTTON", null)
    fun addPhoto() = firebaseAnalytics.logEvent("KEY_GEAR_ADD_ITEM_ADD_PHOTO_BUTTON", null)
    fun openPhotoContextMenu() = firebaseAnalytics.logEvent("key_gear_create_photo_context_menu_open", null)
    fun deletePhoto() = firebaseAnalytics.logEvent("KEY_GEAR_CREATE_DELETE_PHOTO", null)
    fun addPurchaseInfo() = firebaseAnalytics.logEvent("KEY_GEAR_ITEM_VIEW_VALUATION_EMPTY", null)
    fun valuationMoreInfo() = firebaseAnalytics.logEvent("KEY_GEAR_VALUATION_MORE_INFO", null)
    fun saveName() = firebaseAnalytics.logEvent("KEY_GEAR_ITEM_VIEW_ITEM_NAME_SAVE_BUTTON", null)
    fun editName() = firebaseAnalytics.logEvent("KEY_GEAR_ITEM_VIEW_ITEM_NAME_EDIT_BUTTON", null)
    fun showReceipt() = firebaseAnalytics.logEvent("KEY_GEAR_ITEM_VIEW_RECEIPT_SHOW", null)
    fun addReceipt() = firebaseAnalytics.logEvent("KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_ADD_BTN", null)
    fun shareReceipt() = firebaseAnalytics.logEvent("KEY_GEAR_RECCEIPT_VIEW_SHARE_BUTTON", null)
    fun downloadReceipt() = firebaseAnalytics.logEvent("KEY_GEAR_RECEIPT_DOWNLOAD", null)
    fun addDate() = firebaseAnalytics.logEvent("key_gear_valuation_add_date", null)
    fun saveValuation() = firebaseAnalytics.logEvent("key_gear_save_valuation", null)
}
