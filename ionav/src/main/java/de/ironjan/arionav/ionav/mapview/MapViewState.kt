package de.ironjan.arionav.ionav.mapview

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.custom_view_mvvm.MvvmCustomViewState
import de.ironjan.graphhopper.extensions_core.Coordinate

class MapViewState() : MvvmCustomViewState {
    var startCoordinate: Coordinate? = null
    var endCoordinate: Coordinate? = null

    constructor(parcel: Parcel) : this() {
        startCoordinate= Coordinate.fromString(parcel.readString())
        endCoordinate= Coordinate.fromString(parcel.readString())
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(startCoordinate?.asString())
        out.writeString(endCoordinate?.asString())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MapViewState> {
        override fun createFromParcel(parcel: Parcel): MapViewState {
            return MapViewState(parcel)
        }

        override fun newArray(size: Int): Array<MapViewState?> {
            return arrayOfNulls(size)
        }
    }
}