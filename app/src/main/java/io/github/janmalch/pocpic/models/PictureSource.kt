package io.github.janmalch.pocpic.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.bumptech.glide.load.engine.DiskCacheStrategy

enum class DiskCacheStrategyEnum(val strategy: DiskCacheStrategy) {
    ALL(DiskCacheStrategy.ALL),
    DATA(DiskCacheStrategy.DATA),
    RESOURCE(DiskCacheStrategy.RESOURCE),
    NONE(DiskCacheStrategy.NONE),
    AUTOMATIC(DiskCacheStrategy.AUTOMATIC),
}

@Immutable
data class PictureSource(
    val label: String,
    val imageModel: Uri?,
    val cacheStrategy: DiskCacheStrategyEnum = DiskCacheStrategyEnum.AUTOMATIC,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "PocPic",
        parcel.readParcelable(Uri::class.java.classLoader),
        DiskCacheStrategyEnum.valueOf(parcel.readString() ?: DiskCacheStrategyEnum.AUTOMATIC.name)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeParcelable(imageModel, flags)
        parcel.writeString(cacheStrategy.name)
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
}
