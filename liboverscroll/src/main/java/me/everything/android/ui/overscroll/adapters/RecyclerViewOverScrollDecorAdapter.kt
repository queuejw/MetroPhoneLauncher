package me.everything.android.ui.overscroll.adapters

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

/**
 * @see HorizontalOverScrollBounceEffectDecorator
 *
 * @see VerticalOverScrollBounceEffectDecorator
 */
open class RecyclerViewOverScrollDecorAdapter(recyclerView: RecyclerView) : IOverScrollDecoratorAdapter {
    /**
     * A delegation of the adapter implementation of this view that should provide the processing
     * of [.isInAbsoluteStart] and [.isInAbsoluteEnd]. Essentially needed simply
     * because the implementation depends on the layout manager implementation being used.
     */
    interface Impl {
        fun isInAbsoluteStart(): Boolean
        fun isInAbsoluteEnd(): Boolean
    }

    private val mImpl: Impl
    protected val mRecyclerView: RecyclerView = recyclerView

    private var mIsItemTouchInEffect: Boolean = false

    init {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager ||
            layoutManager is StaggeredGridLayoutManager ||
            layoutManager is SpannedGridLayoutManager
        ) {
            val orientation = getOrientation(layoutManager)
            mImpl = if (orientation == LinearLayoutManager.HORIZONTAL) {
                ImplHorizLayout()
            } else {
                ImplVerticalLayout()
            }
        } else {
            throw IllegalArgumentException(
                "Recycler views with custom layout managers are not supported by this adapter out of the box." +
                        "Try implementing and providing an explicit 'impl' parameter to the other c'tors, or otherwise create a custom adapter subclass of your own."
            )
        }
    }

    private fun getOrientation(layoutManager: RecyclerView.LayoutManager): Int {
        var orientation = 0
        if (layoutManager is LinearLayoutManager) {
            orientation = layoutManager.orientation
        }
        if (layoutManager is SpannedGridLayoutManager) {
            orientation = layoutManager.orientation
        }
        if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
        }
        return orientation
    }

    override val view: View
        get() = mRecyclerView

    override val isInAbsoluteStart: Boolean
        get() = !mIsItemTouchInEffect && mImpl.isInAbsoluteStart()

    override val isInAbsoluteEnd: Boolean
        get() = !mIsItemTouchInEffect && mImpl.isInAbsoluteEnd()

    protected inner class ImplHorizLayout : Impl {
        override fun isInAbsoluteStart(): Boolean {
            return !mRecyclerView.canScrollHorizontally(-1)
        }

        override fun isInAbsoluteEnd(): Boolean {
            return !mRecyclerView.canScrollHorizontally(1)
        }
    }

    protected inner class ImplVerticalLayout : Impl {
        override fun isInAbsoluteStart(): Boolean {
            return !mRecyclerView.canScrollVertically(-1)
        }

        override fun isInAbsoluteEnd(): Boolean {
            return !mRecyclerView.canScrollVertically(1)
        }
    }

}
