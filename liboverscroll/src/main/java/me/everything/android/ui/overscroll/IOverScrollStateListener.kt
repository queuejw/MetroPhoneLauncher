package me.everything.android.ui.overscroll

/**
 * A callback-listener enabling over-scroll effect clients to be notified of effect state transitions.
 * <br></br>Invoked whenever state is transitioned onto one of [IOverScrollState.STATE_IDLE],
 * [IOverScrollState.STATE_DRAG_START_SIDE], [IOverScrollState.STATE_DRAG_END_SIDE]
 * or [IOverScrollState.STATE_BOUNCE_BACK].
 *
 * @see IOverScrollUpdateListener
 */
interface IOverScrollStateListener {
    /**
     * The invoked callback.
     *
     * @param decor The associated over-scroll 'decorator'.
     * @param oldState The old over-scroll state; ID's specified by [IOverScrollState], e.g.
     * [IOverScrollState.STATE_IDLE].
     * @param newState The **new** over-scroll state; ID's specified by [IOverScrollState],
     * e.g. [IOverScrollState.STATE_IDLE].
     */
    fun onOverScrollStateChange(decor: IOverScrollDecor?, oldState: Int, newState: Int)
}
