package io.github.janmalch.pocpic.models

import android.content.SharedPreferences
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable

@Immutable
data class PictureSource(
    val label: String,
    val imageModel: Uri?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "PocPic",
        parcel.readParcelable(Uri::class.java.classLoader),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeParcelable(imageModel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PictureSource> {
        override fun createFromParcel(parcel: Parcel): PictureSource {
            return PictureSource(parcel)
        }

        override fun newArray(size: Int): Array<PictureSource?> {
            return arrayOfNulls(size)
        }
    }

    object Preferences {
        private const val URI_KEY = "uri"
        private const val LABEL_KEY = "label"

        fun SharedPreferences.Editor.putPictureSource(key: String, pictureSource: PictureSource) {
            putString("${key}_$URI_KEY", pictureSource.imageModel.toString())
            putString("${key}_$LABEL_KEY", pictureSource.label)
        }

        fun SharedPreferences.Editor.removePictureSource(key: String) {
            remove("${key}_$URI_KEY")
            remove("${key}_$LABEL_KEY")
        }

        fun SharedPreferences.getPictureSource(key: String): PictureSource? {
            val imageModel = getString("${key}_$URI_KEY", null)?.let { Uri.parse(it) } ?: return null
            val label = getString("${key}_$LABEL_KEY", null) ?: return null

            return PictureSource(
                imageModel = imageModel,
                label = label,
            )
        }
    }
}
