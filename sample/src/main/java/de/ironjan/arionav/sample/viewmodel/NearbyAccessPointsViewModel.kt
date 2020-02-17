package de.ironjan.arionav.sample.viewmodel

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.os.Looper



class NearbyAccessPointsViewModel : ViewModel() {
    private val nearbyAccessPoints: MutableLiveData<List<String>> = MutableLiveData(listOf("a", "b", "c"))

    fun getNearbyAccessPoints(): LiveData<List<String>> = nearbyAccessPoints
    fun foo() {
        val tmp = nearbyAccessPoints.value ?: emptyList()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post { nearbyAccessPoints.value = tmp + "foo" }
    }
}