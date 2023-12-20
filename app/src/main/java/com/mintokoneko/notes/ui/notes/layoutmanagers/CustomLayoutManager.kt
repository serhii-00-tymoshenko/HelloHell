package com.mintokoneko.notes.ui.notes.layoutmanagers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class CustomLayoutManager(
    private val viewSize: Int,
    private val childPadding: Int,
    private val columnCount: Int,
) : RecyclerView.LayoutManager() {

    private val parentTop: Int
        get() = paddingTop

    private val parentBottom: Int
        get() = height - paddingBottom

    private val parentLeft: Int
        get() = paddingLeft

    private val parentRight: Int
        get() = width - paddingRight

    private val viewSizeQuarter = viewSize / 4

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)

        if (state.itemCount <= 0) return
        fillBottom(recycler, state.itemCount)
    }

    override fun canScrollVertically(): Boolean = true

    private var offset = 0
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (state.itemCount == 0) return 0
        val lastOffset = offset
        val firstChild = getChildAt(0)!!
        val lastChild = getChildAt(childCount - 1)!!
        val firstChildTop = getDecoratedTop(firstChild)
        val lastChildBottom = getDecoratedBottom(lastChild)
        if (firstChildTop - dy < parentTop && lastChildBottom > parentBottom + dy) {
            offsetChildrenVerticallyBy(dy)
            fill(dy, recycler)
            recycleViewsOutOfBounds(recycler)
        }
        return lastOffset - offset
    }

    private fun fill(dy: Int, recycler: Recycler): Int {
        if (dy > 0) fillBottom(recycler, itemCount) else fillTop(recycler)
        return dy
    }

    private fun recycleViewsOutOfBounds(recycler: RecyclerView.Recycler) {
        if (childCount == 0) return

        var firstVisibleChild = 0
        for (i in 0 until itemCount) {
            val child = getChildAt(i)!!
            if (getDecoratedBottom(child) < parentTop + childPadding) {
                firstVisibleChild++
            } else {
                break
            }
        }

        var lastVisibleChild = firstVisibleChild
        for (i in lastVisibleChild until childCount) {
            val child = getChildAt(i)!!
            if (parentBottom - childPadding > getDecoratedTop(child)) {
                lastVisibleChild++
            } else {
                break
            }
        }

        for (i in firstVisibleChild - 1 downTo 0) removeAndRecycleViewAt(i, recycler)

        for (i in childCount - 1 downTo lastVisibleChild) removeAndRecycleViewAt(i, recycler)
    }

    private fun fillBottom(recycler: RecyclerView.Recycler, adapterItemCount: Int) {
        var top: Int
        var bottom: Int
        var verticalOffset = parentLeft


        val decoratedRight: Int
        val startPosition: Int

        if (childCount > 0) {
            val lastVisibleChild = getChildAt(childCount - 1)!!
            val lastVisibleChildPosition = getPosition(lastVisibleChild)
            startPosition = lastVisibleChildPosition + 1


            decoratedRight = getDecoratedRight(lastVisibleChild)
            if (decoratedRight > parentRight - childPadding) {
                verticalOffset = parentLeft
                top = getDecoratedBottom(lastVisibleChild) - viewSizeQuarter * (columnCount - 1) - childPadding * (columnCount - 1) + childPadding
            } else {
                verticalOffset = getDecoratedLeft(lastVisibleChild) + childPadding + viewSize
                top = getDecoratedTop(lastVisibleChild) + childPadding + viewSizeQuarter
            }

        } else {
            startPosition = - columnCount
            top = parentTop - viewSize - childPadding
        }

        for (i in startPosition until adapterItemCount + columnCount) {
            var pos = i

            if (i < 0) {
                pos = adapterItemCount + i
            }

            if (i >= adapterItemCount) {
                pos = i % adapterItemCount
            }


            val view = recycler.getViewForPosition(pos)
            view.layoutParams.width = viewSize
            view.layoutParams.height = viewSize
            addView(view)
            view.measure()

            bottom = top + viewSize

            layoutView(view, top, bottom, verticalOffset)

            val decoratedRightOfView = getDecoratedRight(view)
            if (decoratedRightOfView > parentRight - childPadding) {
                verticalOffset = parentLeft
                top = bottom - viewSizeQuarter * (columnCount - 1) - childPadding * (columnCount - 1) + childPadding
            } else {
                verticalOffset += childPadding + viewSize
                top += childPadding + viewSizeQuarter
            }
        }
    }

    private fun fillTop(recycler: RecyclerView.Recycler) {
        if (childCount == 0) return

        var top: Int
        var bottom: Int
        val verticalOffset: Int

        val decoratedLeft: Int

        val firstVisibleChild = getChildAt(0)!!
        var firstVisibleChildPosition = getPosition(firstVisibleChild)

        if (firstVisibleChildPosition == 0) {
            firstVisibleChildPosition = itemCount
        }


       decoratedLeft = getDecoratedLeft(firstVisibleChild)

        if (decoratedLeft < parentLeft + childPadding) {
            verticalOffset = parentRight - viewSize
            bottom =  getDecoratedTop(firstVisibleChild) + viewSizeQuarter * (columnCount - 1) - childPadding + childPadding * (columnCount - 1)
        } else {
            verticalOffset = getDecoratedLeft(firstVisibleChild) - viewSize - childPadding
            bottom = getDecoratedBottom(firstVisibleChild) - viewSizeQuarter - childPadding
        }

        for (i in firstVisibleChildPosition - 1 downTo 0) {
            val view = recycler.getViewForPosition(i)
            addView(view, 0)
            view.layoutParams.height = viewSize
            view.layoutParams.width = viewSize
            view.measure()

            top = bottom - viewSize
            measureChild(view, viewSize, viewSize)
            layoutView(view, top, bottom, verticalOffset)

            val decoratedLeftOfView = getDecoratedLeft(view)
            bottom = if (decoratedLeftOfView < parentLeft + childPadding) {
                top + viewSizeQuarter * (columnCount - 1) - childPadding + childPadding * (columnCount - 1)
            } else {
                bottom - viewSizeQuarter - childPadding
            }
        }
    }

    private fun layoutView(
        view: View,
        top: Int,
        bottom: Int,
        vertical: Int = 200
    ) {
        val end = vertical + viewSize
        layoutDecorated(view, vertical, top, end, bottom)
    }

    private fun offsetChildrenVerticallyBy(dy: Int) {
        for (i in 0 until childCount) {
            val view = getChildAt(i)!!
            view.scrollVerticallyBy(dy)
        }
    }

    private fun View.scrollVerticallyBy(dy: Int) {
        offsetTopAndBottom(-dy)
    }

    private fun View.measure() {
        measureChildWithMargins(this, viewSize, viewSize)
    }
}