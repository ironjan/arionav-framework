package de.ironjan.arionav_fw.ionav.custom_view_mvvm

import androidx.lifecycle.LifecycleOwner

interface ModelDrivenMapLayer<V: MvvmCustomViewState, T: MvvmCustomViewModel<V>>{
    fun observe(viewModel: T, lifecycleOwner: LifecycleOwner)
}