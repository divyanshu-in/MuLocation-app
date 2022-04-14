package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.divyanshu_in.multiuserlocationsharingapp.Destinations
import com.divyanshu_in.multiuserlocationsharingapp.data.saveUsername
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@Composable
fun SaveUsernameView(context: Context, navController: NavController) {

    Column {
        Text(text = "µLocation", fontSize = 32.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(18.dp))
        Text(text = "Create A Username", fontSize = 24.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
        var userNameState by remember{ mutableStateOf("") }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = userNameState, onValueChange = {
            userNameState = it
        })
        val coroutineScope = rememberCoroutineScope()
        Button(onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                context.saveUsername(
                    userNameState + "#" + Random.nextInt(0, 9999).toString().padStart(4, '0')
                )
                withContext(Dispatchers.Main){
                    navController.navigate(Destinations.HomeView.name)
                }
            }
        }) {

        }
    }

}