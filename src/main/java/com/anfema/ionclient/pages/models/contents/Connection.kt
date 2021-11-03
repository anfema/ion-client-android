package com.anfema.ionclient.pages.models.contents

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.anfema.utils.EqualsContract
import java.util.ArrayList

class Connection : Parcelable {
    @JvmField
    val scheme: String?

    @JvmField
    val collectionIdentifier: String?

    @JvmField
    val pageIdentifierPath: MutableList<String>?

    @JvmField
    val pageIdentifier: String?

    @JvmField
    val outletIdentifier: String?

    constructor(connectionContentString: String?) {
        if (connectionContentString != null) {
            val uri = Uri.parse(connectionContentString)
            scheme = uri.scheme
            collectionIdentifier = uri.host
            pageIdentifierPath = uri.pathSegments
            pageIdentifier = if (pageIdentifierPath != null && pageIdentifierPath.isNotEmpty()) {
                pageIdentifierPath[pageIdentifierPath.size - 1]
            } else {
                null
            }
            outletIdentifier = uri.fragment
        } else {
            scheme = null
            collectionIdentifier = null
            pageIdentifierPath = ArrayList()
            pageIdentifier = null
            outletIdentifier = null
        }
    }

    constructor(scheme: String?, collectionIdentifier: String?, pageIdentifier: String?, outletIdentifier: String?) {
        this.scheme = scheme
        this.collectionIdentifier = collectionIdentifier
        this.pageIdentifier = pageIdentifier
        pageIdentifierPath = ArrayList()
        if (pageIdentifier != null) {
            pageIdentifierPath.add(pageIdentifier)
        }
        this.outletIdentifier = outletIdentifier
    }

    constructor(
        scheme: String?,
        collectionIdentifier: String?,
        pageIdentifierPath: MutableList<String>?,
        outletIdentifier: String?,
    ) {
        this.scheme = scheme
        this.collectionIdentifier = collectionIdentifier
        this.pageIdentifierPath = pageIdentifierPath
        pageIdentifier = if (pageIdentifierPath != null && !pageIdentifierPath.isEmpty()) {
            pageIdentifierPath[pageIdentifierPath.size - 1]
        } else {
            null
        }
        this.outletIdentifier = outletIdentifier
    }

    override fun toString(): String {
        return ("Connection [scheme = " + scheme + ", collection = " + collectionIdentifier + ", page = " + pageIdentifier
            + ", outlet = " + outletIdentifier + "]")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Connection) {
            return false
        }
        return (EqualsContract.equal(scheme, other.scheme)
            && EqualsContract.equal(collectionIdentifier, other.collectionIdentifier)
            && equalPaths(other)
            && EqualsContract.equal(pageIdentifier, other.pageIdentifier)
            && EqualsContract.equal(outletIdentifier, other.outletIdentifier))
    }

    private fun equalPaths(o: Connection): Boolean {
        if (pageIdentifierPath == null) {
            return o.pageIdentifierPath == null
        }
        if (o.pageIdentifierPath == null) {
            return false
        }
        if (pageIdentifierPath.size != o.pageIdentifierPath.size) {
            return false
        }
        for (i in pageIdentifierPath.indices) {
            val page = pageIdentifierPath[i]
            val otherPage = o.pageIdentifierPath[i]
            if (!EqualsContract.equal(page, otherPage)) {
                return false
            }
        }
        return true
    }

    override fun hashCode() = arrayOf(
        scheme,
        collectionIdentifier,
        pageIdentifierPath,
        pageIdentifier,
        outletIdentifier,
    ).contentHashCode()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(scheme)
        dest.writeString(collectionIdentifier)
        dest.writeStringList(pageIdentifierPath)
        dest.writeString(pageIdentifier)
        dest.writeString(outletIdentifier)
    }

    constructor(`in`: Parcel) {
        scheme = `in`.readString()
        collectionIdentifier = `in`.readString()
        pageIdentifierPath = `in`.createStringArrayList()
        pageIdentifier = `in`.readString()
        outletIdentifier = `in`.readString()
    }

    companion object CREATOR : Parcelable.Creator<Connection> {
        override fun createFromParcel(source: Parcel) = Connection(source)

        override fun newArray(size: Int): Array<Connection?> = arrayOfNulls(size)
    }
}
