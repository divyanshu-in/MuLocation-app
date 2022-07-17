package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divyanshu_in.multiuserlocationsharingapp.R
import com.divyanshu_in.multiuserlocationsharingapp.data.ActionState
import com.divyanshu_in.multiuserlocationsharingapp.utils.HorizontalSpacer
import com.divyanshu_in.multiuserlocationsharingapp.utils.VerticalSpacer

@Composable
fun DrawerContentColumn(viewModel: MainViewModel, context: Activity, onLeaveServerButtonClick: () -> Unit){

    if(viewModel.actionState != ActionState.DEFAULT){
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Users Onboard", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(
                Alignment.CenterHorizontally))
            VerticalSpacer(height = 4)
            MarkerList(viewModel = viewModel)
            VerticalSpacer(height = 10)
            ListSeparator()
            VerticalSpacer(height = 18)
            Text("Chat", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(
                Alignment.CenterHorizontally))
            VerticalSpacer(height = 4)
            MessageColumn(viewModel = viewModel, onLeaveServerButtonClick)
        }
    }else{
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Text(text = "Options like chats, server details will appear here, once you create or join a server!",
                fontStyle = FontStyle.Italic,
                color = Color.Gray)
        }
    }
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