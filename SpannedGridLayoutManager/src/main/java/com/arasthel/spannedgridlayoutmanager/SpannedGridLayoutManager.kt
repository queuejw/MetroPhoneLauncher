/*
 * Copyright © 2017 Jorge Martín Espinosa
 */

package com.arasthel.spannedgridlayoutmanager

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import java.util.TreeMap
import kotlin.math.ceil

/**
 * A [RecyclerView.LayoutManager] which layouts and orders its views
 * based on width and height spans.
 *
 * @param context a Context object
 * @param orientation Whether the views will be laid out and scrolled vertically or horizontally.
 * @param _rowCount How many rows there should be. If your layout only cares about columns, set this to 1,
 * and set [customHeight].
 * @param _columnCount How many columns there should be. If your layout only cares about rows, set this to 1,
 * and set [customWidth].
 *
 * //TODO: the scroll indicators are currently disabled because they're unreliable.
 */
open class SpannedGridLayoutManager(
    context: Context,
    @RecyclerView.Orientation val orientation: Int,
    _rowCount: Int,
    _columnCount: Int
) : RecyclerView.LayoutManager() {

    //==============================================================================================
    //  ~ Orientation & Direction enums
    //==============================================================================================

    /**
     * Direction of scroll for layout process
     * <li>START</li>
     * <li>END</li>
     */
    enum class Direction {
        START, END
    }

    /**
     * The width of the RecyclerView, taking into account padding.
     */
    val decoratedWidth: Int
        get() = width - paddingLeft - paddingRight

    /**
     * The height of the RecyclerView, taking into account padding.
     */
    val decoratedHeight: Int
        get() = height - paddingTop - paddingBottom

    /**
     * The width of each item. Normally this is the width of the RecyclerView, divided
     * by the number of columns in the layout, but you can set your own dimension with [customWidth].
     */
    val itemWidth: Int get() = if (customWidth > 0) customWidth else ceil(decoratedWidth.toFloat() / columnCount.toFloat()).toInt()
    /**
     * The height of each item. Normally this is the height of the RecyclerView, divided
     * by the number of rows in the layout, but you can set your own dimension with [customHeight].
     */
    val itemHeight: Int get() = if (customHeight > 0) customHeight else ceil(decoratedHeight.toFloat() / rowCount.toFloat()).toInt()

    /**
     * Delegate some orientation-specific logic.
     */
    val orientationHelper by lazy {
        OrientationHelper.createOrientationHelper(this, orientation)
    }

    /**
     * A custom block width for items laid out.
     * TODO: Support WRAP_CONTENT?
     */
    var customWidth = -1
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * A custom block height for items laid out.
     * TODO: Support WRAP_CONTENT?
     */
    var customHeight = -1
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * The number of rows to lay out.
     * This is mostly just used for setting the height of items,
     * based on the parent RecyclerView's height. If your items overflow
     * the row count, and you have a vertical layout, you can just scroll.
     */
    var rowCount: Int = _rowCount
        set(value) {
            if (field == value) {
                return
            }

            field = value
            require(rowCount >= 1) {
                ("Span count should be at least 1. Provided "
                        + rowCount)
            }
            spanSizeLookup?.invalidateCache()
            requestLayout()
        }

    /**
     * The number of columns to lay out.
     * This is mostly just used for setting the width of items,
     * based on the parent RecyclerView's width. If your items overflow
     * the column count, and you have a horizontal layout, you can just scroll.
     */
    var columnCount: Int = _columnCount
        set(value) {
            if (field == value) {
                return
            }

            field = value
            require(columnCount >= 1) {
                ("Span count should be at least 1. Provided "
                        + columnCount)
            }
            spanSizeLookup?.invalidateCache()
            requestLayout()
        }

    /**
     * Keep track of how much we've scrolled, since Android doesn't
     * really do that for us.
     */
    var scroll = 0
        protected set

    /**
     * Delegate mapping items.
     */
    protected lateinit var rectsHelper: RectsHelper

    /**
     * First currently visible position.
     * Similar to [LinearLayoutManager.findFirstVisibleItemPosition]
     */
    open val firstVisiblePosition: Int
        get() {
            if (childCount == 0) return -1
            val item = firstVisibleItem ?: return -1

            return getAdapterPosition(item)
        }

    open val firstVisibleItem: View?
        get() {
            if (childCount == 0) return null

            var firstChild: View? = null
            var firstPosition = Int.MAX_VALUE

            for (i in 0 until childCount) {
                val child = getChildAt(i) ?: continue
                val rect = Rect().apply { getDecoratedBoundsWithMargins(child, this) }

                if (orientation == VERTICAL) {
                    if (rect.top in 0 until size || rect.bottom in 1 until size) {
                        val adapterPosition = getAdapterPosition(child)
                        if (adapterPosition < firstPosition) {
                            firstChild = child
                            firstPosition = adapterPosition
                        }
                    }
                } else {
                    if (rect.left in 0 until size || rect.right in 1 until size) {
                        val adapterPosition = getAdapterPosition(child)
                        if (adapterPosition < firstPosition) {
                            firstChild = child
                            firstPosition = adapterPosition
                        }
                    }
                }
            }

            return firstChild
        }

    /**
     * First currently completely visible position.
     * Similar to [LinearLayoutManager.findFirstCompletelyVisibleItemPosition]
     */
    open val firstCompletelyVisiblePosition: Int
        get() {
            if (childCount == 0) return -1
            val item = firstCompletelyVisibleItem ?: return -1

            return getAdapterPosition(item)
        }

    open val firstCompletelyVisibleItem: View?
        get() {
            if (childCount == 0) return null

            var firstChild: View? = null
            var firstPosition = Int.MAX_VALUE

            for (i in 0 until childCount) {
                val child = getChildAt(i) ?: continue
                val rect = Rect().apply { getDecoratedBoundsWithMargins(child, this) }

                if (orientation == VERTICAL) {
                    if (rect.top >= 0 && rect.bottom <= size) {
                        val adapterPosition = getAdapterPosition(child)
                        if (adapterPosition < firstPosition) {
                            firstChild = child
                            firstPosition = adapterPosition
                        }
                    }
                } else {
                    if (rect.left >= 0 && rect.right <= size) {
                        val adapterPosition = getAdapterPosition(child)
                        if (adapterPosition < firstPosition) {
                            firstChild = child
                            firstPosition = adapterPosition
                        }
                    }
                }
            }

            return firstChild
        }

    /**
     * Last currently visible position.
     * Similar to [LinearLayoutManager.findLastVisibleItemPosition]
     */
    open val lastVisiblePosition: Int
        get() {
            if (childCount == 0) return -1
            val item = lastVisibleItem ?: return -1

            return getAdapterPosition(item)
        }

    open val lastVisibleItem: View?
        get() {
            if (childCount == 0) return null

            var firstChild: View? = null
            var firstPosition = Int.MIN_VALUE

            for (i in childCount - 1 downTo 0) {
                val child = getChildAt(i) ?: continue
                val rect = Rect().apply { getDecoratedBoundsWithMargins(child, this) }

                if (orientation == VERTICAL) {
                    if (rect.top in 0 until size || rect.bottom in 1 until size) {
                        val adapterPosition = getAdapterPosition(child)
                        if (adapterPosition > firstPosition) {
                            firstChild = child
                            firstPosition = adapterPosition
                        }
                    }
                } else {
                    if (rect.left in 0 until size || rect.right in 1 until size) {
                        val adapterPosition = getAdapterPosition(child)
                        if (adapterPosition > firstPosition) {
                            firstChild = child
                            firstPosition = adapterPosition
                        }
                    }
                }
            }

            return firstChild
        }

    /**
     * First currently completely visible position.
     * Similar to [LinearLayoutManager.findLastCompletelyVisibleItemPosition]
     */
    open val lastCompletelyVisiblePosition: Int
        get() {
            if (childCount == 0) return -1
            val item = lastCompletelyVisibleItem ?: return -1

            return getAdapterPosition(item)
        }

    open val lastCompletelyVisibleItem: View?
        get() {
            if (childCount == 0) return null

            var firstChild: View? = null
            var firstPosition = Int.MAX_VALUE

            for (i in childCount - 1 downTo 0) {
                val child = getChildAt(i) ?: continue
                val rect = Rect().apply { getDecoratedBoundsWithMargins(child, this) }

                if (orientation == VERTICAL) {
                    if (rect.top >= 0 && rect.bottom <= size) {
                        val adapterPosition = getAdapterPosition(child)
                        if (adapterPosition > firstPosition) {
                            firstChild = child
                            firstPosition = adapterPosition
                        }
                    }
                } else {
                    if (rect.left >= 0 && rect.right <= size) {
                        val adapterPosition = getAdapterPosition(child)
                        if (adapterPosition > firstPosition) {
                            firstChild = child
                            firstPosition = adapterPosition
                        }
                    }
                }
            }

            return firstChild
        }

    /**
     * Start of the layout. Should be [getPaddingEndForOrientation] + first visible item top
     */
    protected var layoutStart = 0
    /**
     * End of the layout. Should be [layoutStart] + last visible item bottom + [getPaddingEndForOrientation]
     */
    protected var layoutEnd = 0

    /**
     * Total length of the layout depending on current orientation
     */
    val size: Int
        get() = if (orientation == VERTICAL) decoratedHeight else decoratedWidth

    /**
     * Cache of rects for laid out Views
     */
    protected val childFrames = mutableMapOf<Int, Rect>()

    /**
     * Temporary variable to store wanted scroll by [scrollToPosition]
     */
    protected var pendingScrollToPosition: Int? = null

    /**
     * Whether item order will be kept along re-creations of this LayoutManager with different
     * configurations of not. Default is false. Only set to true if this condition is met.
     * Otherwise, scroll bugs will happen.
     */
    var itemOrderIsStable = false

    /**
     * Provides SpanSize values for the LayoutManager. Otherwise they will all be (1, 1).
     */
    var spanSizeLookup: SpanSizeLookup? = null
        set(newValue) {
            field = newValue
            // If the SpanSizeLookup changes, the views need a whole re-layout
            requestLayout()
        }

    /**
     * A reference to the [RecyclerView] this layout manager
     * is attached to.
     */
    protected var recyclerView: RecyclerView? = null

    /**
     * SpanSize provider for this LayoutManager.
     * SpanSizes can be cached to improve efficiency.
     *
     * TODO: it's a little weird having the lookup callback be a constructor parameter.
     * TODO: Maybe add a new function to override instead.
     */
    open class SpanSizeLookup(
            /** Used to provide an SpanSize for each item. */
            var lookupFunction: ((Int) -> SpanSize)? = null
    ) {
        
        private var cache = SparseArray<SpanSize>()

        /**
         * Enable SpanSize caching. Can be used to improve performance if calculating the SpanSize
         * for items is a complex process.
         */
        var usesCache = false

        /**
         * Returns an SpanSize for the provided position.
         * @param position Adapter position of the item
         * @return An SpanSize, either provided by the user or the default one.
         */
        fun getSpanSize(position: Int): SpanSize {
            if (usesCache) {
                val cachedValue = cache[position]
                if (cachedValue != null) return cachedValue
                
                val value = getSpanSizeFromFunction(position)
                cache.put(position, value)
                return value
            } else {
                return getSpanSizeFromFunction(position)
            }
        }
        
        private fun getSpanSizeFromFunction(position: Int): SpanSize {
            return lookupFunction?.invoke(position) ?: getDefaultSpanSize()
        }
        
        protected open fun getDefaultSpanSize(): SpanSize {
            return SpanSize(1, 1)
        }

        fun invalidateCache() {
            cache.clear()
        }
    }

    init {
        if (_rowCount < 1) {
            throw InvalidMaxSpansException(_rowCount)
        }

        if (_columnCount < 1) {
            throw InvalidMaxSpansException(_columnCount)
        }
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAttachedToWindow(view: RecyclerView?) {
        recyclerView = view
        super.onAttachedToWindow(view)
    }

    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler?) {
        recyclerView = null
        super.onDetachedFromWindow(view, recycler)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        rectsHelper = RectsHelper(this, orientation)

        //Sometimes the width of the RecyclerView is 0, which can cause divide by zero errors
        if (getItemSizeForOrientation() <= 0) {
            return
        }

        layoutStart = getPaddingStartForOrientation()

        layoutEnd = if (scroll != 0) {
            (scroll - layoutStart)
        } else {
            getPaddingEndForOrientation()
        }

        // Clear cache, since layout may change
        childFrames.clear()

        // If there were any views, detach them so they can be recycled
        detachAndScrapAttachedViews(recycler)

        val start = System.currentTimeMillis()

        for (i in 0 until itemCount) {
            val spanSize = spanSizeLookup?.getSpanSize(i) ?: SpanSize(1, 1)
            val childRect = rectsHelper.findRect(i, spanSize)
            rectsHelper.pushRect(i, childRect)
        }

        if (DEBUG) {
            val elapsed = System.currentTimeMillis() - start
            debugLog("Elapsed time: $elapsed ms")
        }

        // Restore scroll position based on first visible view
        val pendingScrollToPosition = pendingScrollToPosition
        if (itemCount != 0 && pendingScrollToPosition != null) {
            try {
                val s = rectsHelper.getStartForPosition(pendingScrollToPosition)
                val end = rectsHelper.getEndForPosition(pendingScrollToPosition)
                val (greatestEnd, _, _) = getGreatestChildEnd()

                if ((end - scroll) > size && (greatestEnd - end) > 0) {
                    scroll = s - (greatestEnd - end).coerceAtLeast(0)
                }

                if (greatestEnd <= 0 && scroll > s) {
                    scroll = s
                }
            } catch (e: IndexOutOfBoundsException) {
                //pendingScrollPosition is not in dataset bounds
            }

            this.pendingScrollToPosition = null
        }

        // Fill from start to visible end
        fillGap(Direction.END, recycler, state)
        fillGap(Direction.START, recycler, state)

        recycleChildrenOutOfBounds(Direction.END, recycler)
        recycleChildrenOutOfBounds(Direction.START, recycler)
    }

    /**
     * Measure child view using [RectsHelper]
     */
    protected open fun measureChild(position: Int, view: View): Rect {
        val freeRectsHelper = this.rectsHelper
        val spanSize = spanSizeLookup?.getSpanSize(position) ?: SpanSize(1, 1)
        val usedSpan = if (orientation == HORIZONTAL) spanSize.height else spanSize.width

        if (usedSpan > getSpanCountForOrientation() || usedSpan < 1) {
            throw InvalidSpanSizeException(errorSize = usedSpan, maxSpanSize = getSpanCountForOrientation())
        }

        // This rect contains just the row and column number - i.e.: [0, 0, 1, 1]
        val rect = freeRectsHelper.findRect(position, spanSize)

        // Multiply the rect for item width and height to get positions
        val left = rect.left * itemWidth
        val right = rect.right * itemWidth
        val top = rect.top * itemHeight
        val bottom = rect.bottom * itemHeight

        val insetsRect = Rect()
        calculateItemDecorationsForChild(view, insetsRect)

        // Measure child
        val width = right - left - insetsRect.left - insetsRect.right
        val height = bottom - top - insetsRect.top - insetsRect.bottom
        val layoutParams = view.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        view.layoutParams = layoutParams
        measureChildWithMargins(view, width, height)

        val frame = Rect(left, top, right, bottom)
        // Cache rect
        childFrames[position] = frame

        return frame
    }

    /**
     * Layout child once it's measured and its position cached
     */
    protected open fun layoutChild(position: Int, view: View) {
        val frame = getChildFrameForPosition(position, view)

        val startPadding = getPaddingStartForOrientation()

        if (orientation == VERTICAL) {
            layoutDecorated(view,
                frame.left + paddingLeft,
                frame.top - scroll + startPadding,
                frame.right + paddingLeft,
                frame.bottom - scroll + startPadding)
        } else {
            layoutDecorated(view,
                frame.left - scroll + startPadding,
                frame.top + paddingTop,
                frame.right - scroll + startPadding,
                frame.bottom + paddingTop)
        }

        // A new child was layouted, layout edges change
        updateEdgesWithNewChild(view)
    }

    /**
     * Ask the recycler for a view, measure and layout it and add it to the layout
     */
    protected open fun makeAndAddView(position: Int, direction: Direction, recycler: RecyclerView.Recycler): View {
        val view = makeView(position, direction, recycler)

        if (direction == Direction.END) {
            addView(view)
        } else {
            addView(view, 0)
        }

        return view
    }

    protected open fun makeView(position: Int, direction: Direction, recycler: RecyclerView.Recycler): View {
        val view = recycler.getViewForPosition(position)
        measureChild(position, view)
        layoutChild(position, view)

        return view
    }

    /**
     * A new view was added, update layout edges if needed
     */
    protected open fun updateEdgesWithNewChild(view: View) {
        val childStart = getChildStart(view) + scroll + getPaddingStartForOrientation()

        if (childStart < layoutStart) {
            layoutStart = childStart
        }

        val newLayoutEnd = childStart + getItemSizeForOrientation()

        if (newLayoutEnd > layoutEnd) {
            layoutEnd = newLayoutEnd
        }
    }

    //==============================================================================================
    //  ~ Recycling methods
    //==============================================================================================

    /**
     * Recycle any views that are out of bounds
     */
    protected open fun recycleChildrenOutOfBounds(direction: Direction, recycler: RecyclerView.Recycler) {
        if (direction == Direction.END) {
            recycleChildrenFromStart(direction, recycler)
        } else {
            recycleChildrenFromEnd(direction, recycler)
        }
    }

    /**
     * Recycle views from start to first visible item
     */
    protected open fun recycleChildrenFromStart(direction: Direction, recycler: RecyclerView.Recycler) {
        val childCount = childCount
        val start = getPaddingStartForOrientation()

        val toDetach = mutableListOf<View>()

        for (i in 0 until childCount) {
            getChildAt(i)?.let { child ->
                val childEnd = getChildEnd(child)

                if (childEnd < start) {
                    toDetach.add(child)
                }
            }

        }

        for (child in toDetach) {
            removeAndRecycleView(child, recycler)
            updateEdgesWithRemovedChild(child, direction)
        }
    }

    /**
     * Recycle views from end to last visible item
     */
    protected open fun recycleChildrenFromEnd(direction: Direction, recycler: RecyclerView.Recycler) {
        val childCount = childCount
        val end = size + getPaddingEndForOrientation()

        val toDetach = mutableListOf<View>()

        for (i in (0 until childCount).reversed()) {
            getChildAt(i)?.let { child ->
                val childStart = getChildStart(child)

                if (childStart > end) {
                    toDetach.add(child)
                }
            }
        }

        for (child in toDetach) {
            removeAndRecycleView(child, recycler)
            updateEdgesWithRemovedChild(child, direction)
        }
    }

    /**
     * Update layout edges when views are recycled
     */
    protected open fun updateEdgesWithRemovedChild(view: View, direction: Direction) {
        val childStart = getChildStart(view) + scroll
        val childEnd = getChildEnd(view) + scroll

        if (direction == Direction.END) { // Removed from start
            layoutStart = getPaddingStartForOrientation() + childEnd
        } else if (direction == Direction.START) { // Removed from end
            layoutEnd = getPaddingStartForOrientation() + childStart
        }
    }

    override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {
        return computeScrollOffset()
    }

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return computeScrollOffset()
    }

    private fun computeScrollOffset(): Int {
        if (childCount == 0) return 0

        return scroll
    }

    override fun computeVerticalScrollExtent(state: RecyclerView.State): Int {
        return computeScrollExtent()
    }

    override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int {
        return computeScrollExtent()
    }

    private fun computeScrollExtent(): Int {
        if (childCount == 0) return 0

        return size
    }

    override fun computeVerticalScrollRange(state: RecyclerView.State): Int {
        return computeScrollRange()
    }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        return computeScrollRange()
    }

    private fun computeScrollRange(): Int {
        if (childCount == 0) return 0

        return rectsHelper.lastStart
    }

    override fun canScrollVertically(): Boolean {
        return orientation == VERTICAL
    }

    override fun canScrollHorizontally(): Boolean {
        return orientation == HORIZONTAL
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        return scrollBy(dx, recycler, state)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        return scrollBy(dy, recycler, state)
    }

    protected open fun scrollBy(delta: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        // If there are no view or no movement, return
        if (delta == 0) {
            return 0
        }

        val (childEnd, _, _) = getGreatestChildEnd()

        val canScrollBackwards = ((firstVisiblePosition) >= 0 &&
                0 < scroll &&
                delta < 0) || childEnd <= 0

        val canScrollForward = lastVisiblePosition <= state.itemCount &&
                (size) < (childEnd) &&
                delta > 0

        // If can't scroll forward or backwards, return
        if (!(canScrollBackwards || canScrollForward)) {
            return 0
        }

        val correctedDistance = scrollBy(-delta, state, childEnd)
        val direction = if (delta > 0) Direction.END else Direction.START

        recycleChildrenOutOfBounds(direction, recycler)

        fillGap(direction, recycler, state)

        return -correctedDistance
    }

    /**
     * Scrolls distance based on orientation. Corrects distance if out of bounds.
     */
    protected open fun scrollBy(distance: Int, state: RecyclerView.State, childEnd: Int): Int {
        val start = 0

        var correctedDistance = distance

        scroll -= distance

        // Correct scroll if was out of bounds at start
        if (scroll < start) {
            correctedDistance += scroll
            scroll = start
        }

        // Correct scroll if it would make the layout scroll out of bounds at the end
        if (childEnd > 0 && distance < 0 && size - distance > childEnd) {
            correctedDistance = (size - childEnd)
            scroll += distance - correctedDistance
        }

        orientationHelper.offsetChildren(correctedDistance)

        return correctedDistance
    }

    override fun scrollToPosition(position: Int) {
        val wasNull = pendingScrollToPosition == null

        pendingScrollToPosition = position.coerceAtMost(itemCount - 1)
            .coerceAtLeast(0)

        if (wasNull && recyclerView?.isInLayout == false) {
            requestLayout()
        }
    }

    /**
     * TODO: not sure this works properly
     */
    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        val smoothScroller = object: LinearSmoothScroller(recyclerView.context) {

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return this@SpannedGridLayoutManager.computeScrollVectorForPosition(targetPosition)
            }

            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }

            override fun getHorizontalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) {
            return null
        }

        val direction = if (targetPosition < firstVisiblePosition) -1 else 1
        return PointF(if (orientation == HORIZONTAL) direction.toFloat() else 0f,
            if (orientation == VERTICAL) direction.toFloat() else 0f)
    }

    /**
     * Fills gaps on the layout, on directions [Direction.START] or [Direction.END]
     */
    protected open fun fillGap(direction: Direction, recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (direction == Direction.END) {
            fillAfter(recycler)
        } else {
            fillBefore(recycler)
        }
    }

    /**
     * Fill gaps before the current visible scroll position
     * @param recycler Recycler
     */
    protected open fun fillBefore(recycler: RecyclerView.Recycler) {
        val currentRow = (scroll - getPaddingStartForOrientation()) / getItemSizeForOrientation()
        val lastRow = (scroll + size - getPaddingStartForOrientation()) / getItemSizeForOrientation()

        for (row in (currentRow until lastRow).reversed()) {
            val positionsForRow = rectsHelper.findPositionsForRow(row).reversed()

            for (position in positionsForRow) {
                if (findViewByPosition(position) != null) continue
                makeAndAddView(position, Direction.START, recycler)
            }
        }
    }

    /**
     * Fill gaps after the current layouted views
     * @param recycler Recycler
     */
    protected open fun fillAfter(recycler: RecyclerView.Recycler) {
        val visibleEnd = scroll + size

        val lastAddedRow = layoutEnd / getItemSizeForOrientation()
        val lastVisibleRow =  visibleEnd / getItemSizeForOrientation()

        for (rowIndex in lastAddedRow .. lastVisibleRow) {
            val row = rectsHelper.rows[rowIndex] ?: continue

            for (itemIndex in row) {
                if (findViewByPosition(itemIndex) != null) continue
                makeAndAddView(itemIndex, Direction.END, recycler)
            }
        }
    }

    override fun getDecoratedMeasuredWidth(child: View): Int {
        val position = getPosition(child)
        return getChildFrameForPosition(position, child).width()
    }

    override fun getDecoratedMeasuredHeight(child: View): Int {
        val position = getPosition(child)
        return getChildFrameForPosition(position, child).height()
    }

    override fun getDecoratedTop(child: View): Int {
        val position = getPosition(child)
        val decoration = getTopDecorationHeight(child)
        var top = getChildFrameForPosition(position, child).top + decoration

        if (orientation == VERTICAL) {
            top -= scroll
        }

        return top
    }

    override fun getDecoratedRight(child: View): Int {
        val position = getPosition(child)
        val decoration = getLeftDecorationWidth(child) + getRightDecorationWidth(child)
        var right = getChildFrameForPosition(position, child).right + decoration

        if (orientation == HORIZONTAL) {
            right -= scroll - getPaddingStartForOrientation()
        }

        return right
    }

    override fun getDecoratedLeft(child: View): Int {
        val position = getPosition(child)
        val decoration = getLeftDecorationWidth(child)
        var left = getChildFrameForPosition(position, child).left + decoration

        if (orientation == HORIZONTAL) {
            left -= scroll
        }

        return left
    }

    override fun getDecoratedBottom(child: View): Int {
        val position = getPosition(child)
        val decoration = getTopDecorationHeight(child) + getBottomDecorationHeight(child)
        var bottom = getChildFrameForPosition(position, child).bottom + decoration

        if (orientation == VERTICAL) {
            bottom -= scroll - getPaddingStartForOrientation()
        }

        return bottom
    }

    protected open fun getPaddingStartForOrientation(): Int {
        return orientationHelper.startAfterPadding
    }

    protected open fun getPaddingEndForOrientation(): Int {
        return orientationHelper.endPadding
    }

    protected open fun getChildStart(child: View): Int {
        return orientationHelper.getDecoratedStart(child)
    }

    protected open fun getChildEnd(child: View): Int {
        return orientationHelper.getDecoratedEnd(child)
    }

    open fun getAdapterPosition(child: View): Int {
        return (child.layoutParams as RecyclerView.LayoutParams).bindingAdapterPosition
    }

    /**
     * Get the largest currently laid-out end coordinate.
     * For vertical layouts, this will be the bottom of the bottom-most
     * item (not necessarily the bottom of the child in the last adapter
     * position). For horizontal layouts, it's the right.
     *
     * This method traverses the children from the last index to the first,
     * since greater indices are more likely to have the greatest end coordinate.
     */
    protected open fun getGreatestChildEnd(): Triple<Int, Int, View?> {
        var greatestEnd = 0
        var greatestI = 0
        var child: View? = null

        for (i in childCount - 1 downTo 0) {
            child = getChildAt(i) ?: continue

            val end = orientationHelper.getDecoratedEnd(child)

            if (end > greatestEnd) {
                greatestEnd = end
                greatestI = i
            }
        }

        return Triple(greatestEnd, greatestI, child)
    }

    protected open fun getGreatestChildStart(): Triple<Int, Int, View?> {
        var greatestStart = 0
        var greatestI = 0
        var child: View? = null

        for (i in childCount - 1 downTo 0) {
            child = getChildAt(i) ?: continue

            val start = orientationHelper.getDecoratedStart(child)

            if (start > greatestStart) {
                greatestStart = start
                greatestI = i
            }
        }

        return Triple(greatestStart, greatestI, child)
    }

    /**
     * Get the smallest currently laid-out coordinate.
     * For vertical layouts, this will be the top of the top-most
     * item (not necessarily the top of the child in the first adapter
     * position). For horizontal layouts, it's the left.
     *
     * This method traverses the children from the first index to the last,
     * since lower indices are more likely to have the smallest start coordinate.
     */
    protected fun getLeastChildStart(): Triple<Int, Int, View?> {
        var leastStart = Int.MAX_VALUE
        var leastI = 0
        var leastV: View? = null

        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val start = child.run { if (orientation == HORIZONTAL) left else top }

            if (start < leastStart) {
                leastStart = start
                leastI = i
                leastV = child
            }
        }

        return Triple(leastStart, leastI, leastV)
    }

    protected fun getChildStartFromIndex(position: Int): Int {
        val child = getChildAt(position) ?: return 0

        return orientationHelper.getDecoratedStart(child)
    }

    protected open fun getSpanSizeForOrientationOfChild(index: Int): Int {
        return spanSizeLookup?.getSpanSize(index)?.run {
            if (orientation == HORIZONTAL) width
            else height
        } ?: 1
    }

    protected open fun getItemSizeForOrientation(): Int {
        return if (orientation == VERTICAL) {
            itemHeight
        } else {
            itemWidth
        }
    }

    open fun getSpanCountForOrientation(): Int {
        return if (orientation == VERTICAL) {
            columnCount
        } else {
            rowCount
        }
    }

    protected open fun getChildFrameForPosition(position: Int, view: View): Rect {
        return childFrames[position] ?: measureChild(position, view)
    }

    //==============================================================================================
    //  ~ Save & Restore State
    //==============================================================================================

    override fun onSaveInstanceState(): Parcelable? {
        return if (itemOrderIsStable && childCount > 0) {
            debugLog("Saving first visible position: $firstVisiblePosition")
            SavedState(firstVisiblePosition)
        } else {
            null
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        debugLog("Restoring state")
        val savedState = state as? SavedState
        if (savedState != null) {
            val firstVisibleItem = savedState.firstVisibleItem
            scrollToPosition(firstVisibleItem)
        }
    }

    companion object {
        const val TAG = "SpannedGridLayoutMan"
        const val DEBUG = false

        fun debugLog(message: String) {
            if (DEBUG) Log.d(TAG, message)
        }
    }

    class SavedState(val firstVisibleItem: Int): Parcelable {

        companion object {

            @JvmField val CREATOR = object: Parcelable.Creator<SavedState> {

                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source.readInt())
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(firstVisibleItem)
        }

        override fun describeContents(): Int {
            return 0
        }

    }

}

