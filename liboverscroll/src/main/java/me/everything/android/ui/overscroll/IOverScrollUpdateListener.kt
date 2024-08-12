package me.everything.android.ui.overscroll

/**
 * A callback-listener enabling over-scroll effect clients to subscribe to **real-time** updates
 * of over-scrolling intensity, provided as the view-translation offset from pre-scroll position.
 *
 * @see IOverScrollStateListener
 */
interface IOverScrollUpdateListener {
    /**
     * The invoked callback.
     *
     * @param decor The associated over-scroll 'decorator'.
     * @param state One of: [IOverScrollState.STATE_IDLE], [IOverScrollState.STATE_DRAG_START_SIDE],
     * [IOverScrollState.STATE_DRAG_START_SIDE] or [IOverScrollState.STATE_BOUNCE_BACK].
     * @param offset The currently visible offset created due to over-scroll.
     */
    fun onOverScrollUpdate(decor: IOverScrollDecor?, state: Int, offset: Float)
}
