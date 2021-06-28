package com.hedvig.app.feature.offer.quotedetail

import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestBuilder
import com.carousell.concatadapterextension.ConcatItemDecoration
import com.carousell.concatadapterextension.ConcatSpanSizeLookup
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.QuoteDetailActivityBinding
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.insurablelimits.InsurableLimitsAdapter
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.perils.PerilsAdapter
import com.hedvig.app.util.extensions.toArrayList
import com.hedvig.app.util.extensions.viewBinding
import e
import org.koin.android.ext.android.inject

class QuoteDetailActivity : BaseActivity(R.layout.quote_detail_activity) {
    private val binding by viewBinding(QuoteDetailActivityBinding::bind)
    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val perils: List<PerilItem>? = intent.getParcelableArrayListExtra<PerilItem.Peril>(PERILS)
        val insurableLimits: List<InsurableLimitItem>? =
            intent.getParcelableArrayListExtra<InsurableLimitItem.InsurableLimit>(INSURABLE_LIMITS)
        val documents = intent.getParcelableArrayListExtra<DocumentItems.Document>(DOCUMENTS)

        if (perils == null || insurableLimits == null || documents == null) {
            e { "Programmer error: PERILS/INSURABLE_LIMITS/DOCUMENTS not provided to ${this.javaClass.name}" }
            return
        }

        with(binding) {
            val perilAdapter = PerilsAdapter(
                requestBuilder = requestBuilder,
                fragmentManager = supportFragmentManager,
            ).also { it.submitList(perils) }

            val insurableLimitAdapter = InsurableLimitsAdapter(
                fragmentManager = supportFragmentManager
            ).also { it.submitList(listOf(InsurableLimitItem.Header.Details) + insurableLimits) }

            val documentAdapter = DocumentAdapter {}
                .also {
                    it.submitList(
                        listOf(
                            DocumentItems.Header(R.string.OFFER_DOCUMENTS_SECTION_TITLE)
                        ) + documents
                    )
                }

            val concatAdapter = ConcatAdapter(perilAdapter, insurableLimitAdapter, documentAdapter)
            recycler.adapter = concatAdapter
            recycler.addItemDecoration(ConcatItemDecoration { concatAdapter.adapters })
            (recycler.layoutManager as? GridLayoutManager)?.let { gridLayoutManager ->
                gridLayoutManager.spanSizeLookup =
                    ConcatSpanSizeLookup(gridLayoutManager.spanCount) { concatAdapter.adapters }
            }
        }
    }

    companion object {
        private const val PERILS = "PERILS"
        private const val INSURABLE_LIMITS = "INSURABLE_LIMITS"
        private const val DOCUMENTS = "DOCUMENTS"
        fun newInstance(
            context: Context,
            perils: List<PerilItem>,
            insurableLimits: List<InsurableLimitItem.InsurableLimit>,
            documents: List<DocumentItems.Document>,
        ) = Intent(context, QuoteDetailActivity::class.java).apply {
            putParcelableArrayListExtra(PERILS, perils.toArrayList())
            putParcelableArrayListExtra(INSURABLE_LIMITS, insurableLimits.toArrayList())
            putParcelableArrayListExtra(DOCUMENTS, documents.toArrayList())
        }
    }
}
