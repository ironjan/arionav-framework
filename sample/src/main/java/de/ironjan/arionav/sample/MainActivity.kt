package de.ironjan.arionav.sample

import android.Manifest.permission.*
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Xml
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav.ionav.OsmBoundsExtractor
import kotlinx.android.synthetic.main.activity_main.*
import org.oscim.core.GeoPoint
import org.oscim.event.Gesture
import org.oscim.event.GestureListener
import org.oscim.event.MotionEvent
import org.oscim.layers.Layer
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.layers.tile.buildings.BuildingLayer
import org.oscim.layers.tile.vector.labeling.LabelLayer
import org.oscim.theme.VtmThemes
import org.oscim.tiling.source.mapfile.MapFileTileSource
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val cameraRequestCode: Int = 1
    private val locationRequestCode: Int = 2

    private val mapName = "saw"
    private val mapFolder
        get() = File(filesDir, mapName).absolutePath
    private val mapFilePath
        get() = File(mapFolder, "$mapName.map").absolutePath
    private val osmFilePath
        get() = File(mapFolder, "$mapName.osm").absolutePath

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(this)

        // todo move to Application class?

        unzipGhzToStorage()
        loadMap()
    }

    private fun unzipGhzToStorage() {
        GhzExtractor.unzipGhzToStorage(this, R.raw.saw, mapFolder)
    }

    private fun loadMap() {
        mapView!!.map().layers().add(MapEventsReceiver(mapView!!.map()))

        // Map file source
        val tileSource = MapFileTileSource()
        tileSource.setMapFile(mapFilePath)
        val l = mapView!!.map().setBaseMap(tileSource)
        mapView!!.map().setTheme(VtmThemes.DEFAULT)
        mapView!!.map().layers().add(BuildingLayer(mapView!!.map(), l))
        mapView!!.map().layers().add(LabelLayer(mapView!!.map(), l))

        var itemizedLayer: ItemizedLayer<MarkerItem>? = ItemizedLayer(mapView!!.map(), null as MarkerSymbol?)
        mapView!!.map().layers().add(itemizedLayer)

        // Map position
        val mapCenter = getCenterFromOsm(osmFilePath)//tileSource.getMapInfo().boundingBox.getCenterPoint();
        mapView!!.map().setMapPosition(mapCenter.latitude, mapCenter.longitude, (1 shl 15).toDouble())


        loadGraphStorage()
    }

    private fun getCenterFromOsm(osmFilePath: String): GeoPoint {
        var readBoundsFromOsm: OsmBoundsExtractor.Bounds? = OsmBoundsExtractor.extractBoundsFromOsm(osmFilePath)

        if (readBoundsFromOsm != null) {

            val centerLat = (readBoundsFromOsm.minLat + readBoundsFromOsm.maxLat) / 2
            val centerLon = (readBoundsFromOsm.minLon + readBoundsFromOsm.maxLon) / 2

            return GeoPoint(centerLat, centerLon)
        }

        return GeoPoint(0, 0)
    }

    private fun loadGraphStorage() {
        // TODO
        Log.d(TAG, "loading graphstorage..")
    }

    private fun doActualUnzip(resId: Int, targetFolder: String, folderName: String) {
        val inputStream = resources.openRawResource(resId)

        inputStream.use {
            val zipInputStream = ZipInputStream(inputStream)
            zipInputStream.use {
                var zipEntry: ZipEntry? = it.nextEntry
                while (zipEntry != null) {
                    // ghz files are flat
                    val fileName = zipEntry.name

                    val targetFile = "$targetFolder$fileName"

                    Log.d(TAG, "Unzipping ghz resource $folderName. Unzipping file $fileName  to $targetFile.")
                    // TODO write unzipped file
                    //                    FileOutputStream(targetFile)

                    zipEntry = it.nextEntry
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

    private fun requestPermissions(activity: Activity) {
//        requestCameraPermission(activity)
//        requestLocationPermission(activity)
    }

    private fun requestLocationPermission(activity: Activity) {
        if (isPermissionGranted(ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "Location permissions are already granted.")
        } else {
            if (shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)
            ) {
                Log.i(TAG, "Displaying fine location permission rationale to provide additional context.")
                showFineLocationRationale()
            } else {
                requestPermissions(activity, arrayOf(ACCESS_FINE_LOCATION), locationRequestCode)
            }
        }
    }

    private fun showFineLocationRationale() = showPermissionRational(R.string.permission_fine_location_rationale, ACCESS_FINE_LOCATION, locationRequestCode)

    /**
     * TODO move to library
     */
    private fun requestCameraPermission(activity: Activity) {
        if (isPermissionGranted(CAMERA)) {
            Log.d(TAG, "Camera permission is already granted.")
        } else {
            if (shouldShowRequestPermissionRationale(activity, CAMERA)) {
                Log.i(TAG, "Displaying camera permission rationale to provide additional context.")
                showCameraRationale()
            } else {
                Log.i(TAG, "Requesting camera permission.")
                requestPermissions(activity, arrayOf(CAMERA), cameraRequestCode)
            }
        }
    }

    private fun showCameraRationale() = showPermissionRational(R.string.permission_camera_rationale, CAMERA, cameraRequestCode)

    private fun showPermissionRational(rationaleResId: Int, permission: String, requestCode: Int) {
        Snackbar.make(mainLayout, rationaleResId, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok) {
                requestPermissions(this, arrayOf(permission), requestCode)
            }
            .show()
    }

    private fun isPermissionGranted(permission: String) = checkSelfPermission(permission) == PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissions.isEmpty()) return


        when (requestCode) {
            cameraRequestCode -> {
                if (grantResults.any { it == PERMISSION_DENIED }) {
                    showCameraRationale()
                }
            }
            locationRequestCode -> {
                if (grantResults.any { it == PERMISSION_DENIED }) {
                    showFineLocationRationale()
                }
            }
        }
    }

    internal inner class MapEventsReceiver(map: org.oscim.map.Map) : Layer(map), GestureListener {

        override fun onGesture(g: Gesture, e: MotionEvent): Boolean {
            if (g is Gesture.LongPress) {
                val p = mMap.viewport().fromScreenPoint(e.x, e.y)
                return onLongPress(p)
            }
            return false
        }
    }

    private fun onLongPress(p: GeoPoint?): Boolean {
        Log.d(TAG, "longpress at $p")
        return true
    }

}
