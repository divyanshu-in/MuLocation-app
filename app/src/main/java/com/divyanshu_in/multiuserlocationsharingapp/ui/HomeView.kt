package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.divyanshu_in.multiuserlocationsharingapp.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import timber.log.Timber
import java.util.*


val permissionList = arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeView(context: Context, viewModel: MainViewModel) {

    val permissionState = rememberMultiplePermissionsState(permissions = permissionList)

    LaunchedEffect(key1 = permissionState.allPermissionsGranted){
        if(!permissionState.allPermissionsGranted){
            permissionState.launchMultiplePermissionRequest()
        }
    }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var locOfUser by remember{ mutableStateOf(LatLng(0.0, 0.0)) }
    var isNavigatedToUserLoc by remember{ mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 20f)
    }

    if(permissionState.allPermissionsGranted){
        fusedLocationClient.lastLocation.addOnSuccessListener {
            it?.let {
                Timber.e("lastLoc")
                if (!isNavigatedToUserLoc){
                    locOfUser = LatLng(it.latitude, it.longitude)
                    viewModel.sendLocation(locOfUser)
                    cameraPositionState.move(CameraUpdateFactory.newLatLng(locOfUser))
                    isNavigatedToUserLoc = true
                }
            }
        }
    }else{
        Toast.makeText(context, "Grant All Permissions First!", Toast.LENGTH_SHORT).show()
    }


    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = permissionState.allPermissionsGranted)){

            viewModel.stateOfMarkerPositions.forEach { userLocObject ->
                Marker(position = userLocObject.value.latLng, title = userLocObject.key, icon = BitmapDescriptorFactory.defaultMarker(userLocObject.value.colorHue))
            }
            
        }

        when(viewModel.actionState){
            ActionState.DEFAULT -> {
                ButtonGroup(context, modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp), viewModel)

            }
            ActionState.SERVER_JOINED -> {
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

            Text(text = viewModel.linkToServerState, fontSize = 12.sp, modifier = Modifier
                .padding(16.dp)
                .selectable(selected = false, enabled = true, Role.Button, onClick = {}))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Card(shape = CircleShape) {

                IconButton(onClick = {
                    context.shareLinkVia(viewModel.linkToServerState)
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
                    context.copyTextToClipBoard(viewModel.linkToServerState)
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

fun Context.shareLinkVia(textToSend: String){
    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND
    shareIntent.type="text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, textToSend)
    startActivity(Intent.createChooser(shareIntent,getString(R.string.send_to)))
}

fun Context.copyTextToClipBoard(textToCopy: String ){
    val clipboard: ClipboardManager? =
        this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("SERVER_LINK", textToCopy)
    clipboard?.setPrimaryClip(clip)
}

enum class ActionState{
    DEFAULT, SERVER_JOINED, SERVER_CREATED
}