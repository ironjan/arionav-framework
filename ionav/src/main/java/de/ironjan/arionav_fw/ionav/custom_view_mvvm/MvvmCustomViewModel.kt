package de.ironjan.arionav_fw.ionav.custom_view_mvvm

interface MvvmCustomViewModel<T: MvvmCustomViewState> {
    var state: T
}
