package com.example.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

private const val MAX_IMAGE_DIMENSION_PX = 1280
private const val JPEG_COMPRESSION_QUALITY = 85

object ImageUtils {

    fun createTempCameraUri(context: Context): Uri {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val photoFile = File.createTempFile("fast_ai_photo_", ".jpg", cameraDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    fun decodeScaledBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null
            }

            var sampleSize = 1
            val longestSide = max(boundsOptions.outWidth, boundsOptions.outHeight)
            while (longestSide / sampleSize > MAX_IMAGE_DIMENSION_PX * 2) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val oriented = applyExifOrientationIfNeeded(context, uri, decoded)
            scaleDownIfNeeded(oriented)
        } catch (_: Exception) {
            null
        }
    }

    fun bitmapToJpegByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, stream)
        return stream.toByteArray()
    }

    private fun applyExifOrientationIfNeeded(
        context: Context,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }

        val rotated = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap): Bitmap {
        val maxDim = max(bitmap.width, bitmap.height)
        if (maxDim <= MAX_IMAGE_DIMENSION_PX) return bitmap

        val ratio = MAX_IMAGE_DIMENSION_PX.toFloat() / maxDim.toFloat()
        val targetWidth = (bitmap.width * ratio).roundToInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * ratio).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        if (scaled != bitmap) {
            bitmap.recycle()
        }
        return scaled
    }
}
