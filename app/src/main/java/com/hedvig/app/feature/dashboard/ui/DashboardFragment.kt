package com.hedvig.app.feature.dashboard.ui

import android.content.res.Resources
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.android.owldroid.type.InsuranceType
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.util.extensions.addViews
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.displayMetrics
import com.hedvig.app.util.extensions.proxyNavigate
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.animateCollapse
import com.hedvig.app.util.extensions.view.animateExpand
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.isApartmentOwner
import com.hedvig.app.util.isStudentInsurance
import com.hedvig.app.util.safeLet
import com.hedvig.app.viewmodel.DirectDebitViewModel
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dashboard_footnotes.view.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import kotlin.math.min

class DashboardFragment : BaseTabFragment() {
    private val tracker: DashboardTracker by inject()

    private val dashboardViewModel: DashboardViewModel by sharedViewModel()
    private val directDebitViewModel: DirectDebitViewModel by sharedViewModel()

    private val bottomNavigationHeight: Int by lazy { resources.getDimensionPixelSize(R.dimen.bottom_navigation_height) }
    private val halfMargin: Int by lazy { resources.getDimensionPixelSize(R.dimen.base_margin_half) }
    private val doubleMargin: Int by lazy { resources.getDimensionPixelSize(R.dimen.base_margin_double) }
    private val tripleMargin: Int by lazy { resources.getDimensionPixelSize(R.dimen.base_margin_triple) }
    private val perilTotalWidth: Int by lazy { resources.getDimensionPixelSize(R.dimen.peril_width) + (doubleMargin * 2) }
    private val rowWidth: Int by lazy {
        var margin = tripleMargin * 2 // perilCategoryView margin
        margin += halfMargin * 2 // strange padding in perilCategoryView
        margin += doubleMargin * 2 // perilCategoryView ConstraintLayout padding
        requireActivity().displayMetrics.widthPixels - margin
    }

    private var isInsurancePendingExplanationExpanded = false

    private var setActivationFiguresInterval: Disposable? = null
    private val compositeDisposable = CompositeDisposable()

    override val layout = R.layout.fragment_dashboard

    override fun onResume() {
        super.onResume()
        dashboardViewModel.data.value?.insurance?.activeFrom?.let { localDate ->
            setActivationFigures(localDate)
        }
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()
    }

    private fun loadData() {
        dashboardViewModel.data.observe(this) { bindData() }
        directDebitViewModel.data.observe(this) { bindData() }
    }

    private fun bindData() {
        val dashboardData = dashboardViewModel.data.value ?: return
        val directDebitData = directDebitViewModel.data.value ?: return
        loadingSpinner.remove()
        setupInsuranceStatusStatus(dashboardData.insurance)

        perilCategoryContainer.removeAllViews()

        dashboardData.insurance.perilCategories?.forEach { category ->
            val categoryView = makePerilCategoryRow(category.fragments.perilCategoryFragment)
            perilCategoryContainer.addView(categoryView)
        }

        dashboardData.insurance.status.let { insuranceStatus ->
            when (insuranceStatus) {
                InsuranceStatus.ACTIVE -> insuranceActive.show()
                else -> {
                }
            }
        }
        dashboardData.insurance.type?.let { setupAdditionalInformationRow(it) }

        setupDirectDebitStatus(directDebitData.directDebitStatus)
    }

