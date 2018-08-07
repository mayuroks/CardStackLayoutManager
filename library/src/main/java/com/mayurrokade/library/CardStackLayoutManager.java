package com.mayurrokade.library;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO Step 1 return layout params
 * TODO Step 2 define constructor
 * TODO Step 3 no child to show remove and recycler all VIEWs
 * TODO Step 4 setup child view size based on orientation
 * <p>
 * <p>
 * TODO Mods:
 * 1 - How is mScrollOffset modified
 * 2 - Relation between, mScrollOffset and BottomChild
 */
public class CardStackLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "CardStackLayoutManager";
    private static final int INVALIDATE_SCROLL_OFFSET = Integer.MAX_VALUE;

    public static final int VERTICAL = 111;
    public static final int HORIZONTAL = 222;

    private static final float DEFAULT_CHILD_LAYOUT_OFFSET = 0.2f;

    private float mItemHeightWidthRatio;
    private float mScale;
    private int mOrientation;
    private Interpolator mInterpolator;

    // Peeksize is cards that are behind, how much region should be exposed
    private int mChildPeekSize;
    private int mChildPeekSizeInput;

    private static final String CHILD_WIDTH = "childWidth";
    private static final String CHILD_HEIGHT = "childHeight";
    private Map<String, Integer> mChildXYParams;

    // TODO change variable name to is
    private boolean mCheckedChildSize;

    // The entire scroll range for recycler view
    // This will be in multiples of childSize
    // When user scrolls, this scroll offset is converted
    // to the bottom card that is shown to the user
    private int mScrollOffset = INVALIDATE_SCROLL_OFFSET;
    private static final int MIN_ITEM_LAYOUT_COUNT = 2;
    private int mChildCount;
    private boolean mReverse;
    private ChildDecorateHelper mDecorateHelper;
    private int mMaxItemLayoutCount;
    private int UNLIMITED = 0;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        // TODO Step 1 return layout params
        return new RecyclerView.LayoutParams(getChildWidth(), getChildHeight());
    }

    public CardStackLayoutManager(float itemHeightWidthRatio, float scale, int orientation) {
        mItemHeightWidthRatio = itemHeightWidthRatio;
        mScale = scale;
        mOrientation = orientation;
        mChildXYParams = new HashMap<>();

        // setup interpolator
        mInterpolator = new DecelerateInterpolator();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.i(TAG, "onLayoutChildren: called when layout out children views");

        if (state.getItemCount() == 0) {
            // TODO step 3 No child to show remove and recycler all VIEWs
            removeAndRecycleAllViews(recycler);
            return;
        }

        // TODO step 4 setup child view size based on orientation
        if (!mCheckedChildSize) {
//            if (mOrientation == VERTICAL) {
//                mChildSize[0] = getHorizontalSpace();
//                mChildSize[1] = (int) (mItemHeightWidthRatio * mChildSize[0]);
//            } else {
//                mChildSize[1] = getVerticalSpace();
//                mChildSize[0] = (int) (mChildSize[1] / mItemHeightWidthRatio);
//            }

            setChildSize(mOrientation);

            mChildPeekSize = mChildPeekSizeInput == 0 ?
                    (int) (getChildSize(mOrientation) * DEFAULT_CHILD_LAYOUT_OFFSET) : mChildPeekSizeInput;
            mCheckedChildSize = true;
        }

        int itemCount = getItemCount();

        // TODO setup scroll offset, what happens when its not setup
//        if (mReverse) { // figure out how reverse works
//            mScrollOffset += (itemCount - mChildCount) * getChildSize(mOrientation);
//        }

        mScrollOffset = getChildSize(mOrientation);
        mChildCount = itemCount;
        mScrollOffset = makeScrollOffsetWithinRange(mScrollOffset);
        fill2(recycler);
    }

    public void fill(RecyclerView.Recycler recycler) {
        Log.i(TAG, "fill: scrollOffset " + mScrollOffset);
        int bottomItemPosition = (int) Math.floor(mScrollOffset / getChildSize(mOrientation));//>=1
        int bottomItemVisibleSize = mScrollOffset % getChildSize(mOrientation);
        final float offsetFactor = mInterpolator.getInterpolation( // offsetFactor [0,1)
                bottomItemVisibleSize * 1.0f / getChildSize(mOrientation));
        final int recyclerViewSpace = mOrientation == VERTICAL ? getVerticalSpace() : getHorizontalSpace();

        ArrayList<ItemLayoutInfo> layoutInfos = new ArrayList<>();
        Log.i(TAG, "fill: bottomItemPosition "
                + bottomItemPosition + " "
                + bottomItemVisibleSize + " "
                + offsetFactor + " "
                + getChildSize(mOrientation));

        for (int i = bottomItemPosition - 1, j = 1, remainSpace = recyclerViewSpace - getChildSize(mOrientation); i >= 0; i--, j++) {
//            Log.i(TAG, "forloop: " + remainSpace + " " + recyclerViewSpace);

            double maxOffset = mChildPeekSize * Math.pow(mScale, j);
            int childStart = (int) (remainSpace - offsetFactor * maxOffset);
            float layoutPercent = childStart * 1.0f / recyclerViewSpace;
            float scale = (float) (Math.pow(mScale, j - 1) * (1 - offsetFactor * (1 - mScale)));

            ItemLayoutInfo info
                    = new ItemLayoutInfo(childStart, scale, offsetFactor, layoutPercent);

            layoutInfos.add(0, info);
            if (mMaxItemLayoutCount != UNLIMITED && j == mMaxItemLayoutCount - 1) {
                Log.i(TAG, "fill: mMaxItemLayoutCount != UNLIMITED && j == mMaxItemLayoutCount - 1");
                if (offsetFactor != 0) {
                    Log.i(TAG, "fill: offsetFactor != 0");
                    info.start = remainSpace;
                    info.positionOffsetFactor = 0;
                    info.layoutPercent = remainSpace / recyclerViewSpace;
                    info.scaleXY = (float) Math.pow(mScale, j - 1);
                }
                break;
            }

            // Reduce remain space for next card
            remainSpace -= maxOffset;
//            Log.i(TAG, "fill: offsetFactor " + offsetFactor + " remainSpace " + remainSpace + " maxOffset " + maxOffset);

            // If there is no space left, set layoutInfo so that the childView is just visible
            if (remainSpace <= 0) {
//                Log.i(TAG, "fill: remainSpace <= 0");
                info.start = (int) (remainSpace + maxOffset);
                info.positionOffsetFactor = 0;
                info.layoutPercent = info.start / recyclerViewSpace;
                info.scaleXY = (float) Math.pow(mScale, j - 1);
//                Log.i(TAG, "fill: " + info.printInfo());
                break;
            }
        }

        if (bottomItemPosition < mChildCount) {
            int start = recyclerViewSpace - bottomItemVisibleSize;
            float positionOffset = bottomItemVisibleSize * 1.0f / getChildSize(mOrientation);
            float layoutPercent = start * 1.0f / recyclerViewSpace;

            ItemLayoutInfo layoutInfo
                    = new ItemLayoutInfo(start, 1.0f, positionOffset, layoutPercent);
            layoutInfo.setIsBottom();

            layoutInfos.add(layoutInfo);
        } else {
            bottomItemPosition -= 1;
        }

        int layoutCount = layoutInfos.size();

        // Check if a child position is out of visible range
        final int startPos = bottomItemPosition - (layoutCount - 1);
        final int endPos = bottomItemPosition;
//        Log.i(TAG, "fill2: startPos " + startPos
//                + " endPos " + endPos
//                + " layoutCount " + layoutCount
//                + " bottomItemPosition " + bottomItemPosition);

        // Is it necessary??
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            int pos = convert2LayoutPosition(getPosition(childView));

            if (pos < startPos || pos > endPos) {
                Log.i(TAG, "recycling view " + pos + " " + endPos + " " + startPos);
                removeAndRecycleView(childView, recycler);
            }
        }

        // Remove all views
        detachAndScrapAttachedViews(recycler);

        // Layout childViews using updated layoutInfo
        for (int i = 0; i < layoutCount; i++) {
            int position = convert2AdapterPosition(startPos + i);

            View view = recycler.getViewForPosition(position);
            ItemLayoutInfo layoutInfo = layoutInfos.get(i);

            Log.i(TAG, "fillChild: " + position + " " + layoutInfo.printInfo());
            fillChild(view, layoutInfo);
        }
    }

    public void fill2(RecyclerView.Recycler recycler) {
        Log.i(TAG, "fill2: scrollOffset " + mScrollOffset);
        final int recyclerViewSpace = mOrientation == VERTICAL ? getVerticalSpace() : getHorizontalSpace();

        // Get bottom item position
        int bottomItemPosition = mChildCount - (int) Math.floor(mScrollOffset / getChildSize(mOrientation));//>=1
        int bottomItemVisibleSize = getChildSize(mOrientation) - mScrollOffset % getChildSize(mOrientation);

        final float offsetFactor = mInterpolator.getInterpolation( // offsetFactor [0,1)
                bottomItemVisibleSize * 1.0f / getChildSize(mOrientation));

        ArrayList<ItemLayoutInfo> layoutInfos = new ArrayList<>();
//        Log.i(TAG, "fill2: bottomItemPosition "
//                + bottomItemPosition + " "
//                + bottomItemVisibleSize + " "
//                + offsetFactor + " "
//                + getChildSize(mOrientation));

        for (int i = bottomItemPosition - 1, j = 1, remainSpace = recyclerViewSpace - getChildSize(mOrientation); i >= 0; i--, j++) {
            double maxOffset = mChildPeekSize * Math.pow(mScale, j);
            // figure out a good formula to calculate start position
            int childEnd = (int) (recyclerViewSpace - remainSpace + offsetFactor * maxOffset);
            int childStart = (int) (childEnd - getChildSize(mOrientation));
            float layoutPercent = remainSpace * 1.0f / recyclerViewSpace;
            float scale = (float) (Math.pow(mScale, j - 1) * (1 - offsetFactor * (1 - mScale)));
            Log.i(TAG, "forLoop: " + remainSpace + " " + recyclerViewSpace + " " + childStart + " " + layoutPercent);

            ItemLayoutInfo info
                    = new ItemLayoutInfo(childStart, scale, offsetFactor, layoutPercent);

            layoutInfos.add(0, info);
            if (mMaxItemLayoutCount != UNLIMITED && j == mMaxItemLayoutCount - 1) {
                Log.i(TAG, "fill2: mMaxItemLayoutCount != UNLIMITED && j == mMaxItemLayoutCount - 1");
                if (offsetFactor != 0) {
                    int end = (int) (recyclerViewSpace - remainSpace + maxOffset);
                    info.start = end - getChildSize(mOrientation);
                    info.positionOffsetFactor = 0;
                    info.layoutPercent = remainSpace / recyclerViewSpace;
                    info.scaleXY = (float) Math.pow(mScale, j - 1);
                }
                break;
            }

            // Reduce remain space for next card
            remainSpace -= maxOffset;
//            Log.i(TAG, "fill2: offsetFactor " + offsetFactor + " remainSpace " + remainSpace + " maxOffset " + maxOffset);

            // If there is no space left, set layoutInfo so that the childView is just visible
            if (remainSpace <= 0) {
                Log.i(TAG, "fill2: remainSpace <= 0");
                remainSpace += maxOffset;
                int end = (int) (recyclerViewSpace - remainSpace);
                info.start = end - getChildSize(mOrientation);
                info.positionOffsetFactor = 0;
                info.layoutPercent = remainSpace / recyclerViewSpace;
                info.scaleXY = (float) Math.pow(mScale, j - 1);
                Log.i(TAG, "fill2: " + info.printInfo());
//                Log.i(TAG, "fill2: layoutPercent " + remainSpace + " " + maxOffset);
                break;
            }
        }

        if (bottomItemPosition < mChildCount) {
            Log.i(TAG, "fill2: bottomItemPosition < mChildCount " + bottomItemPosition + " " + mChildCount);
            int start = bottomItemVisibleSize - getChildSize(mOrientation);
            float positionOffset = bottomItemVisibleSize * 1.0f / getChildSize(mOrientation);
            float layoutPercent = bottomItemVisibleSize * 1.0f / getChildSize(mOrientation);
            float scale = 1.0f;

            ItemLayoutInfo layoutInfo
                    = new ItemLayoutInfo(start, scale, positionOffset, layoutPercent);
            layoutInfo.setIsBottom();

            layoutInfos.add(layoutInfo);
        } else {
            Log.i(TAG, "fill2: bottomItemPosition >= mChildCount " + bottomItemPosition + " " + mChildCount);
            bottomItemPosition -= 1;
        }

        int layoutCount = layoutInfos.size();

        // Check if a child position is out of visible range
        final int startPos = bottomItemPosition - (layoutCount - 1);
        final int endPos = bottomItemPosition;
        Log.i(TAG, "fill2: startPos " + startPos
                + " endPos " + endPos
                + " layoutCount " + layoutCount
                + " bottomItemPosition " + bottomItemPosition);

        // Is it necessary??
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            int pos = convert2LayoutPosition(getPosition(childView));

            if (pos < startPos || pos > endPos) {
                Log.i(TAG, "recycling view " + pos + " " + endPos + " " + startPos);
//                removeAndRecycleView(childView, recycler);
            }
        }

        // Remove all views
        detachAndScrapAttachedViews(recycler);

        // Layout childViews using updated layoutInfo
        for (int i = 0; i < layoutCount; i++) {
            int position = convert2AdapterPosition(startPos + i);
            View view = recycler.getViewForPosition(position);
            ItemLayoutInfo layoutInfo = layoutInfos.get(i);

            Log.i(TAG, "fillChild: " + position + " " + layoutInfo.printInfo());
            fillChild(view, layoutInfo);
        }
    }

    private void fillChild(View view, CardStackLayoutManager.ItemLayoutInfo layoutInfo) {
        // add the view
        addView(view);

        // what does this do ???
        measureChildWithExactlySize(view);

        final int scaleFix = (int) (getChildSize(mOrientation) * (1 - layoutInfo.scaleXY) / 2);
//        final float gap = (mOrientation == VERTICAL ? getHorizontalSpace() : getVerticalSpace())
//                - mChildSize[(mOrientation + 1) % 2] * layoutInfo.scaleXY;


        if (mOrientation == VERTICAL) {
            // Calculate sides
            int left = getPaddingLeft();
            int top = layoutInfo.start + scaleFix;
            int right = left + getChildWidth();
            int bottom = top + getChildHeight() + scaleFix;

            // layout the child
            layoutDecoratedWithMargins(view, left, top, right, bottom);

        } else {
            // Calculate sides
            int left = layoutInfo.start + scaleFix;
            int top = getPaddingTop();
            int right = left + getChildWidth() + scaleFix;
            int bottom = top + getChildHeight();

            // layout the child
            layoutDecoratedWithMargins(view, left, top, right, bottom);
        }

        // set scale of card based on layout info
        ViewCompat.setScaleX(view, layoutInfo.scaleXY);
        ViewCompat.setScaleY(view, layoutInfo.scaleXY);

        if (mDecorateHelper != null) {
            mDecorateHelper.decorateChild(view, layoutInfo.positionOffsetFactor, layoutInfo.layoutPercent, layoutInfo.isBottom);
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pendingScrollOffset = mScrollOffset + dx;
        mScrollOffset = makeScrollOffsetWithinRange(pendingScrollOffset);
        fill2(recycler);
        return mScrollOffset - pendingScrollOffset + dx;
    }

    private int getVerticalSpace() {
        /* TODO
         * Play with params
         * what if only height is returned
         * */
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getHorizontalSpace() {
        /* TODO
         * Play with params
         * what if only width is returned
         * */
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int makeScrollOffsetWithinRange(int scrollOffset) {
        /* TODO
         * What does this formula do
         * */
        Log.i(TAG, "makeScrollOffsetWithinRange: before " + scrollOffset);
        int offset = Math.max(getChildSize(mOrientation), scrollOffset);
        int childCountOffset = mChildCount * getChildSize(mOrientation);

        Log.i(TAG, "makeScrollOffsetWithinRange: after " + offset + " " + childCountOffset);
        return Math.min(offset, childCountOffset);
    }

    private void measureChildWithExactlySize(View child) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(
                getChildWidth() - lp.leftMargin - lp.rightMargin, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(
                getChildHeight() - lp.topMargin - lp.bottomMargin, View.MeasureSpec.EXACTLY);
        child.measure(widthSpec, heightSpec);
    }

    public int convert2LayoutPosition(int adapterPosition) {
//        return mReverse ? mChildCount - 1 - adapterPosition : adapterPosition;
        return adapterPosition;
    }

    public int convert2AdapterPosition(int layoutPosition) {
//        return mReverse ? mChildCount - 1 - layoutPosition : layoutPosition;
        return layoutPosition;
    }

    public void setChildPeekSize(int childPeekSize) {
        Log.i(TAG, "setChildPeekSize: called");
        mChildPeekSizeInput = childPeekSize;
        mCheckedChildSize = false;
        if (getChildCount() > 0) {
            requestLayout();
        }
    }

    public void setMaxItemLayoutCount(int count) {
        mMaxItemLayoutCount = Math.max(MIN_ITEM_LAYOUT_COUNT, count);
        if (getChildCount() > 0) {
            requestLayout();
        }
    }

    public CardStackLayoutManager setChildDecorateHelper(CardStackLayoutManager.ChildDecorateHelper layoutHelper) {
        mDecorateHelper = layoutHelper;
        return this;
    }

    private static class ItemLayoutInfo {
        // More like visibility index [0 ,1)
        float layoutPercent;

        float scaleXY;

        // OffsetFactor [0,1)
        float positionOffsetFactor;

        // Start position of item.
        int start;

        boolean isBottom;

        ItemLayoutInfo(int start, float scale, float positionOffset, float layoutPercent) {
            this.start = start;
            this.scaleXY = scale;
            this.positionOffsetFactor = positionOffset;
            this.layoutPercent = layoutPercent;
        }

        CardStackLayoutManager.ItemLayoutInfo setIsBottom() {
            isBottom = true;
            return this;
        }

        public String printInfo() {
            return "layoutInfo : start: " + start
                    + " layoutPercent: " + layoutPercent
                    + " positionOffsetFactor: " + positionOffsetFactor
                    + " scaleXY: " + scaleXY
                    + " isBottom: " + isBottom;
        }
    }

    public interface ChildDecorateHelper {
        /**
         * @param child
         * @param posoffsetFactor childview相对于自身起始位置的偏移量百分比范围[0，1)
         * @param layoutPercent   childview 在整个布局中的位置百分比
         * @param isBottom        childview 是否处于底部
         */
        void decorateChild(View child, float posoffsetFactor, float layoutPercent, boolean isBottom);
    }

    public static class DefaultChildDecorateHelper implements CardStackLayoutManager.ChildDecorateHelper {
        private float mElevation;

        public DefaultChildDecorateHelper(float maxElevation) {
            mElevation = maxElevation;
        }

        @Override
        public void decorateChild(View child, float posOffsetFactor, float layoutPercent, boolean isBottom) {
            float elevation = (float) (layoutPercent * mElevation * 0.7 + mElevation * 0.3);

            if (isBottom) elevation = Math.max(elevation, mElevation);

            ViewCompat.setElevation(child, elevation);
        }
    }

    private int getChildSize(int orientation) {
        if (orientation == VERTICAL) {
            return mChildXYParams.get(CHILD_HEIGHT);
        } else {
            return mChildXYParams.get(CHILD_WIDTH);
        }
    }

    private int getChildWidth() {
        return mChildXYParams.get(CHILD_WIDTH);
    }

    private int getChildHeight() {
        return mChildXYParams.get(CHILD_HEIGHT);
    }

    private void setChildSize(int orientation) {
        int childHeight, childWidth;

        switch (orientation) {
            case VERTICAL:
                childWidth = getHorizontalSpace();
                childHeight = (int) (mItemHeightWidthRatio * childWidth);
                mChildXYParams.put(CHILD_WIDTH, childWidth);
                mChildXYParams.put(CHILD_HEIGHT, childHeight);
                break;

            case HORIZONTAL:
                childHeight = getVerticalSpace();
                childWidth = (int) (childHeight / mItemHeightWidthRatio);
                mChildXYParams.put(CHILD_WIDTH, childWidth);
                mChildXYParams.put(CHILD_HEIGHT, childHeight);
                break;
        }
    }
}
