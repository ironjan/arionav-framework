package de.ironjan.arionav_fw.ionav.views.mapview

import android.os.Parcel
import android.os.Parcelable
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomViewState
import de.ironjan.graphhopper.extensions_core.Coordinate

class SimpleMapViewState() : MvvmCustomViewState {
    var destination: Coordinate? = null
    var destinationString: String? = null

    constructor(parcel: Parcel) : this() {
        destination= Coordinate.fromString(parcel.readString())
        destinationString = parcel.readString()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(destination?.asString())
        out.writeString(destinationString)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SimpleMapViewState> {
        override fun createFromParcel(parcel: Parcel): SimpleMapViewState {
            return SimpleMapViewState(parcel)
        }

        override fun newArray(size: Int): Array<SimpleMapViewState?> {
            return arrayOfNulls(size)
        }
    }
}