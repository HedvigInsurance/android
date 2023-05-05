import com.feature.changeaddress.data.AddressId
import com.feature.changeaddress.data.AddressInput
import com.feature.changeaddress.data.MoveIntentId
import kotlinx.datetime.LocalDate

enum class HousingType {
  APARTMENT_RENT, APARTMENT_OWN, VILLA
}

fun HousingType.toDisplayName() = when (this) {
  HousingType.APARTMENT_RENT -> "Hyresrätt"
  HousingType.APARTMENT_OWN -> "Bostadsrätt"
  HousingType.VILLA -> "Villa"
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
