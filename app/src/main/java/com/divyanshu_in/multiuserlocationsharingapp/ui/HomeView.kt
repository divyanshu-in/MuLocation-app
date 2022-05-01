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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.divyanshu_in.multiuserlocationsharingapp.R
import com.divyanshu_in.multiuserlocationsharingapp.data.ActionState
import com.divyanshu_in.multiuserlocationsharingapp.utils.copyTextToClipBoard
import com.divyanshu_in.multiuserlocationsharingapp.utils.shareLinkVia
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


val permissionList = arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeView(context: Context, viewModel: MainViewModel, serverId: String?) {

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
        val visibleRegion = cameraPositionState.projection?.visibleRegion
        viewModel.getDirectionOfMarker(visibleRegion)
    }

    LaunchedEffect(key1 = viewModel.userLocationState){
        cameraPositionState.move(CameraUpdateFactory.newLatLng(viewModel.userLocationState))
    }

    LaunchedEffect(key1 = permissionState.allPermissionsGranted){
        if(permissionState.allPermissionsGranted){
            viewModel.addLocationChangeListener(context)
        }else{
            Toast.makeText(context, "Grant All Permissions First!", Toast.LENGTH_SHORT).show()
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {

        
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = permissionState.allPermissionsGranted),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ){

            viewModel.stateOfMarkerPositions.forEach { userLocObject ->
                val markerState = rememberMarkerState(position = userLocObject.value.latLng)
                Marker(state = markerState , title = userLocObject.key, icon = BitmapDescriptorFactory.defaultMarker(userLocObject.value.colorHue))
            }
        }

        Column(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp)) {
            viewModel.stateOfMarkerPositions.filter{!it.value.isVisible}.forEach {
                FloatingActionButton(
                    onClick = {
                              cameraPositionState.move(CameraUpdateFactory.newLatLng(it.value.latLng))
                    },
                    backgroundColor = it.value.color,
                    modifier = Modifier.rotate((it.value.angleFromAxis!!)))
                    {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "")
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


