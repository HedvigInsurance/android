package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.fragment.QuoteCartFragment
import com.hedvig.android.owldroid.fragment.TableFragment
import com.hedvig.android.owldroid.graphql.DataCollectionResultQuery
import com.hedvig.android.owldroid.graphql.DataCollectionResultQuery.AsHouseInsuranceCollection
import com.hedvig.android.owldroid.graphql.DataCollectionStatusSubscription
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.CheckoutMethod
import com.hedvig.android.owldroid.type.DataCollectionStatus
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationApproveButtonTerminology
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationPostSignStep
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationStartDateTerminology
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationTitle
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.android.owldroid.type.TypeOfContractGradientOption
import com.hedvig.app.testdata.common.builders.CostBuilder
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import com.hedvig.app.testdata.feature.insurance.builders.PerilBuilder
import java.time.LocalDate
import java.util.UUID

data class QuoteCartOfferDataBuilder(
    private val bundleDisplayName: String = "Bundle Display Name",
    private val quotes: List<QuoteBundleFragment.Quote> = listOf(QuoteBuilder().build()),
    private val insuranceCost: CostFragment = CostBuilder()
        .build(),
    private val redeemedCampaigns: List<OfferQuery.RedeemedCampaign> = emptyList(),
    private val frequentlyAskedQuestions: List<QuoteBundleFragment.FrequentlyAskedQuestion> = emptyList(),
    private val inceptions: QuoteBundleFragment.Inception1 = ConcurrentInceptionBuilder().build(),
    private val signMethod: SignMethod = SignMethod.SWEDISH_BANK_ID,
    private val postSignStep: QuoteBundleAppConfigurationPostSignStep =
        QuoteBundleAppConfigurationPostSignStep.CONNECT_PAYIN,
    private val appConfiguration: QuoteBundleFragment.AppConfiguration = AppConfigurationBuilder().build(),
) {
    fun build(): QuoteCartFragment {
        return QuoteCartFragment(
            id = UUID.randomUUID().toString(),
            bundle = QuoteCartFragment.Bundle(
                possibleVariations = List(3) { index ->
                    QuoteCartFragment.PossibleVariation(
                        id = index.toString(),
                        tag = "test tag $index",
                        bundle = QuoteCartFragment.Bundle1(
                            fragments = QuoteCartFragment.Bundle1.Fragments(
                                quoteBundleFragment = QuoteBundleFragment(
                                    displayName = "$bundleDisplayName:$index",
                                    quotes = quotes,
                                    bundleCost = QuoteBundleFragment.BundleCost(
                                        fragments = QuoteBundleFragment.BundleCost.Fragments(insuranceCost)
                                    ),
                                    frequentlyAskedQuestions = frequentlyAskedQuestions,
                                    inception = inceptions,
                                    appConfiguration = appConfiguration
                                )
                            )
                        )
                    )
                },
            ),
            checkoutMethods = listOf(CheckoutMethod.UNKNOWN__),
            checkout = null,
            paymentConnection = null,
            campaign = null,
        )
    }
}

data class OfferDataBuilder(
    private val bundleDisplayName: String = "Bundle Display Name",
    private val quotes: List<QuoteBundleFragment.Quote> = listOf(QuoteBuilder().build()),
    private val insuranceCost: CostFragment = CostBuilder()
        .build(),
    private val redeemedCampaigns: List<OfferQuery.RedeemedCampaign> = emptyList(),
    private val frequentlyAskedQuestions: List<QuoteBundleFragment.FrequentlyAskedQuestion> = emptyList(),
    private val inceptions: QuoteBundleFragment.Inception1 = ConcurrentInceptionBuilder().build(),
    private val signMethod: SignMethod = SignMethod.SWEDISH_BANK_ID,
    private val postSignStep: QuoteBundleAppConfigurationPostSignStep =
        QuoteBundleAppConfigurationPostSignStep.CONNECT_PAYIN,
    private val appConfiguration: QuoteBundleFragment.AppConfiguration = AppConfigurationBuilder().build(),
) {
    fun build() = OfferQuery.Data(
        quoteBundle = OfferQuery.QuoteBundle(
            possibleVariations = listOf(
                OfferQuery.PossibleVariation(
                    id = "test",
                    tag = "test tag",
                    bundle = OfferQuery.Bundle(
                        fragments = OfferQuery.Bundle.Fragments(
                            quoteBundleFragment = QuoteBundleFragment(
                                displayName = bundleDisplayName,
                                quotes = quotes,
                                bundleCost = QuoteBundleFragment.BundleCost(
                                    fragments = QuoteBundleFragment.BundleCost.Fragments(insuranceCost)
                                ),
                                frequentlyAskedQuestions = frequentlyAskedQuestions,
                                inception = inceptions,
                                appConfiguration = appConfiguration
                            )
                        )
                    )
                )
            ),
        ),
        redeemedCampaigns = redeemedCampaigns,
        signMethodForQuotes = signMethod,
    )
}

