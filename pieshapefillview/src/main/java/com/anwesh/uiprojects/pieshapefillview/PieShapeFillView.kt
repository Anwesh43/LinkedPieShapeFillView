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
    for (j in 0..(parts - 1)) {
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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class PSFNode(var i : Int, val state : State = State()) {

        private var prev : PSFNode? = null
        private var next : PSFNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = PSFNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawPSFNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : PSFNode {
            var curr : PSFNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

    }

    data class PieShapeFill(var i : Int) {

        private var curr : PSFNode = PSFNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : PieShapeFillView) {

        private val animator : Animator = Animator(view)
        private val psf : PieShapeFill = PieShapeFill(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            psf.draw(canvas, paint)
            animator.animate {
                psf.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            psf.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : PieShapeFillView {
            val view : PieShapeFillView = PieShapeFillView(activity)
            activity.setContentView(view)
            return view
        }
    }
}