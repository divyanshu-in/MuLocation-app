package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.DisplayMetrics
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divyanshu_in.multiuserlocationsharingapp.Constants
import com.divyanshu_in.multiuserlocationsharingapp.data.Directions
import com.divyanshu_in.multiuserlocationsharingapp.data.MessageBody
import com.divyanshu_in.multiuserlocationsharingapp.data.MessageType
import com.divyanshu_in.multiuserlocationsharingapp.data.getUsername
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.net.URI
import java.util.*
import kotlin.math.atan2

class MainViewModel(): ViewModel(){
    private var angleBetweenCorners1: Double? = null
    private var angleBetweenCorners2: Double? = null

    var actionState by mutableStateOf(ActionState.DEFAULT)
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var userName: String
    private var isConnected = false
    var linkToServerState by mutableStateOf("")
    var sharableLinkState by mutableStateOf("")

    var stateOfMarkerPositions by mutableStateOf(mapOf<String, LocationData>())
    var stateOfNonMapMarkerPositions by mutableStateOf(mapOf<String, Double>())

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

    private fun calculateAngleBetweenTheCorners(visibleReg: VisibleRegion){
        if(angleBetweenCorners1 == null){
            angleBetweenCorners1 = visibleReg.latLngBounds.center.getAngleBetween(visibleReg.farLeft, visibleReg.farRight)
            angleBetweenCorners2 = Math.PI - angleBetweenCorners1!!
            Timber.e(angleBetweenCorners1.toString())
            Timber.e(angleBetweenCorners2.toString())

        }
    }

    suspend fun getDirectionOfMarker(visibleRegion: VisibleRegion?){
        withContext(Dispatchers.IO) {

            val outOfBoundMarkers =
                stateOfMarkerPositions.filter { visibleRegion?.latLngBounds?.contains(it.value.latLng) == false }

            outOfBoundMarkers.forEach {
                val mutableMap = stateOfNonMapMarkerPositions.toMutableMap()
                mutableMap[it.key] = visibleRegion?.nearRight?.getAngleBetween(it.value.latLng,
                    visibleRegion.nearLeft) as Double
                Timber.e(mutableMap[it.key].toString())
                stateOfNonMapMarkerPositions = mutableMap

//            }else{
//                val newMap = stateOfNonMapMarkerPositions.toMutableMap()
//                newMap.remove(it.key)
//                stateOfNonMapMarkerPositions = newMap
//            }
            }

        }
    }


    fun getPaddingAndDirection(latLng: LatLng, visibleReg: VisibleRegion, context: Context, user: String){

        calculateAngleBetweenTheCorners(visibleReg)

        val angle = visibleReg.latLngBounds.center.getAngleBetween(visibleReg.farLeft, visibleReg.farRight)

        val detailsOfMarker = when(angle){
            0.0 -> {Pair(0.dp, Directions.NORTH_WEST)}
            in 0.0..angleBetweenCorners1!! -> {Pair(latLng.getPaddingForPointOnBounds(visibleReg.farLeft, visibleReg.farRight, context), Directions.NORTH)}
            angleBetweenCorners1 -> Pair(0.dp, Directions.NORTH_EAST)
            in angleBetweenCorners1!!..Math.PI -> {Pair(latLng.getPaddingForPointOnBounds(visibleReg.farRight, visibleReg.nearRight, context), Directions.WEST)}
            Math.PI -> Pair(0.dp, Directions.SOUTH_EAST)
            in Math.PI..(Math.PI + angleBetweenCorners1!!) -> {Pair(latLng.getPaddingForPointOnBounds(visibleReg.nearRight, visibleReg.nearLeft, context), Directions.SOUTH)}
            (Math.PI + angleBetweenCorners1!!) -> Pair(0.dp, Directions.SOUTH_WEST)
            in (Math.PI + angleBetweenCorners1!!)..2*Math.PI -> {Pair(latLng.getPaddingForPointOnBounds(visibleReg.nearLeft, visibleReg.farLeft, context), Directions.EAST)}
            else -> {null}
        }

        Timber.e(detailsOfMarker.toString())

        detailsOfMarker?.let {
            val newMap = stateOfNonMapMarkerPositions.toMutableMap()
//            newMap[user] = it
            stateOfNonMapMarkerPositions = newMap
        }
    }


    private fun LatLng.getAngleBetween(latLng1: LatLng, latLng2: LatLng): Double {
        val angle1 = Math.atan2(
            this.longitude - latLng1.longitude,
            this.latitude - latLng1.latitude
        )

        val angle2 = atan2(
            this.longitude - latLng2.longitude,
            this.latitude - latLng2.latitude
        )

        return angle1 - angle2
    }

    private fun LatLng.getPaddingForPointOnBounds(latLng1: LatLng, latLng2: LatLng, context: Context): Dp {
        val latLngOfPointOnRect = this.getPerpendicularPointOnBounds(latLng1, latLng2)
        val padding = context.resources.displayMetrics.widthPixels *(latLngOfPointOnRect.getDistanceFrom(latLng1)/latLng1.getDistanceFrom(latLng2))
        return padding.dp
    }

    private fun LatLng.getDistanceFrom(latLng: LatLng): Float {
        val locationA = Location("point A")
        val locationB = Location("point B")
        locationA.apply {
            latitude = this@getDistanceFrom.latitude
            longitude = this@getDistanceFrom.longitude
        }

        locationB.apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
        }
        return locationA.distanceTo(locationB)
    }

    private fun LatLng.getPerpendicularPointOnBounds(latLng1: LatLng, latLng2: LatLng): LatLng {

        val latLngP = this

        val a1 = latLng1.longitude - latLng2.longitude
        val b1b2 = latLng2.latitude - latLng1.latitude
        val c1 = latLng2.latitude*latLng1.longitude - latLng1.latitude*latLng2.longitude
        val a2 = -(latLng1.longitude - latLng2.longitude)

        val c2 = b1b2*latLngP.latitude - a2*latLngP.longitude

        val determinant = a1*b1b2 - a2*b1b2

        val x = (c1*b1b2 - c2*b1b2)/determinant
        val y = (a1*c2 - a2*c1)/determinant

        return LatLng(x, y)
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
    val latLng: LatLng, val colorHue: Float,
)