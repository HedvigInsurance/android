package com.hedvig.app.util

import android.view.GestureDetector
import android.view.MotionEvent

// This class hugely inspired by https://stackoverflow.com/a/26387629
@Suppress("MagicNumber")
abstract class OnSwipeListener : GestureDetector.SimpleOnGestureListener() {
    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val x1 = e1.x
        val x2 = e2.x
        val y1 = e1.y
        val y2 = e2.y

        val direction = getDirection(x1, x2, y1, y2)

        return onSwipe(direction)
    }

    private fun getDirection(x1: Float, x2: Float, y1: Float, y2: Float): Direction {
        val angle = getAngle(x1, x2, y1, y2)
        return Direction.fromAngle(angle)
    }

    private fun getAngle(x1: Float, x2: Float, y1: Float, y2: Float): Double {
        val rad = Math.atan2((y1 - y2).toDouble(), (x2 - x1).toDouble()) + Math.PI
        return (rad * 180 / Math.PI + 180) % 360
    }

    abstract fun onSwipe(direction: Direction): Boolean

    enum class Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT;

        companion object {
            fun fromAngle(angle: Double): Direction {
                if (inRange(angle, 45f, 135f)) {
                    return UP
                }
                if (inRange(
                        angle,
                        0f,
                        45f
                    ) || inRange(angle, 315f, 360f)
                ) {
                    return RIGHT
                }
                if (inRange(angle, 225f, 315f)) {
                    return DOWN
                }

                return LEFT
            }

            private fun inRange(angle: Double, init: Float, end: Float): Boolean {
                return (angle >= init) && (angle < end)
            }
        }
    }
}
