package me.everything.android.ui.overscroll.adapters

import android.view.View
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

/**
 * A static adapter for views that are ALWAYS over-scroll-able (e.g. image view).
 *
 * @see HorizontalOverScrollBounceEffectDecorator
 *
 * @see VerticalOverScrollBounceEffectDecorator
 */
class StaticOverScrollDecorAdapter(override val view: View) : IOverScrollDecoratorAdapter {
    override val isInAbsoluteStart: Boolean
        get() = true

    override val isInAbsoluteEnd: Boolean
        get() = true
}
