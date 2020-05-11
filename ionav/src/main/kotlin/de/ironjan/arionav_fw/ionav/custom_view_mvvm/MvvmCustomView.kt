package de.ironjan.arionav_fw.ionav.custom_view_mvvm

import androidx.lifecycle.LifecycleOwner

/** See https://medium.com/@matthias.c.siegmund/mvvm-architecture-for-custom-views-on-android-b5636cb6be26 */
interface MvvmCustomView<T: MvvmCustomViewModel> {
    val viewModel: T

    fun onLifecycleOwnerAttached(lifecycleOwner: LifecycleOwner)
}

