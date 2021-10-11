package com.anfema.ionclient.pages.models.contents

import kotlinx.parcelize.Parcelize

@Parcelize
data class ContainerContent(
    val children: List<Content>,
) : Content()
