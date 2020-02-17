package de.ironjan.arionav.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NearbyAccessPointsViewModel : ViewModel() {
    private val nearbyAccessPoints: MutableLiveData<Array<String>> = MutableLiveData(arrayOf("a", "b", "c"))

    fun getNearbyAccessPointsList(): LiveData<Array<String>> = nearbyAccessPoints
}