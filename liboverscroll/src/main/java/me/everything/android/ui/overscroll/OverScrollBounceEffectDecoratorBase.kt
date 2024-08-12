package me.everything.android.ui.overscroll

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.util.Log
import android.util.Property
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import me.everything.android.ui.overscroll.ListenerStubs.OverScrollStateListenerStub
import me.everything.android.ui.overscroll.ListenerStubs.OverScrollUpdateListenerStub
import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter
import kotlin.math.abs
import kotlin.math.max

/**
 * A standalone view decorator adding over-scroll with a smooth bounce-back effect to (potentially) any view -
 * provided that an appropriate [IOverScrollDecoratorAdapter] implementation exists / can be written
 * for that view type (e.g. [RecyclerViewOverScrollDecorAdapter]).
 *
 *
 * Design-wise, being a standalone class, this decorator powerfully provides the ability to add
 * the over-scroll effect over any view without adjusting the view's implementation. In essence, this
 * eliminates the need to repeatedly implement the effect per each view type (list-view,
 * recycler-view, image-view, etc.). Therefore, using it is highly recommended compared to other
 * more intrusive solutions.
 *
 *
 * Note that this class is abstract, having [HorizontalOverScrollBounceEffectDecorator] and
 * [VerticalOverScrollBounceEffectDecorator] providing concrete implementations that are
 * view-orientation specific.
 *
 * <hr width="97%"></hr>
 * <h2>Implementation Notes</h2>
 *
 *
 * At it's core, the class simply registers itself as a touch-listener over the decorated view and
 * intercepts touch events as needed.
 *
 *
 * Internally, it delegates the over-scrolling calculations onto 3 state-based classes:
 *
 *  1. **Idle state** - monitors view state and touch events to intercept over-scrolling initiation
 * (in which case it hands control over to the Over-scrolling state).
 *  1. **Over-scrolling state** - handles motion events to apply the over-scroll effect as users
 * interact with the view.
 *  1. **Bounce-back state** - runs the bounce-back animation, all-the-while blocking all
 * touch events till the animation completes (in which case it hands control back to the idle
 * state).
 *
 *
 *
 * @see RecyclerViewOverScrollDecorAdapter
 *
 * @see IOverScrollDecoratorAdapter
 */
