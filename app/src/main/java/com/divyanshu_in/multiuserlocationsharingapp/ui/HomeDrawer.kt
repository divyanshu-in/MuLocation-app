package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divyanshu_in.multiuserlocationsharingapp.R
import com.divyanshu_in.multiuserlocationsharingapp.utils.HorizontalSpacer
import com.divyanshu_in.multiuserlocationsharingapp.utils.VerticalSpacer
import kotlinx.coroutines.launch

@Composable
fun HomeDrawer(viewModel: MainViewModel, context: Context, serverId: String?) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val gestureState by remember { derivedStateOf {
        drawerState.isOpen
    } }

    ModalDrawer(drawerContent = {
        DrawerContentColumn(viewModel)

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
fun DrawerContentColumn(viewModel: MainViewModel){
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Users Onboard", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(4.dp))
        MarkerList(viewModel = viewModel)
        VerticalSpacer(height = 10)
        ListSeparator()
        VerticalSpacer(height = 18)
        Text("Chat", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(4.dp))
        MessageColumn(viewModel = viewModel)
    }
}

@Composable
fun ListSeparator(){
    Card(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color.Black)){}
}

@Composable
fun MarkerList(viewModel: MainViewModel){
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if(viewModel.stateOfMarkerPositions.isEmpty()){
            CircularProgressIndicator()
            VerticalSpacer(height = 2)
            Text("Waiting for users to join!", fontWeight = FontWeight.SemiBold, color = Color.Gray)
        }
        viewModel.stateOfMarkerPositions.forEach {
            MarkerDetailsView(markerDetails = it)
            ListSeparator()
            VerticalSpacer(height = 2)
        }
    }
}

@Composable
fun MarkerDetailsView(markerDetails: Map.Entry<String, LocationData>){
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(painterResource(id = R.drawable.ic_radio_button_filled), contentDescription = "", tint = markerDetails.value.color, modifier = Modifier.padding(2.dp))
        HorizontalSpacer(width = 4)
        Column {
            Text(markerDetails.key, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            VerticalSpacer(height = 2)
            Text(markerDetails.value.distance + " away", color = Color.Gray)
        }
    }
}