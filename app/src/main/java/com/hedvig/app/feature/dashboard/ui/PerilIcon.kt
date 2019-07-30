package com.hedvig.app.feature.dashboard.ui

import androidx.annotation.DrawableRes
import com.hedvig.app.R

object PerilIcon {
    @DrawableRes
    fun from(id: String) = when (id) {
        "ME.LEGAL" -> R.drawable.ic_legal
        "ME.ASSAULT" -> R.drawable.ic_assault
        "ME.TRAVEL.SICK" -> R.drawable.ic_illness
        "ME.TRAVEL.LUGGAGE.DELAY" -> R.drawable.ic_luggage_delay
        "HOUSE.BRF.FIRE",
        "HOUSE.RENT.FIRE",
        "HOUSE.SUBLET.BRF.FIRE",
        "HOUSE.SUBLET.RENT.FIRE" -> R.drawable.ic_fire_red
        "HOUSE.BRF.APPLIANCES",
        "HOUSE.RENT.APPLIANCES",
        "HOUSE.SUBLET.BRF.APPLIANCES" -> R.drawable.ic_appliances
        "HOUSE.BRF.WEATHER",
        "HOUSE.RENT.WEATHER",
        "HOUSE.SUBLET.BRF.WEATHER",
        "HOUSE.SUBLET.RENT.WEATHER" -> R.drawable.ic_weather_red
        "HOUSE.BRF.WATER",
        "HOUSE.RENT.WATER",
        "HOUSE.SUBLET.BRF.WATER",
        "HOUSE.SUBLET.RENT.WATER" -> R.drawable.ic_water_red
        "HOUSE.BREAK-IN" -> R.drawable.ic_break_in
        "HOUSE.DAMAGE" -> R.drawable.ic_vandalism_red
        "STUFF.CARELESS" -> R.drawable.ic_accidental_damage
        "STUFF.THEFT" -> R.drawable.ic_theft
        "STUFF.DAMAGE" -> R.drawable.ic_vandalism_green
        "STUFF.BRF.FIRE",
        "STUFF.RENT.FIRE",
        "STUFF.SUBLET.BRF.FIRE",
        "STUFF.SUBLET.RENT.FIRE" -> R.drawable.ic_fire_green
        "STUFF.BRF.WATER",
        "STUFF.RENT.WATER",
        "STUFF.SUBLET.BRF.WATER",
        "STUFF.SUBLET.RENT.WATER" -> R.drawable.ic_water_green
        "STUFF.BRF.WEATHER" -> R.drawable.ic_weather_green
        else -> R.drawable.ic_vandalism_green
    }
}
