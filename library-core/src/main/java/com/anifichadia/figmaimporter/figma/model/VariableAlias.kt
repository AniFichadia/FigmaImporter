package com.anifichadia.figmaimporter.figma.model

import kotlinx.serialization.Serializable

@Serializable
data class VariableAlias(
    val type: String,
    val id: String,
)
