package com.hedvig.app.viewgallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R

class ViewGalleryBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.view_gallery_bottom_sheet, container, false)

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, TAG)

    companion object {
        const val TAG = "ViewGalleryBottomSheet"
        fun newInstance() = ViewGalleryBottomSheet()
    }
}
