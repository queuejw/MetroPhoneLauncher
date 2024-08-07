package ru.dimon6018.metrolauncher.helpers.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import ru.dimon6018.metrolauncher.helpers.disklru.DiskLruCache
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest

class CacheUtils {
    companion object {
        private fun getDiskCacheDir(context: Context): File {
            val cachePath = context.cacheDir.path
            return File(cachePath + File.separator + "icons")
        }
        fun initDiskCache(context: Context): DiskLruCache? {
            try {
                val cacheDir = getDiskCacheDir(context)
                val cacheSize = 25 * 1024 * 1024 // 10 MiB
                return DiskLruCache.open(cacheDir, 1, 1, cacheSize.toLong())
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }
        fun saveIconToDiskCache(diskLruCache: DiskLruCache?, key: String, bitmap: Bitmap?) {
            if(diskLruCache != null || bitmap != null) {
                val editor = diskLruCache!!.edit(key.toMd5())
                if (editor != null) {
                    try {
                        val outputStream: OutputStream = editor.newOutputStream(0)
                        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        editor.commit()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        editor.abort()
                    }
                }
            } else {
                Log.d("saveIconToDiskCache", "diskLruCache or bitmap is null")
            }
        }
        private fun String.toMd5(): String {
            val md = MessageDigest.getInstance("MD5")
            return md.digest(toByteArray())
                .joinToString("") { "%02x".format(it) }
        }
        fun loadIconFromDiskCache(diskLruCache: DiskLruCache, key: String): Bitmap? {
            val snapshot = diskLruCache.get(key.toMd5()) ?: return null
            val inputStream = snapshot.getInputStream(0)
            return BitmapFactory.decodeStream(inputStream)
        }
        fun closeDiskCache(diskLruCache: DiskLruCache): Boolean {
            try {
                diskLruCache.close()
                return true
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }
    }
}