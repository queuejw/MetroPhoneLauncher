package me.everything.android.ui.overscroll

interface IOverScrollState {
    companion object {
        /** No over-scroll is in-effect.  */
        const val STATE_IDLE: Int = 0

        /** User is actively touch-dragging, thus enabling over-scroll at the view's *start* side.  */
        const val STATE_DRAG_START_SIDE: Int = 1

        /** User is actively touch-dragging, thus enabling over-scroll at the view's *end* side.  */
        const val STATE_DRAG_END_SIDE: Int = 2

        /** User has released their touch, thus throwing the view back into place via bounce-back animation.  */
        const val STATE_BOUNCE_BACK: Int = 3
    }
}
