package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
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
                            id = "81398a4b-5ae4-4639-8711-652d76237366"
                        ),
                        asSwedishApartmentAgreement = DashboardQuery.AsSwedishApartmentAgreement(
                            address = DashboardQuery.Address(
                                fragments = DashboardQuery.Address.Fragments(
                                    AddressFragment(
                                        street = "Testvägen 1",
                                        postalCode = "123 45",
                                        city = "Tensta"
                                    )
                                )
                            ),
                            numberCoInsured = 2,
                            squareMeters = 50,
                            saType = SwedishApartmentLineOfBusiness.BRF
                        ),
                        asNorwegianHomeContentAgreement = null,
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
