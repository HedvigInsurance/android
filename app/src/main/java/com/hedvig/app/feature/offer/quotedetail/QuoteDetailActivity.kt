package com.hedvig.app.feature.offer.quotedetail

import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.perils.PerilsAdapter
import com.hedvig.app.util.extensions.toArrayList
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.Insetter
import e
import org.koin.android.ext.android.inject

class QuoteDetailActivity : BaseActivity(R.layout.quote_detail_activity) {
    private val binding by viewBinding(QuoteDetailActivityBinding::bind)
    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayName = intent.getStringExtra(QUOTE_DISPLAY_NAME)
        val perils: List<PerilItem>? = intent.getParcelableArrayListExtra<PerilItem.Peril>(PERILS)
        val insurableLimits: List<InsurableLimitItem>? =
            intent.getParcelableArrayListExtra<InsurableLimitItem.InsurableLimit>(INSURABLE_LIMITS)
        val documents = intent.getParcelableArrayListExtra<DocumentItems.Document>(DOCUMENTS)

        if (displayName == null || perils == null || insurableLimits == null || documents == null) {
            e { "Programmer error: PERILS/INSURABLE_LIMITS/DOCUMENTS not provided to ${this.javaClass.name}" }
            return
        }

        with(binding) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            Insetter.builder()
                .setOnApplyInsetsListener { view, insets, initialState ->
                    view.updatePadding(
                        top = initialState.paddings.top + insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                    )
                }
                .applyToView(toolbar)
            toolbar.title = displayName
            toolbar.setNavigationOnClickListener { finish() }

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
            Insetter
                .builder()
                .setOnApplyInsetsListener { view, insets, initialState ->
                    view.updatePadding(
                        bottom = initialState.paddings.bottom +
                            insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                    )
                }
                .applyToView(recycler)
        }
    }

    companion object {
        private const val QUOTE_DISPLAY_NAME = "QUOTE_DISPLAY_NAME"
        private const val PERILS = "PERILS"
        private const val INSURABLE_LIMITS = "INSURABLE_LIMITS"
        private const val DOCUMENTS = "DOCUMENTS"
        fun newInstance(
            context: Context,
            quoteDetailItems: OfferViewModel.QuoteDetailItems,
        ) = Intent(context, QuoteDetailActivity::class.java).apply {
            putExtra(QUOTE_DISPLAY_NAME, quoteDetailItems.displayName)
            putParcelableArrayListExtra(PERILS, quoteDetailItems.perils.toArrayList())
            putParcelableArrayListExtra(INSURABLE_LIMITS, quoteDetailItems.insurableLimits.toArrayList())
            putParcelableArrayListExtra(DOCUMENTS, quoteDetailItems.documents.toArrayList())
        }
    }
}
