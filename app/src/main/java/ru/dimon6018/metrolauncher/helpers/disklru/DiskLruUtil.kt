package ru.dimon6018.metrolauncher.helpers.disklru

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.Reader
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/** Junk drawer of utility methods.  */
internal object DiskLruUtil {
    val US_ASCII: Charset = StandardCharsets.US_ASCII
    val UTF_8: Charset = StandardCharsets.UTF_8

    @Throws(IOException::class)
    fun readFully(reader: Reader): String {
        reader.use { it ->
            val writer = StringWriter()
            val buffer = CharArray(1024)
            var count: Int
            while ((it.read(buffer).also { count = it }) != -1) {
                writer.write(buffer, 0, count)
            }
            return writer.toString()
        }
    }

    /**
     * Deletes the contents of `dir`. Throws an IOException if any file
     * could not be deleted, or if `dir` is not a readable directory.
     */
    @Throws(IOException::class)
    fun deleteContents(dir: File) {
        val files = dir.listFiles() ?: throw IOException("not a readable directory: $dir")
        for (file in files) {
            if (file.isDirectory) {
                deleteContents(file)
            }
            if (!file.delete()) {
                throw IOException("failed to delete file: $file")
            }
        }
    }

    fun closeQuietly( /*Auto*/
                      closeable: Closeable?
    ) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) {
            }
        }
    }
}