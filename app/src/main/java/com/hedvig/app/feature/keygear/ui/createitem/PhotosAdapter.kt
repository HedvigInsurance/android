package com.hedvig.app.feature.keygear.ui.createitem

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.R
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.view.performOnLongPressHapticFeedback
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.create_key_gear_item_new_photo.view.*
import kotlinx.android.synthetic.main.create_key_gear_item_photo.view.*

class PhotosAdapter(
    private val takePhoto: () -> Unit,
    private val requestPhotoPermissionsAndTakePhoto: () -> Unit,
    private val deletePhoto: (photo: Photo) -> Unit
) : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {
    var photos: List<Photo> = listOf()
        set(value) {
            val callback =
                PhotoDiffCallback(
                    field,
                    value
                )
            val result = DiffUtil.calculateDiff(callback)
            field = value
            result.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            ADD_PHOTO -> ViewHolder.AddPhoto(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.create_key_gear_item_new_photo,
                    parent,
                    false
                ).apply {
                    layoutParams =
                        ConstraintLayout.LayoutParams(
                            (parent.measuredWidth * ITEM_WIDTH).toInt(),
                            ITEM_HEIGHT.dp
                        )
                }
            )
            PHOTO -> ViewHolder.Photo(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.create_key_gear_item_photo,
                    parent,
                    false
                ).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            (parent.measuredWidth * ITEM_WIDTH).toInt(),
                            ITEM_HEIGHT.dp
                        )
                }
            )
            else -> {
                throw Error("Unknown viewType $viewType")
            }
        }

    override fun getItemCount() = photos.size + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.AddPhoto -> holder.bind(takePhoto, requestPhotoPermissionsAndTakePhoto)
            is ViewHolder.Photo -> holder.bind(photos[position], deletePhoto)
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        photos.isEmpty() -> ADD_PHOTO
        position == photos.size -> ADD_PHOTO
        else -> PHOTO
    }

    companion object {
        private const val PHOTO = 0
        private const val ADD_PHOTO = 1

        private const val ITEM_WIDTH = 0.90
        private const val ITEM_HEIGHT = 250
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class Photo(view: View) : ViewHolder(view) {
            val photo: ImageView = view.photo

            fun bind(
                data: com.hedvig.app.feature.keygear.ui.createitem.Photo,
                deletePhoto: (photo: com.hedvig.app.feature.keygear.ui.createitem.Photo) -> Unit
            ) {
                Glide
                    .with(photo)
                    .load(data.uri)
                    .transform(CenterCrop(), RoundedCorners(BASE_MARGIN))
                    .into(photo)

                photo.setOnCreateContextMenuListener { _, v, _ ->
                    v.performOnLongPressHapticFeedback()
                    val popup = PopupMenu(v.context, v)
                    popup.menuInflater.inflate(R.menu.create_key_gear_item_photo, popup.menu)
                    popup.setOnMenuItemClickListener { item ->
                        if (item.itemId == R.id.delete) {
                            deletePhoto(data)
                        }
                        true
                    }
                    popup.show()
                }
            }
        }

        class AddPhoto(view: View) : ViewHolder(view) {
            val root: ConstraintLayout = view.addPhoto

            fun bind(
                takePhoto: () -> Unit,
                requestPhotoPermissionsAndTakePhoto: () -> Unit
            ) {
                root.setHapticClickListener {
                    if (root.context.hasPermissions(Manifest.permission.CAMERA)) {
                        takePhoto()
                    } else {
                        requestPhotoPermissionsAndTakePhoto()
                    }
                }
            }
        }
    }
}

