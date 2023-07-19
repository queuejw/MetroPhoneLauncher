@file:JvmName("SpannedGridLayoutManager")
/*
 * Copyright © 2017 Jorge Martín Espinosa
 */

package com.arasthel.spannedgridlayoutmanager

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * A [android.support.v7.widget.RecyclerView.LayoutManager] which layouts and orders its views
 * based on width and height spans.
 *
 * @param orientation Whether the views will be layouted and scrolled in vertical or horizontal
 * @param spans How many spans does the layout have per row or column
 */
open class SpannedGridLayoutManager(val orientation: Orientation,
                                    val spans: Int) : RecyclerView.LayoutManager() {

    //==============================================================================================
    //  ~ Orientation & Direction enums
    //==============================================================================================

    /**
     * Orientations to layout and scroll views
     * <li>VERTICAL</li>
     * <li>HORIZONTAL</li>
     */
    enum class Orientation {
        VERTICAL, HORIZONTAL
    }

    /**
     * Direction of scroll for layouting process
     * <li>START</li>
     * <li>END</li>
     */
    enum class Direction {
        START, END
    }

    //==============================================================================================
    //  ~ Properties
    //==============================================================================================

    /**
     * Current scroll amount
     */
    protected var scroll = 0

    /**
     * Helper get free rects to place views
     */
    protected lateinit var rectsHelper: RectsHelper

    /**
     * First visible position of 2x2 layout - changes with recycling
     */
    open val firstVisible2x2LayoutPosition: Int
        get() {
            if (childCount <= 0) {
                return -1
            }
            for (i in 0 until childCount - 1) {
                val position = getPosition(getChildAt(i)!!)
                val spanSize = spanSizeLookup?.getSpanSize(position)
                if (spanSize != null && spanSize.height == 2 && spanSize.width == 2) {
                    return position
                }
            }
            return -1
        }

    /**
     * First visible position in layout - changes with recycling
     */
    open val firstVisiblePosition: Int
        get() {
            if (childCount == 0) {
                return 0
            }
            return getPosition(getChildAt(0)!!)
        }

    /**
     * Last visible position in layout - changes with recycling
     */
    open val lastVisiblePosition: Int
        get() {
            if (childCount == 0) {
                return 0
            }
            return getPosition(getChildAt(childCount - 1)!!)
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
    val size: Int get() = if (orientation == Orientation.VERTICAL) height else width

    /**
     * Cache of rects for layouted views
     */
    protected val childFrames = mutableMapOf<Int, RectF>()

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
     * SpanSize provider for this LayoutManager.
     * SpanSizes can be cached to improve efficiency.
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

    //==============================================================================================
    //  ~ Initializer
    //==============================================================================================

    init {
        if (spans < 1) {
            throw InvalidMaxSpansException(spans)
        }
    }

    //==============================================================================================
    //  ~ Override parent
    //==============================================================================================

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    //==============================================================================================
    //  ~ View layouting methods
    //==============================================================================================

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {

        rectsHelper = RectsHelper(this, orientation)

        layoutStart = getPaddingStartForOrientation()

        layoutEnd = if (scroll != 0) {
            val currentRow = (scroll - layoutStart) / rectsHelper.itemSize
            currentRow * rectsHelper.itemSize
        } else {
            getPaddingEndForOrientation()
        }

        // Clear cache, since layout may change
        childFrames.clear()

        // If there were any views, detach them so they can be recycled
        detachAndScrapAttachedViews(recycler)

        val start = System.currentTimeMillis()

        for (i in 0 until state.itemCount) {
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
        if (state.itemCount != 0 && pendingScrollToPosition != null && pendingScrollToPosition >= spans) {

            val currentRow = rectsHelper.rows.filter { (_, value) -> value.contains(pendingScrollToPosition) }.keys.firstOrNull()

            if (currentRow != null) {
                scroll = getPaddingStartForOrientation() + (currentRow * rectsHelper.itemSize)
            }

            this.pendingScrollToPosition = null
        }

        // Fill from start to visible end
        fillGap(Direction.END, recycler, state)

        recycleChildrenOutOfBounds(Direction.END, recycler)

        // Check if after changes in layout we aren't out of its bounds
        val overScroll = scroll + size - layoutEnd - getPaddingEndForOrientation()
        val isLastItemInScreen = (0 until childCount).map { getPosition(getChildAt(it)!!) }.contains(state.itemCount - 1)
        val allItemsInScreen = state.itemCount == 0 || (firstVisiblePosition == 0 && isLastItemInScreen)
        if (!allItemsInScreen && overScroll > 0) {
            // If we are, fix it
            scrollBy(overScroll, state)

            if (overScroll > 0) {
                fillBefore(recycler)
            } else {
                fillAfter(recycler)
            }
        }
    }

    /**
     * Measure child view using [RectsHelper]
     */
    protected open fun measureChild(position: Int, view: View) {

        val freeRectsHelper = this.rectsHelper

        val itemWidth = freeRectsHelper.itemSize
        val itemHeight = freeRectsHelper.itemSize

        val spanSize = spanSizeLookup?.getSpanSize(position) ?: SpanSize(1, 1)

        val usedSpan = if (orientation == Orientation.HORIZONTAL) spanSize.height else spanSize.width

        if (usedSpan > this.spans || usedSpan < 1) {
            throw InvalidSpanSizeException(errorSize = usedSpan, maxSpanSize = spans)
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
        layoutParams.width = width.toInt()
        layoutParams.height = height.toInt()
        measureChildWithMargins(view, width.toInt(), height.toInt())

        // Cache rect
        childFrames[position] = RectF(left, top, right, bottom)
    }

    /**
     * Layout child once it's measured and its position cached
     */
    protected open fun layoutChild(position: Int, view: View) {
        val frame = childFrames[position]

        if (frame != null) {
            val scroll = this.scroll

            val startPadding = getPaddingStartForOrientation()

            if (orientation == Orientation.VERTICAL) {
                layoutDecorated(view,
                        (frame.left + paddingLeft).toInt(),
                        (frame.top - scroll + startPadding).toInt(),
                        (frame.right + paddingLeft).toInt(),
                        (frame.bottom - scroll + startPadding).toInt())
            } else {
                layoutDecorated(view,
                        (frame.left - scroll + startPadding).toInt(),
                        (frame.top + paddingTop).toInt(),
                        (frame.right - scroll + startPadding).toInt(),
                        (frame.bottom + paddingTop).toInt())
            }
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

        val newLayoutEnd = childStart + rectsHelper.itemSize

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
            val child = getChildAt(i)!!
            val childEnd = getChildEnd(child)

            if (childEnd < start) {
                toDetach.add(child)
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
            val child = getChildAt(i)!!
            val childStart = getChildStart(child)

            if (childStart > end) {
                toDetach.add(child)
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

    //==============================================================================================
    //  ~ Scroll methods
    //==============================================================================================

    override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {
        return computeScrollOffset()
    }

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return computeScrollOffset()
    }

    private fun computeScrollOffset(): Int {
        return if (childCount == 0) 0 else firstVisiblePosition
    }

    override fun computeVerticalScrollExtent(state: RecyclerView.State): Int {
        return childCount
    }

    override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int {
        return childCount
    }

    override fun computeVerticalScrollRange(state: RecyclerView.State): Int {
        return state.itemCount
    }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        return state.itemCount
    }

    override fun canScrollVertically(): Boolean {
        return orientation == Orientation.VERTICAL
    }

    override fun canScrollHorizontally(): Boolean {
        return orientation == Orientation.HORIZONTAL
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

        val canScrollBackwards = (firstVisiblePosition) >= 0 &&
                0 < scroll &&
                delta < 0

        val canScrollForward = (firstVisiblePosition + childCount) <= state.itemCount &&
                (scroll + size) < (layoutEnd + rectsHelper.itemSize + getPaddingEndForOrientation())
        delta > 0

        // If can't scroll forward or backwards, return
        if (!(canScrollBackwards || canScrollForward)) {
            return 0
        }

        val correctedDistance = scrollBy(-delta, state)

        val direction = if (delta > 0) Direction.END else Direction.START

        recycleChildrenOutOfBounds(direction, recycler)

        fillGap(direction, recycler, state)

        return -correctedDistance
    }

    /**
     * Scrolls distance based on orientation. Corrects distance if out of bounds.
     */
    protected open fun scrollBy(distance: Int, state: RecyclerView.State): Int {
        val paddingEndLayout = getPaddingEndForOrientation()

        val start = 0

        var totalTwoLastRowRect = 0
        val totalTwoLastRowRectMap = mutableMapOf<Int, Int>()
        for (i in rectsHelper.rows.size - 1 downTo rectsHelper.rows.size - 2) {
            totalTwoLastRowRectMap[i] = rectsHelper.rows[i]?.size ?: 0
        }
        val iterator = totalTwoLastRowRectMap.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val value = totalTwoLastRowRectMap[key] ?: 0
            totalTwoLastRowRect += value
        }
        val spanSize = spanSizeLookup?.getSpanSize(state.itemCount - 1) ?: SpanSize(1, 1)
        val end = layoutEnd +
                if (totalTwoLastRowRect <= spans && spanSize.width != spans) {
                    if (totalTwoLastRowRectMap[rectsHelper.rows.size - 2] ?: 0 >= (spans - 1) &&
                            totalTwoLastRowRectMap[rectsHelper.rows.size - 1] ?: 0 == (spans - 2) &&
                            (state.itemCount % spans < (spans - 1))) {
                        0
                    } else {
                        rectsHelper.itemSize
                    }
                } else {
                    0
                } +
                paddingEndLayout

        scroll -= distance

        var correctedDistance = distance

        // Correct scroll if was out of bounds at start
        if (scroll < start) {
            correctedDistance += scroll
            scroll = start
        }

        // Correct scroll if it would make the layout scroll out of bounds at the end
        if (scroll + size > end && (firstVisiblePosition + childCount + spans) >= state.itemCount) {
            correctedDistance -= (end - scroll - size)
            scroll = end - size
        }

        if (orientation == Orientation.VERTICAL) {
            offsetChildrenVertical(correctedDistance)
        } else {
            offsetChildrenHorizontal(correctedDistance)
        }

        return correctedDistance
    }

    override fun scrollToPosition(position: Int) {
        pendingScrollToPosition = position

        requestLayout()
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                if (childCount == 0) {
                    return null
                }

                val direction = if (targetPosition < firstVisiblePosition) -1 else 1
                return PointF(0f, direction.toFloat())
            }

            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
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
        val extraHeight = if (rectsHelper.dynamicExtraHeight != null) (rectsHelper.dynamicExtraHeight)?.toInt()!! else 0
        val currentRow = (scroll - getPaddingStartForOrientation() - extraHeight) / rectsHelper.itemSize
        val lastRow = (scroll + size - getPaddingStartForOrientation()) / rectsHelper.itemSize

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

        val extraHeight = if (rectsHelper.dynamicExtraHeight != null) (rectsHelper.dynamicExtraHeight)?.toInt()!! else 0
        val lastAddedRow = (layoutEnd - extraHeight) / rectsHelper.itemSize
        val lastVisibleRow = visibleEnd / rectsHelper.itemSize

        for (rowIndex in lastAddedRow..lastVisibleRow) {
            val row = rectsHelper.rows[rowIndex] ?: continue

            for (itemIndex in row) {

                if (findViewByPosition(itemIndex) != null) continue

                makeAndAddView(itemIndex, Direction.END, recycler)
            }
        }
    }

    //==============================================================================================
    //  ~ Decorated position and sizes
    //==============================================================================================

    override fun getDecoratedMeasuredWidth(child: View): Int {
        val position = getPosition(child)
        return childFrames[position]!!.width().roundToInt()
    }

    override fun getDecoratedMeasuredHeight(child: View): Int {
        val position = getPosition(child)
        return childFrames[position]!!.height().toInt()
    }

    override fun getDecoratedTop(child: View): Int {
        val position = getPosition(child)
        val decoration = getTopDecorationHeight(child)
        var top = childFrames[position]!!.top + decoration

        if (orientation == Orientation.VERTICAL) {
            top -= scroll
        }

        return top.toInt()
    }

    override fun getDecoratedRight(child: View): Int {
        val position = getPosition(child)
        val decoration = getLeftDecorationWidth(child) + getRightDecorationWidth(child)
        var right = childFrames[position]!!.right + decoration

        if (orientation == Orientation.HORIZONTAL) {
            right -= scroll - getPaddingStartForOrientation()
        }

        return right.toInt()
    }

    override fun getDecoratedLeft(child: View): Int {
        val position = getPosition(child)
        val decoration = getLeftDecorationWidth(child)
        var left = childFrames[position]!!.left + decoration

        if (orientation == Orientation.HORIZONTAL) {
            left -= scroll
        }

        return left.toInt()
    }

    override fun getDecoratedBottom(child: View): Int {
        val position = getPosition(child)
        val decoration = getTopDecorationHeight(child) + getBottomDecorationHeight(child)
        var bottom = childFrames[position]!!.bottom + decoration

        if (orientation == Orientation.VERTICAL) {
            bottom -= scroll - getPaddingStartForOrientation()
        }
        return bottom.toInt()
    }

    //==============================================================================================
    //  ~ Orientation Utils
    //==============================================================================================

    protected open fun getPaddingStartForOrientation(): Int {
        return if (orientation == Orientation.VERTICAL) {
            paddingTop
        } else {
            paddingLeft
        }
    }

    protected open fun getPaddingEndForOrientation(): Int {
        return if (orientation == Orientation.VERTICAL) {
            paddingBottom
        } else {
            paddingRight
        }
    }

    protected open fun getChildStart(child: View): Int {
        return if (orientation == Orientation.VERTICAL) {
            getDecoratedTop(child)
        } else {
            getDecoratedLeft(child)
        }
    }

    protected open fun getChildEnd(child: View): Int {
        return if (orientation == Orientation.VERTICAL) {
            getDecoratedBottom(child)
        } else {
            getDecoratedRight(child)
        }
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

    class SavedState(val firstVisibleItem: Int) : Parcelable {

        companion object {

            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {

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
                       val orientation: SpannedGridLayoutManager.Orientation) {

    /**
     * Comparator to sort free rects by position, based on orientation
     */
    private val rectComparator = Comparator<RectF> { rect1, rect2 ->
        when (orientation) {
            SpannedGridLayoutManager.Orientation.VERTICAL -> {
                if (rect1.top == rect2.top) {
                    if (rect1.left < rect2.left) {
                        -1
                    } else {
                        1
                    }
                } else {
                    if (rect1.top < rect2.top) {
                        -1
                    } else {
                        1
                    }
                }
            }
            SpannedGridLayoutManager.Orientation.HORIZONTAL -> {
                if (rect1.left == rect2.left) {
                    if (rect1.top < rect2.top) {
                        -1
                    } else {
                        1
                    }
                } else {
                    if (rect1.left < rect2.left) {
                        -1
                    } else {
                        1
                    }
                }
            }
        }

    }

    val rows = mutableMapOf<Int, Set<Int>>()

    /**
     * Cache of rects that are already used
     */
    private val rectsCache = mutableMapOf<Int, RectF>()

    /**
     * List of rects that are still free
     */
    private val freeRects = mutableListOf<RectF>()

    /**
     * Free space to divide in spans
     */
    val size: Int
        get() {
            return if (orientation == SpannedGridLayoutManager.Orientation.VERTICAL) {
                layoutManager.width - layoutManager.paddingLeft - layoutManager.paddingRight
            } else {
                layoutManager.height - layoutManager.paddingTop - layoutManager.paddingBottom
            }
        }

    /**
     * Space occupied by each span
     */
    val itemSize: Int get() = size / layoutManager.spans

    /**
     * Start row/column for free rects
     */
    val start: Float
        get() {
            return if (orientation == SpannedGridLayoutManager.Orientation.VERTICAL) {
                freeRects[0].top * itemSize
            } else {
                freeRects[0].left * itemSize
            }
        }

    /**
     * End row/column for free rects
     */
    val end: Float
        get() {
            return if (orientation == SpannedGridLayoutManager.Orientation.VERTICAL) {
                (freeRects.last().top + 1) * itemSize
            } else {
                (freeRects.last().left + 1) * itemSize
            }
        }

    init {
        // There will always be a free rect that goes to Int.MAX_VALUE
        val initialFreeRect = if (orientation == SpannedGridLayoutManager.Orientation.VERTICAL) {
            RectF(0f, 0f, layoutManager.spans.toFloat(), Float.MAX_VALUE)
        } else {
            RectF(0F, 0F, Float.MAX_VALUE, layoutManager.spans.toFloat())
        }
        freeRects.add(initialFreeRect)
    }

    /**
     * Get a free rect for the given span and item position
     */
    fun findRect(position: Int, spanSize: SpanSize): RectF {
        return rectsCache[position] ?: findRectForSpanSize(position, spanSize)
    }

    var dynamicExtraHeight: Float? = null

    /**
     * Find a valid free rect for the given span size
     */
    protected open fun findRectForSpanSize(position: Int, spanSize: SpanSize): RectF {
        val lane = freeRects.first {
            val itemRect = RectF(
                    it.left,
                    it.top,
                    it.left + spanSize.width,
                    it.top + spanSize.height
            )
            it.contains(itemRect)
        }
        //Calculate view height dynamically
        val dynamicHeight = if (layoutManager.spanSizeLookup?.getSpanSize(position)?.viewHeight != null) {
            layoutManager.spanSizeLookup?.getSpanSize(position)?.viewHeight!! / (layoutManager.width / layoutManager.spans)
        } else {
            null
        }
        dynamicHeight?.let {//
            dynamicExtraHeight = if (dynamicExtraHeight != null) {
                dynamicExtraHeight?.plus(layoutManager.spanSizeLookup?.getSpanSize(position)?.viewHeight!!)
            } else {
                layoutManager.spanSizeLookup?.getSpanSize(position)?.viewHeight!!
            }
        }
        return RectF(lane.left,
                lane.top,
                lane.left + spanSize.width,
                if (dynamicHeight != null)
                    lane.top + dynamicHeight
                else
                    lane.top + spanSize.height)
//        return RectF(lane.left, lane.top, lane.left + spanSize.width, lane.top + spanSize.height)
    }

    /**
     * Push this rect for the given position, subtract it from [freeRects]
     */
    fun pushRect(position: Int, rect: RectF) {
        val start = if (orientation == SpannedGridLayoutManager.Orientation.VERTICAL)
            rect.top else
            rect.left
        val startRow = rows[start.toInt()]?.toMutableSet() ?: mutableSetOf()
        startRow.add(position)
        rows[start.toInt()] = startRow

        val end = if (orientation == SpannedGridLayoutManager.Orientation.VERTICAL)
            rect.bottom else
            rect.right
        val endRow = rows[(end - 1).toInt()]?.toMutableSet() ?: mutableSetOf()
        endRow.add(position)
        rows[(end - 1).toInt()] = endRow

        rectsCache[position] = rect
        subtract(rect)
    }

    fun findPositionsForRow(rowPosition: Int): Set<Int> {
        return rows[rowPosition] ?: emptySet()
    }

    /**
     * Remove this rect from the [freeRects], merge and reorder new free rects
     */
    protected open fun subtract(subtractedRect: RectF) {
        val interestingRects = freeRects.filter { it.isAdjacentTo(subtractedRect) || it.intersects(subtractedRect) }

        val possibleNewRects = mutableListOf<RectF>()
        val adjacentRects = mutableListOf<RectF>()

        for (free in interestingRects) {
            if (free.isAdjacentTo(subtractedRect) && !subtractedRect.contains(free)) {
                adjacentRects.add(free)
            } else {
                freeRects.remove(free)

                if (free.left < subtractedRect.left) { // Left
                    possibleNewRects.add(RectF(free.left, free.top, subtractedRect.left, free.bottom))
                }

                if (free.right > subtractedRect.right) { // Right
                    possibleNewRects.add(RectF(subtractedRect.right, free.top, free.right, free.bottom))
                }

                if (free.top < subtractedRect.top) { // Top
                    possibleNewRects.add(RectF(free.left, free.top, free.right, subtractedRect.top))
                }

                if (free.bottom > subtractedRect.bottom) { // Bottom
                    possibleNewRects.add(RectF(free.left, subtractedRect.bottom, free.right, free.bottom))
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
class SpanSize(val width: Int, val height: Int, val viewHeight: Float? = null)