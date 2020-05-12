package de.ironjan.arionav_fw.ionav.custom_view_mvvm

import androidx.lifecycle.LifecycleOwner

interface ModelDrivenUiComponent<T: MvvmCustomViewModel>{
    fun observe(viewModel: T, lifecycleOwner: LifecycleOwner)
}