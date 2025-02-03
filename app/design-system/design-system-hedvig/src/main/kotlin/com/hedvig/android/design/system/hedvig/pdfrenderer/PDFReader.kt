package com.hedvig.android.design.system.hedvig.pdfrenderer

import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun PDFReaderFirstPage(
  file: File,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    val pdfRender = PdfRender(
      fileDescriptor = ParcelFileDescriptor.open(
        file,
        ParcelFileDescriptor.MODE_READ_ONLY,
      ),
    )
    DisposableEffect(key1 = Unit) {
      onDispose {
        pdfRender.close()
      }
    }
    val page = pdfRender.pageLists[0]
    page.pageContent.collectAsState().value?.asImageBitmap()?.let {
      Image(
        bitmap = it,
        contentDescription = "Pdf",
        modifier = modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth,
      )
    }
  }
}


@Composable
fun PDFReader(
  file: File,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
  ) {
    val pdfRender = PdfRender(
      fileDescriptor = ParcelFileDescriptor.open(
        file,
        ParcelFileDescriptor.MODE_READ_ONLY,
      ),
    )
    DisposableEffect(key1 = Unit) {
      onDispose {
        pdfRender.close()
      }
    }
        LazyColumn {
      items(count = pdfRender.pageCount) { index ->
        BoxWithConstraints(
          modifier = Modifier.fillMaxWidth()
        ) {
          val page = pdfRender.pageLists[index]
          DisposableEffect(key1 = Unit) {
            page.load()
            onDispose {
              page.recycle()
            }
          }
          page.pageContent.collectAsState().value?.asImageBitmap()?.let {
            Image(
              bitmap = it,
              contentDescription = "Pdf page number: $index",
              modifier = Modifier.fillMaxWidth(),
              contentScale = ContentScale.FillWidth
            )
          } ?: Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(
                page
                  .heightByWidth(this.constraints.maxWidth)
                  .dp
              )
          )
        }
      }
    }
  }
}
