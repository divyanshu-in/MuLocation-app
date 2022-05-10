package com.divyanshu_in.multiuserlocationsharingapp.data

import kotlinx.serialization.Serializable

@Serializable
data class MessageBody(
    val lat: Double? = null,
    val long: Double? = null,
    val username: String? = null,
    val msg_type: MessageType = MessageType.LOCATION_MSG,
    val message: String? = null,
    val markerDetails: MarkerDetails? = null
)

@kotlinx.serialization.Serializable
data class MarkerDetails(
    val markerId: String,
    val title: String,
    val colorHue: Float,
    val lat: Double,
    val long: Double,
    val action: MarkerAction = MarkerAction.ADD
)

enum class MessageType{
    LOCATION_MSG, GENERAL_MSG, MARKER_MSG
}