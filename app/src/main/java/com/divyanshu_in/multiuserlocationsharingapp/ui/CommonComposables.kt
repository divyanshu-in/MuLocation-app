package com.divyanshu_in.multiuserlocationsharingapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ListSeparator(){
    Card(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color.Black)){}
}