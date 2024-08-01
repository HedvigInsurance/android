package com.hedvig.android.feature.changeaddress.data

import java.util.UUID
import kotlinx.serialization.Serializable
import octopus.type.MoveExtraBuildingType

@Serializable
internal data class ExtraBuilding(
  val id: String = UUID.randomUUID().toString(),
  val size: Int,
  val type: ExtraBuildingType,
  val hasWaterConnected: Boolean,
)

internal enum class ExtraBuildingType {
  GARAGE,
  CARPORT,
  SHED,
  STOREHOUSE,
  FRIGGEBOD,
  ATTEFALL,
  OUTHOUSE,
  GUESTHOUSE,
  GAZEBO,
  GREENHOUSE,
  SAUNA,
  BARN,
  BOATHOUSE,
  OTHER,
  UNKNOWN,
}

internal fun ExtraBuildingType.stringRes(): Int = when (this) {
  ExtraBuildingType.GARAGE -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_GARAGE_LABEL
  ExtraBuildingType.CARPORT -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_CARPORT_LABEL
  ExtraBuildingType.SHED -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_SHED_LABEL
  ExtraBuildingType.STOREHOUSE -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_STOREHOUSE_LABEL
  ExtraBuildingType.FRIGGEBOD -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_FRIGGEBOD_LABEL
  ExtraBuildingType.ATTEFALL -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_ATTEFALL_LABEL
  ExtraBuildingType.OUTHOUSE -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_OUTHOUSE_LABEL
  ExtraBuildingType.GUESTHOUSE -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_GUESTHOUSE_LABEL
  ExtraBuildingType.GAZEBO -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_GAZEBO_LABEL
  ExtraBuildingType.GREENHOUSE -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_GREENHOUSE_LABEL
  ExtraBuildingType.SAUNA -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_SAUNA_LABEL
  ExtraBuildingType.BARN -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_BARN_LABEL
  ExtraBuildingType.BOATHOUSE -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_BOATHOUSE_LABEL
  ExtraBuildingType.OTHER -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_OTHER_LABEL
  ExtraBuildingType.UNKNOWN -> hedvig.resources.R.string.FIELD_EXTRA_BUIDLINGS_OTHER_LABEL
}

internal fun ExtraBuildingType.toMoveExtraBuildingType() = when (this) {
  ExtraBuildingType.GARAGE -> MoveExtraBuildingType.GARAGE
  ExtraBuildingType.CARPORT -> MoveExtraBuildingType.CARPORT
  ExtraBuildingType.SHED -> MoveExtraBuildingType.SHED
  ExtraBuildingType.STOREHOUSE -> MoveExtraBuildingType.STOREHOUSE
  ExtraBuildingType.FRIGGEBOD -> MoveExtraBuildingType.FRIGGEBOD
  ExtraBuildingType.ATTEFALL -> MoveExtraBuildingType.ATTEFALL
  ExtraBuildingType.OUTHOUSE -> MoveExtraBuildingType.OUTHOUSE
  ExtraBuildingType.GUESTHOUSE -> MoveExtraBuildingType.GUESTHOUSE
  ExtraBuildingType.GAZEBO -> MoveExtraBuildingType.GAZEBO
  ExtraBuildingType.GREENHOUSE -> MoveExtraBuildingType.GREENHOUSE
  ExtraBuildingType.SAUNA -> MoveExtraBuildingType.SAUNA
  ExtraBuildingType.BARN -> MoveExtraBuildingType.BARN
  ExtraBuildingType.BOATHOUSE -> MoveExtraBuildingType.BOATHOUSE
  ExtraBuildingType.OTHER -> MoveExtraBuildingType.OTHER
  ExtraBuildingType.UNKNOWN -> MoveExtraBuildingType.UNKNOWN__
}

internal fun MoveExtraBuildingType.toExtraBuildingType() = when (this) {
  MoveExtraBuildingType.GARAGE -> ExtraBuildingType.GARAGE
  MoveExtraBuildingType.CARPORT -> ExtraBuildingType.CARPORT
  MoveExtraBuildingType.SHED -> ExtraBuildingType.SHED
  MoveExtraBuildingType.STOREHOUSE -> ExtraBuildingType.STOREHOUSE
  MoveExtraBuildingType.FRIGGEBOD -> ExtraBuildingType.FRIGGEBOD
  MoveExtraBuildingType.ATTEFALL -> ExtraBuildingType.ATTEFALL
  MoveExtraBuildingType.OUTHOUSE -> ExtraBuildingType.OUTHOUSE
  MoveExtraBuildingType.GUESTHOUSE -> ExtraBuildingType.GUESTHOUSE
  MoveExtraBuildingType.GAZEBO -> ExtraBuildingType.GAZEBO
  MoveExtraBuildingType.GREENHOUSE -> ExtraBuildingType.GREENHOUSE
  MoveExtraBuildingType.SAUNA -> ExtraBuildingType.SAUNA
  MoveExtraBuildingType.BARN -> ExtraBuildingType.BARN
  MoveExtraBuildingType.BOATHOUSE -> ExtraBuildingType.BOATHOUSE
  MoveExtraBuildingType.OTHER -> ExtraBuildingType.OTHER
  MoveExtraBuildingType.UNKNOWN__ -> ExtraBuildingType.UNKNOWN
}
