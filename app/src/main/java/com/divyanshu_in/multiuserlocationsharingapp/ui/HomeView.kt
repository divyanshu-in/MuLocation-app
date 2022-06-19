package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.divyanshu_in.multiuserlocationsharingapp.R
import com.divyanshu_in.multiuserlocationsharingapp.data.ActionState
import com.divyanshu_in.multiuserlocationsharingapp.data.MarkerDetails
import com.divyanshu_in.multiuserlocationsharingapp.utils.HorizontalSpacer
import com.divyanshu_in.multiuserlocationsharingapp.utils.VerticalSpacer
import com.divyanshu_in.multiuserlocationsharingapp.utils.copyTextToClipBoard
import com.divyanshu_in.multiuserlocationsharingapp.utils.shareLinkVia
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch


val permissionList = arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)


@Composable
fun HomeView(context: Context, viewModel: MainViewModel, serverId: String?){
    HomeDrawer(viewModel = viewModel, context, serverId)
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapView(context: Context, viewModel: MainViewModel, serverId: String?) {

    serverId?.let {
        viewModel.actionState = ActionState.SERVER_JOINED
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissionList)

    LaunchedEffect(key1 = permissionState.allPermissionsGranted){
        if(!permissionState.allPermissionsGranted){
            permissionState.launchMultiplePermissionRequest()
        }
    }

    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 20f)
    }

    LaunchedEffect(key1 = cameraPositionState.position){
        launch {
            val visibleRegion = cameraPositionState.projection?.visibleRegion
            viewModel.getDirectionOfMarker(visibleRegion)
        }
    }

    var isMovedOnLastKnownLocation by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = viewModel.userLocationState){
        if(!isMovedOnLastKnownLocation){
            cameraPositionState.move(CameraUpdateFactory.newLatLng(viewModel.userLocationState))
            isMovedOnLastKnownLocation = true
        }
    }

    LaunchedEffect(key1 = permissionState.allPermissionsGranted){
        if(permissionState.allPermissionsGranted){
            viewModel.addLocationChangeListener(context)
        }else{
            Toast.makeText(context, "Grant All Permissions First!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        var markerDetailsState: MarkerDetails? by remember{ mutableStateOf(null) }
        var markerActionDialogVisibilityState by remember{ mutableStateOf(false) }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = permissionState.allPermissionsGranted),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapLongClick = { loc ->
                viewModel.addShareableMarker(loc)
            }
        ){

            viewModel.stateOfMarkerPositions.forEach { userLocObject ->
                val markerState = rememberMarkerState(position = userLocObject.value.latLng)
                Marker(state = markerState , title = userLocObject.key, icon = BitmapDescriptorFactory.defaultMarker(userLocObject.value.colorHue))
            }

            viewModel.stateOfShareableMarkers.forEach { markerDetails ->
                val markerState = rememberMarkerState(position = LatLng(markerDetails.lat, markerDetails.long))

                MarkerInfoWindow(onInfoWindowClick = {
                    markerDetailsState = markerDetails
                    markerActionDialogVisibilityState = true
                }, draggable = true, visible = true,
                    state = markerState,
                    icon = BitmapDescriptorFactory.defaultMarker(markerDetails.colorHue), onClick = {
                        return@MarkerInfoWindow false
                    }){
                    Card(backgroundColor = Color.White, modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)) {
                        Text(markerDetails.title)
                        HorizontalSpacer(2)
                        Text(text = "tap for more options", fontStyle = FontStyle.Italic, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(32.dp))

                    }
                }
            }
        }



        Column(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp)) {

            val stateOfOutOfBoundMarkers by derivedStateOf { viewModel.stateOfMarkerPositions.filter{!it.value.isVisible} }

            stateOfOutOfBoundMarkers.forEach { locationOfUser ->
                FloatingActionButton(
                    onClick = {
                              cameraPositionState.move(CameraUpdateFactory.newLatLng(locationOfUser.value.latLng))
                    },
                    backgroundColor = locationOfUser.value.color,
                    modifier = Modifier.rotate((locationOfUser.value.angleFromAxis!!)))
                    {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "")
                            locationOfUser.value.distance?.let {
                                Text(it)
                            }
                        }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        when(viewModel.actionState){
            ActionState.DEFAULT -> {
                ButtonGroup(context, modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp), viewModel)

            }
            ActionState.SERVER_JOINED -> {
                if(permissionState.allPermissionsGranted){
                    viewModel.joinWebSocketFromDeepLink(serverId!!)


                }else{
                    LaunchedEffect(key1 = true){
                        permissionState.launchMultiplePermissionRequest()
                    }
                }
            }
            ActionState.SERVER_CREATED -> {

                var dialogVisibilityState by remember{ mutableStateOf(true) }
                if(dialogVisibilityState){
                    Dialog(onDismissRequest = { dialogVisibilityState = false }) {
                        GetLinkRow(context, modifier = Modifier.align(Alignment.Center), viewModel)
                    }
                }else{
                    VerticalSpacer(height = 80)
                    Card(shape = CircleShape, modifier = Modifier.padding(16.dp)){
                        IconButton(onClick = {dialogVisibilityState = true}) {
                            Icon(
                                painterResource(R.drawable.ic_round_link),
                                contentDescription = "Show Link"
                            )
                        }
                    }
                }
            }
        }

        if(markerActionDialogVisibilityState){
            markerDetailsState?.let {
                Dialog(onDismissRequest = { markerActionDialogVisibilityState = false }) {
                    MarkerActionDialog(viewModel = viewModel, markerDetails = it) {
                        markerActionDialogVisibilityState = false
                    }
                }
            }
        }
    }
}


