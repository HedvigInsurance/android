package com.hedvig.app.feature.keygear.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.service.FileService
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.useEdgeToEdge
import com.hedvig.app.util.interpolateTextKey
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import e
import kotlinx.android.synthetic.main.activity_receipt.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class ReceiptActivity : BaseActivity(R.layout.activity_receipt) {
    private val fileService: FileService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.useEdgeToEdge()

        topBar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = insets.systemWindowInsetTop + initialState.paddings.top)
        }

        val fileUrl = intent.getStringExtra(RECEIPT_URL)

        close.setHapticClickListener {
            onBackPressed()
        }

        share.setHapticClickListener {
            shareImage(fileUrl)
        }

        download.setHapticClickListener {
            downloadFile(fileUrl)
        }

        if (appearsToBeAnImage(fileUrl)) {
            receipt.show()
            loadImage(fileUrl)
        } else {
            share.remove()
            fileIcon.show()
            download.show()
        }
    }

    private fun loadImage(fileUrl: String) {
        Glide.with(this)
            .load(fileUrl)
            .transform(CenterCrop())
            .into(receipt)
    }

    private fun shareImage(fileUrl: String) {
        Glide.with(this)
            .asBitmap()
            .load(fileUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    lifecycleScope.launch {
                        val filePath = saveImage(resource)
                        if (filePath == null) {
                            e { "Failed to save image to temp file" }
                            return@launch
                        }

                        withContext(Dispatchers.Main) {
                            val sendIntent = Intent().apply {

                                action = Intent.ACTION_SEND
                                type = "image/jpg"

                                this.data = FileProvider.getUriForFile(
                                    applicationContext,
                                    getString(R.string.file_provider_authority),
                                    File(filePath)
                                )

                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                putExtra(
                                    Intent.EXTRA_STREAM,
                                    FileProvider.getUriForFile(
                                        applicationContext,
                                        getString(R.string.file_provider_authority),
                                        File(filePath)
                                    )
                                )
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                        }
                    }
                }
            })
    }

    private fun downloadFile(fileUrl: String) {
        val uri = Uri.parse(fileUrl)
        val filename = fileService.getFileName(uri)
        val request = DownloadManager.Request(uri).apply {
            setDescription(getString(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_TITLE))
            setTitle(fileService.getFileName(uri))
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileService.getFileName(uri))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        (getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager)?.enqueue(request)

        Snackbar
            .make(
                root,
                interpolateTextKey(
                    getString(R.string.KEY_GEAR_RECEIPT_DOWNLOAD_SNACKBAR),
                    "FILENAME" to (
                        filename ?: getString(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_TITLE)
                        )
                ),
                Snackbar.LENGTH_LONG
            )
            .show()
    }

    suspend fun saveImage(finalBitmap: Bitmap): String? =
        withContext(Dispatchers.IO) {
            val root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
            val myDir = File(root)
            myDir.mkdirs()

            val fName = "Image-receipt.jpg"
            val file = File(myDir, fName)

            if (file.exists()) file.delete()

            return@withContext try {
                val out = FileOutputStream(file)
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                file.absolutePath
            } catch (e: Exception) {
                Timber.e(e, "Error saving image")
                null
            }
        }

    companion object {
        private const val RECEIPT_URL = "RECEIPT_URL"

        fun newInstance(context: Context, receiptUrl: String) =
            Intent(context, ReceiptActivity::class.java).apply {
                putExtra(RECEIPT_URL, receiptUrl)
            }

        private fun appearsToBeAnImage(url: String): Boolean {
            return try {
                when (Uri.parse(url).toString().substringAfterLast('.', "")) {
                    "jpg", "jpeg", "png" -> true
                    else -> false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}
