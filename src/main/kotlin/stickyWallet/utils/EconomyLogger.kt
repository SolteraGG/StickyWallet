package stickyWallet.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import stickyWallet.StickyPlugin

class EconomyLogger(private val plugin: StickyPlugin) {

    private val folder = File("${plugin.dataFolder}${File.separator}logs")
    private val latest: File
    private val toAdd: MutableSet<String> = mutableSetOf()
    @Volatile
    private var zipping = false

    init {
        if (!folder.exists()) folder.mkdirs()
        latest = File(folder, "latest.log")
        if (!latest.exists()) {
            try {
                latest.createNewFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    fun save() = zipAndReplace()

    private fun zipAndReplace() {
        zipping = true

        StickyPlugin.doAsync(Runnable {
            try {
                val date = TimeUtils.date()

                var zipFile = File(folder, "$date.zip")
                var link = 1
                while (zipFile.exists()) {
                    zipFile = File(folder, "$date[$link].zip")
                    link++
                }

                val fos = FileOutputStream(zipFile)
                val zipOut = ZipOutputStream(fos)

                val fileToZip = latest
                val fis = FileInputStream(fileToZip)

                val zipEntry = ZipEntry("$date.log")
                zipOut.putNextEntry(zipEntry)

                val bytes = ByteArray(1024)
                var length: Int
                while ((fis.read(bytes).also { length = it }) > 0) {
                    zipOut.write(bytes, 0, length)
                }
                zipOut.close()
                fis.close()
                fos.close()
                latest.delete()
                if (!plugin.isDisabling) {
                    latest.createNewFile()
                    val writer = PrintWriter(FileWriter(latest, true))
                    toAdd.forEach { writer.println(it) }
                    toAdd.clear()
                    writer.close()
                }
                zipping = false
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        })
    }

    fun log(message: String) {
        try {
            val builder = StringBuilder()
            appendDate(builder)
            builder.append("[Economy Log] ").append(message)
            writeToFile(builder.toString())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun warn(message: String) {
        try {
            val builder = StringBuilder()
            appendDate(builder)
            builder.append("[Warning] ").append(message)
            writeToFile(builder.toString())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun error(message: String, ex: Exception) {
        try {
            val builder = StringBuilder()
            appendDate(builder)
            val element = ex.stackTrace[0]
            builder.append("[ERROR - $ex] ")
                .append(ex.message)
                .append(" (${element.fileName} in ${element.methodName} at ${element.lineNumber})\n")
            builder.append(message)
            writeToFile(builder.toString())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun appendDate(builder: StringBuilder) {
        builder.append('[').append(TimeUtils.now()).append("] ")
    }

    private fun writeToFile(string: String) {
        if (zipping) {
            toAdd.add(string)
            return
        }
        val writer = PrintWriter(FileWriter(latest, true))
        writer.println(string)
        writer.close()
    }
}
