package com.divyanshu_in.multiuserlocationsharingapp.data

import kotlinx.serialization.Serializable

@Serializable
data class MessageBody(
    val lat: Double? = null,
    val long: Double? = null,
    val username: String? = null,
    val msg_type: MessageType = MessageType.LOCATION_MSG,
    val message: String? = null
)

enum class MessageType{
    LOCATION_MSG, GENERAL_MSG
}