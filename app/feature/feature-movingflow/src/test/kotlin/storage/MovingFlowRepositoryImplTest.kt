package storage


import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepositoryImpl
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.feature.movingflow.fragment.MoveIntentQuotesFragment
import octopus.type.CurrencyCode
import octopus.type.MoveExtraBuildingType
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MovingFlowRepositoryImplTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  val moveIntent = object : MoveIntentFragment {
    override val id: String
      get() = "ididid"
    override val minMovingDate: LocalDate
      get() = LocalDate(2026, 1, 1)
    override val maxMovingDate: LocalDate
      get() = LocalDate(2025, 1, 1)
    override val maxHouseNumberCoInsured: Int?
      get() = 6
    override val maxHouseSquareMeters: Int?
      get() = 200
    override val maxApartmentNumberCoInsured: Int?
      get() = 6
    override val maxApartmentSquareMeters: Int?
      get() = 200
    override val isApartmentAvailableforStudent: Boolean?
      get() = false
    override val extraBuildingTypes: List<MoveExtraBuildingType>
      get() = listOf(MoveExtraBuildingType.GREENHOUSE)
    override val suggestedNumberCoInsured: Int
      get() = 2
    override val currentHomeAddresses: List<MoveIntentFragment.CurrentHomeAddress>
      get() = listOf<MoveIntentFragment.CurrentHomeAddress>(
        object : MoveIntentFragment.CurrentHomeAddress {
          override val id: String
            get() = "adsressid"
          override val oldAddressCoverageDurationDays: Int?
            get() = 30
        },
      )
  }

  @Test
  fun `should initiate a new moving flow with given move intent and housing type`() = runTest {
    val storage = movingFlowStorage()
    val repo = MovingFlowRepositoryImpl(storage)
    repo.initiateNewMovingFlow(moveIntent, HousingType.ApartmentOwn)
    repo.movingFlowState().test {
      val result = awaitItem()
      assertThat(result).isNotNull().prop(MovingFlowState::housingType).isEqualTo(HousingType.ApartmentOwn)
      assertThat(result).isNotNull().prop(MovingFlowState::propertyState).isInstanceOf(
        MovingFlowState.PropertyState.ApartmentState::class,
      )
    }
  }

  @Test
  fun `should update state with property input`() = runTest {
    val storage = movingFlowStorage()
    val repo = MovingFlowRepositoryImpl(storage)
    repo.initiateNewMovingFlow(moveIntent, HousingType.ApartmentOwn)
    repo.updateWithPropertyInput(
      movingDate = LocalDate(2025,2,2),
      address = "some addr",
      postalCode = "some code",
      squareMeters = 67,
      numberCoInsured = 3,
      isStudent = false
    )
    repo.movingFlowState().test {
      val result = awaitItem()
      assertThat(result).isNotNull().prop(MovingFlowState::addressInfo).isEqualTo(
        MovingFlowState.AddressInfo(
          street = "some addr",
          postalCode = "some code",
        )
      )
      assertThat(result).isNotNull().prop(MovingFlowState::propertyState).isInstanceOf(
        MovingFlowState.PropertyState.ApartmentState::class,
      )
    }
  }

  @Test
  fun `should update house state with house input details`() = runTest {
    val storage = movingFlowStorage()
    val repo = MovingFlowRepositoryImpl(storage)
    repo.initiateNewMovingFlow(moveIntent, HousingType.Villa)
    repo.updateWithHouseInput(
      yearOfConstruction = 2000,
      ancillaryArea = 33,
      numberOfBathrooms = 5,
      isSublet = false,
      extraBuildings = emptyList()
    )
    repo.movingFlowState().test {
      val result = awaitItem()
      assertThat(result).isNotNull().prop(MovingFlowState::propertyState).isInstanceOf(
        MovingFlowState.PropertyState.HouseState::class,
      ).all {
        prop(MovingFlowState.PropertyState.HouseState::yearOfConstruction).isEqualTo(2000)
        prop(MovingFlowState.PropertyState.HouseState::ancillaryArea).isEqualTo(33)
        prop(MovingFlowState.PropertyState.HouseState::numberOfBathrooms).isEqualTo(5)
        prop(MovingFlowState.PropertyState.HouseState::isSublet).isEqualTo(false)
      }
    }
  }

  @Test
  fun `should not update when trying updating house state on a non-house state`() = runTest {
    val storage = movingFlowStorage()
    val repo = MovingFlowRepositoryImpl(storage)
    repo.initiateNewMovingFlow(moveIntent, HousingType.ApartmentOwn)
    repo.updateWithHouseInput(
      yearOfConstruction = 2000,
      ancillaryArea = 33,
      numberOfBathrooms = 5,
      isSublet = false,
      extraBuildings = emptyList()
    )
    repo.movingFlowState().test {
      val result = awaitItem()
      assertThat(result).isNotNull().prop(MovingFlowState::propertyState).isInstanceOf(
        MovingFlowState.PropertyState.ApartmentState::class,
      )
    }
  }

  @Test
  fun `should update state with move intent quotes and preselect the default choice one`() = runTest {
    val storage = movingFlowStorage()
    val repo = MovingFlowRepositoryImpl(storage)
    repo.initiateNewMovingFlow(moveIntent, HousingType.ApartmentOwn)
    repo.updateWithMoveIntentQuotes(object: MoveIntentQuotesFragment{
      override val homeQuotes: List<MoveIntentQuotesFragment.HomeQuote>?
        get() = buildList {
          add(homeQuote1)
          add(homeQuote2)
        }
      override val mtaQuotes: List<MoveIntentQuotesFragment.MtaQuote>?
        get() = emptyList()
    })
    repo.movingFlowState().test {
      assertThat(awaitItem()).isNotNull().prop(MovingFlowState::lastSelectedHomeQuoteId).isEqualTo(homeQuote1.id)
    }
  }

  @Test
  fun `should update selected home quote id`() = runTest {
    val storage = movingFlowStorage()
    val repo = MovingFlowRepositoryImpl(storage)
    repo.initiateNewMovingFlow(moveIntent, HousingType.ApartmentOwn)
    repo.updateWithMoveIntentQuotes(object: MoveIntentQuotesFragment{
      override val homeQuotes: List<MoveIntentQuotesFragment.HomeQuote>?
        get() = buildList {
          add(homeQuote1)
          add(homeQuote2)
        }
      override val mtaQuotes: List<MoveIntentQuotesFragment.MtaQuote>?
        get() = emptyList()
    })
    repo.movingFlowState().test {
      repo.updatePreselectedHomeQuoteId(homeQuote2.id)
      assertThat(awaitItem()).isNotNull().prop(MovingFlowState::lastSelectedHomeQuoteId).isEqualTo(homeQuote2.id)
      repo.updatePreselectedHomeQuoteId(homeQuote1.id)
      assertThat(awaitItem()).isNotNull().prop(MovingFlowState::lastSelectedHomeQuoteId).isEqualTo(homeQuote1.id)
    }
  }

  private fun TestScope.movingFlowStorage() = MovingFlowStorage(
    TestPreferencesDataStore(
      datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
      coroutineScope = backgroundScope,
    ),
  )
}


