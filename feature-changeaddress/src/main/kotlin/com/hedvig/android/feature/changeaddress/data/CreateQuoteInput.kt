import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.AddressInput
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import kotlinx.datetime.LocalDate

enum class HousingType {
  APARTMENT_RENT, APARTMENT_OWN, VILLA
}

fun HousingType.displayNameResource() = when (this) {
  HousingType.APARTMENT_RENT -> hedvig.resources.R.string.CHANGE_ADDRESS_APARTMENT_RENT_LABEL
  HousingType.APARTMENT_OWN -> hedvig.resources.R.string.CHANGE_ADDRESS_APARTMENT_OWN_LABEL
  HousingType.VILLA -> hedvig.resources.R.string.CHANGE_ADDRESS_VILLA_LABEL
}

data class CreateQuoteInput(
  val moveIntentId: MoveIntentId,
  val moveFromAddressId: AddressId,
  val address: AddressInput,
  val movingDate: LocalDate,
  val numberCoInsured: Int,
  val squareMeters: Int,
  val apartmentOwnerType: HousingType,
  val isStudent: Boolean,
)
