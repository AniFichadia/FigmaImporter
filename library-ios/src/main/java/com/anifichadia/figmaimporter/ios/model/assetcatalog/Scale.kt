package com.anifichadia.figmaimporter.ios.model.assetcatalog

enum class Scale(val scale: Float) {
    `1x`(1f),
    `2x`(2f),
    `3x`(3f),
    ;

    fun scaleRelativeTo(other: Scale): Float {
        return other.scale / this.scale
    }
}

fun Scale.asFileSuffix(): String {
    return "@$this"
}

fun Scale.removeSuffix(from: String): String {
    val scaleSuffix = this.asFileSuffix()

    return from.substringBeforeLast(scaleSuffix)
}
