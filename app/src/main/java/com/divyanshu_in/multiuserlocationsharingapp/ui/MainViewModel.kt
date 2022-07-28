package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divyanshu_in.multiuserlocationsharingapp.Constants
import com.divyanshu_in.multiuserlocationsharingapp.data.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.VisibleRegion
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


class MainViewModel: ViewModel(){

    private var fusedLocationClient: FusedLocationProviderClient? = null

    var userLocationState by mutableStateOf(LatLng(0.0, 0.0))

    var actionState by mutableStateOf(ActionState.DEFAULT)
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var userName: String

    private var isConnected by mutableStateOf(false)
    var linkToServerState by mutableStateOf("")
    var sharableLinkState by mutableStateOf("")
    private var isNavigatedToUserLoc = false

    var stateOfShareableMarkers by mutableStateOf( listOf<MarkerDetails>())

    var stateOfPolygons = mutableStateOf(listOf<LatLng>())



    var stateOfMessagesReceived by mutableStateOf(listOf<Pair<String, String>>())

    var stateOfMarkerPositions by mutableStateOf(mapOf<String, LocationData>())

    fun generateServerLinkAndConnect(context: Context){
        viewModelScope.launch {
            context.getUsername.collect{
                userName = it.toString()
                val uuid = UUID.randomUUID()
                linkToServerState = Constants.BASE_URL + it + "_${uuid}"
                sharableLinkState = Constants.SHARABLE_URL + it + "_${uuid}"
                connectToWebSocket(linkToServerState)
                addLocationUpdateListener(context)
            }
        }
    }

    fun addShareableMarker(pos: LatLng){

        val colorHue = Random().nextInt(360).toFloat()
        val markerId = UUID.randomUUID().toString()

        val markerDetails = MarkerDetails(markerId, "", colorHue, pos.latitude, pos.longitude, order = stateOfShareableMarkers.size + 1)

        val mutableListOfMarkers = stateOfShareableMarkers.toMutableList().also {
            it.add(markerDetails)
        }
        stateOfShareableMarkers = mutableListOfMarkers

        sendMarkerDetails(markerDetails)
        generatePolygons()
    }

    private fun generatePolygons(){
        val polygonState = stateOfShareableMarkers.sortedBy { it.order }.map { LatLng(it.lat, it.long) }
        val newList = stateOfPolygons.value.toMutableList()
        newList.clear()
        newList.addAll(polygonState)
        stateOfPolygons.value = newList
    }


    fun sendMarkerDetails(markerDetails: MarkerDetails){
        if(isConnected){
            val messageBodyObject = MessageBody(username = this@MainViewModel.userName,  msg_type = MessageType.MARKER_MSG, markerDetails = markerDetails)
            webSocketClient.send(messageBodyObject.parseToJsonString())
        }
    }

