package me.everything.android.ui.overscroll.adapters

import android.view.View
import android.widget.HorizontalScrollView
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

/**
 * An adapter that enables over-scrolling support over a [HorizontalScrollView].
 * <br></br>Seeing that [HorizontalScrollView] only supports horizontal scrolling, this adapter
 * should only be used with a [HorizontalOverScrollBounceEffectDecorator].
 *
 * @see HorizontalOverScrollBounceEffectDecorator
 *
 * @see VerticalOverScrollBounceEffectDecorator
 */
open class HorizontalScrollViewOverScrollDecorAdapter(private val mView: HorizontalScrollView) :
    IOverScrollDecoratorAdapter {
    override val view: View
        get() = mView

    override val isInAbsoluteStart: Boolean
        get() = !mView.canScrollHorizontally(-1)

    override val isInAbsoluteEnd: Boolean
        get() = !mView.canScrollHorizontally(1)
}
