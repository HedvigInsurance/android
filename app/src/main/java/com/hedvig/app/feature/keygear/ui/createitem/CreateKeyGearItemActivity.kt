package com.hedvig.app.feature.keygear.ui.createitem

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import androidx.core.content.FileProvider
import androidx.core.view.doOnNextLayout
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.recyclerview.widget.PagerSnapHelper
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.ui.animator.SlideInItemAnimator
import com.hedvig.app.ui.decoration.CenterItemDecoration
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.askForPermissions
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.activity_create_key_gear_item.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.IOException

class CreateKeyGearItemActivity : BaseActivity(R.layout.activity_create_key_gear_item) {
    private val model: CreateKeyGearViewModel by viewModel()

    private lateinit var tempPhotoPath: String
    private var dirty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportPostponeEnterTransition()

        setupLargeTitle(
            R.string.KEY_GEAR_ADD_ITEM_PAGE_TITLE,
            R.font.circular_bold,
            R.drawable.ic_back,
            backAction = this::onBackPressed
        )

        photos.adapter =
            PhotosAdapter(
                { takePhoto() },
                {
                    askForPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        PHOTO_PERMISSION_REQUEST_CODE
                    )
                },
                model::deletePhoto
            )
        photos.addItemDecoration(CenterItemDecoration())
        photos.itemAnimator = SlideInItemAnimator(Gravity.START)
        PagerSnapHelper().attachToRecyclerView(photos)

        categories.adapter = CategoryAdapter(
            model::setActiveCategory
        )

        categories.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF.dp))
        categories.doOnNextLayout {
            supportStartPostponedEnterTransition()
        }

        save.setHapticClickListener {
            makeToast("TODO: Save item, animate, show Item Detail Screen")
        }

        model.photos.observe(this) { photos ->
            photos?.let { bind(it) }
        }

        model.categories.observe(this) { categories ->
            categories?.let { bindCategories(it) }
        }

        model.dirty.observe(this) { d ->
            d?.let { dirty = it }
        }
    }

    private fun bind(data: List<Photo>) {
        (photos.adapter as? PhotosAdapter)?.photos = data
        photos.scrollToPosition(data.size - 1)
    }

    private fun bindCategories(data: List<Category>) {
        (categories.adapter as? CategoryAdapter)?.categories = data

        if (data.any { c -> c.selected }) {
            save.show()
            save
                .spring(SpringAnimation.TRANSLATION_Y)
                .animateToFinalPosition(0f)
        }
    }

    private fun takePhoto() {
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: run {
                Timber.e("Could not getExternalFilesDir")
                return
            }

        try {
            File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                storageDir
            )
        } catch (ex: IOException) {
            Timber.e("Error occurred while creating the photo file")
            null
        }?.let { tempPhotoFile ->
            tempPhotoPath = tempPhotoFile.absolutePath
            startActivityForResult(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                    intent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(
                            this,
                            getString(R.string.file_provider_authority),
                            tempPhotoFile
                        )
                    )
                },
                PHOTO_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            model.addPhotoUri(
                FileProvider.getUriForFile(
                    this,
                    getString(R.string.file_provider_authority),
                    File(tempPhotoPath)
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PHOTO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                takePhoto()
            }
        }
    }

    override fun onBackPressed() {
        if (dirty) {
            makeToast("Should show an alert allowing user to verify that they want to discard data")
            showAlert(
                R.string.KEY_GEAR_ADD_ITEM_PAGE_CLOSE_ALERT_TITLE,
                R.string.KEY_GEAR_ADD_ITEM_PAGE_CLOSE_ALERT_BODY,
                R.string.KEY_GEAR_ADD_ITEM_PAGE_CLOSE_ALERT_DISMISS_BUTTON,
                R.string.KEY_GEAR_ADD_ITEM_PAGE_CLOSE_ALERT_CONTINUE_BUTTON,
                positiveAction = {},
                negativeAction = {
                    super.onBackPressed()
                }
            )
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val PHOTO_REQUEST_CODE = 9876
        private const val PHOTO_PERMISSION_REQUEST_CODE = 9875

        fun newInstance(context: Context) = Intent(context, CreateKeyGearItemActivity::class.java)
    }
}
