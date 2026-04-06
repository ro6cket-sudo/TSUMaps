package com.example.tsumaps.core

@JvmInline
value class Point private constructor(private val packed: Long) {
    val x: Int get() = (packed shr 32).toInt()
    val y: Int get() = (packed and 0xFFFFFFFFL).toInt()

    companion object {
        fun of(x: Int, y: Int): Point = Point((x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL))
    }

    operator fun component1(): Int = x
    operator fun component2(): Int = y
    override fun toString(): String = "Point($x, $y)"
}