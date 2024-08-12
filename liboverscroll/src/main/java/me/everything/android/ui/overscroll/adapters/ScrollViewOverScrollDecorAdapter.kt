package me.everything.android.ui.overscroll.adapters

import android.view.View
import android.widget.ScrollView
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

/**
 * An adapter that enables over-scrolling over a [ScrollView].
 * <br></br>Seeing that [ScrollView] only supports vertical scrolling, this adapter
 * should only be used with a [VerticalOverScrollBounceEffectDecorator]. For horizontal
 * over-scrolling, use [HorizontalScrollViewOverScrollDecorAdapter] in conjunction with
 * a [android.widget.HorizontalScrollView].
 *
 * @see HorizontalOverScrollBounceEffectDecorator
 *
 * @see VerticalOverScrollBounceEffectDecorator
 */
open class ScrollViewOverScrollDecorAdapter(private val mView: ScrollView) :
    IOverScrollDecoratorAdapter {
    override val view: View
        get() = mView

    override val isInAbsoluteStart: Boolean
        get() = !mView.canScrollVertically(-1)

    override val isInAbsoluteEnd: Boolean
        get() = !mView.canScrollVertically(1)
}