private val homeQuote1 = object: MoveIntentQuotesFragment.HomeQuote {
  override val id: String
    get() = "id1"
  override val premium: MoveIntentQuotesFragment.HomeQuote.Premium
    get() = object: MoveIntentQuotesFragment.HomeQuote.Premium {
      override val __typename: String
        get() = "str"
      override val amount: Double
        get() = 30.0
      override val currencyCode: CurrencyCode
        get() = CurrencyCode.SEK
    }
  override val startDate: LocalDate
    get() = LocalDate(2025,6,6)
  override val defaultChoice: Boolean
    get() = true
  override val tierName: String
    get() = "tierName"
  override val tierLevel: Int
    get() = 2
  override val deductible: MoveIntentQuotesFragment.HomeQuote.Deductible?
    get() = null
  override val displayItems: List<MoveIntentQuotesFragment.HomeQuote.DisplayItem>
    get() = emptyList()
  override val exposureName: String
    get() = "exposure"
  override val productVariant: MoveIntentQuotesFragment.HomeQuote.ProductVariant
    get() = object : MoveIntentQuotesFragment.HomeQuote.ProductVariant {
      override val __typename: String
        get() = "string"
      override val displayName: String
        get() = "string"
      override val displayNameTier: String?
        get() = "string"
      override val tierDescription: String?
        get() = "string"
      override val typeOfContract: String
        get() = "string"
      override val partner: String?
        get() = "string"
      override val termsVersion: String
        get() = "string"
      override val perils: List<MoveIntentQuotesFragment.HomeQuote.ProductVariant.Peril>
        get() = emptyList()
      override val insurableLimits: List<MoveIntentQuotesFragment.HomeQuote.ProductVariant.InsurableLimit>
        get() = emptyList()
      override val documents: List<MoveIntentQuotesFragment.HomeQuote.ProductVariant.Document>
        get() = emptyList()

    }
  override val addons: List<MoveIntentQuotesFragment.HomeQuote.Addon>?
    get() = emptyList()
}

private val homeQuote2 = object: MoveIntentQuotesFragment.HomeQuote {
  override val id: String
    get() = "id1"
  override val premium: MoveIntentQuotesFragment.HomeQuote.Premium
    get() = object: MoveIntentQuotesFragment.HomeQuote.Premium {
      override val __typename: String
        get() = "str"
      override val amount: Double
        get() = 30.0
      override val currencyCode: CurrencyCode
        get() = CurrencyCode.SEK
    }
  override val startDate: LocalDate
    get() = LocalDate(2025,6,6)
  override val defaultChoice: Boolean
    get() = true
  override val tierName: String
    get() = "tierName"
  override val tierLevel: Int
    get() = 2
  override val deductible: MoveIntentQuotesFragment.HomeQuote.Deductible?
    get() = null
  override val displayItems: List<MoveIntentQuotesFragment.HomeQuote.DisplayItem>
    get() = emptyList()
  override val exposureName: String
    get() = "exposure"
  override val productVariant: MoveIntentQuotesFragment.HomeQuote.ProductVariant
    get() = object : MoveIntentQuotesFragment.HomeQuote.ProductVariant {
      override val __typename: String
        get() = "string"
      override val displayName: String
        get() = "string"
      override val displayNameTier: String?
        get() = "string"
      override val tierDescription: String?
        get() = "string"
      override val typeOfContract: String
        get() = "string"
      override val partner: String?
        get() = "string"
      override val termsVersion: String
        get() = "string"
      override val perils: List<MoveIntentQuotesFragment.HomeQuote.ProductVariant.Peril>
        get() = emptyList()
      override val insurableLimits: List<MoveIntentQuotesFragment.HomeQuote.ProductVariant.InsurableLimit>
        get() = emptyList()
      override val documents: List<MoveIntentQuotesFragment.HomeQuote.ProductVariant.Document>
        get() = emptyList()

    }
  override val addons: List<MoveIntentQuotesFragment.HomeQuote.Addon>?
    get() = emptyList()
}
