package com.smart.accounting.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.smart.accounting.data.db.AppDatabase
import java.io.File

object BackupManager {

    private fun dbFile(ctx: Context) = ctx.getDatabasePath(AppDatabase.DB_NAME)
    private fun backupDir(ctx: Context) =
        File(ctx.filesDir, "backup").apply { if (!exists()) mkdirs() }

    fun stagedDbCopy(ctx: Context): File? {
        val src = dbFile(ctx)
        if (!src.exists()) return null
        val out = File(backupDir(ctx), "backup_${System.currentTimeMillis()}.db")
        src.inputStream().use { i -> out.outputStream().use { o -> i.copyTo(o) } }
        return out
    }

    fun writeToUri(ctx: Context, target: Uri): Boolean = runCatching {
        val src = dbFile(ctx)
        if (!src.exists()) return false
        ctx.contentResolver.openOutputStream(target)?.use { o ->
            src.inputStream().use { i -> i.copyTo(o) }
        }
        true
    }.getOrDefault(false)

    fun restoreFromUri(ctx: Context, source: Uri): Boolean = runCatching {
        val target = dbFile(ctx)
        ctx.contentResolver.openInputStream(source)?.use { i ->
            target.outputStream().use { o -> i.copyTo(o) }
        }
        AppDatabase.close()
        true
    }.getOrDefault(false)

    fun shareIntent(ctx: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}