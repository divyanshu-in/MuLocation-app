package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divyanshu_in.multiuserlocationsharingapp.R
import com.google.maps.android.compose.Circle
import kotlinx.coroutines.launch

@Composable
fun HomeDrawer(viewModel: MainViewModel, context: Context, serverId: String?) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val gestureState by remember { derivedStateOf {
        drawerState.isOpen
    } }

    ModalDrawer(drawerContent = {
        Text("Users Onboard", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(4.dp))
        MarkerList(viewModel = viewModel)
    }, drawerState = drawerState, content = {
        Box {
            MapView(context, viewModel, serverId)
            IconButton(onClick = {
                scope.launch {
                    drawerState.open()
                } },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(
                        shape = CutCornerShape(topEndPercent = 50, bottomEndPercent = 50),
                        color = Color.White))
            {
                Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = "")
            }
        }
    }
    , gesturesEnabled = gestureState)
}

@Composable
fun MarkerList(viewModel: MainViewModel){
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if(viewModel.stateOfMarkerPositions.isEmpty()){
            CircularProgressIndicator()
            Spacer(modifier  = Modifier.height(2.dp))
            Text("Waiting for users to join!", fontWeight = FontWeight.SemiBold, color = Color.Gray)
        }
        viewModel.stateOfMarkerPositions.forEach {
            MarkerDetailsView(markerDetails = it)
            Card(modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black)){}
            Spacer(modifier = Modifier.height(2.dp))

        }
    }
}

@Composable
fun MarkerDetailsView(markerDetails: Map.Entry<String, LocationData>){
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(painterResource(id = R.drawable.ic_radio_button_filled), contentDescription = "", tint = markerDetails.value.color, modifier = Modifier.padding(2.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column() {
            Text(markerDetails.key, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(markerDetails.value.distance + " away", color = Color.Gray)
        }
    }
}