    private fun makePerilCategoryRow(category: PerilCategoryFragment): PerilCategoryView {
        val categoryView =
            PerilCategoryView.build(requireContext())

        categoryView.categoryIconId = category.iconUrl
        categoryView.title = category.title
        categoryView.subtitle = category.description
        categoryView.expandedContentContainer.measure(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        categoryView.onAnimateExpand = { handleExpandShowEntireView(categoryView) }
        category.perils?.let { categoryView.expandedContent = makePerilCategoryExpandContent(it, category) }

        return categoryView
    }

    private fun makePerilCategoryExpandContent(
        perils: List<PerilCategoryFragment.Peril>,
        category: PerilCategoryFragment
    ): LinearLayout {
        val expandedContent = LinearLayout(requireContext())
        val maxPerilsPerRow = rowWidth / perilTotalWidth
        if (perils.size > maxPerilsPerRow) {
            for (row in 0 until perils.size step maxPerilsPerRow) {
                expandedContent.orientation = LinearLayout.VERTICAL
                val rowView = LinearLayout(requireContext())
                val rowPerils = perils.subList(row, min(row + maxPerilsPerRow, perils.size))
                rowView.addViews(rowPerils.map { makePeril(it, category) })
                expandedContent.addView(rowView)
            }
        } else {
            expandedContent.addViews(perils.map { makePeril(it, category) })
        }


        return expandedContent
    }

    private fun makePeril(peril: PerilCategoryFragment.Peril, subject: PerilCategoryFragment) = PerilView.build(
        requireContext(),
        name = peril.title,
        iconId = peril.id,
        onClick = {
            safeLet(subject.title, peril.id, peril.title, peril.description) { name, id, title, description ->
                tracker.perilClick(id)
                PerilBottomSheet.newInstance(name, PerilIcon.from(id), title, description)
                    .show(childFragmentManager, PerilBottomSheet.TAG)
            }
        }
    )

    private fun setupAdditionalInformationRow(insuranceType: InsuranceType) {
        val additionalInformation = PerilCategoryView.build(
            requireContext(),
            bottomMargin = tripleMargin
        )

        additionalInformation.onAnimateExpand = { handleExpandShowEntireView(additionalInformation) }
        additionalInformation.categoryIcon = requireContext().compatDrawable(R.drawable.ic_more_info)
        additionalInformation.title = resources.getString(R.string.DASHBOARD_MORE_INFO_TITLE)
        additionalInformation.subtitle = resources.getString(R.string.DASHBOARD_MORE_INFO_SUBTITLE)

        val content = layoutInflater.inflate(
            R.layout.dashboard_footnotes,
            additionalInformation.expandedContentContainer,
            false
        )

        content.totalCoverageFootnote.text = interpolateTextKey(
            resources.getString(R.string.DASHBOARD_INSURANCE_AMOUNT_FOOTNOTE),
            "AMOUNT" to
                if (isStudentInsurance(insuranceType)) resources.getString(R.string.two_hundred_thousand)
                else resources.getString(R.string.one_million)
        )
        if (isApartmentOwner(insuranceType)) {
            content.ownerFootnote.show()
        }

        additionalInformation.expandedContent = content

        perilCategoryContainer.addView(additionalInformation)
    }

    private fun setupDirectDebitStatus(directDebitStatus: DirectDebitStatus) {
        when (directDebitStatus) {
            DirectDebitStatus.ACTIVE,
            DirectDebitStatus.PENDING,
            DirectDebitStatus.`$UNKNOWN` -> {
                directDebitNeedsSetup.remove()
            }
            DirectDebitStatus.NEEDS_SETUP -> {
                directDebitNeedsSetup.show()
                directDebitConnectButton.setHapticClickListener {
                    tracker.setupDirectDebit()
                    navController.proxyNavigate(R.id.action_dashboardFragment_to_trustlyFragment)
                }
            }
        }
    }

    private fun setupInsuranceStatusStatus(insurance: DashboardQuery.Insurance) {
        insurancePending.remove()
        insuranceActive.remove()
        when (insurance.status) {
            InsuranceStatus.ACTIVE -> {
                insuranceActive.show()
            }
            InsuranceStatus.INACTIVE -> {
                insurancePending.show()
                insurancePendingCountDownContainer.remove()
                insurancePendingLoadingAnimation.show()

                insurancePendingLoadingAnimation.playAnimation()
                insurancePendingExplanation.text =
                    getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_PENDING_NO_START_DATE_EXPLANATION)
                setupInsurancePendingMoreInfo()
            }
            InsuranceStatus.INACTIVE_WITH_START_DATE -> {
                insurancePending.show()
                insurancePendingLoadingAnimation.remove()

                insurance.activeFrom?.let { localDate ->
                    insurancePendingCountDownContainer.show()

                    setActivationFigures(localDate)
                    val formattedString = localDate.format(DateTimeFormatter.ofPattern("d LLLL yyyy"))
                    insurancePendingExplanation.text = interpolateTextKey(
                        getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_PENDING_HAS_START_DATE_EXPLANATION),
                        "START_DATE" to formattedString
                    )
                } ?: Timber.e("InsuranceStatus INACTIVE_WITH_START_DATE but got no start date")

                setupInsurancePendingMoreInfo()
            }
            InsuranceStatus.`$UNKNOWN`,
            InsuranceStatus.PENDING,
            InsuranceStatus.TERMINATED -> {
            }
        }
    }

    private fun setupInsurancePendingMoreInfo() {
        insurancePendingMoreInfo.setOnClickListener {
            if (isInsurancePendingExplanationExpanded) {
                tracker.expandInsurancePendingInfo()
                insurancePendingExplanation.animateCollapse()
                insurancePendingMoreInfo.text =
                    resources.getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_PENDING_BUTTON_LABEL)
            } else {
                tracker.collapseInsurancePendingInfo()
                insurancePendingExplanation.animateExpand()
                insurancePendingMoreInfo.text =
                    resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_PENDING_BUTTON_CLOSE)
            }
            isInsurancePendingExplanationExpanded = !isInsurancePendingExplanationExpanded
        }
        insurancePendingExplanation.animateCollapse(0, 0)
        isInsurancePendingExplanationExpanded = false
    }

    private fun setActivationFigures(startDate: LocalDate) {
        val period = LocalDate.now().until(startDate)

        insurancePendingCountdownMonths.text = period.months.toString()
        insurancePendingCountdownDays.text = period.days.toString()

        // insurances is started at mid night
        val midnight = GregorianCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val millisToMidnight = midnight.timeInMillis - System.currentTimeMillis()
        val hours = ((millisToMidnight / (1000 * 60 * 60)) % 24)
        val minutes = ((millisToMidnight / (1000 * 60)) % 60)

        insurancePendingCountdownHours.text = hours.toString()
        insurancePendingCountdownMinutes.text = minutes.toString()

        // dispose interval if one all ready exists
        setActivationFiguresInterval?.dispose()
        // start interval
        val disposable = Flowable.interval(30, TimeUnit.SECONDS, Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                { setActivationFigures(startDate) }, { Timber.e(it) }
            )
        compositeDisposable += disposable
        setActivationFiguresInterval = disposable
    }

    private fun handleExpandShowEntireView(view: View) {
        val bottomBreakPoint =
            Resources.getSystem().displayMetrics.heightPixels - (bottomNavigationHeight + doubleMargin)
        val position = intArrayOf(0, 0)
        view.getLocationOnScreen(position)
        val viewBottomPos = position[1] + view.measuredHeight

        if (viewBottomPos > bottomBreakPoint) {
            activity?.findViewById<AppBarLayout>(R.id.appBarLayout)
                ?.setExpanded(false, true)
            val d = viewBottomPos - bottomBreakPoint
            dashboardNestedScrollView.scrollY += d
        }
    }
}
