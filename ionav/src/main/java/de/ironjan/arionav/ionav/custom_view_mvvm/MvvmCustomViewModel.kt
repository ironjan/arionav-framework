package de.ironjan.arionav.ionav.custom_view_mvvm

interface MvvmCustomViewModel<T: MvvmCustomViewState> {
    var state: T
}
