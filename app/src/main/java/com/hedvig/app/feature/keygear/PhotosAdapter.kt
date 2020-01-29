package com.hedvig.app.feature.keygear

import android.Manifest
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hedvig.app.R
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.create_key_gear_item_new_photo.view.*
import kotlinx.android.synthetic.main.create_key_gear_item_photo.view.*

data class Photo(
    val uri: Uri
)

class PhotosAdapter(
    private val takePhoto: () -> Unit,
    private val requestPhotoPermissionsAndTakePhoto: () -> Unit
) : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {
    var photos: List<Photo> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            ADD_PHOTO -> ViewHolder.AddPhoto(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.create_key_gear_item_new_photo,
                    parent,
                    false
                )
            )
            PHOTO -> ViewHolder.Photo(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.create_key_gear_item_photo,
                    parent,
                    false
                )
            )
            else -> {
                throw Error("Unknown viewType $viewType")
            }
        }

    override fun getItemCount() = photos.size + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.AddPhoto -> holder.apply {
                root.setHapticClickListener {
                    if (root.context.hasPermissions(Manifest.permission.CAMERA)) {
                        takePhoto()
                    } else {
                        requestPhotoPermissionsAndTakePhoto()
                    }
                }
            }
            is ViewHolder.Photo -> holder.apply {
                val data = photos[position]
                Glide
                    .with(photo)
                    .load(data.uri)
                    .transform(CenterCrop(), RoundedCorners(8))
                    .into(photo)
            }
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
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class Photo(view: View) : ViewHolder(view) {
            val photo: ImageView = view.photo
        }

        class AddPhoto(view: View) : ViewHolder(view) {
            val root: ConstraintLayout = view.addPhoto
        }
    }
}
