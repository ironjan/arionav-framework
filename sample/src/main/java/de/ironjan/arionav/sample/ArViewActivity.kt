package de.ironjan.arionav.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import org.slf4j.LoggerFactory
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper

class ArViewActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_view)

        supportFragmentManager.findFragmentById(R.id.fragment_ar_x)?.arguments = intent.extras
        LoggerFactory.getLogger(ArViewActivity::class.java.simpleName).debug("Passed arguments to fragment...")


        ARLocationPermissionHelper.requestPermission(this)
    }
}
