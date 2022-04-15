package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.content.Context
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
import java.lang.Exception
import java.net.URI
import java.util.*

class MainViewModel(): ViewModel(){
    var actionState by mutableStateOf(ActionState.DEFAULT)
    private lateinit var webSocketClient: WebSocketClientImpl
    private lateinit var userName: String
    private var isConnected = false
    var linkToServerState by mutableStateOf("")

    var stateOfMarkerPositions by mutableStateOf(mapOf<String, LocationData>())

    fun generateServerLinkAndConnect(context: Context){
        viewModelScope.launch {
            context.getUsername.collect{
                userName = it.toString()
                linkToServerState = Constants.BASE_URL + it + "_${UUID.randomUUID()}"
                connectToWebSocket(linkToServerState)
            }
        }
    }

    private fun connectToWebSocket(serverUrl: String){
        val rnd = Random()
        if(!::webSocketClient.isInitialized){

            webSocketClient = object: WebSocketClientImpl(URI(serverUrl)){
                override fun onOpen(handshakedata: ServerHandshake?) {
                    super.onOpen(handshakedata)
                    Timber.e("connection opened!")
                    webSocketClient.send(MessageBody(username = userName, msg_type = MessageType.GENERAL_MSG).parseToJsonString())
                    isConnected = true
                }

                override fun onMessage(message: String?) {
                    super.onMessage(message)
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
                    super.onClose(code, reason, remote)
                    isConnected = false
                    Timber.e("closed, $reason, $code, $remote")
                }

                override fun onError(ex: Exception?) {
                    super.onError(ex)
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



open class WebSocketClientImpl(uri: URI) : WebSocketClient(uri){
    override fun onOpen(handshakedata: ServerHandshake?) {
    }

    override fun onMessage(message: String?) {
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
    }

    override fun onError(ex: Exception?) {
    }

}

data class LocationData(
    val latLng: LatLng, val colorHue: Float
)