package de.ironjan.arionav_fw.ionav.views.mapview

import android.os.Parcel
import android.os.Parcelable
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomViewState
import de.ironjan.graphhopper.extensions_core.Coordinate

class SimplifiedMapViewState() : MvvmCustomViewState {
    var endCoordinate: Coordinate? = null

    constructor(parcel: Parcel) : this() {
        endCoordinate= Coordinate.fromString(parcel.readString())
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(endCoordinate?.asString())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SimplifiedMapViewState> {
        override fun createFromParcel(parcel: Parcel): SimplifiedMapViewState {
            return SimplifiedMapViewState(parcel)
        }

        override fun newArray(size: Int): Array<SimplifiedMapViewState?> {
            return arrayOfNulls(size)
        }
    }
}