package com.divyanshu_in.multiuserlocationsharingapp.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.divyanshu_in.multiuserlocationsharingapp.R

val uchenFontFamily = FontFamily(Font(R.font.lato_reg), Font(R.font.lato_bold, weight = FontWeight.Bold), Font(R.font.lato_italic, style = FontStyle.Italic))

val typography = Typography(
    defaultFontFamily = uchenFontFamily
)