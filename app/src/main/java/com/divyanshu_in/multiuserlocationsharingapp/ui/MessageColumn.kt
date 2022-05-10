package com.divyanshu_in.multiuserlocationsharingapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divyanshu_in.multiuserlocationsharingapp.utils.VerticalSpacer

@Composable
fun MessageColumn(viewModel: MainViewModel){
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        LazyColumn(modifier = Modifier.weight(1f)){
            items(viewModel.stateOfMessagesReceived){ usrMsgPair ->
                Message(usrMsgPair, viewModel.stateOfMarkerPositions[usrMsgPair.first])
            }
        }

        CreateTextRow(viewModel)
        Spacer(modifier = Modifier.height(2.dp))
        Button(
            onClick = {  },
            colors = ButtonDefaults.buttonColors(Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp)) {
            Text("Leave Server!", color = Color.White)
        }
        VerticalSpacer(12)
    }

}

@Composable
fun CreateTextRow(viewModel: MainViewModel) {
    
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(4.dp)) {
        var textValueState by remember{mutableStateOf("")}
        Row{
            TextField(value = textValueState, onValueChange = {
                textValueState = it
            }, colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White))
            IconButton(onClick = {
                viewModel.sendTextMessage(textValueState)
            }) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = null)
            }
        }
    }
    
}


@Composable
fun Message(userMsgPair: Pair<String, String>, locationData: LocationData?){
    Row(horizontalArrangement = if(locationData == null) Arrangement.End else Arrangement.Start) {
        if(locationData==null){
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "You",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = userMsgPair.second)
            }
            Spacer(modifier = Modifier.width(2.dp))
            Card(shape = CircleShape, backgroundColor = Color.Cyan, modifier = Modifier.size(18.dp)) {
                Text(text = userMsgPair.first.first().uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White)
            }
        }else {
            Card(shape = CircleShape, backgroundColor = locationData.color, modifier = Modifier.size(12.dp)) {
                Text(text = userMsgPair.first.first().uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White)
            }
            Spacer(modifier = Modifier.width(2.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = userMsgPair.first,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = userMsgPair.second)
            }
        }
    }
}