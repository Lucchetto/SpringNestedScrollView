package com.zhenxiang.spring

import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView

class SpringEdgeEffect(private val view: View, val direction: Int) {

    // A reference to the [SpringAnimation] for this RecyclerView used to bring the item back after the over-scroll effect.
    var translationAnim: SpringAnimation? = null

    private var pullDistance = 0f

    private val isHorizontal =
        direction == RecyclerView.EdgeEffectFactory.DIRECTION_LEFT ||
                direction == RecyclerView.EdgeEffectFactory.DIRECTION_RIGHT

    private val sign = if (direction == RecyclerView.EdgeEffectFactory.DIRECTION_BOTTOM ||
            direction == RecyclerView.EdgeEffectFactory.DIRECTION_RIGHT) -1 else 1

    fun onPull(deltaDistance: Float) {
        handlePull(deltaDistance)
    }

    private fun handlePull(deltaDistance: Float) {
        // This is called on every touch event while the list is scrolled with a finger.

        pullDistance += deltaDistance
        pullDistance = Math.min(1.0f, pullDistance);
        // Translate the recyclerView with the distance
        val translationDelta = sign * (if (isHorizontal) view.width else view.height) *
                deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
        if (isHorizontal) {
            view.translationX += translationDelta
        } else {
            view.translationY += translationDelta
        }

        translationAnim?.cancel()
    }

    fun handlePullDistance(deltaDistance: Float, displacement: Float): Float {
        val finalDistance = 0.0f.coerceAtLeast(getPullDistance() + deltaDistance)
        val f: Float = getPullDistance()
        val delta = finalDistance - f
        if (delta == 0.0f && f == 0.0f) {
            return 0.0f
        }
        onPull(deltaDistance)
        return delta
    }

    fun getPullDistance(): Float {
        return pullDistance.coerceAtLeast(0f)
    }

    fun onAbsorb(velocity: Int) {
        // The list has reached the edge on fling.
        val translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE
        translationAnim?.cancel()
        translationAnim = createAnim().setStartVelocity(translationVelocity)?.also {
            it.start()
        }
    }

    fun onRelease(animate: Boolean) {
        clearTranslation(animate)
    }

    fun onRelease() {
        onRelease(false)
    }

    fun isFinished(): Boolean {
        // Without this, will skip future calls to onAbsorb()
        return translationAnim?.isRunning?.not() ?: true
    }

    private fun clearTranslation(animate: Boolean) {
        pullDistance = 0f
        if (animate) {
            if ((view.translationY != 0f && !isHorizontal) || (view.translationX != 0f && isHorizontal)) {
                translationAnim = createAnim()?.also { it.start() }
            }
        } else {
            if (isHorizontal) {
                view.translationX = 0f
            } else {
                view.translationY = 0f
            }
        }
    }

    private fun createAnim() = SpringAnimation(view, if (isHorizontal) SpringAnimation.TRANSLATION_X else SpringAnimation.TRANSLATION_Y)
        .setSpring(SpringForce()
            .setFinalPosition(0f)
            .setDampingRatio(0.7f)
            .setStiffness(225f)
        )

    companion object {
        /** The magnitude of translation distance while the list is over-scrolled. */
        private const val OVERSCROLL_TRANSLATION_MAGNITUDE = 0.175f

        /** The magnitude of translation distance when the list reaches the edge on fling. */
        private const val FLING_TRANSLATION_MAGNITUDE = 0.275f
    }
}