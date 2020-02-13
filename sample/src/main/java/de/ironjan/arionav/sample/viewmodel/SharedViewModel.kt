package de.ironjan.arionav.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel: ViewModel() {
    private val counter: MutableLiveData<Int> = MutableLiveData(0)

    fun getC(): LiveData<Int> = counter
    fun inc() {
        counter.value = (counter.value?:0) + 1
    }
}