package com.example.cacciaaltesoro.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import java.io.FileNotFoundException

fun uriToBitmap(imageUri: Uri, contentResolver: ContentResolver): Bitmap {
    val source = ImageDecoder.createSource(contentResolver, imageUri)
    return ImageDecoder.decodeBitmap(source)
}
