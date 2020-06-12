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
val parts : Int = 2

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawPieLeg(i : Int, scale : Float, size : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sfi : Float = sf.divideScale(i, parts)
    val si : Float = 1f - 2 * i
    val xStart : Float = size * (1 - i) * (1 - sfi)
    val xEnd : Float = size * (1 - i) + size * i * sfi
    save()
    rotate(rot * si)
    drawLine(xStart, 0f, xEnd, 0f, paint)
    restore()
}

fun Canvas.drawPieShapeFill(scale : Float, size : Float, paint : Paint) {
    for (j in 0..1) {
        drawPieLeg(j, scale, size, paint)
    }
    val sf : Float = scale.sinify()
    drawLine(-size, 0f, -size + 2 * size * sf, 0f, paint)
}

fun Canvas.drawPSFNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val size : Float = Math.min(w, h) / sizeFactor
    paint.color = Color.parseColor(colors[i])
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(w / 2, h / 2)
    drawPieShapeFill(scale, size, paint)
    restore()
}

class PieShapeFillView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}