    @SuppressLint("MissingPermission")
    fun addLocationChangeListener(context: Context){

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient?.lastLocation?.addOnSuccessListener {
            it?.let {
                Timber.e("lastLoc")
                val locOfUser = LatLng(it.latitude, it.longitude)
                userLocationState = locOfUser
                fusedLocationClient = null
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun addLocationUpdateListener(context: Context){

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1f
        ) { location ->
            Timber.e(location.toString())
            val latLng = LatLng(location.latitude, location.longitude)
            sendLocation(latLng)
            userLocationState = latLng
        }

    }


    //  calculates
    suspend fun getDirectionOfMarker(visibleRegion: VisibleRegion?){
        withContext(Dispatchers.Default) {

            val mutableMap = stateOfMarkerPositions.toMutableMap()

            stateOfMarkerPositions.forEach {

                if(visibleRegion?.latLngBounds?.contains(it.value.latLng) == false){
                    mutableMap[it.key]?.apply {
                        distance = getDistanceWithApprUnit(userLocationState.getDistanceFrom(it.value.latLng).toInt())
                        angleFromAxis = visibleRegion.nearRight.getAngleBetween(it.value.latLng, visibleRegion.nearLeft)
                        isVisible = false
                    }
                }else{
                    mutableMap[it.key]?.apply {
                        isVisible = true
                    }
                }
            }

            stateOfMarkerPositions = mutableMap
        }
    }

//    returns distance in appropriate unit (km/m).
    suspend fun getDistanceWithApprUnit(distance: Int): String {
        when(distance){
            in 0..100 -> {
                return "$distance'm'"
            }
            else -> return "${distance/1000}km"
        }
    }

    private fun LatLng.getAngleBetween(latLng1: LatLng, latLng2: LatLng): Float {
        val angle1 = atan2(
            this.longitude - latLng1.longitude,
            this.latitude - latLng1.latitude
        )

        val angle2 = atan2(
            this.longitude - latLng2.longitude,
            this.latitude - latLng2.latitude
        )

        return ((angle1 - angle2)*180/Math.PI).toFloat() - 90
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
                    val username = messageBody?.username.toString()
                    when (messageBody?.msg_type) {
                        MessageType.LOCATION_MSG -> {
                            val newMap = stateOfMarkerPositions.toMutableMap()
                            var color = newMap[username]?.color
                            val colorHue = rnd.nextInt(360).toFloat()
                            if(color == null)
                                color = Color.hsv(colorHue, 1f, 1f)

                            newMap[username] = LocationData(LatLng(messageBody.lat!!, messageBody.long!!), color = color, colorHue = colorHue)
                            stateOfMarkerPositions = newMap
                        }
                        MessageType.GENERAL_MSG -> {
                            if(messageBody.message?.contains("You are connected") == false){
                                val newList = stateOfMessagesReceived.toMutableList()
                                newList.add(Pair(username, messageBody.message.toString()))
                                stateOfMessagesReceived = newList
                                Timber.e(stateOfMessagesReceived.toString())
                            }
                        }
                        MessageType.MARKER_MSG ->{
                            when(messageBody.markerDetails?.action){
                                MarkerAction.DELETE -> {
                                    stateOfShareableMarkers.toMutableList().also { mutableList ->
                                        mutableList.removeIf { it.markerId == messageBody.markerDetails.markerId }
                                    }
                                }
                                MarkerAction.ADD -> {
                                    stateOfShareableMarkers.toMutableList().also { mutableList ->
                                        mutableList.add(messageBody.markerDetails)
                                    }
                                }
                                MarkerAction.UPDATE -> {}

                                null -> {}
                            }
                        }
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

    fun sendTextMessage(text: String){
        if(isConnected){
            val newList = stateOfMessagesReceived.toMutableList()
            newList.add(Pair(userName, text))
            stateOfMessagesReceived = newList

            val messageBodyObject = MessageBody(username = this@MainViewModel.userName, message = text, msg_type = MessageType.GENERAL_MSG)
            webSocketClient.send(messageBodyObject.parseToJsonString())
        }
    }

    private fun sendLocation(latLng: LatLng){
        Timber.e("sending loc! $latLng")
        if(isConnected){
            val messageBodyObject = MessageBody(latLng.latitude, latLng.longitude, this@MainViewModel.userName)
            webSocketClient.send(messageBodyObject.parseToJsonString())
        }
    }

    fun MessageBody.parseToJsonString() = Json.encodeToString(this)

    fun String.parseToMessageBody() = Json.decodeFromString<MessageBody>(this)

    fun updateMarker(markerDetails: MarkerDetails) {
        sendMarkerDetails(markerDetails.also {
            it.action = MarkerAction.UPDATE
        })

        stateOfShareableMarkers.toMutableList().also { mutableList ->
            mutableList.removeIf { it.markerId == markerDetails.markerId }
            mutableList.add(markerDetails)
            stateOfShareableMarkers = mutableList
        }
    }

    fun deleteMarker(markerDetails: MarkerDetails) {
        sendMarkerDetails(markerDetails.also { details ->
            details.action = MarkerAction.DELETE
        })

        stateOfShareableMarkers.toMutableList().also { mutableList ->
            mutableList.removeIf { it.markerId == markerDetails.markerId }
            stateOfShareableMarkers = mutableList
        }
    }
}

data class LocationData(
    val latLng: LatLng,
    val color: Color,
    var isVisible: Boolean = true,
    var angleFromAxis: Float? = null,
    var colorHue: Float,
    var distance: String? = null,
)