package com.hedvig.app.feature.keygear.ui.createitem

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnNextLayout
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.recyclerview.widget.PagerSnapHelper
import com.hedvig.android.owldroid.graphql.CreateKeyGearItemMutation
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.BASE_MARGIN_TRIPLE
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityCreateKeyGearItemBinding
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailActivity
import com.hedvig.app.ui.animator.SlideInItemAnimator
import com.hedvig.app.ui.decoration.CenterItemDecoration
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.askForPermissions
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.doOnEnd
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.applyNavigationBarInsetsMargin
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.centerX
import com.hedvig.app.util.extensions.view.centerY
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.spring
import e
import java.io.File
import java.io.IOException
import kotlin.math.max
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateKeyGearItemActivity : BaseActivity(R.layout.activity_create_key_gear_item) {
    private val model: CreateKeyGearItemViewModel by viewModel()
    private val binding by viewBinding(ActivityCreateKeyGearItemBinding::bind)
    private val tracker: KeyGearTracker by inject()

    private lateinit var tempPhotoPath: String
    private var dirty = false
    private var isShowingPostCreateAnimation = false
    private var isUploading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportPostponeEnterTransition()
        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            topBar.applyStatusBarInsets()
            saveContainer.applyNavigationBarInsetsMargin()

            topBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            scrollViewContent.applyStatusBarInsets()
            topBar.applyStatusBarInsets()
            saveContainer.applyNavigationBarInsetsMargin()

            photos.adapter =
                PhotosAdapter(
                    tracker,
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
            photos.doOnNextLayout {
                supportStartPostponedEnterTransition()
            }

            categories.adapter = CategoryAdapter(
                model::setActiveCategory
            )
            categories.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))

            close.setHapticClickListener {
                onBackPressed()
            }

            save.setHapticClickListener {
                tracker.saveItem()
                if (isUploading) {
                    return@setHapticClickListener
                }
                isUploading = true
                transitionToUploading()
                model.createItem()
            }
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

        model.createResult.observe(this) { cr ->
            cr?.let { showCreatedAnimation(it) }
        }
    }

    private fun transitionToUploading() {
        binding.apply {
            loadingIndicator.show()
            ValueAnimator.ofInt(saveContainer.width, saveContainer.height).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = SAVE_BUTTON_TRANSITION_DURATION
                addUpdateListener { va ->
                    saveContainer.updateLayoutParams<FrameLayout.LayoutParams> {
                        width = va.animatedValue as Int
                    }
                    save.alpha = 1 - va.animatedFraction
                    loadingIndicator.alpha = va.animatedFraction
                }
                start()
            }
        }
    }

    private fun bind(data: List<Photo>) {
        (binding.photos.adapter as? PhotosAdapter)?.photos = data
        binding.photos.scrollToPosition(data.size - 1)
    }

    private fun bindCategories(data: List<Category>) {
        binding.apply {
            (categories.adapter as? CategoryAdapter)?.submitList(data)

            if (data.any { c -> c.selected }) {
                saveContainer.show()
                saveContainer
                    .spring(SpringAnimation.TRANSLATION_Y)
                    .animateToFinalPosition(0f)
            }
        }
    }

    private fun showCreatedAnimation(data: CreateKeyGearItemMutation.Data) {
        binding.apply {
            isShowingPostCreateAnimation = true
            postCreate.show()

            val finalRadius = max(root.width, root.height).toFloat() * 1.1f
            ViewAnimationUtils.createCircularReveal(
                postCreate,
                saveContainer.centerX,
                saveContainer.centerY,
                0f,
                finalRadius
            ).apply {
                duration = POST_CREATE_REVEAL_DURATION
                interpolator = AccelerateDecelerateInterpolator()
                doOnEnd {
                    val category = data.createKeyGearItem.fragments.keyGearItemFragment.category
                    scrollView.remove()

                    createdLabel.show()
                    createdLabel.text =
                        getString(R.string.KEY_GEAR_ADD_ITEM_SUCCESS, getString(category.label))

                    createdIllustration.show()
                    createdIllustration.setImageResource(category.illustration)
                    createdIllustration
                        .animate()
                        .alpha(1f)
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(1000)
                        .start()
                    createdCheckmark.show()
                    createdCheckmark
                        .animate()
                        .alpha(1f)
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(1000)
                        .withEndAction {
                            finish()
                            startActivity(
                                KeyGearItemDetailActivity.newInstance(
                                    this@CreateKeyGearItemActivity,
                                    data.createKeyGearItem.fragments.keyGearItemFragment
                                ),
                                ActivityOptionsCompat.makeCustomAnimation(
                                    this@CreateKeyGearItemActivity,
                                    0,
                                    R.anim.fade_out
                                ).toBundle()
                            )
                        }
                        .start()

                    Handler(mainLooper).postDelayed(
                        {
                            createdLabel.spring(SpringAnimation.TRANSLATION_Y)
                                .addUpdateListener { _, value, _ ->
                                    createdLabel.alpha = 1 - (value / BASE_MARGIN_TRIPLE)
                                }
                                .animateToFinalPosition(0f)
                        },
                        POST_CREATE_LABEL_REVEAL_DELAY
                    )
                }
                start()
            }
        }
    }

    private fun takePhoto() {
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: run {
                e { "Could not getExternalFilesDir" }
                return
            }

        try {
            File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                storageDir
            )
        } catch (ex: IOException) {
            e { "Error occurred while creating the photo file" }
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
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onBackPressed() {
        if (isShowingPostCreateAnimation) {
            return
        }
        if (dirty) {
            showAlert(
                R.string.KEY_GEAR_ADD_ITEM_PAGE_CLOSE_ALERT_TITLE,
                R.string.KEY_GEAR_ADD_ITEM_PAGE_CLOSE_ALERT_BODY,
                R.string.KEY_GEAR_ADD_ITEM_PAGE_CLOSE_ALERT_CONTINUE_BUTTON,
                R.string.KEY_GEAR_ADD_ITEM_PAGE_CLOSE_ALERT_DISMISS_BUTTON,
                positiveAction = {
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

        private const val POST_CREATE_REVEAL_DURATION = 400L
        private const val POST_CREATE_LABEL_REVEAL_DELAY = 150L

        private const val SAVE_BUTTON_TRANSITION_DURATION = 200L

        fun newInstance(context: Context) = Intent(context, CreateKeyGearItemActivity::class.java)
    }
}
