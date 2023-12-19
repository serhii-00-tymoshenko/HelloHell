package com.mintokoneko.notes.ui.notes.layoutmanagers

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler


class CustomLayoutManager(
    private val viewSize: Int,
    private val childPadding: Int,
    private val columnCount: Int,
) : RecyclerView.LayoutManager() {

    class LayoutParams : RecyclerView.LayoutParams {
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.MarginLayoutParams) : super(source)
    }

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
        LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        val recyclerViewLayoutParams = super.generateLayoutParams(lp)
        return LayoutParams(recyclerViewLayoutParams)
    }

    override fun generateLayoutParams(c: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
        val recyclerViewLayoutParams = super.generateLayoutParams(c, attrs)
        return LayoutParams(recyclerViewLayoutParams)
    }


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

        val startPosition: Int

        if (childCount > 0) {
            val lastChild = getChildAt(childCount - 1)!!
            val lastChildPosition = getPosition(lastChild)
            startPosition = lastChildPosition + 1



            if ((lastChildPosition + 1) % columnCount == 0) {
                verticalOffset = parentLeft
            } else {
                verticalOffset = getDecoratedLeft(lastChild) + childPadding + viewSize
            }

            top = if ((lastChildPosition + 1) % columnCount == 0) {
                getDecoratedBottom(lastChild) - viewSizeQuarter * (columnCount - 1) - childPadding * (columnCount - 1) + childPadding
            } else {
                getDecoratedTop(lastChild) + childPadding + viewSizeQuarter
            }

        } else {
            startPosition = 0
            top = parentTop
        }

        for (i in startPosition until adapterItemCount) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            view.measure()

            bottom = top + viewSize

            layoutView(view, top, bottom, verticalOffset)

            if ((i + 1) % columnCount == 0) {
                verticalOffset = childPadding
            } else {
                verticalOffset += childPadding + viewSize
            }


            top = if ((i + 1) % columnCount == 0) {
                bottom - viewSizeQuarter * (columnCount - 1) - childPadding * (columnCount - 1) + childPadding
            } else {
                top + viewSizeQuarter + childPadding
            }
        }
    }

    private fun fillTop(recycler: RecyclerView.Recycler) {
        if (childCount == 0) return

        var top: Int
        var bottom: Int
        var verticalOffset: Int

        val firstVisibleChild = getChildAt(0)!!
        val firstVisibleChildPosition = getPosition(firstVisibleChild)

        if (firstVisibleChildPosition == 0) return

        verticalOffset = if ((firstVisibleChildPosition + 1) % columnCount == 1) {
            parentRight - viewSize
        } else {
            getDecoratedLeft(firstVisibleChild) - childPadding - viewSize
        }

        bottom = if ((firstVisibleChildPosition + 1) % columnCount == 1) {
            getDecoratedTop(firstVisibleChild) + viewSizeQuarter * (columnCount - 1) - childPadding + childPadding * (columnCount - 1)
        } else {
            getDecoratedBottom(firstVisibleChild) - viewSizeQuarter - childPadding
        }


        for (i in firstVisibleChildPosition - 1 downTo 0) {
            val view = recycler.getViewForPosition(i)
            addView(view, 0)
            view.measure()

            top = bottom - viewSize
            layoutView(view, top, bottom, verticalOffset)

            bottom = if ((firstVisibleChildPosition + 1) % columnCount == 1) {
                top + viewSizeQuarter * (columnCount - 1) - childPadding + childPadding * (columnCount - 1)
            } else {
                bottom - viewSizeQuarter - childPadding
            }

            if ((i + 1) % columnCount == 1) {
                verticalOffset = parentRight - viewSize
            } else {
                verticalOffset += childPadding + viewSize
            }
        }
    }

    private fun layoutView(
        view: View,
        top: Int,
        bottom: Int,
        vertical: Int = 100
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
