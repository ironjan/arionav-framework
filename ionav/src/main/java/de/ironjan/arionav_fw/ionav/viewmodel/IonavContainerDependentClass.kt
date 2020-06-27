package de.ironjan.arionav_fw.ionav.viewmodel

import de.ironjan.arionav_fw.ionav.di.IonavContainer

interface IonavContainerDependentClass {
    /**
     * Will initialize this view model. Does nothing, if {@param ionavContainer} is already known.
     * When overriding, you should call super first.
     */
    fun initialize(ionavContainer: IonavContainer)
}