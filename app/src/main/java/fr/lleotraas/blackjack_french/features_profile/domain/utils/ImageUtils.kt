package fr.lleotraas.blackjack_french.features_profile.domain.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.ByteArrayOutputStream


class ImageUtils {

    fun reduceImageSize(file: Uri, contentResolver: ContentResolver): ByteArray? {
        val maxImageSize = 250000
        var bmpPic = MediaStore.Images.Media.getBitmap(contentResolver, file)
        if ((bmpPic.width >= 1024) && (bmpPic.height >= 1024)) {
            val bmpOptions = BitmapFactory.Options()
            bmpOptions.inSampleSize = 1
            while (bmpOptions.outWidth >= 1024 && bmpOptions.outHeight >= 1024) {

                bmpOptions.inSampleSize++
                bmpPic = BitmapFactory.decodeFile(file.path, bmpOptions)
            }
            Log.e("ProfileFragment", "getResizedBitmap: Resize: ${bmpOptions.inSampleSize}")
        }

        var compressQuality = 104
        var streamLength = maxImageSize
        var baos = ByteArrayOutputStream()
        while (streamLength >= maxImageSize) {
            val bmpStream = ByteArrayOutputStream()
            compressQuality -= 5
            Log.e("ProfileFragment", "getResizedBitmap: compressQuality: $compressQuality")
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            Log.e("profileFragment", "getResizedBitmap: Size: $streamLength")
            baos = bmpStream
            if (streamLength > 5000000) {
                return null
            }
        }
        return baos.toByteArray()
    }
}