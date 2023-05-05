import com.feature.changeaddress.data.AddressId
import com.feature.changeaddress.data.AddressInput
import com.feature.changeaddress.data.MoveIntentId
import kotlinx.datetime.LocalDate

enum class ApartmentOwnerType {
  RENT, OWN
}

fun ApartmentOwnerType.toDisplayName() = when (this) {
  ApartmentOwnerType.RENT -> "Hyresrätt"
  ApartmentOwnerType.OWN -> "Bostadsrätt"
}

data class CreateQuoteInput(
  val moveIntentId: MoveIntentId,
  val moveFromAddressId: AddressId,
  val address: AddressInput,
  val movingDate: LocalDate,
  val numberCoInsured: Int,
  val squareMeters: Int,
  val apartmentOwnerType: ApartmentOwnerType,
  val isStudent: Boolean,
)
