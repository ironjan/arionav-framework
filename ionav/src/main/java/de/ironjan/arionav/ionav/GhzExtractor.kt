package de.ironjan.arionav.ionav

import android.content.Context
import android.util.Log
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class GhzExtractor(private val context: Context, private val resId: Int,private val  mapName: String) {
    private val ghzResId = resId
    val mapFolder
        get() = File(context.filesDir, mapName).absolutePath
    val mapFilePath
        get() = File(mapFolder, "$mapName.map").absolutePath
    val osmFilePath
        get() = File(mapFolder, "$mapName.osm").absolutePath

    fun unzipGhzToStorage() {
        val targetFolder = File(mapFolder)
        targetFolder.mkdirs()

        // check if unzipped ghz exists
        val timestampFile = File(mapFolder, timestampFileName)
        val doesExtractedTimestampFileExist = timestampFile.exists()

        // get the timestamp of the existing unzipped ghz file
        var timestampInExtracted: String? = ""
        if (doesExtractedTimestampFileExist) {
            Log.d(TAG, "Target folder for ghz file exists. Reading the existing timestamp...")
            timestampInExtracted = readContentOf(timestampFile)
        }

        // there is an unzipped ghz file. verify that it is the one contained in this sample.
        var timestampInGhz: String? = ""
        if (timestampInExtracted != null) {
            Log.d(TAG, "Extracting timestamp from zip file.")
            timestampInGhz = extractTimestampFromGhz(context, resId, targetFolder)
        }

        if (timestampInGhz == timestampInExtracted) {
            Log.d(TAG, "Timestamps do match and are both at $timestampInGhz. Skipping unzip.")
            return
        }

        Log.d(TAG, "Timestamps do not match: $timestampInExtracted in extracted, $timestampInGhz in ghz file. Extracting zip.")
        context.resources.openRawResource(resId).use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var zipEntry: ZipEntry? = zipInputStream.nextEntry
                while (zipEntry != null) {
                    // ghz files are flat
                    val fileName = zipEntry?.name

                    val targetFile = File(targetFolder, fileName).absolutePath

                    Log.d(TAG, "Unzipping ghz resource $mapFolder. Unzipping file $fileName  to $targetFile.")
                    extractZipEntry(zipInputStream, targetFile)

                    zipInputStream.closeEntry()
                    zipEntry = zipInputStream.nextEntry
                }
                Log.d(TAG, "Completed unzip.")
            }
        }
    }

    private fun extractTimestampFromGhz(context: Context, resId: Int, targetFolder: File): String? {
        var timestamp: String? = null
        context.resources.openRawResource(resId).use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var zipEntry: ZipEntry? = zipInputStream.nextEntry
                while (zipEntry != null) {
                    Log.d(TAG, "Processing ${zipEntry.name}...")
                    if (timestampFileName == zipEntry.name) {
                        // extract timestamp from zip and compare
                        Log.d(TAG, "Extract timestamp for comparison.")

                        val targetFile = File(targetFolder, "_timestamp_tmp").absolutePath
                        extractZipEntry(zipInputStream, targetFile)

                        Log.d(TAG, "Extracted timestamp entry to $targetFile")
                        timestamp = readContentOf(File(targetFile))
                        Log.d(TAG, "Extracted timestamp: $timestamp")

                        File(targetFile).deleteOnExit()
                        Log.d(TAG, "Deleting $targetFile")

                        // Exiting loop
                        zipEntry = null
                    } else {
                        Log.d(TAG, "Skipping non-timestamp entry")
                        zipInputStream.closeEntry()
                        zipEntry = zipInputStream.nextEntry
                    }
                }
            }
        }
        return timestamp
    }

    private fun extractZipEntry(zipInputStream: ZipInputStream, targetFile: String) {
        val buffer = ByteArray(1024)
        FileOutputStream(targetFile).use { fileOutputStream ->
            var count = zipInputStream.read(buffer)
            while (count != -1) {
                fileOutputStream.write(buffer, 0, count)
                count = zipInputStream.read(buffer)
            }
        }
    }

    private fun readContentOf(file: File): String? {
        var content: String? = null
        FileInputStream(file).use { fileInputStream ->
            InputStreamReader(fileInputStream).use { inputStreamReader ->
                BufferedReader(inputStreamReader).use { bufferedReader ->
                    content = bufferedReader.readLine()
                    Log.d(TAG, "Read timestamp from extracted file: $content")
                }
            }
        }
        return content
    }

    companion object {
        private const val TAG = "GhzExtractor"
        private const val timestampFileName = "_timestamp"
    }
}