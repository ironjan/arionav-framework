package de.ironjan.arionav.sample

import android.content.Context
import android.util.Log
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object GhzExtractor {
    fun unzipGhzToStorage(context: Context, resId: Int, folderName: String) {
        val targetFolderPath = "${context.filesDir.absolutePath}/$folderName/"
        val targetFolder = File(targetFolderPath)
        targetFolder.mkdirs()

        // check if unzipped ghz exists
        val timestampFileName = "_timestamp"

        val timestampFile = File(targetFolderPath + timestampFileName)
        val doesUnzipGhzExist = timestampFile.exists()

        // get the timestamp of the existing unzipped ghz file
        var timestampInExtracted: String? = null
        if (doesUnzipGhzExist) {
            Log.d(MainActivity.TAG, "Target folder for ghz file exists. Reading the existing timestamp...")
            FileInputStream(timestampFile).use { fileInputStream ->
                InputStreamReader(fileInputStream).use { inputStreamReader ->
                    BufferedReader(inputStreamReader).use { bufferedReader ->
                        timestampInExtracted = bufferedReader.readLine()
                        Log.d(MainActivity.TAG, "Read timestamp from extracted file: $timestampInExtracted")
                    }
                }
            }
        }

        // there is an unzipped ghz file. verify that it is the one contained in this sample.
        var timestampInGhz: String? = ""
        if (timestampInExtracted != null) {
            Log.d(MainActivity.TAG, "Extracting timestamp from zip file.")
            // compare with timestamp in zip
            context.resources.openRawResource(resId).use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var zipEntry: ZipEntry? = zipInputStream.nextEntry
                    while (zipEntry != null) {
                        Log.d(MainActivity.TAG, "Processing ${zipEntry.name}...")
                        if (timestampFileName == zipEntry.name) {
                            // extract timestamp from zip and compare
                            Log.d(MainActivity.TAG, "Extract timestamp for comparison.")
                            val buffer = ByteArray(1024)
                            val targetFile = "${targetFolder.absolutePath}/_timestamp_tmp"
                            FileOutputStream(targetFile).use { fileOutputStream ->
                                var count = zipInputStream.read(buffer)
                                while (count != -1) {
                                    Log.d(MainActivity.TAG, "Count: $count")
                                    fileOutputStream.write(buffer, 0, count)
                                    count = zipInputStream.read(buffer)
                                }
                            }
                            Log.d(MainActivity.TAG, "Extracted timestamp entry to $targetFile")
                            FileInputStream(targetFile).use { fileInputStream ->
                                InputStreamReader(fileInputStream).use { inputStreamReader ->
                                    BufferedReader(inputStreamReader).use { bufferedReader ->
                                        timestampInGhz = bufferedReader.readLine()
                                        Log.d(MainActivity.TAG, "Extracted timestamp: $timestampInGhz")
                                    }
                                }
                            }
                            File(targetFile).deleteOnExit()
                            Log.d(MainActivity.TAG, "Deleting $targetFile")
                            zipEntry = null
                        } else {
                            Log.d(MainActivity.TAG, "Skipping entry")
                            zipInputStream.closeEntry()
                            zipEntry = zipInputStream.nextEntry
                        }

                    }
                }
            }
        }

        if (timestampInGhz == timestampInExtracted) {
            Log.d(MainActivity.TAG, "Timestamps do match and are both at $timestampInGhz. Skipping unzip.")
//            return
        }

        Log.d(MainActivity.TAG, "Timestamps do not match: $timestampInExtracted in extracted, $timestampInGhz in ghz file. Extracting zip.")
        context.resources.openRawResource(resId).use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var zipEntry: ZipEntry? = zis.nextEntry
                while (zipEntry != null) {
                    // ghz files are flat
                    val fileName = zipEntry?.name

                    val targetFile = "$targetFolderPath$fileName"

                    Log.d(MainActivity.TAG, "Unzipping ghz resource $folderName. Unzipping file $fileName  to $targetFile.")
                    val buffer = ByteArray(1024)
                    FileOutputStream(targetFile).use { fileOutputStream ->
                        var count = zis.read(buffer)
                        while (count != -1) {
                            fileOutputStream.write(buffer, 0, count)
                            count = zis.read(buffer)
                        }
                    }

                    zis.closeEntry()
                    zipEntry = zis.nextEntry
                }
                Log.d(MainActivity.TAG, "Completed unzip.")
            }
        }
    }
}