/**
 * A helper to find free rects in the current layout.
 */
open class RectsHelper(val layoutManager: SpannedGridLayoutManager,
                  @RecyclerView.Orientation val orientation: Int) {

    /**
     * Comparator to sort free rects by position, based on orientation
     */
    private val rectComparator = Comparator<Rect> { rect1, rect2 ->
        when (orientation) {
            VERTICAL -> {
                if (rect1.top == rect2.top) {
                    if (rect1.left < rect2.left) { -1 } else { 1 }
                } else {
                    if (rect1.top < rect2.top) { -1 } else { 1 }
                }
            }
            HORIZONTAL -> {
                if (rect1.left == rect2.left) {
                    if (rect1.top < rect2.top) { -1 } else { 1 }
                } else {
                    if (rect1.left < rect2.left) { -1 } else { 1 }
                }
            }
            else -> 0
        }

    }

    val rows = mutableMapOf<Int, Set<Int>>()

    /**
     * Cache of rects that are already used
     */
    private val rectsCache = mutableMapOf<Int, Rect>()

    /**
     * List of rects that are still free
     */
    private val freeRects = mutableListOf<Rect>()

    /**
     * Start row/column for free rects
     */
    val start: Int get() {
        return if (orientation == VERTICAL) {
            freeRects[0].top * layoutManager.itemHeight
        } else {
            freeRects[0].left * layoutManager.itemWidth
        }
    }

    /**
     * End row/column for free rects
     */
    val end: Int get() {
        return if (orientation == VERTICAL) {
            (freeRects.last().bottom + 1) * layoutManager.itemHeight
        } else {
            (freeRects.last().right + 1) * layoutManager.itemWidth
        }
    }

    val lastStart: Int get() {
        return if (orientation == VERTICAL) {
            (freeRects.last().top) * layoutManager.itemHeight
        } else {
            (freeRects.last().left) * layoutManager.itemWidth
        }
    }

    val largestStart: Int get() {
        var largest = 0

        freeRects.forEach {
            val s = if (orientation == VERTICAL) {
                it.top * layoutManager.itemHeight
            } else {
                it.left * layoutManager.itemWidth
            }

            if (s > largest) {
                largest = s
            }
        }

        return largest
    }

    val largestEnd: Int get() {
        var largest = 0

        freeRects.forEach {
            val s = if (orientation == VERTICAL) {
                it.bottom * layoutManager.itemHeight
            } else {
                it.right * layoutManager.itemWidth
            }

            if (s > largest) {
                largest = s
            }
        }

        return largest
    }

    init {
        // There will always be a free rect that goes to Int.MAX_VALUE
        val initialFreeRect = if (orientation == VERTICAL) {
            Rect(0, 0, layoutManager.columnCount, Int.MAX_VALUE)
        } else {
            Rect(0, 0, Int.MAX_VALUE, layoutManager.rowCount)
        }
        freeRects.add(initialFreeRect)
    }

    fun getRowIndexForItemPosition(position: Int): Int {
        val sortedRows = TreeMap(rows)

        synchronized(sortedRows) {
            sortedRows.values.forEachIndexed { index, set ->
                if (set.contains(position)) {
                    return index
                }
            }
        }

        return 0
    }

    fun getEndForPosition(position: Int): Int {
        return if (orientation == VERTICAL) {
            (rectsCache[position]!!.bottom) * layoutManager.itemHeight
        } else {
            (rectsCache[position]!!.right) * layoutManager.itemWidth
        }
    }

    fun getStartForPosition(position: Int): Int {
        return if (orientation == VERTICAL) {
            (rectsCache[position]!!.top) * layoutManager.itemHeight
        } else {
            (rectsCache[position]!!.left) * layoutManager.itemWidth
        }
    }

    /**
     * Get a free rect for the given span and item position
     */
    fun findRect(position: Int, spanSize: SpanSize): Rect {
        return rectsCache[position] ?: findRectForSpanSize(spanSize)
    }

    /**
     * Find a valid free rect for the given span size
     */
    protected open fun findRectForSpanSize(spanSize: SpanSize): Rect {
        val lane = freeRects.first {
            val itemRect = Rect(it.left, it.top, it.left + spanSize.width, it.top + spanSize.height)
            it.contains(itemRect)
        }

        return Rect(lane.left, lane.top, lane.left + spanSize.width, lane.top + spanSize.height)
    }

    /**
     * Push this rect for the given position, subtract it from [freeRects]
     */
    fun pushRect(position: Int, rect: Rect) {
        val start = if (orientation == VERTICAL)
            rect.top else
            rect.left
        val startRow = rows[start]?.toMutableSet() ?: mutableSetOf()
        startRow.add(position)
        rows[start] = startRow

        val end = if (orientation == VERTICAL)
            rect.bottom else
            rect.right
        val endRow = rows[end - 1]?.toMutableSet() ?: mutableSetOf()
        endRow.add(position)
        rows[end - 1] = endRow

        rectsCache[position] = rect
        subtract(rect)
    }

    fun findPositionsForRow(rowPosition: Int): Set<Int> {
        return rows[rowPosition] ?: emptySet()
    }

    /**
     * Remove this rect from the [freeRects], merge and reorder new free rects
     */
    protected open fun subtract(subtractedRect: Rect) {
        val interestingRects = freeRects.filter { it.isAdjacentTo(subtractedRect) || it.intersects(subtractedRect) }

        val possibleNewRects = mutableListOf<Rect>()
        val adjacentRects = mutableListOf<Rect>()

        for (free in interestingRects) {
            if (free.isAdjacentTo(subtractedRect) && !subtractedRect.contains(free)) {
                adjacentRects.add(free)
            } else {
                freeRects.remove(free)

                if (free.left < subtractedRect.left) { // Left
                    possibleNewRects.add(Rect(free.left, free.top, subtractedRect.left, free.bottom))
                }

                if (free.right > subtractedRect.right) { // Right
                    possibleNewRects.add(Rect(subtractedRect.right, free.top, free.right, free.bottom))
                }

                if (free.top < subtractedRect.top) { // Top
                    possibleNewRects.add(Rect(free.left, free.top, free.right, subtractedRect.top))
                }

                if (free.bottom > subtractedRect.bottom) { // Bottom
                    possibleNewRects.add(Rect(free.left, subtractedRect.bottom, free.right, free.bottom))
                }
            }
        }

        for (rect in possibleNewRects) {
            val isAdjacent = adjacentRects.firstOrNull { it != rect && it.contains(rect) } != null
            if (isAdjacent) continue

            val isContained = possibleNewRects.firstOrNull { it != rect && it.contains(rect) } != null
            if (isContained) continue

            freeRects.add(rect)
        }

        freeRects.sortWith(rectComparator)
    }
}

/**
 * Helper to store width and height spans
 */
class SpanSize(val width: Int, val height: Int)