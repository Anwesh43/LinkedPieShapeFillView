package com.anwesh.uiprojects.pieshapefillview

/**
 * Created by anweshmishra on 12/06/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val lines : Int = 4
val scGap : Float = 0.02f
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val colors : Array<String> = arrayOf("#4CAF50", "#FF5722", "#3F51B5", "#009688", "#9C27B0")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val rot : Float = 15f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
