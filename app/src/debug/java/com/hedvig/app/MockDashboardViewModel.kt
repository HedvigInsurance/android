package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.app.feature.dashboard.ui.Contract
import com.hedvig.app.feature.dashboard.ui.DashboardData
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import com.hedvig.app.feature.dashboard.ui.PerilCategory
import org.threeten.bp.LocalDate

class MockDashboardViewModel : DashboardViewModel() {
    override val data = MutableLiveData<DashboardData>()

    init {
        data.postValue(
            DashboardData(
                listOf(
                    Contract(
                        "120e9ac9-84b1-4e5d-add1-70a9bad340be",
                        ContractStatus.ACTIVE,
                        LocalDate.of(2020, 1, 1),
                        null,
                        DashboardQuery.CurrentAgreement(
                            "SwedishApartmentAgreement"
                        ),
                        listOf(
                            PerilCategory(
                                "ME",
                                "Jag och min familj",
                                "försäkras för",
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
                                    )
                                )
                            ),
                            PerilCategory(
                                "HOME",
                                "Jag och min familj",
                                "försäkras för",
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
                                    )
                                )
                            ),
                            PerilCategory(
                                "THINGS",
                                "Jag och min familj",
                                "försäkras för",
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
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
