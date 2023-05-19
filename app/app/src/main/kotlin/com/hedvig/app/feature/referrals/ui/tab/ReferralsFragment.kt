package com.hedvig.app.feature.referrals.ui.tab

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.snackbar.Snackbar
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.language.LanguageService
import com.hedvig.app.R
import com.hedvig.app.databinding.ReferralsCodeBinding
import com.hedvig.app.databinding.ReferralsHeaderBinding
import com.hedvig.app.databinding.ReferralsRowBinding
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.apollo.toWebLocaleTag
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.copyToClipboard
import com.hedvig.app.util.extensions.showShareSheet
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.safeLet
import giraffe.ReferralsQuery
import giraffe.fragment.ReferralFragment
import org.javamoney.moneta.Money
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import slimber.log.e
import java.util.Locale
import javax.money.MonetaryAmount

class ReferralsFragment : Fragment() {
  private val referralsViewModel: ReferralsViewModel by viewModel()
  private val languageService: LanguageService by inject()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        HedvigTheme {
          Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
          ) {
            val data by referralsViewModel.data.collectAsStateWithLifecycle()
            when (val uiState = data) {
              is ReferralsUiState.Error -> {
                Column {
                  Text(
                    text = stringResource(hedvig.resources.R.string.PROFILE_REFERRAL_TITLE),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                      .padding(horizontal = 16.dp)
                      .padding(top = 8.dp),
                  )
                  GenericErrorScreen(
                    onRetryButtonClick = { referralsViewModel.reload() },
                    Modifier
                      .padding(16.dp)
                      .padding(top = (64 - 16).dp),
                  )
                }
              }
              else -> {
                ForeverScreen(
                  uiState = uiState,
                  reload = referralsViewModel::reload,
                  onShareCodeClick = { code, incentive ->
                    requireContext().showShareSheet(hedvig.resources.R.string.REFERRALS_SHARE_SHEET_TITLE) { intent ->
                      intent.putExtra(
                        Intent.EXTRA_TEXT,
                        getString(
                          hedvig.resources.R.string.REFERRAL_SMS_MESSAGE,
                          incentive.format(languageService.getLocale()),
                          buildString {
                            append(getString(R.string.WEB_BASE_URL))
                            append("/")
                            append(languageService.getGraphQLLocale().toWebLocaleTag())
                            append("/forever/")
                            append(Uri.encode(code))
                          },
                        ),
                      )
                      intent.type = "text/plain"
                    }
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ForeverScreen(
  uiState: ReferralsUiState,
  reload: () -> Unit,
  onShareCodeClick: (code: String, incentive: MonetaryAmount) -> Unit,
) {
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isLoading,
    onRefresh = reload,
  )
  Box() {
    Column(
      Modifier
        .matchParentSize()
        .pullRefresh(pullRefreshState)
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
      Spacer(Modifier.height(64.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.PROFILE_REFERRAL_TITLE),
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      ReferralsContent(uiState)
      val incentive = uiState.incentive
      if (incentive == null) {
        LaunchedEffect(incentive) {
          e { "Invariant detected: referralInformation.campaign.incentive is null" }
        }
      } else if (uiState is ReferralsUiState.Success) {
        val code: String = uiState.data.referralInformation.campaign.code
        LargeContainedTextButton(
          text = stringResource(hedvig.resources.R.string.referrals_empty_share_code_button),
          onClick = { onShareCodeClick(code, incentive) },
          modifier = Modifier.padding(16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    PullRefreshIndicator(
      refreshing = uiState.isLoading,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

// Maps 1:1 to the old ReferralsAdapter, to ease migration to compose
@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.ReferralsContent(uiState: ReferralsUiState) {
  val referralsModelList: List<ReferralsModel> = when (uiState) {
    is ReferralsUiState.Error -> emptyList()
    ReferralsUiState.Loading -> {
      listOf(
        ReferralsModel.Header.LoadingHeader,
        ReferralsModel.Code.LoadingCode,
        ReferralsModel.InvitesHeader,
        ReferralsModel.Referral.LoadingReferral,
      )
    }
    is ReferralsUiState.Success -> {
      if (
        uiState.data.referralInformation.invitations.isEmpty() &&
        uiState.data.referralInformation.referredBy == null
      ) {
        listOf(
          ReferralsModel.Header.LoadedEmptyHeader(uiState.data),
          ReferralsModel.Code.LoadedCode(uiState.data),
        )
      } else {
        buildList {
          add(ReferralsModel.Header.LoadedHeader(uiState.data))
          add(ReferralsModel.Code.LoadedCode(uiState.data))
          add(ReferralsModel.InvitesHeader)
          addAll(
            uiState.data.referralInformation.invitations
              .filter {
                val referralFragment = it.fragments.referralFragment
                referralFragment.asActiveReferral != null ||
                  referralFragment.asInProgressReferral != null ||
                  referralFragment.asTerminatedReferral != null
              }
              .map {
                ReferralsModel.Referral.LoadedReferral(
                  it.fragments.referralFragment,
                )
              },
          )

          uiState.data.referralInformation.referredBy?.let { referredBy ->
            add(ReferralsModel.Referral.Referee(referredBy.fragments.referralFragment))
          }
        }
      }
    }
  }
  for (referralsModel in referralsModelList) {
    ReferralsModelRenderer(referralsModel)
  }
}

@Composable
fun ReferralsModelRenderer(referralModel: ReferralsModel) {
  val locale = getLocale()
  when (referralModel) {
    is ReferralsModel.Header -> {
      AndroidViewBinding(
        factory = ReferralsHeaderBinding::inflate,
        update = bindReferralsHeaderBinding(locale, referralModel),
      )
    }
    is ReferralsModel.Code -> {
      AndroidViewBinding(
        factory = ReferralsCodeBinding::inflate,
        update = bindReferralsCodeBinding(locale, referralModel),
      )
    }
    ReferralsModel.InvitesHeader -> {
      Text(
        text = stringResource(hedvig.resources.R.string.referrals_active_invited_title),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 40.dp, bottom = 16.dp).padding(horizontal = 24.dp),
      )
    }
    is ReferralsModel.Referral -> {
      AndroidViewBinding(
        factory = ReferralsRowBinding::inflate,
        update = bindReferralsReferralBinding(locale, referralModel),
      )
    }
  }
}

private fun bindReferralsHeaderBinding(
  locale: Locale,
  referralModel: ReferralsModel.Header,
): ReferralsHeaderBinding.() -> Unit = { ->
  fun bindPiechart(data: ReferralsQuery.Data) {
    (piechart.getTag(R.id.slice_blink_animation) as? ValueAnimator)?.cancel()
    piechartPlaceholder.hideShimmer()
    grossPrice.show()
    val grossPriceAmount = data
      .chargeEstimation
      .subscription
      .fragments
      .monetaryAmountFragment
      .toMonetaryAmount()
    grossPrice.text = grossPriceAmount.format(locale)
    val potentialDiscountAmount = data
      .referralInformation
      .campaign
      .incentive
      ?.asMonthlyCostDeduction
      ?.amount
      ?.fragments
      ?.monetaryAmountFragment
      ?.toMonetaryAmount()
    val currentDiscountAmount = data
      .referralInformation
      .costReducedIndefiniteDiscount
      ?.fragments
      ?.costFragment
      ?.monthlyDiscount
      ?.fragments
      ?.monetaryAmountFragment
      ?.toMonetaryAmount()

    safeLet(
      grossPriceAmount,
      potentialDiscountAmount,
      currentDiscountAmount,
    ) { gpa, pda, cda ->
      val pdaAsPercentage = (pda.number.doubleValueExact() / gpa.number.doubleValueExact()).toFloat() * 100
      val cdaAsPercentage = (cda.number.doubleValueExact() / gpa.number.doubleValueExact()).toFloat() * 100
      val rest = 100f - (pdaAsPercentage + cdaAsPercentage)

      val potentialDiscountColor = piechart.context.compatColor(R.color.forever_orange_300)
      val restColor = piechart.context.compatColor(R.color.forever_orange_500)

      val segments = listOfNotNull(
        if (cdaAsPercentage != 0f) {
          PieChartSegment(
            CURRENT_DISCOUNT_SLICE,
            cdaAsPercentage,
            piechart.context.colorAttr(com.google.android.material.R.attr.colorSurface),
          )
        } else {
          null
        },
        PieChartSegment(POTENTIAL_DISCOUNT_SLICE, pdaAsPercentage, potentialDiscountColor),
        PieChartSegment(REST_SLICE, rest, restColor),
      )
      piechart.reveal(
        segments,
      ) {
        ValueAnimator.ofFloat(1f, 0f).apply {
          duration = SLICE_BLINK_DURATION
          repeatCount = ValueAnimator.INFINITE
          repeatMode = ValueAnimator.REVERSE
          interpolator = AccelerateDecelerateInterpolator()
          addUpdateListener { va ->
            piechart.segments = piechart.segments.map { segment ->
              if (segment.id == POTENTIAL_DISCOUNT_SLICE) {
                return@map segment.copy(
                  color = boundedColorLerp(
                    potentialDiscountColor,
                    restColor,
                    va.animatedFraction,
                  ),
                )
              }
              segment
            }
          }
          piechart.setTag(R.id.slice_blink_animation, this)
          piechart.doOnDetach { cancel() }
          start()
        }
      }
    }
  }
  when (referralModel) {
    ReferralsModel.Header.LoadingHeader -> {
      (piechart.getTag(R.id.slice_blink_animation) as? ValueAnimator)?.cancel()
      piechart.segments = listOf(
        PieChartSegment(
          LOADING_SLICE,
          100f,
          piechart.context.colorAttr(R.attr.colorPlaceholder),
        ),
      )
      piechartPlaceholder.showShimmer(true)
      grossPrice.hide()
      emptyTexts.remove()
      nonEmptyTexts.show()
      placeholders.show()
      loadedData.remove()
      otherDiscountBox.remove()
    }

    is ReferralsModel.Header.LoadedEmptyHeader -> {
      bindPiechart(referralModel.inner)
      placeholders.remove()
      emptyTexts.show()
      loadedData.remove()
      nonEmptyTexts.remove()
      otherDiscountBox.remove()
      referralModel
        .inner
        .referralInformation
        .campaign
        .incentive
        ?.asMonthlyCostDeduction
        ?.amount
        ?.fragments
        ?.monetaryAmountFragment
        ?.toMonetaryAmount()
        ?.let { incentiveAmount ->
          emptyBody.text = emptyBody.context.getString(
            hedvig.resources.R.string.referrals_empty_body,
            incentiveAmount.format(locale),
            Money.of(0, incentiveAmount.currency.currencyCode)
              .format(locale),
          )
        }
    }

    is ReferralsModel.Header.LoadedHeader -> {
      bindPiechart(referralModel.inner)
      placeholders.remove()
      emptyTexts.remove()
      loadedData.show()
      nonEmptyTexts.show()
      val costFragment = referralModel
        .inner
        .referralInformation
        .costReducedIndefiniteDiscount
        ?.fragments
        ?.costFragment
      costFragment
        ?.monthlyDiscount
        ?.fragments
        ?.monetaryAmountFragment
        ?.toMonetaryAmount()
        ?.negate()
        ?.format(locale)
        ?.let { discountPerMonth.text = it }
      costFragment
        ?.monthlyNet
        ?.fragments
        ?.monetaryAmountFragment
        ?.toMonetaryAmount()
        ?.let { referralNet ->
          newPrice.text = referralNet.format(locale)
          otherDiscountBox.isVisible = referralModel
            .inner
            .chargeEstimation
            .subscription
            .fragments
            .monetaryAmountFragment
            .toMonetaryAmount() != referralNet
        }
    }
  }
}

private fun bindReferralsCodeBinding(
  locale: Locale,
  referralModel: ReferralsModel.Code,
): ReferralsCodeBinding.() -> Unit = {
  when (referralModel) {
    ReferralsModel.Code.LoadingCode -> {
      codePlaceholder.show()
      code.remove()
      edit.remove()
    }
    is ReferralsModel.Code.LoadedCode -> {
      codePlaceholder.remove()
      code.show()
      code.text = referralModel.inner.referralInformation.campaign.code
      codeContainer.setOnLongClickListener {
        code.context.copyToClipboard(referralModel.inner.referralInformation.campaign.code)
        Snackbar
          .make(
            code,
            hedvig.resources.R.string.referrals_active__toast_text,
            Snackbar.LENGTH_SHORT,
          )
          .setAnchorView(R.id.bottomNavigation)
          .show()
        true
      }
      referralModel
        .inner
        .referralInformation
        .campaign
        .incentive
        ?.asMonthlyCostDeduction
        ?.amount
        ?.fragments
        ?.monetaryAmountFragment
        ?.toMonetaryAmount()
        ?.let { incentiveAmount ->
          codeFootnote.text = codeFootnote.resources.getString(
            hedvig.resources.R.string.referrals_empty_code_footer,
            incentiveAmount.format(locale),
          )
        }
      edit.setHapticClickListener {
        edit.context.startActivity(
          ReferralsEditCodeActivity.newInstance(
            edit.context,
            referralModel.inner.referralInformation.campaign.code,
          ),
        )
      }
      edit.show()
    }
  }
}

private fun bindReferralsReferralBinding(
  locale: Locale,
  referralModel: ReferralsModel.Referral,
): ReferralsRowBinding.() -> Unit = {
  fun ReferralsRowBinding.bindReferral(data: ReferralFragment) {
    placeholders.remove()
    texts.show()
    data.name?.let { name.text = it }
    icon.show()
    status.show()
    data.asActiveReferral?.let { activeReferral ->
      icon.setImageResource(R.drawable.ic_basketball)
      status.background =
        status.context.compatDrawable(R.drawable.background_slightly_rounded_corners)
          ?.apply {
            mutate().compatSetTint(status.context.colorAttr(com.google.android.material.R.attr.colorSurface))
          }
      val discountAsNegative =
        activeReferral.discount.fragments.monetaryAmountFragment.toMonetaryAmount()
          .negate()
      status.text = discountAsNegative.format(locale)
    }
    data.asInProgressReferral?.let {
      icon.setImageResource(R.drawable.ic_clock_colorless)
      status.background = null
      status.text = status.context.getString(hedvig.resources.R.string.referalls_invitee_states_awaiting___)
    }
    data.asTerminatedReferral?.let {
      icon.setImageResource(R.drawable.ic_x_in_circle)
      status.background = null
      status.text = status.context.getString(hedvig.resources.R.string.referalls_invitee_states_terminated)
    }
  }
  when (referralModel) {
    ReferralsModel.Referral.LoadingReferral -> {
      placeholders.show()
      texts.remove()
      icon.remove()
      status.remove()
    }

    is ReferralsModel.Referral.Referee -> {
      bindReferral(referralModel.inner)
      refereeLabel.show()
    }

    is ReferralsModel.Referral.LoadedReferral -> {
      bindReferral(referralModel.inner)
      refereeLabel.remove()
    }
  }
}

val ReferralsUiState.incentive: MonetaryAmount?
  get() = when (this) {
    is ReferralsUiState.Success -> {
      data
        .referralInformation
        .campaign
        .incentive
        ?.asMonthlyCostDeduction
        ?.amount
        ?.fragments
        ?.monetaryAmountFragment
        ?.toMonetaryAmount()
    }
    else -> null
  }

private val ReferralFragment.name: String?
  get() {
    asActiveReferral?.name?.let { return it }
    asInProgressReferral?.name?.let { return it }
    asTerminatedReferral?.name?.let { return it }

    return null
  }

private const val CURRENT_DISCOUNT_SLICE = 0
private const val POTENTIAL_DISCOUNT_SLICE = 1
private const val REST_SLICE = 2
private const val LOADING_SLICE = 3

private const val SLICE_BLINK_DURATION = 800L
