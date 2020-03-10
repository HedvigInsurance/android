package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.app.feature.dashboard.ui.Contract
import com.hedvig.app.feature.dashboard.ui.DashboardData
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import org.threeten.bp.LocalDate

class MockDashboardViewModel : DashboardViewModel() {
    override val data = MutableLiveData<DashboardData>()

    init {
        data.postValue(DATA)
    }

    companion object {
        val DATA = DashboardData(
            listOf(
                Contract(
                    "120e9ac9-84b1-4e5d-add1-70a9bad340be",
                    ContractStatus.ACTIVE,
                    LocalDate.of(2020, 1, 1),
                    null,
                    DashboardQuery.CurrentAgreement(
                        asAgreementCore = DashboardQuery.AsAgreementCore(
                            status = AgreementStatus.ACTIVE,
                            activeFrom = LocalDate.of(2020, 1, 1),
                            activeTo = null
                        ),
                        asNorwegianHomeContentAgreement = DashboardQuery.AsNorwegianHomeContentAgreement(
                            address = DashboardQuery.Address2(
                                fragments = DashboardQuery.Address2.Fragments(
                                    AddressFragment(
                                        street = "Testvägen 1",
                                        postalCode = "123 45",
                                        city = "Tensta"
                                    )
                                )
                            ),
                            numberCoInsured = 2,
                            squareMeters = 50,
                            nhcType = NorwegianHomeContentLineOfBusiness.RENT
                        ),
                        asSwedishApartmentAgreement = null,
                        asNorwegianTravelAgreement = null,
                        asSwedishHouseAgreement = null
                    ),
                    listOf(
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        ),
                        PerilCategoryFragment.Peril(
                            id = "ME.LEGAL",
                            title = "Mock",
                            description = "Mock"
                        )
                    )
                )
            )
        )
    }
}
