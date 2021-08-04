package com.hedvig.app.feature.keygear.ui

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityReceiptBinding
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.service.FileService
import com.hedvig.app.util.extensions.askForPermissions
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream

class ReceiptActivity : BaseActivity(R.layout.activity_receipt) {
    private val binding by viewBinding(ActivityReceiptBinding::bind)
    private val fileService: FileService by inject()
    private val tracker: KeyGearTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            topBar.applyStatusBarInsets()

            val fileUrl = intent.getStringExtra(RECEIPT_URL)
            if (fileUrl == null) {
                e { "Programmer error: No file url passed to ${this.javaClass}" }
                return
            }

            close.setHapticClickListener {
                onBackPressed()
            }

            share.setHapticClickListener {
                tracker.shareReceipt()
                shareImage(fileUrl)
            }

            download.setHapticClickListener {
                tracker.downloadReceipt()
                if (hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    downloadFile(fileUrl)
                } else {
                    askForPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        EXTERNAL_STORAGE_REQUEST_CODE
                    )
                }
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
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val fileUrl = intent.getStringExtra(RECEIPT_URL)
            if (fileUrl == null) {
                e { "Programmer error: No file url passed to ${this.javaClass}" }
                return
            }
            downloadFile(fileUrl)
        }
    }

    private fun loadImage(fileUrl: String) {
        Glide.with(this)
            .load(fileUrl)
            .transform(CenterCrop())
            .into(binding.receipt)
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
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileService.getFileName(uri)
            )
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        (getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager)?.enqueue(request)

        Snackbar
            .make(
                binding.root,
                getString(
                    R.string.KEY_GEAR_RECEIPT_DOWNLOAD_SNACKBAR,
                    filename ?: getString(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_TITLE)
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
                e(e) { "Error saving image" }
                null
            }
        }

    companion object {
        private const val RECEIPT_URL = "RECEIPT_URL"

        private const val EXTERNAL_STORAGE_REQUEST_CODE = 6457

        fun newInstance(context: Context, receiptUrl: String) =
            Intent(context, ReceiptActivity::class.java).apply {
                putExtra(RECEIPT_URL, receiptUrl)
            }

        private fun appearsToBeAnImage(url: String): Boolean {
            return try {
                when (
                    Uri.parse(url).buildUpon().clearQuery().build().toString().substringAfterLast(
                        '.',
                        ""
                    )
                ) {
                    "jpg", "jpeg", "png" -> true
                    else -> false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}
