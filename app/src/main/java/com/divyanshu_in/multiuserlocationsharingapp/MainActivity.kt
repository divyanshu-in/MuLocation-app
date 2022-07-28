package com.divyanshu_in.multiuserlocationsharingapp

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.divyanshu_in.multiuserlocationsharingapp.data.getUsername
import com.divyanshu_in.multiuserlocationsharingapp.ui.HomeView
import com.divyanshu_in.multiuserlocationsharingapp.ui.MainViewModel
import com.divyanshu_in.multiuserlocationsharingapp.ui.SaveUsernameView
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.divyanshu_in.multiuserlocationsharingapp.ui.theme.CustomMaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()

        setContent {

            CustomMaterialTheme {
                val navController = rememberNavController()
                var isUserNameAvailable by remember{ mutableStateOf(true) }

                LaunchedEffect(key1 = isUserNameAvailable){
                    if(!isUserNameAvailable){
                        navController.navigate(Destinations.SaveUsernameView.name)
                    }else{
                        lifecycleScope.launch {
                            this@MainActivity.getUsername.collect{
                                if(it == null){
                                    isUserNameAvailable = false
                                }
                            }
                        }
                    }
                }

                NavHost(navController = navController, startDestination = Destinations.HomeView.name) {
                    composable(Destinations.HomeView.name){
                        HomeView(this@MainActivity, viewModel, intent.data?.pathSegments?.last())
                    }
                    composable(Destinations.SaveUsernameView.name){
                        SaveUsernameView(this@MainActivity, navController)
                    }
                }
            }
        }
    }
}

enum class Destinations{
    HomeView, SaveUsernameView
}