data class QuoteBuilder(
    private val startDate: LocalDate? = null,
    private val id: String = "ea656f5f-40b2-4953-85d9-752b33e69e38",
    private val dataCollectionId: String? = null,
    private val typeOfContract: TypeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    private val currentInsurer: QuoteBundleFragment.CurrentInsurer? = null,
    private val perils: List<QuoteBundleFragment.ContractPeril> = PerilBuilder().offerQueryBuild(5),
    private val termsAndConditionsUrl: String = "https://www.example.com",
    private val insurableLimits: List<QuoteBundleFragment.InsurableLimit> = emptyList(),
    private val insuranceTerms: List<QuoteBundleFragment.InsuranceTerm> = emptyList(),
    private val detailsTable: TableFragment = TableFragmentBuilder().build(),
    private val displayName: String = typeOfContract.toString(),
) {
    fun build() = QuoteBundleFragment.Quote(
        displayName = displayName,
        startDate = startDate,
        id = id,
        dataCollectionId = dataCollectionId,
        currentInsurer = currentInsurer,
        detailsTable = QuoteBundleFragment.DetailsTable(
            fragments = QuoteBundleFragment.DetailsTable.Fragments(detailsTable),
        ),
        contractPerils = perils,
        termsAndConditions = QuoteBundleFragment.TermsAndConditions(
            displayName = "Villkor",
            url = termsAndConditionsUrl,
        ),
        insurableLimits = insurableLimits,
        insuranceTerms = insuranceTerms,
        typeOfContract = typeOfContract,
        email = "test@email.com"
    )
}

data class AppConfigurationBuilder(
    private val showCampaignManagement: Boolean = true,
    private val showFAQ: Boolean = true,
    private val ignoreCampaigns: Boolean = false,
    private val approveButtonTerminology: QuoteBundleAppConfigurationApproveButtonTerminology =
        QuoteBundleAppConfigurationApproveButtonTerminology.APPROVE_CHANGES,
    private val title: QuoteBundleAppConfigurationTitle = QuoteBundleAppConfigurationTitle.LOGO,
    private val gradientOption: TypeOfContractGradientOption = TypeOfContractGradientOption.GRADIENT_ONE,
    private val startDateTerminology: QuoteBundleAppConfigurationStartDateTerminology =
        QuoteBundleAppConfigurationStartDateTerminology.START_DATE,
    private val postSignStep: QuoteBundleAppConfigurationPostSignStep =
        QuoteBundleAppConfigurationPostSignStep.CONNECT_PAYIN,
) {
    fun build() = QuoteBundleFragment.AppConfiguration(
        showCampaignManagement = showCampaignManagement,
        showFAQ = showFAQ,
        ignoreCampaigns = ignoreCampaigns,
        approveButtonTerminology = approveButtonTerminology,
        title = title,
        gradientOption = gradientOption,
        startDateTerminology = startDateTerminology,
        postSignStep = postSignStep
    )
}

data class FaqBuilder(
    private val headline: String,
    private val body: String,
    private val id: String = "123",
) {
    fun build() = QuoteBundleFragment.FrequentlyAskedQuestion(
        id = id,
        headline = headline,
        body = body
    )
}

data class DataCollectionStatusSubscriptionBuilder(
    private val id: String = "id",
    private val status: DataCollectionStatus = DataCollectionStatus.UNKNOWN__,
    private val insuranceCompany: String = "Some Insurance Company",
) {
    fun build(): DataCollectionStatusSubscription.Data {
        return DataCollectionStatusSubscription.Data(
            dataCollectionStatusV2 = DataCollectionStatusSubscription.DataCollectionStatusV2(
                status = status,
                insuranceCompany = insuranceCompany,
            )
        )
    }
}

data class DataCollectionResultQueryBuilder(
    private val insuranceProvider: String = "insuranceProvider",
    private val insuranceName: String = "insuranceName",
    private val payouts: List<MonetaryAmountFragment> = listOf(MonetaryAmountFragment(amount = "19", currency = "SEK")),
) {
    fun build(): DataCollectionResultQuery.Data {
        return DataCollectionResultQuery.Data(
            externalInsuranceProvider = DataCollectionResultQuery.ExternalInsuranceProvider(
                dataCollectionV2 = payouts.map { payout ->
                    DataCollectionResultQuery.DataCollectionV2(
                        asHouseInsuranceCollection = AsHouseInsuranceCollection(
                            insuranceProvider = insuranceProvider,
                            insuranceHolderAddress = null,
                            insuranceHolderName = null,
                            insuranceName = insuranceName,
                            insuranceSubType = null,
                            insuranceType = null,
                            renewalDate = null,
                            monthlyNetPremium = DataCollectionResultQuery.MonthlyNetPremium(
                                fragments = DataCollectionResultQuery.MonthlyNetPremium.Fragments(
                                    monetaryAmountFragment = payout
                                )
                            ),
                            monthlyGrossPremium = null,
                            monthlyDiscount = null,
                            insuranceObjectAddress = null,
                            livingArea = null,
                            postalCode = null,
                        ),
                        asPersonTravelInsuranceCollection = null,
                    )
                }
            )
        )
    }
}
