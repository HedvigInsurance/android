package com.hedvig.app.feature.embark

sealed class Response {
    data class SingleResponse(
        val text: String,
    ) : Response()

    data class GroupedResponse(
        val title: String?,
        val groups: List<String>,
    ) : Response()
}
