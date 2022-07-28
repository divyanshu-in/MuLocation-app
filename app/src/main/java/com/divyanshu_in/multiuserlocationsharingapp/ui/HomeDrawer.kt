package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
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
fun HomeDrawer(viewModel: MainViewModel, context: Activity, serverId: String?, onLeaveServerButtonClick: () -> Unit) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val gestureState by remember { derivedStateOf {
        drawerState.isOpen
    } }

    ModalDrawer(drawerContent = {
        DrawerContentColumn(viewModel, context, onLeaveServerButtonClick)

    }, drawerState = drawerState, content = {
        Box {
            MapView(context, viewModel, serverId) {
                scope.launch {
                    drawerState.open()
                }
            }
        }
    }
    , gesturesEnabled = gestureState, drawerBackgroundColor = Color.White)
}
