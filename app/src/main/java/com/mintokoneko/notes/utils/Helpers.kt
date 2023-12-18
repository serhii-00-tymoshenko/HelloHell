package com.mintokoneko.notes.utils

import android.content.res.Resources

private val density = Resources.getSystem().displayMetrics.density

fun getScreenWidthDp(): Float {
    val screenWidthPx = Resources.getSystem().displayMetrics.widthPixels
    return pxToDp(screenWidthPx)
}

fun pxToDp(px: Int): Float = px / density

fun dpToPx(dp: Float): Int = (dp * density).toInt()