package com.divyanshu_in.multiuserlocationsharingapp.ui

import android.content.Context
import android.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.divyanshu_in.multiuserlocationsharingapp.R
import com.divyanshu_in.multiuserlocationsharingapp.ui.theme.Colors
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


object BitmapHelper {

    fun getNumberedMarker(context: Context, number: Int, colorHue: Float): BitmapDescriptor? {

        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_custom_marker) ?: return null
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

        val scale = context.resources.displayMetrics.density

        val textRect = Rect()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = 255
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            textSize = 12*scale
            color = android.graphics.Color.WHITE
            getTextBounds(number.toString(), 0, number.toString().length, textRect)
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.YELLOW
            style = Paint.Style.STROKE
            strokeWidth = 2*scale
            alpha = 255
        }

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Colors.purple.toArgb()
            alpha = 255
        }

        bitmap?.let {
            val canvas = Canvas(it)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)

            canvas.drawCircle((bitmap.width/2).toFloat(), (bitmap.width/2).toFloat(), (bitmap.width/4).toFloat(), circlePaint)
            canvas.drawText(
                number.toString(),
                (bitmap.width/2).toFloat(),
                (bitmap.width/2).toFloat(),
                textPaint
            )
            // draw circular stroke
            canvas.drawCircle(
                (bitmap.width/2f),
                (bitmap.width/2.5f),
                (bitmap.width/4f),
                strokePaint)

            return BitmapDescriptorFactory.fromBitmap(it)
        }
        return null
    }

}