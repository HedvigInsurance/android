package com.hedvig.authlib.authservice.model

import kotlinx.serialization.Serializable

@Serializable
internal data class RevokeTokenInput(val token: String)
