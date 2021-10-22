package com.hedvig.app.feature.offer.quotedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import com.carousell.concatadapterextension.ConcatItemDecoration
import com.carousell.concatadapterextension.ConcatSpanSizeLookup
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.QuoteDetailActivityBinding
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.detail.handleAction
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.insurablelimits.InsurableLimitsAdapter
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.perils.PerilsAdapter
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.toArrayList
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.Insetter
import e
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.android.inject

class QuoteDetailActivity : BaseActivity(R.layout.quote_detail_activity) {
    private val binding by viewBinding(QuoteDetailActivityBinding::bind)
    private val imageLoader: ImageLoader by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra(TITLE)
        val perils: List<PerilItem>? = intent.getParcelableArrayListExtra<PerilItem.Peril>(PERILS)
        val insurableLimits: List<InsurableLimitItem>? =
            intent.getParcelableArrayListExtra<InsurableLimitItem.InsurableLimit>(INSURABLE_LIMITS)
        val documents = intent.getParcelableArrayListExtra<DocumentItems.Document>(DOCUMENTS)

        if (title == null || perils == null || insurableLimits == null || documents == null) {
            e { "Programmer error: PERILS/INSURABLE_LIMITS/DOCUMENTS not provided to ${this.javaClass.name}" }
            return
        }

        val actionData = intent.getParcelableExtra<QuoteDetailAction>(ACTION)

        with(binding) {
            window.compatSetDecorFitsSystemWindows(false)

            Insetter.builder()
                .setOnApplyInsetsListener { view, insets, initialState ->
                    view.updatePadding(
                        top = initialState.paddings.top + insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                    )
                }
                .applyToView(toolbar)
            toolbar.title = title
            toolbar.setNavigationOnClickListener { finish() }

            val perilAdapter = PerilsAdapter(
                fragmentManager = supportFragmentManager,
                imageLoader = imageLoader,
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

            if (actionData != null) {
                Insetter
                    .builder()
                    .setOnApplyInsetsListener { view, insets, initialState ->
                        view.updateMargin(
                            bottom = initialState.margins.bottom +
                                insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                        )
                    }
                    .applyToView(action)
                action.isVisible = true
                action.text = actionData.label
                action.setHapticClickListener {
                    handleAction(this@QuoteDetailActivity, actionData.action)
                }
            }
        }
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val PERILS = "PERILS"
        private const val INSURABLE_LIMITS = "INSURABLE_LIMITS"
        private const val DOCUMENTS = "DOCUMENTS"
        private const val ACTION = "ACTION"
        fun newInstance(
            context: Context,
            title: String,
            perils: List<PerilItem.Peril>,
            insurableLimits: List<InsurableLimitItem.InsurableLimit>,
            documents: List<DocumentItems.Document>,
            action: QuoteDetailAction? = null
        ) = Intent(context, QuoteDetailActivity::class.java).apply {
            putExtra(TITLE, title)
            putParcelableArrayListExtra(PERILS, perils.toArrayList())
            putParcelableArrayListExtra(INSURABLE_LIMITS, insurableLimits.toArrayList())
            putParcelableArrayListExtra(DOCUMENTS, documents.toArrayList())
            if (action != null) {
                putExtra(ACTION, action)
            }
        }
    }
}

@Parcelize
data class QuoteDetailAction(
    val action: CrossSellData.Action,
    val label: String,
) : Parcelable
