package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divyanshu_in.multiuserlocationsharingapp.Constants
import com.divyanshu_in.multiuserlocationsharingapp.data.MessageBody
import com.divyanshu_in.multiuserlocationsharingapp.data.MessageType
import com.divyanshu_in.multiuserlocationsharingapp.data.getUsername
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URI
import java.util.*

class MainViewModel(): ViewModel(){
    var actionState by mutableStateOf(ActionState.DEFAULT)
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var userName: String
    private var isConnected = false
    var linkToServerState by mutableStateOf("")
    var sharableLinkState by mutableStateOf("")

    var stateOfMarkerPositions by mutableStateOf(mapOf<String, LocationData>())

    fun generateServerLinkAndConnect(context: Context){
        viewModelScope.launch {
            context.getUsername.collect{
                userName = it.toString()
                val uuid = UUID.randomUUID()
                linkToServerState = Constants.BASE_URL + it + "_${uuid}"
                sharableLinkState = Constants.SHARABLE_URL + it + "_${uuid}"
                connectToWebSocket(linkToServerState)
            }
        }
    }

    fun joinWebSocketFromDeepLink(serverId: String){
        linkToServerState = Constants.BASE_URL + serverId
        connectToWebSocket(linkToServerState)
    }


    private fun connectToWebSocket(serverUrl: String){
        val rnd = Random()
        if(!::webSocketClient.isInitialized){

            webSocketClient = object: WebSocketClient(URI(serverUrl)){

                override fun onOpen(handshakedata: ServerHandshake?) {
                    Timber.e("connection opened!")
                    webSocketClient.send(MessageBody(username = userName, msg_type = MessageType.GENERAL_MSG).parseToJsonString())
                    isConnected = true
                }

                override fun onMessage(message: String?) {
                    val messageBody = message?.parseToMessageBody()
                    if (messageBody?.msg_type == MessageType.LOCATION_MSG){
                        val newMap = stateOfMarkerPositions.toMutableMap()
                        var hue = newMap[messageBody.username.toString()]?.colorHue

                        if(hue == null)
                            hue = rnd.nextInt(360).toFloat()

                        newMap[messageBody.username.toString()] = LocationData(LatLng(messageBody.lat!!, messageBody.long!!), hue)
                        stateOfMarkerPositions = newMap
                    }

                    Timber.e(message)
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    isConnected = false
                    Timber.e("closed, $reason, $code, $remote")
                }

                override fun onError(ex: Exception?) {
                    ex?.printStackTrace()
                }
            }
            webSocketClient.connect()
        }
    }

    fun sendLocation(latLng: LatLng){
        if(isConnected){
            val messageBodyObject = MessageBody(latLng.latitude, latLng.longitude, this@MainViewModel.userName)
            webSocketClient.send(messageBodyObject.parseToJsonString())
        }
    }

    fun MessageBody.parseToJsonString() = Json.encodeToString(this)

    fun String.parseToMessageBody() = Json.decodeFromString<MessageBody>(this)
}

data class LocationData(
    val latLng: LatLng, val colorHue: Float
)