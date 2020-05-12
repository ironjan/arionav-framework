package de.ironjan.arionav_fw.ionav.util

interface Observer<T> {
    fun update(state: T)
}