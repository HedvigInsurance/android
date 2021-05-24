package com.hedvig.app.feature.insurance.ui.detail.documents

import androidx.annotation.StringRes

data class DocumentsModel(
    @StringRes
    val label: Int,
    @StringRes
    val subtitle: Int,
    val url: String,
)