@Composable
fun MarkerActionDialog(viewModel: MainViewModel, markerDetails: MarkerDetails, dismissDialogCallback: () -> Unit){
    Card(modifier = Modifier.padding(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Edit Title", color = Color.Gray)
            VerticalSpacer(height = 2)
            var textState by remember{mutableStateOf(markerDetails.title)}
            TextField(value = textState, onValueChange = {textState = it})
            VerticalSpacer(height = 8)
            Row() {
                IconButton(
                    onClick =
                    {
                        viewModel.updateMarker(markerDetails.also {
                            it.title = textState
                        })
                        dismissDialogCallback.invoke()
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.Done, contentDescription = null, tint = Color.Green)
                }

                IconButton(
                    onClick = {
                        viewModel.deleteMarker(markerDetails)
                        dismissDialogCallback.invoke()
                    }) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ButtonGroup(context: Context, modifier: Modifier, viewModel: MainViewModel){
    val permissionState = rememberMultiplePermissionsState(permissions = permissionList)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        FloatingActionButton(onClick = {
        }, shape = RoundedCornerShape(16.dp)){
            Text(text = "Join Server", modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(onClick = {
            if(permissionState.allPermissionsGranted){
                viewModel.generateServerLinkAndConnect(context)
                viewModel.actionState = ActionState.SERVER_CREATED
            }else{
                permissionState.launchMultiplePermissionRequest()
            }
        }, shape = RoundedCornerShape(16.dp)){
            Text(text = "Create Server", modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 8.dp))
        }
    }
}

@Composable
fun GetLinkRow(context: Context, modifier: Modifier, viewModel: MainViewModel){

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Card(shape = RoundedCornerShape(8.dp)) {

            Text(text = viewModel.sharableLinkState, fontSize = 12.sp, modifier = Modifier
                .padding(16.dp)
                .selectable(selected = false, enabled = true, Role.Button, onClick = {}))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Card(shape = CircleShape) {

                IconButton(onClick = {
                    context.shareLinkVia(viewModel.sharableLinkState)
                }, modifier = Modifier.background(Color.White)){
                    Icon(
                        Icons.Rounded.Share,
                        contentDescription = "Share"
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Card(shape = CircleShape) {
                IconButton(onClick = {
                    context.copyTextToClipBoard(viewModel.sharableLinkState)
                }){
                    Icon(
                        painterResource(R.drawable.ic_round_assignment),
                        contentDescription = "Copy"
                    )
                }
            }
        }
    }
}


