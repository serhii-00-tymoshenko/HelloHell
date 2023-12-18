package com.mintokoneko.notes.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val title: String,
    val content: String? = null,
    val imageUri: Uri? = null,
    val id: Int
) : Parcelable
