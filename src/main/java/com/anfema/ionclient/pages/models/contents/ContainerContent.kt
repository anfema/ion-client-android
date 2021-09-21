package com.anfema.ionclient.pages.models.contents

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

open class ContainerContent : Content {

    @JvmField
    var children: ArrayList<Content>? = null

    constructor()

    protected constructor(`in`: Parcel?) : super(`in`) {
        // TODO write logic to read children. (recover type + element)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        // TODO write logic to parcel children. (Save element + type)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ContainerContent?> {

        override fun createFromParcel(source: Parcel) = ContainerContent(source)
        override fun newArray(size: Int): Array<ContainerContent?> = arrayOfNulls(size)
    }
}
}
