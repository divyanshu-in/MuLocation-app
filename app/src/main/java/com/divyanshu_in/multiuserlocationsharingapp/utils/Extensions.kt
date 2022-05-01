package com.divyanshu_in.multiuserlocationsharingapp.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.divyanshu_in.multiuserlocationsharingapp.R

fun Context.shareLinkVia(textToSend: String){
    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND
    shareIntent.type="text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, textToSend)
    startActivity(Intent.createChooser(shareIntent,getString(R.string.send_to)))
}

fun Context.copyTextToClipBoard(textToCopy: String ){
    val clipboard: ClipboardManager? =
        this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("SERVER_LINK", textToCopy)
    clipboard?.setPrimaryClip(clip)
}