package me.everything.android.ui.overscroll

import android.view.MotionEvent
import android.view.View
import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter
import kotlin.math.abs

/**
 * A concrete implementation of [OverScrollBounceEffectDecoratorBase] for a horizontal orientation.
 */
open class HorizontalOverScrollBounceEffectDecorator
/**
 * C'tor, creating the effect with explicit arguments.
 * @param viewAdapter The view's encapsulation.
 * @param touchDragRatioFwd Ratio of touch distance to actual drag distance when in 'forward' direction.
 * @param touchDragRatioBck Ratio of touch distance to actual drag distance when in 'backward'
 * direction (opposite to initial one).
 * @param decelerateFactor Deceleration factor used when decelerating the motion to create the
 * bounce-back effect.
 */
/**
 * C'tor, creating the effect with default arguments:
 * <br></br>Touch-drag ratio in 'forward' direction will be set to DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD.
 * <br></br>Touch-drag ratio in 'backwards' direction will be set to DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK.
 * <br></br>Deceleration factor (for the bounce-back effect) will be set to DEFAULT_DECELERATE_FACTOR.
 *
 * @param viewAdapter The view's encapsulation.
 */
@JvmOverloads constructor(
    viewAdapter: IOverScrollDecoratorAdapter,
    touchDragRatioFwd: Float = DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD,
    touchDragRatioBck: Float = DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK,
    decelerateFactor: Float = DEFAULT_DECELERATE_FACTOR
) : OverScrollBounceEffectDecoratorBase(
    viewAdapter,
    decelerateFactor,
    touchDragRatioFwd,
    touchDragRatioBck
) {
    protected class MotionAttributesHorizontal : MotionAttributes() {
        override fun init(view: View?, event: MotionEvent): Boolean {
            // We must have history available to calc the dx. Normally it's there - if it isn't temporarily,
            // we declare the event 'invalid' and expect it in consequent events.

            if (event.historySize == 0) {
                return false
            }

            // Allow for counter-orientation-direction operations (e.g. item swiping) to run fluently.
            val dy = event.getY(0) - event.getHistoricalY(0, 0)
            val dx = event.getX(0) - event.getHistoricalX(0, 0)
            if (abs(dx.toDouble()) < abs(dy.toDouble())) {
                return false
            }

            mAbsOffset = view!!.translationX
            mDeltaOffset = dx
            mDir = mDeltaOffset > 0

            return true
        }
    }

    protected class AnimationAttributesHorizontal : AnimationAttributes() {
        init {
            mProperty = View.TRANSLATION_X
        }

        override fun init(view: View?) {
            mAbsOffset = view!!.translationX
            mMaxOffset = view.width.toFloat()
        }
    }

    override fun createMotionAttributes(): MotionAttributes {
        return MotionAttributesHorizontal()
    }

    override fun createAnimationAttributes(): AnimationAttributes {
        return AnimationAttributesHorizontal()
    }

    override fun translateView(view: View?, offset: Float) {
        view!!.translationX = offset
    }

    override fun translateViewAndEvent(view: View?, offset: Float, event: MotionEvent) {
        view!!.translationX = offset
        event.offsetLocation(offset - event.getX(0), 0f)
    }
}
