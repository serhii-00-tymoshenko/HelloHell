import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.LogPrinter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.RecyclerView
import androidx.resourceinspection.annotation.Attribute.IntMap
import com.mintokoneko.notes.utils.dpToPx
import kotlin.math.max
import kotlin.math.min

private const val TAG = "Custom"

class CustomLayoutManager(
    private val viewSize: Int,
    private val columnCount: Int,
    private val gravity: Gravity
) : RecyclerView.LayoutManager() {

    enum class Gravity {
        LEFT, RIGHT
    }

    data class State(val anchorPosition: Int, val anchorOffset: Int) : Parcelable {

        constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt())

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(anchorPosition)
            dest.writeInt(anchorOffset)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<State> {
            override fun createFromParcel(parcel: Parcel): State = State(parcel)
            override fun newArray(size: Int): Array<State?> = arrayOfNulls(size)
        }
    }

    class LayoutParams : RecyclerView.LayoutParams {
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.MarginLayoutParams) : super(source)

        var spanSize: Int = 1
    }

    private var anchorPosition = 0
    private var anchorOffset = 0

    private val parentTop: Int
        get() = paddingTop

    private val parentBottom: Int
        get() = height - paddingBottom

    private val parentLeft: Int
        get() = paddingLeft

    private val parentRight: Int
        get() = width - paddingRight

    private val parentMiddle: Int
        get() = width / 2

    private val parentWidth: Int
        get() = parentRight - parentLeft

    private val columnWidth: Int
        get() = parentWidth / 2

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

    override fun scrollToPosition(position: Int) {
        anchorPosition = position
        anchorOffset = 0
        requestLayout()
    }

    override fun canScrollVertically(): Boolean = true

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int =
        when {
            childCount == 0 -> 0
            dy < 0 -> {
                val availableTop = if (clipToPadding) parentTop else 0
                var scrolled = 0
                while (scrolled > dy) {
                    val firstChild = getChildAt(0)!!
                    val firstChildTop =
                        getDecoratedTop(firstChild) - firstChild.layoutParams().topMargin
                    val hangingTop = max(0, availableTop - firstChildTop)
                    val scrollBy = min(hangingTop, scrolled - dy)
                    offsetChildrenVerticallyBy(-scrollBy)
                    scrolled -= scrollBy
                    if (anchorPosition == 0) break
                    fillTop(recycler)
                }
                scrolled
            }

            dy > 0 -> {
                val availableBottom = if (clipToPadding) parentBottom else height
                var scrolled = 0
                while (scrolled < dy) {
                    val lastChild = getChildAt(childCount - 1)!!
                    val lastChildPosition = getPosition(lastChild)
                    val layoutParams = lastChild.layoutParams()
                    val lastChildBottom = getDecoratedBottom(lastChild) + layoutParams.bottomMargin
                    val hangingBottom = max(0, lastChildBottom - availableBottom)
                    val scrollBy = min(hangingBottom, dy - scrolled)
                    offsetChildrenVerticallyBy(scrollBy)
                    scrolled += scrollBy
                    if (lastChildPosition == state.itemCount - 1) break
                    fillBottom(recycler, state.itemCount)
                }
                scrolled
            }

            else -> 0
        }
            .also {
                recycleViewsOutOfBounds(recycler)
                updateAnchorOffset()
            }

    fun lastLaidOutViewPosition(): Int =
        when {
            childCount <= 0 -> 0
            else -> {
                val view = getChildAt(childCount - 1)!!
                getPosition(view)
            }
        }

    private fun recycleViewsOutOfBounds(recycler: RecyclerView.Recycler) {

        if (childCount == 0) return
        val childCount = childCount

//        var firstVisibleChild = 0
//        for (i in 0 until childCount) {
//            val child = getChildAt(i)!!
//            val layoutParams = child.layoutParams()
//            val top = if (clipToPadding) parentTop else 0
//            if (getDecoratedBottom(child) + layoutParams.bottomMargin < top) {
//                firstVisibleChild++
//            } else {
//                break
//            }
//        }
//
//        var lastVisibleChild = firstVisibleChild
//        for (i in lastVisibleChild until childCount) {
//            val child = getChildAt(i)!!
//            val layoutParams = child.layoutParams()
//            if (getDecoratedTop(child) - layoutParams.topMargin <= if (clipToPadding) parentBottom else height) {
//                lastVisibleChild++
//            } else {
//                lastVisibleChild--
//                break
//            }
//        }
//
//        for (i in childCount - 1 downTo lastVisibleChild + 1) removeAndRecycleViewAt(i, recycler)
//        for (i in firstVisibleChild - 1 downTo 0) removeAndRecycleViewAt(i, recycler)
//        anchorPosition += firstVisibleChild
    }

    private fun fillBottom(recycler: RecyclerView.Recycler, adapterItemCount: Int) {
        var top: Int
        var bottom: Int
        var vertical = if (gravity == Gravity.LEFT) parentLeft else parentRight

        var startPosition: Int

        if (childCount > 0) {
            val lastChild = getChildAt(childCount - 1)!!
            val lastChildPosition = getPosition(lastChild)
            startPosition = lastChildPosition + 1

            top = getDecoratedBottom(lastChild) + dpToPx(16F) - viewSizeQuarter * 3

            vertical = if (gravity == Gravity.LEFT) {
                getDecoratedLeft(lastChild) + dpToPx(16F) + viewSize
            } else {
                getDecoratedRight(lastChild) - dpToPx(16F) - viewSize
            }
            if ((lastChildPosition + 1) % 3 == 0) top += dpToPx(16F) * 2
            if ((lastChildPosition + 1) % 3 == 0) vertical =
                if (gravity == Gravity.LEFT) parentLeft else parentRight
        } else {
            startPosition = 0
            top = parentTop
        }

        for (i in startPosition until adapterItemCount) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            view.measure()

            bottom = top + viewSize

            layoutView(view, top, bottom, gravity, vertical)

            vertical += if (gravity == Gravity.LEFT) {
                dpToPx(16F) + viewSize
            } else {
                -dpToPx(16F) - viewSize
            }

            top = bottom + dpToPx(16F) - (viewSize / 4) * 3
            if ((i + 1) % columnCount == 0) {
                top += dpToPx(16F) * 2
                vertical = dpToPx(16F)
            }
        }
    }

    private fun fillTop(recycler: RecyclerView.Recycler) {
        if (childCount == 0) return

        var top: Int
        var bottom: Int
        var verticalOffset: Int

        val firstChild = getChildAt(0)!!
        val firstChildPosition = getPosition(firstChild)
        if (firstChildPosition == 0) return

        verticalOffset = if (gravity == Gravity.LEFT) {
            getDecoratedLeft(firstChild) - dpToPx(16F) - viewSize


        } else {
            getDecoratedRight(firstChild) + dpToPx(16F) + viewSize
        }
        Log.d(TAG, "fillTop22: $verticalOffset")


        bottom = getDecoratedTop(firstChild) - dpToPx(16F) + viewSizeQuarter * 3
        if ((firstChildPosition + 1) % columnCount == 1) bottom -= dpToPx(16F) * 2
        if ((firstChildPosition + 1) % columnCount == 1) verticalOffset =
            if (gravity == Gravity.LEFT) parentRight - viewSize else parentRight

        val availableTop = if (clipToPadding) parentTop else 0

        for (i in firstChildPosition - 1 downTo 0) {
            val view = recycler.getViewForPosition(i)
            addView(view, 0)
            view.measure()
            if (bottom < availableTop + viewSize) break

            top = bottom - viewSize

            layoutView(view, top, bottom, gravity, verticalOffset)

            verticalOffset += if (gravity == Gravity.LEFT) {
                -dpToPx(16F) - viewSize
            } else {
                dpToPx(16F) + viewSize
            }

            bottom = top - dpToPx(16F) + (viewSize / 4) * 3
            if ((i + 1) % columnCount == 1) {
                bottom -= dpToPx(16F) * 2
                verticalOffset = if (gravity == Gravity.LEFT) parentRight - viewSize else parentRight
            }
        }
    }

        private fun layoutView(view: View, top: Int, bottom: Int, gravity: Gravity, vertical: Int) {
            var start: Int = 0
            var end: Int = 0

            if (gravity == Gravity.LEFT) {
                start = vertical
                end = start + viewSize
            } else {
                end = parentRight - vertical
                start = end - viewSize
            }

            layoutDecorated(view, start, top, end, bottom)
        }

        private fun updateAnchorOffset() {
            anchorOffset =
                if (childCount > 0) {
                    val view = getChildAt(0)!!
                    getDecoratedTop(view) - view.layoutParams().topMargin - parentTop
                } else {
                    0
                }
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

        private fun View.layoutParams(): LayoutParams =
            layoutParams as LayoutParams

        private fun View.measure() {
            measureChildWithMargins(this, viewSize, viewSize)
        }
    }
