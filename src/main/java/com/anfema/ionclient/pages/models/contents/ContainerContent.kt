package com.anfema.ionclient.pages.models.contents

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

enum class Subtype {
    @SerializedName("generic")
    Generic,

    @SerializedName("list")
    List,

    @SerializedName("structblock")
    StructBlock,

    @SerializedName("streamblock")
    StreamBlock,
}

@Parcelize
data class ContainerContent(
    val children: List<Content>,
    val subtype: Subtype,
) : Content()
