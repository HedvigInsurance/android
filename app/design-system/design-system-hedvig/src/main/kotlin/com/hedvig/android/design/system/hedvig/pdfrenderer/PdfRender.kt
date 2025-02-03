package com.hedvig.android.design.system.hedvig.pdfrenderer

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


internal class PdfRender(
    private val fileDescriptor: ParcelFileDescriptor
) {
    private val pdfRenderer = PdfRenderer(fileDescriptor)
    val pageCount get() = pdfRenderer.pageCount
    private val mutex: Mutex = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val pageLists: List<Page> = List(pdfRenderer.pageCount) {
        Page(
            index = it,
            pdfRenderer = pdfRenderer,
            coroutineScope = coroutineScope,
            mutex = mutex
        )
    }

    fun close() {
        pageLists.forEach {
            it.recycle()
        }
        pdfRenderer.close()
        fileDescriptor.close()
    }

    class Page(
        val mutex: Mutex,
        val index: Int,
        val pdfRenderer: PdfRenderer,
        val coroutineScope: CoroutineScope
    ) {
        var isLoaded = false

        var job: Job? = null

        val dimension = pdfRenderer.openPage(index).use { currentPage ->
            Dimension(
                width = currentPage.width,
                height = currentPage.height
            )
        }

        fun heightByWidth(width: Int): Int {
            val ratio = dimension.width.toFloat() / dimension.height
            return (ratio * width).toInt()
        }

        val pageContent = MutableStateFlow<Bitmap?>(null)

        fun load() {
            if (!isLoaded) {
                job = coroutineScope.launch {
                    mutex.withLock {
                        val newBitmap: Bitmap
                        pdfRenderer.openPage(index).use { currentPage ->
                            newBitmap = createBlankBitmap(
                                width = currentPage.width,
                                height = currentPage.height
                            )
                            currentPage.render(
                                newBitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                        }
                        isLoaded = true
                        pageContent.emit(newBitmap)
                    }
                }
            }
        }

        fun recycle() {
            isLoaded = false
            val oldBitmap = pageContent.value
            pageContent.tryEmit(null)
            oldBitmap?.recycle()
        }

        private fun createBlankBitmap(
            width: Int,
            height: Int
        ): Bitmap {
            return createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            ).apply {
                val canvas = Canvas(this)
                canvas.drawColor(android.graphics.Color.WHITE)
                canvas.drawBitmap(this, 0f, 0f, null)
            }
        }
    }

    data class Dimension(
        val width: Int,
        val height: Int
    )
}