abstract class OverScrollBounceEffectDecoratorBase(
    protected val mViewAdapter: IOverScrollDecoratorAdapter,
    decelerateFactor: Float,
    touchDragRatioFwd: Float,
    touchDragRatioBck: Float
) : IOverScrollDecor, OnTouchListener {
    protected val mStartAttr: OverScrollStartAttributes = OverScrollStartAttributes()

    protected val mIdleState: IdleState
    protected val mOverScrollingState: OverScrollingState =
        OverScrollingState(touchDragRatioFwd, touchDragRatioBck)
    protected val mBounceBackState: BounceBackState = BounceBackState(decelerateFactor)

    private var mCurrentState: IDecoratorState

    protected var mStateListener: IOverScrollStateListener = OverScrollStateListenerStub()
    protected var mUpdateListener: IOverScrollUpdateListener = OverScrollUpdateListenerStub()

    /**
     * When in over-scroll mode, keep track of dragging velocity to provide a smooth slow-down
     * for the bounce-back effect.
     */
    protected var mVelocity: Float = 0f

    /**
     * Motion attributes: keeps data describing current motion event.
     * <br></br>Orientation agnostic: subclasses provide either horizontal or vertical
     * initialization of the agnostic attributes.
     */
    protected abstract class MotionAttributes {
        var mAbsOffset: Float = 0f
        var mDeltaOffset: Float = 0f
        var mDir: Boolean = false // True = 'forward', false = 'backwards'.

        abstract fun init(view: View?, event: MotionEvent): Boolean
    }

    protected class OverScrollStartAttributes {
        var mPointerId: Int = 0
        var mAbsOffset: Float = 0f
        var mDir: Boolean = false // True = 'forward', false = 'backwards'.
    }

    protected abstract class AnimationAttributes {
        var mProperty: Property<View?, Float>? = null
        var mAbsOffset: Float = 0f
        var mMaxOffset: Float = 0f

        abstract fun init(view: View?)
    }

    /**
     * Interface of decorator-state delegation classes. Defines states as handles of two fundamental
     * touch events: actual movement, up/cancel.
     */
    protected interface IDecoratorState {
        /**
         * Handle a motion (touch) event.
         *
         * @param event The event from onTouch.
         * @return Return value for onTouch.
         */
        fun handleMoveTouchEvent(event: MotionEvent): Boolean

        /**
         * Handle up / touch-cancel events.
         *
         * @param event The event from onTouch.
         * @return Return value for onTouch.
         */
        fun handleUpOrCancelTouchEvent(event: MotionEvent?): Boolean

        /**
         * Handle a transition onto this state, as it becomes 'current' state.
         * @param fromState
         */
        fun handleEntryTransition(fromState: IDecoratorState)

        /**
         * The client-perspective ID of the state associated with this (internal) one. ID's
         * are as specified in [IOverScrollState].
         *
         * @return The ID, e.g. [IOverScrollState.STATE_IDLE].
         */
        val stateId: Int
    }

    /**
     * Idle state: monitors move events, trying to figure out whether over-scrolling should be
     * initiated (i.e. when scrolled further when the view is at one of its displayable ends).
     * <br></br>When such is the case, it hands over control to the over-scrolling state.
     */
    protected inner class IdleState : IDecoratorState {
        private val mMoveAttr: MotionAttributes = createMotionAttributes()

        override val stateId: Int
            get() = IOverScrollState.STATE_IDLE

        override fun handleMoveTouchEvent(event: MotionEvent): Boolean {
            val view = mViewAdapter.view
            if (!mMoveAttr.init(view, event)) {
                return false
            }

            // Has over-scrolling officially started?
            if ((mViewAdapter.isInAbsoluteStart && mMoveAttr.mDir) ||
                (mViewAdapter.isInAbsoluteEnd && !mMoveAttr.mDir)
            ) {
                // Save initial over-scroll attributes for future reference.

                mStartAttr.mPointerId = event.getPointerId(0)
                mStartAttr.mAbsOffset = mMoveAttr.mAbsOffset
                mStartAttr.mDir = mMoveAttr.mDir

                issueStateTransition(mOverScrollingState)
                return mOverScrollingState.handleMoveTouchEvent(event)
            }

            return false
        }

        override fun handleUpOrCancelTouchEvent(event: MotionEvent?): Boolean {
            return false
        }

        override fun handleEntryTransition(fromState: IDecoratorState) {
            mStateListener.onOverScrollStateChange(
                this@OverScrollBounceEffectDecoratorBase, fromState.stateId,
                stateId
            )
        }
    }

    /**
     * Handles the actual over-scrolling: thus translating the view according to configuration
     * and user interactions, dynamically.
     *
     * <br></br><br></br>The state is exited - thus completing over-scroll handling, in one of two cases:
     * <br></br>When user lets go of the view, it transitions control to the bounce-back state.
     * <br></br>When user moves the view back onto a potential 'under-scroll' state, it abruptly
     * transitions control to the idle-state, so as to return touch-events management to the
     * normal over-scroll-less environment (thus preventing under-scrolling and potentially regaining
     * regular scrolling).
     */
    protected open inner class OverScrollingState(touchDragRatioFwd: Float, touchDragRatioBck: Float) :
        IDecoratorState {
        private val mTouchDragRatioFwd: Float = touchDragRatioFwd
        private val mTouchDragRatioBck: Float = touchDragRatioBck

        private val mMoveAttr: MotionAttributes = createMotionAttributes()

        // This is really a single class that implements 2 states, so our ID depends on what
        // it was during the last invocation.
        override var stateId: Int = 0

        override fun handleMoveTouchEvent(event: MotionEvent): Boolean {
            // Switching 'pointers' (e.g. fingers) on-the-fly isn't supported -- abort over-scroll
            // smoothly using the default bounce-back animation in this case.

            if (mStartAttr.mPointerId != event.getPointerId(0)) {
                issueStateTransition(mBounceBackState)
                return true
            }

            val view = mViewAdapter.view
            if (!mMoveAttr.init(view, event)) {
                // Keep intercepting the touch event as long as we're still over-scrolling...
                return true
            }

            val deltaOffset =
                mMoveAttr.mDeltaOffset / (if (mMoveAttr.mDir == mStartAttr.mDir) mTouchDragRatioFwd else mTouchDragRatioBck)
            val newOffset = mMoveAttr.mAbsOffset + deltaOffset

            // If moved in counter direction onto a potential under-scroll state -- don't. Instead, abort
            // over-scrolling abruptly, thus returning control to which-ever touch handlers there
            // are waiting (e.g. regular scroller handlers).
            if ((mStartAttr.mDir && !mMoveAttr.mDir && (newOffset <= mStartAttr.mAbsOffset)) ||
                (!mStartAttr.mDir && mMoveAttr.mDir && (newOffset >= mStartAttr.mAbsOffset))
            ) {
                translateViewAndEvent(view, mStartAttr.mAbsOffset, event)
                mUpdateListener.onOverScrollUpdate(
                    this@OverScrollBounceEffectDecoratorBase,
                    stateId,
                    0f
                )

                issueStateTransition(mIdleState)
                return true
            }

            if (view.parent != null) {
                view.parent.requestDisallowInterceptTouchEvent(true)
            }

            val dt = event.eventTime - event.getHistoricalEventTime(0)
            if (dt > 0) { // Sometimes (though rarely) dt==0 cause originally timing is in nanos, but is presented in millis.
                mVelocity = deltaOffset / dt
            }

            translateView(view, newOffset)
            mUpdateListener.onOverScrollUpdate(
                this@OverScrollBounceEffectDecoratorBase,
                stateId,
                newOffset
            )

            return true
        }

        override fun handleUpOrCancelTouchEvent(event: MotionEvent?): Boolean {
            issueStateTransition(mBounceBackState)
            return false
        }

        override fun handleEntryTransition(fromState: IDecoratorState) {
            stateId =
                (if (mStartAttr.mDir) IOverScrollState.STATE_DRAG_START_SIDE else IOverScrollState.STATE_DRAG_END_SIDE)
            mStateListener.onOverScrollStateChange(
                this@OverScrollBounceEffectDecoratorBase, fromState.stateId,
                stateId
            )
        }
    }

    /**
     * When entered, starts the bounce-back animation.
     * <br></br>Upon animation completion, transitions control onto the idle state; Does so by
     * registering itself as an animation listener.
     * <br></br>In the meantime, blocks (intercepts) all touch events.
     */
    protected open inner class BounceBackState(private val mDecelerateFactor: Float) : IDecoratorState,
        Animator.AnimatorListener, AnimatorUpdateListener {
        private val mBounceBackInterpolator: Interpolator = DecelerateInterpolator()
        private val mDoubleDecelerateFactor: Float = 2f * mDecelerateFactor

        private val mAnimAttributes: AnimationAttributes = createAnimationAttributes()

        override val stateId: Int
            get() = IOverScrollState.STATE_BOUNCE_BACK

        override fun handleEntryTransition(fromState: IDecoratorState) {
            mStateListener.onOverScrollStateChange(
                this@OverScrollBounceEffectDecoratorBase, fromState.stateId,
                stateId
            )

            val bounceBackAnim = createAnimator()
            bounceBackAnim.addListener(this)

            bounceBackAnim.start()
        }

        override fun handleMoveTouchEvent(event: MotionEvent): Boolean {
            // Flush all touches down the drain till animation is over.
            return true
        }

        override fun handleUpOrCancelTouchEvent(event: MotionEvent?): Boolean {
            // Flush all touches down the drain till animation is over.
            return true
        }

        override fun onAnimationEnd(animation: Animator) {
            issueStateTransition(mIdleState)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            mUpdateListener.onOverScrollUpdate(
                this@OverScrollBounceEffectDecoratorBase,
                IOverScrollState.STATE_BOUNCE_BACK,
                animation.animatedValue as Float
            )
        }

        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}

        private fun createAnimator(): Animator {
            val view = mViewAdapter.view

            mAnimAttributes.init(view)

            // Set up a low-duration slow-down animation IN the drag direction.

            // Exception: If wasn't dragging in 'forward' direction (or velocity=0 -- i.e. not dragging at all),
            // skip slow-down anim directly to the bounce-back.
            if (mVelocity == 0f || (mVelocity < 0 && mStartAttr.mDir) || (mVelocity > 0 && !mStartAttr.mDir)) {
                return createBounceBackAnimator(mAnimAttributes.mAbsOffset)
            }

            // dt = (Vt - Vo) / a; Vt=0 ==> dt = -Vo / a
            var slowdownDuration = -mVelocity / mDecelerateFactor
            slowdownDuration =
                (if (slowdownDuration < 0) 0f else slowdownDuration) // Happens in counter-direction dragging

            // dx = (Vt^2 - Vo^2) / 2a; Vt=0 ==> dx = -Vo^2 / 2a
            val slowdownDistance = -mVelocity * mVelocity / mDoubleDecelerateFactor
            val slowdownEndOffset = mAnimAttributes.mAbsOffset + slowdownDistance

            val slowdownAnim =
                createSlowdownAnimator(view, slowdownDuration.toInt(), slowdownEndOffset)

            // Set up the bounce back animation, bringing the view back into the original, pre-overscroll position (translation=0).
            val bounceBackAnim = createBounceBackAnimator(slowdownEndOffset)

            // Play the 2 animations as a sequence.
            val wholeAnim = AnimatorSet()
            wholeAnim.playSequentially(slowdownAnim, bounceBackAnim)
            return wholeAnim
        }

        private fun createSlowdownAnimator(
            view: View?,
            slowdownDuration: Int,
            slowdownEndOffset: Float
        ): ObjectAnimator {
            val slowdownAnim =
                ObjectAnimator.ofFloat(view, mAnimAttributes.mProperty, slowdownEndOffset)
            slowdownAnim.setDuration(slowdownDuration.toLong())
            slowdownAnim.interpolator = mBounceBackInterpolator
            slowdownAnim.addUpdateListener(this)
            return slowdownAnim
        }

        private fun createBounceBackAnimator(startOffset: Float): ObjectAnimator {
            val view = mViewAdapter.view

            // Duration is proportional to the view's size.
            val bounceBackDuration =
                (abs(startOffset.toDouble()) / mAnimAttributes.mMaxOffset * MAX_BOUNCE_BACK_DURATION_MS).toFloat()
            val bounceBackAnim =
                ObjectAnimator.ofFloat(view, mAnimAttributes.mProperty, mStartAttr.mAbsOffset)
            bounceBackAnim.setDuration(
                max(
                    bounceBackDuration.toInt().toDouble(),
                    MIN_BOUNCE_BACK_DURATION_MS.toDouble()
                ).toLong()
            )
            bounceBackAnim.interpolator = mBounceBackInterpolator
            bounceBackAnim.addUpdateListener(this)
            return bounceBackAnim
        }
    }

    init {
        mIdleState = IdleState()

        mCurrentState = mIdleState

        attach()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> return mCurrentState.handleMoveTouchEvent(event)

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> return mCurrentState.handleUpOrCancelTouchEvent(
                event
            )
        }
        return false
    }

    override fun setOverScrollStateListener(listener: IOverScrollStateListener?) {
        mStateListener = (listener ?: OverScrollStateListenerStub())
    }

    override fun setOverScrollUpdateListener(listener: IOverScrollUpdateListener?) {
        mUpdateListener = (listener ?: OverScrollUpdateListenerStub())
    }

    override val currentState: Int
        get() = mCurrentState.stateId

    override val view: View?
        get() = mViewAdapter.view

    protected fun issueStateTransition(state: IDecoratorState) {
        val oldState = mCurrentState
        mCurrentState = state
        mCurrentState.handleEntryTransition(oldState)
    }

    final override fun attach() {
        view!!.setOnTouchListener(this)
        view!!.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun detach() {
        if (mCurrentState !== mIdleState) {
            Log.w(
                TAG,
                "Decorator detached while over-scroll is in effect. You might want to add a precondition of that getCurrentState()==STATE_IDLE, first."
            )
        }
        view!!.setOnTouchListener(null)
        view!!.overScrollMode = View.OVER_SCROLL_ALWAYS
    }

    protected abstract fun createMotionAttributes(): MotionAttributes
    protected abstract fun createAnimationAttributes(): AnimationAttributes
    protected abstract fun translateView(view: View?, offset: Float)
    protected abstract fun translateViewAndEvent(view: View?, offset: Float, event: MotionEvent)

    companion object {
        const val TAG: String = "OverScrollDecor"

        const val DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD: Float = 3f
        const val DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK: Float = 1f
        const val DEFAULT_DECELERATE_FACTOR: Float = -2f

        protected const val MAX_BOUNCE_BACK_DURATION_MS: Int = 800
        protected const val MIN_BOUNCE_BACK_DURATION_MS: Int = 200
    }
}
