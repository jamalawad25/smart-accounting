package com.smart.accounting.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorage {
    private fun dir(ctx: Context): File =
        File(ctx.filesDir, "vouchers").apply { if (!exists()) mkdirs() }

    fun copyFromUri(ctx: Context, uri: Uri): String? = runCatching {
        val dest = File(dir(ctx), "IMG_${UUID.randomUUID()}.jpg")
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { out -> input.copyTo(out) }
        }
        dest.absolutePath
    }.getOrNull()
}