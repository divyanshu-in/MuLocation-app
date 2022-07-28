package com.divyanshu_in.multiuserlocationsharingapp.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

val lightTheme = lightColors(
    primary = Colors.orange,
    secondary = Colors.green,
    surface = Colors.white,
    onSurface = Colors.purple,
    onPrimary = Colors.purple,
    onSecondary = Colors.purple,
    primaryVariant = Colors.yellow
)


@Composable
fun CustomMaterialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = lightTheme,
        content = content,
        typography = typography,
    )

}