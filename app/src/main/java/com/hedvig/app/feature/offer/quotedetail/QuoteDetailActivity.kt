package com.hedvig.app.feature.offer.quotedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
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
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.toArrayList
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.Insetter
import e
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

    with(binding) {
      window.compatSetDecorFitsSystemWindows(false)

      Insetter.builder()
        .setOnApplyInsetsListener { view, insets, initialState ->
          view.updatePadding(
            top = initialState.paddings.top + insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
          )
        }
        .applyToView(toolbar)
      toolbar.title = title
      toolbar.setNavigationOnClickListener { finish() }

      val perilAdapter = PerilsAdapter(
        fragmentManager = supportFragmentManager,
        imageLoader = imageLoader,
      ).also {
        it.submitList(
          listOf(
            PerilItem.Header.Simple(getString(hedvig.resources.R.string.cross_sell_info_coverage_title)),
            PerilItem.Header.Simple(getString(hedvig.resources.R.string.cross_sell_info_coverage_title)),
          ) + perils,
        )
      }

      val insurableLimitAdapter = InsurableLimitsAdapter(
        fragmentManager = supportFragmentManager,
      ).also { it.submitList(listOf(InsurableLimitItem.Header.Details) + insurableLimits) }

      val documentAdapter = DocumentAdapter()
        .also {
          it.submitList(
            listOf(
              DocumentItems.Header(hedvig.resources.R.string.OFFER_DOCUMENTS_SECTION_TITLE),
            ) + documents,
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
              insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom,
          )
        }
        .applyToView(recycler)
    }
  }

  companion object {
    private const val TITLE = "TITLE"
    private const val PERILS = "PERILS"
    private const val INSURABLE_LIMITS = "INSURABLE_LIMITS"
    private const val DOCUMENTS = "DOCUMENTS"

    fun newInstance(
      context: Context,
      title: String,
      perils: List<PerilItem.Peril>,
      insurableLimits: List<InsurableLimitItem.InsurableLimit>,
      documents: List<DocumentItems.Document>,
    ) = Intent(context, QuoteDetailActivity::class.java).apply {
      putExtra(TITLE, title)
      putParcelableArrayListExtra(PERILS, perils.toArrayList())
      putParcelableArrayListExtra(INSURABLE_LIMITS, insurableLimits.toArrayList())
      putParcelableArrayListExtra(DOCUMENTS, documents.toArrayList())
    }
  }
}
