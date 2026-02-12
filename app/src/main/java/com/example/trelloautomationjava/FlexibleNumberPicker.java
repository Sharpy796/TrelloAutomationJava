package com.example.trelloautomationjava;/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater.Filter;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker.Formatter;
import android.widget.Scroller;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.Keep;
import androidx.annotation.Px;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_FLING;
import static android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE;
import static android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

/**
 * A widget that enables the user to select a number from a predefined range.
 *
 * The widget presents the current value as a scrolling vertical selector with
 * the selected value in the center and the previous and following numbers above
 * and below, separated by a divider. The value is changed by flinging vertically.
 *
 * <a href="https://gist.github.com/c9f473b7095f0c1dac6c36c18bde5b77">This Gist permalink.</a>
 */
public class FlexibleNumberPicker extends FrameLayout {

    /**
     * User choice on whether the selector wheel should be wrapped.
     */
    private boolean mWrapSelectorWheelPreferred = true;

    /**
     * The text for showing the current value.
     */
//    final EditText mInputText;

    /**
     * The min height of this widget.
     */
    private final int mMinHeight;

    /**
     * The max height of this widget.
     */
    private final int mMaxHeight;

    /**
     * The max width of this widget.
     */
    private final int mMinWidth;

    /**
     * The max width of this widget.
     */
    private int mMaxWidth;

    /**
     * Flag whether to compute the max width.
     */
    private final boolean mComputeMaxWidth;

    /**
     * The height of the gap between text elements if the selector wheel.
     */
    private int mSelectorTextGapHeight;

    /**
     * The values to be displayed instead the indices.
     */
    String[] mDisplayedValues;

    /**
     * Lower value of the range of numbers allowed for the NumberPicker
     */
    int mMinValue;

    /**
     * Upper value of the range of numbers allowed for the NumberPicker
     */
    int mMaxValue;

    /**
     * Current value of this NumberPicker
     */
    int mValue;

    /**
     * Listener to be notified upon current value change.
     */
    private OnValueChangeListener mOnValueChangeListener;

    /**
     * Listener to be notified upon scroll state change.
     */
    private OnScrollListener mOnScrollListener;

    /**
     * Formatter for displaying the current value.
     */
    private Formatter mFormatter;

    /**
     * The speed for updating the value form long press.
     */
    long mLongPressUpdateInterval = 300;

    /**
     * Cache for the string representation of selector indices.
     */
    private final SparseArray<String> mSelectorIndexToStringCache = new SparseArray<>();

    /**
     * The selector indices whose value are show by the selector.
     */
    private int[] mSelectorIndices = new int[3];

    /**
     * The {@link Paint} for drawing the selector.
     */
    private final Paint mSelectorWheelPaint;

    /**
     * The height of a selector element (text + gap).
     */
    private int mSelectorElementHeight;

    /**
     * The initial offset of the scroll selector.
     */
    private int mInitialScrollOffset = Integer.MIN_VALUE;

    /**
     * The current offset of the scroll selector.
     */
    private int mCurrentScrollOffset;

    /**
     * The {@link Scroller} responsible for flinging the selector.
     */
    private final Scroller mFlingScroller;

    /**
     * The {@link Scroller} responsible for adjusting the selector.
     */
    private final Scroller mAdjustScroller;

    /**
     * The previous Y coordinate while scrolling the selector.
     */
    private int mPreviousScrollerY;

    /**
     * Handle to the reusable command for setting the input text selection.
     */
    SetSelectionCommand mSetSelectionCommand;

    /**
     * Handle to the reusable command for changing the current value from long
     * press by one.
     */
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;

    /**
     * Command for beginning an edit of the current value via IME on long press.
     */
    private BeginSoftInputOnLongPressCommand mBeginSoftInputOnLongPressCommand;

    /**
     * The Y position of the last down event.
     */
    private float mLastDownEventY;

    /**
     * The time of the last down event.
     */
    private long mLastDownEventTime;

    /**
     * The Y position of the last down or move event.
     */
    private float mLastDownOrMoveEventY;

    /**
     * Determines speed during touch scrolling.
     */
    private VelocityTracker mVelocityTracker;

    /**
     * @see ViewConfiguration#getScaledTouchSlop()
     */
    private final int mTouchSlop;

    /**
     * @see ViewConfiguration#getScaledMinimumFlingVelocity()
     */
    private final int mMinimumFlingVelocity;

    /**
     * @see ViewConfiguration#getScaledMaximumFlingVelocity()
     */
    private final int mMaximumFlingVelocity;

    /**
     * Flag whether the selector should wrap around.
     */
    boolean mWrapSelectorWheel;

    /**
     * The back ground color used to optimize scroller fading.
     */
    private int mSolidColor = Color.TRANSPARENT;

    /**
     * Divider for showing item to be selected while scrolling
     */
    Drawable mSelectionDivider;

    /**
     * The current scroll state of the number picker.
     */
    private int mScrollState = SCROLL_STATE_IDLE;

    /**
     * Flag whether to ignore move events - we ignore such when we show in IME
     * to prevent the content from scrolling.
     */
    private boolean mIgnoreMoveEvents;

    /**
     * Flag whether to perform a click on tap.
     */
    private boolean mPerformClickOnTap;

    /**
     * The top of the top selection divider.
     */
    int mTopSelectionDividerTop;

    /**
     * The bottom of the bottom selection divider.
     */
    int mBottomSelectionDividerBottom;

    /**
     * The virtual id of the last hovered child.
     */
    private int mLastHoveredChildVirtualViewId;

    /**
     * Whether the increment virtual button is pressed.
     */
    boolean mIncrementVirtualButtonPressed;

    /**
     * Whether the decrement virtual button is pressed.
     */
    boolean mDecrementVirtualButtonPressed;

    /**
     * Provider to report to clients the semantic structure of this widget.
     */
    private AccessibilityNodeProviderImpl mAccessibilityNodeProvider;

    /**
     * Helper class for managing pressed state of the virtual buttons.
     */
    private final PressedStateHelper mPressedStateHelper;

    /**
     * The keycode of the last handled DPAD down event.
     */
    private int mLastHandledDownDpadKeyCode = -1;


    private float dividerOffset = .5f;

    private static final TimeInterpolator RETARDED_INTERPOLATOR = (value) -> 1f;
    private TimeInterpolator scaleInterpolator = RETARDED_INTERPOLATOR;
    private TimeInterpolator alphaInterpolator = RETARDED_INTERPOLATOR;

    private float lineHeight;

    /**
     * Interface to listen for changes of the current value.
     */
    public interface OnValueChangeListener {

        /**
         * Called upon a change of the current value.
         *
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        void onValueChange(FlexibleNumberPicker picker, int oldVal, int newVal);
    }

    /**
     * Interface to listen for the picker scroll state.
     */
    public interface OnScrollListener {
        @IntDef({SCROLL_STATE_IDLE, SCROLL_STATE_TOUCH_SCROLL, SCROLL_STATE_FLING})
        @Retention(RetentionPolicy.SOURCE)
        @interface ScrollState {}

        /**
         * Callback invoked while the number picker scroll state has changed.
         *
         * @param view The view whose scroll state is being reported.
         * @param scrollState The current scroll state.
         */
        void onScrollStateChange(FlexibleNumberPicker view, @ScrollState int scrollState);
    }

    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     */
    public FlexibleNumberPicker(Context context) {
        this(context, null);
    }

    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     */
    public FlexibleNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        float dp = getResources().getDisplayMetrics().density;
        final Drawable selectionDivider = new ColorDrawable(Color.BLACK);
        selectionDivider.setBounds(0, 0, 0, (int) (2 * dp));
        setSelectionDivider(selectionDivider);

        mMinHeight = -1;
        mMaxHeight = (int) (180 * dp);
        mMinWidth = (int) (64 * dp);
        mMaxWidth = -1;
        mComputeMaxWidth = mMaxWidth == -1;

        mPressedStateHelper = new PressedStateHelper();

        // By default Linearlayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(false);

        /*
        class="android.widget.NumberPicker$CustomEditText"
        android:id="@+id/numberpicker_input"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:singleLine="true"
        android:background="@null"
        android:textAppearance="@style/TextAppearance.Material.NumberPicker" />
         */
        /*mInputText = new CustomEditText(context);
        mInputText.setGravity(Gravity.CENTER);
        mInputText.setSingleLine(true);
        mInputText.setBackground(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mInputText.setTextAppearance(android.R.style.TextAppearance_Material_Body1);
        } else {
            //noinspection deprecation
            mInputText.setTextAppearance(context, android.R.style.TextAppearance_Material_Body1);
        }
        addView(mInputText, new LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        // input text
        mInputText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mInputText.selectAll();
            } else {
                mInputText.setSelection(0, 0);
                validateInputTextView(v);
            }
        });
        mInputText.setFilters(new InputFilter[]{new InputTextFilter()});

        mInputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        mInputText.setImeOptions(EditorInfo.IME_ACTION_DONE);*/

        // initialize constants
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity() / 8;

        // create the selector wheel paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Align.CENTER);
        TypedArray ta = context.obtainStyledAttributes(
                null, new int[] { android.R.attr.textSize, android.R.attr.textColor },
                android.R.attr.textAppearance, android.R.style.TextAppearance_Material_Widget_TextView);
        paint.setTextSize(lineHeight = ta.getDimensionPixelSize(0, 0));
        paint.setColor(ta.getColorStateList(1).getColorForState(ENABLED_STATE_SET, Color.BLACK));
        mSelectorWheelPaint = paint;

        // create the fling and adjust scrollers
        mFlingScroller = new Scroller(getContext(), null, true);
        mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));

        updateInputTextView();

        // If not explicitly specified this view is important for accessibility.
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        // Should be focusable by default, as the text view whose visibility changes is focusable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getFocusable() == View.FOCUSABLE_AUTO) {
            setFocusable(View.FOCUSABLE);
            setFocusableInTouchMode(true);
        }
    }

    /**
     * Set divider above and below current selection. Use intrinsic height, if possible.
     */
    public void setSelectionDividerWithIntrinsicHeight(Drawable selectionDivider) { // ADDED
        if (selectionDivider != null && selectionDivider.getIntrinsicHeight() > 0) {
            selectionDivider.setBounds(0, 0, 0, selectionDivider.getIntrinsicHeight());
        }
        setSelectionDivider(selectionDivider);
    }

    /**
     * Set divider above and below current selection.
     * Reuses height from selectionDivider drawable's bounds, make sure to set it.
     */
    public void setSelectionDivider(Drawable selectionDivider) { // ADDED
        if (mSelectionDivider != selectionDivider) {
            if (mSelectionDivider != null)
                mSelectionDivider.setCallback(null);

            mSelectionDivider = selectionDivider;
            if (selectionDivider != null) {
                selectionDivider.setCallback(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    selectionDivider.setLayoutDirection(getLayoutDirection());
                if (selectionDivider.isStateful())
                    selectionDivider.setState(getDrawableState());
            }
            invalidate();
        }
    }

    /**
     * Update selector wheel paint.
     */
    @SuppressLint("WrongConstant") // Typeface.BOLD_ITALIC is actually Typeface.BOLD | Typeface.ITALIC
    public void setSelectorWheelPaint(Paint copyFrom) { // ADDED
        boolean requestLayout = Build.VERSION.SDK_INT < Build.VERSION_CODES.P ||
                !mSelectorWheelPaint.equalsForTextMeasurement(copyFrom);
        setGravity(gravity(copyFrom));
        mSelectorWheelPaint.set(copyFrom);
        /*mInputText.setTextScaleX(copyFrom.getTextScaleX());
        mInputText.setTypeface(
                copyFrom.getTypeface(),
                (copyFrom.isFakeBoldText() ? Typeface.BOLD : Typeface.NORMAL) |
                        (copyFrom.getTextSkewX() == -.25f ? Typeface.ITALIC : Typeface.NORMAL));
        mInputText.setElegantTextHeight(copyFrom.isElegantTextHeight());
        mInputText.setLetterSpacing(copyFrom.getLetterSpacing());
        mInputText.setFontFeatureSettings(copyFrom.getFontFeatureSettings());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mInputText.setFontVariationSettings(copyFrom.getFontVariationSettings());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mInputText.setShadowLayer(
                    copyFrom.getShadowLayerRadius(), copyFrom.getShadowLayerDx(),
                    copyFrom.getShadowLayerDy(), copyFrom.getShadowLayerColor()
            );
        mInputText.setPaintFlags(copyFrom.getFlags());
        mInputText.setTextSize(TypedValue.COMPLEX_UNIT_PX, copyFrom.getTextSize());
        mInputText.setTextColor(copyFrom.getColor());*/
        if (requestLayout) requestLayout();
        invalidate();
    }

    /**
     * Update selector wheel shadowLayer.
     * This is useful for SDK<29 where paint.getShadowLayer*() methods were introduced.
     * {@link #setSelectorWheelPaint} will copy shadowLayer properties properly only since SDK29.
     */
    public void setShadowLayer(@Px float radius, @Px float dx, @Px float dy, @ColorInt int color) { // ADDED
        mSelectorWheelPaint.setShadowLayer(radius, dx, dy, color);
//        mInputText.setShadowLayer(radius, dx, dy, color);
        invalidate();
    }

    public void setGravity(int gravity) { // ADDED
        Paint.Align align = align(gravity);
        if (mSelectorWheelPaint.getTextAlign() != align) {
            mSelectorWheelPaint.setTextAlign(align);
//            mInputText.setGravity(gravity & Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.CENTER_VERTICAL);
            invalidate();
        }
    }
    @SuppressLint("RtlHardcoded")
    private static int gravity(Paint paint) { // ADDED
        switch (paint.getTextAlign()) {
            case LEFT: return Gravity.LEFT | Gravity.CENTER_VERTICAL;
            case CENTER: return Gravity.CENTER;
            case RIGHT: return Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        }
        throw new AssertionError();
    }
    @SuppressLint("RtlHardcoded")
    private static Paint.Align align(int gravity) { // ADDED
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT: return Align.LEFT;
            case Gravity.CENTER_HORIZONTAL: return Align.CENTER;
            case Gravity.RIGHT: return Align.RIGHT;
            default: throw new UnsupportedOperationException(); // too lazy for this shit
        }
    }
    public void setWheelItemCount(int count) { // ADDED
        if (mSelectorIndices.length != count) {
            if (count < 1 || (count&1) == 0) throw new IllegalArgumentException("count must be positive odd, got " + count);
            mSelectorIndices = new int[count];
            initializeSelectorWheelIndices();
            requestLayout();
        }
    }
    public void setDividerOffset(@FloatRange(from = 0f, to = 1f) float dividerOffset) { // ADDED
        if (this.dividerOffset != dividerOffset) {
            if (dividerOffset < 0f || dividerOffset > 1f)
                throw new IllegalArgumentException("dividerOffset " + dividerOffset + " overlaps text");
            this.dividerOffset = dividerOffset;
            invalidate();
        }
    }
    public void setScaleInterpolator(TimeInterpolator scaleInterpolator) { // ADDED
        if (scaleInterpolator == null) scaleInterpolator = RETARDED_INTERPOLATOR;
        if (this.scaleInterpolator != scaleInterpolator) {
            this.scaleInterpolator = scaleInterpolator;
            invalidate();
        }
    }
    public void setAlphaInterpolator(TimeInterpolator alphaInterpolator) { // ADDED
        if (alphaInterpolator == null) alphaInterpolator = RETARDED_INTERPOLATOR;
        if (this.alphaInterpolator != alphaInterpolator) {
            this.alphaInterpolator = alphaInterpolator;
            invalidate();
        }
    }
    public void setLineHeight(int unit, float value) { // ADDED
        value = TypedValue.applyDimension(unit, value, getResources().getDisplayMetrics());
        if (this.lineHeight != value) {
            this.lineHeight = value;
            initializeSelectorWheel();
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /*final int msrdWdth = getMeasuredWidth();
        final int msrdHght = getMeasuredHeight();

        // Input text centered horizontally.
        final int inptTxtMsrdWdth = mInputText.getMeasuredWidth();
        final int inptTxtMsrdHght = mInputText.getMeasuredHeight();
        final int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        final int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        final int inptTxtRight = inptTxtLeft + inptTxtMsrdWdth;
        final int inptTxtBottom = inptTxtTop + inptTxtMsrdHght;
        mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom);*/

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel();
            initializeFadingEdges();
            float offset = dividerOffset * mSelectorTextGapHeight + mSelectorWheelPaint.getTextSize() / 2f +
                    (mSelectionDivider == null ? 0 : mSelectionDivider.getBounds().height());
            float center = getHeight() / 2f;
            mTopSelectionDividerTop = (int) (center - offset);
            mBottomSelectionDividerBottom = (int) (center + offset);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try greedily to fit the max width and height.
        final int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth);
        final int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        // Flag if we are measured with width or height less than the respective min.
        final int widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, getMeasuredWidth(), widthMeasureSpec);
        final int heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, getMeasuredHeight(), heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * Move to the final position of a scroller. Ensures to force finish the scroller
     * and if it is not at its final position a scroll of the selector wheel is
     * performed to fast forward to the final position.
     *
     * @param scroller The scroller to whose final position to get.
     * @return Whether a move was performed, i.e. the scroller was not in final position.
     */
    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        int futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementHeight;
        int overshootAdjustment = mInitialScrollOffset - futureScrollOffset;
        if (overshootAdjustment != 0) {
            if (Math.abs(overshootAdjustment) > mSelectorElementHeight / 2) {
                if (overshootAdjustment > 0) {
                    overshootAdjustment -= mSelectorElementHeight;
                } else {
                    overshootAdjustment += mSelectorElementHeight;
                }
            }
            amountToScroll += overshootAdjustment;
            scrollBy(0, amountToScroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                removeAllCallbacks();
                hideSoftInput();
                mLastDownOrMoveEventY = mLastDownEventY = event.getY();
                mLastDownEventTime = event.getEventTime();
                mIgnoreMoveEvents = false;
                mPerformClickOnTap = false;
                // Handle pressed state before any state change.
                if (mLastDownEventY < mTopSelectionDividerTop) {
                    if (mScrollState == SCROLL_STATE_IDLE) {
                        mPressedStateHelper.buttonPressDelayed(PressedStateHelper.BUTTON_DECREMENT);
                    }
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    if (mScrollState == SCROLL_STATE_IDLE) {
                        mPressedStateHelper.buttonPressDelayed(PressedStateHelper.BUTTON_INCREMENT);
                    }
                }
                // Make sure we support flinging inside scrollables.
                getParent().requestDisallowInterceptTouchEvent(true);
                if (!mFlingScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                    onScrollerFinished(mFlingScroller);
                    onScrollStateChange(SCROLL_STATE_IDLE);
                } else if (!mAdjustScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                    onScrollerFinished(mAdjustScroller);
                } else if (mLastDownEventY < mTopSelectionDividerTop) {
                    postChangeCurrentByOneFromLongPress(
                            false, ViewConfiguration.getLongPressTimeout());
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    postChangeCurrentByOneFromLongPress(
                            true, ViewConfiguration.getLongPressTimeout());
                } else {
                    mPerformClickOnTap = true;
                    postBeginSoftInputOnLongPressCommand();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (mIgnoreMoveEvents) {
                    break;
                }
                float currentMoveY = event.getY();
                if (mScrollState != SCROLL_STATE_TOUCH_SCROLL) {
                    int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
                    if (deltaDownY > mTouchSlop) {
                        removeAllCallbacks();
                        onScrollStateChange(SCROLL_STATE_TOUCH_SCROLL);
                    }
                } else {
                    int deltaMoveY = (int) ((currentMoveY - mLastDownOrMoveEventY));
                    scrollBy(0, deltaMoveY);
                    invalidate();
                }
                mLastDownOrMoveEventY = currentMoveY;
            } break;
            case MotionEvent.ACTION_UP: {
                removeBeginSoftInputCommand();
                removeChangeCurrentByOneFromLongPress();
                mPressedStateHelper.cancel();
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                    fling(initialVelocity);
                    onScrollStateChange(SCROLL_STATE_FLING);
                } else {
                    int eventY = (int) event.getY();
                    int deltaMoveY = (int) Math.abs(eventY - mLastDownEventY);
                    long deltaTime = event.getEventTime() - mLastDownEventTime;
                    if (deltaMoveY <= mTouchSlop && deltaTime < ViewConfiguration.getTapTimeout()) {
                        if (mPerformClickOnTap) {
                            mPerformClickOnTap = false;
                            performClick();
                        } else {
                            int selectorIndexOffset = (eventY / mSelectorElementHeight) - mSelectorIndices.length/2;
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true);
                                mPressedStateHelper.buttonTapped(PressedStateHelper.BUTTON_INCREMENT);
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false);
                                mPressedStateHelper.buttonTapped(PressedStateHelper.BUTTON_DECREMENT);
                            }
                        }
                    } else {
                        ensureScrollWheelAdjusted();
                    }
                    onScrollStateChange(SCROLL_STATE_IDLE);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            } break;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                removeAllCallbacks();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        if (mWrapSelectorWheel ||
                                ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN) ? mValue < mMaxValue : mValue > mMinValue)) {
                            requestFocus();
                            mLastHandledDownDpadKeyCode = keyCode;
                            removeAllCallbacks();
                            if (mFlingScroller.isFinished()) {
                                changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN);
                            }
                            return true;
                        }
                        break;
                    case KeyEvent.ACTION_UP:
                        if (mLastHandledDownDpadKeyCode == keyCode) {
                            mLastHandledDownDpadKeyCode = -1;
                            return true;
                        }
                        break;
                }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTrackballEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (accessibilityManager().isEnabled()) {
            final int eventY = (int) event.getY();
            final int hoveredVirtualViewId;
            if (eventY < mTopSelectionDividerTop) {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_DECREMENT;
            } else if (eventY > mBottomSelectionDividerBottom) {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INCREMENT;
            } else {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INPUT;
            }
            final int action = event.getActionMasked();
            AccessibilityNodeProviderImpl provider =
                (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER: {
                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                    mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                    provider.performAction(hoveredVirtualViewId,
                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                } break;
                case MotionEvent.ACTION_HOVER_MOVE: {
                    if (mLastHoveredChildVirtualViewId != hoveredVirtualViewId
                            && mLastHoveredChildVirtualViewId != View.NO_ID) {
                        provider.sendAccessibilityEventForVirtualView(
                                mLastHoveredChildVirtualViewId,
                                AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
                        provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                                AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                        mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                        provider.performAction(hoveredVirtualViewId,
                                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    }
                } break;
                case MotionEvent.ACTION_HOVER_EXIT: {
                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
                    mLastHoveredChildVirtualViewId = View.NO_ID;
                } break;
            }
        }
        return false;
    }

    @Override
    public void computeScroll() {
        Scroller scroller = mFlingScroller;
        if (scroller.isFinished()) {
            scroller = mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        int currentScrollerY = scroller.getCurrY();
        if (mPreviousScrollerY == 0) {
            mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - mPreviousScrollerY);
        mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            invalidate();
        }
    }

    /*@Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mInputText.setEnabled(enabled);
    }*/

    @Override
    public void scrollBy(int x, int y) {
        int[] selectorIndices = mSelectorIndices;
        int startScrollOffset = mCurrentScrollOffset;
        int midIndex = mSelectorIndices.length/2;
        if (!mWrapSelectorWheel) {
            int midSelectorIdx = selectorIndices[midIndex];
            if ((y > 0 && midSelectorIdx <= mMinValue) || (y < 0 && midSelectorIdx >= mMaxValue)) {
                mCurrentScrollOffset = mInitialScrollOffset;
                return;
            }
        }
        mCurrentScrollOffset += y;
        while (mCurrentScrollOffset - mInitialScrollOffset > mSelectorElementHeight/2) {
            mCurrentScrollOffset -= mSelectorElementHeight;
            decrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[midIndex], true);
            if (!mWrapSelectorWheel && selectorIndices[midIndex] <= mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -mSelectorElementHeight/2) {
            mCurrentScrollOffset += mSelectorElementHeight;
            incrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[midIndex], true);
            if (!mWrapSelectorWheel && selectorIndices[midIndex] >= mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
        if (startScrollOffset != mCurrentScrollOffset) {
            onScrollChanged(0, mCurrentScrollOffset, 0, startScrollOffset);
        }
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return mCurrentScrollOffset;
    }

    @Override
    protected int computeVerticalScrollRange() {
        return (mMaxValue - mMinValue + 1) * mSelectorElementHeight;
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return getHeight();
    }

    @Override
    public int getSolidColor() {
        return mSolidColor;
    }
    public void setSolidColor(@ColorInt int solidColor) {
        mSolidColor = solidColor;
    }

    /**
     * Sets the listener to be notified on change of the current value.
     *
     * @param onValueChangedListener The listener.
     */
    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        mOnValueChangeListener = onValueChangedListener;
    }

    /**
     * Set listener to be notified for scroll state changes.
     *
     * @param onScrollListener The listener.
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    /**
     * Set the formatter to be used for formatting the current value.
     * <p>
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     * </p>
     *
     * @param formatter The formatter object. If formatter is <code>null</code>,
     *            {@link String#valueOf(int)} will be used.
     *@see #setDisplayedValues(String[])
     */
    public void setFormatter(Formatter formatter) {
        if (formatter == mFormatter) {
            return;
        }
        mFormatter = formatter;
        initializeSelectorWheelIndices();
        updateInputTextView();
    }

    /**
     * Set the current value for the number picker.
     * <p>
     * If the argument is less than the {@link #getMinValue()} and
     * {@link #getWrapSelectorWheel()} is <code>false</code> the
     * current value is set to the {@link #getMinValue()} value.
     * </p>
     * <p>
     * If the argument is less than the {@link #getMinValue()} and
     * {@link #getWrapSelectorWheel()} is <code>true</code> the
     * current value is set to the {@link #getMaxValue()} value.
     * </p>
     * <p>
     * If the argument is more than the {@link #getMaxValue()} and
     * {@link #getWrapSelectorWheel()} is <code>false</code> the
     * current value is set to the {@link #getMaxValue()} value.
     * </p>
     * <p>
     * If the argument is more than the {@link #getMaxValue()} and
     * {@link #getWrapSelectorWheel()} is <code>true</code> the
     * current value is set to the {@link #getMinValue()} value.
     * </p>
     *
     * @param value The current value.
     * @see #setWrapSelectorWheel(boolean)
     * @see #setMinValue(int)
     * @see #setMaxValue(int)
     */
    public void setValue(int value) {
        setValueInternal(value, false);
    }

    @Override
    public boolean performClick() {
        if (!super.performClick()) {
            showSoftInput();
        }
        return true;
    }

    @Override
    public boolean performLongClick() {
        if (!super.performLongClick()) {
            showSoftInput();
            mIgnoreMoveEvents = true;
        }
        return true;
    }

    /**
     * Shows the soft input for its input text.
     */
    private void showSoftInput() {
        /*InputMethodManager inputMethodManager = imm();
        if (inputMethodManager != null) {
            mInputText.setVisibility(View.VISIBLE);
            mInputText.requestFocus();
            inputMethodManager.showSoftInput(mInputText, 0);
        }*/
    }

    /**
     * Hides the soft input if it is active for the input text.
     */
    private void hideSoftInput() {
        /*InputMethodManager inputMethodManager = imm();
        if (inputMethodManager != null && inputMethodManager.isActive(mInputText)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
        mInputText.setVisibility(View.INVISIBLE);*/
    }

    /**
     * Computes the max width if no such specified as an attribute.
     */
    private void tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return;
        }
        int maxTextWidth = 0;
        if (mDisplayedValues == null) {
            float maxDigitWidth = 0;
            for (int i = 0; i <= 9; i++) {
                final float digitWidth = mSelectorWheelPaint.measureText(formatNumberWithLocale(i));
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth;
                }
            }
            int numberOfDigits = 0;
            int current = mMaxValue;
            while (current > 0) {
                numberOfDigits++;
                current = current / 10;
            }
            maxTextWidth = (int) (numberOfDigits * maxDigitWidth);
        } else {
            for (String displayedValue : mDisplayedValues) {
                final float textWidth = mSelectorWheelPaint.measureText(displayedValue);
                if (textWidth > maxTextWidth) {
                    maxTextWidth = (int) textWidth;
                }
            }
        }
//        maxTextWidth += mInputText.getPaddingLeft() + mInputText.getPaddingRight();
        if (mMaxWidth != maxTextWidth) {
            mMaxWidth = Math.max(maxTextWidth, mMinWidth);
            invalidate();
        }
    }

    /**
     * Gets whether the selector wheel wraps when reaching the min/max value.
     *
     * @return True if the selector wheel wraps.
     *
     * @see #getMinValue()
     * @see #getMaxValue()
     */
    public boolean getWrapSelectorWheel() {
        return mWrapSelectorWheel;
    }

    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap around the {@link #getMinValue()} and
     * {@link #getMaxValue()} values.
     * <p>
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     * </p>
     * <p>
     * <strong>Note:</strong> If the number of items, i.e. the range (
     * {@link #getMaxValue()} - {@link #getMinValue()}) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     * </p>
     *
     * @param wrapSelectorWheel Whether to wrap.
     */
    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        mWrapSelectorWheelPreferred = wrapSelectorWheel;
        updateWrapSelectorWheel();

    }

    /**
     * Whether or not the selector wheel should be wrapped is determined by user choice and whether
     * the choice is allowed. The former comes from {@link #setWrapSelectorWheel(boolean)}, the
     * latter is calculated based on min & max value set vs selector's visual length. Therefore,
     * this method should be called any time any of the 3 values (i.e. user choice, min and max
     * value) gets updated.
     */
    private void updateWrapSelectorWheel() {
        final boolean wrappingAllowed = (mMaxValue - mMinValue) >= mSelectorIndices.length;
        mWrapSelectorWheel = wrappingAllowed && mWrapSelectorWheelPreferred;
    }

    /**
     * Sets the speed at which the numbers be incremented and decremented when
     * the up and down buttons are long pressed respectively.
     * <p>
     * The default value is 300 ms.
     * </p>
     *
     * @param intervalMillis The speed (in milliseconds) at which the numbers
     *            will be incremented and decremented.
     */
    public void setOnLongPressUpdateInterval(long intervalMillis) {
        mLongPressUpdateInterval = intervalMillis;
    }

    /**
     * Returns the value of the picker.
     *
     * @return The value.
     */
    public int getValue() {
        return mValue;
    }

    /**
     * Returns the min value of the picker.
     *
     * @return The min value
     */
    public int getMinValue() {
        return mMinValue;
    }

    /**
     * Sets the min value of the picker.
     *
     * @param minValue The min value inclusive.
     *
     * <strong>Note:</strong> The length of the displayed values array
     * set via {@link #setDisplayedValues(String[])} must be equal to the
     * range of selectable numbers which is equal to
     * {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     */
    public void setMinValue(int minValue) {
        if (mMinValue == minValue) {
            return;
        }
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be >= 0");
        }
        mMinValue = minValue;
        if (mMinValue > mValue) {
            mValue = mMinValue;
        }
        onMinMaxChanged();
    }

    /**
     * Returns the max value of the picker.
     *
     * @return The max value.
     */
    public int getMaxValue() {
        return mMaxValue;
    }

    /**
     * Sets the max value of the picker.
     *
     * @param maxValue The max value inclusive.
     *
     * <strong>Note:</strong> The length of the displayed values array
     * set via {@link #setDisplayedValues(String[])} must be equal to the
     * range of selectable numbers which is equal to
     * {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     */
    public void setMaxValue(int maxValue) {
        if (mMaxValue == maxValue) {
            return;
        }
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        mMaxValue = maxValue;
        if (mMaxValue < mValue) {
            mValue = mMaxValue;
        }
        onMinMaxChanged();
    }

    private void onMinMaxChanged() {
        updateWrapSelectorWheel();
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    /**
     * Gets the values to be displayed instead of string values.
     *
     * @return The displayed values.
     */
    public String[] getDisplayedValues() {
        return mDisplayedValues;
    }

    /**
     * Sets the values to be displayed.
     *
     * @param displayedValues The displayed values.
     *
     * <strong>Note:</strong> The length of the displayed values array
     * must be equal to the range of selectable numbers which is equal to
     * {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     */
    public void setDisplayedValues(String[] displayedValues) {
        if (mDisplayedValues == displayedValues) {
            return;
        }
        mDisplayedValues = displayedValues;
        /*if (mDisplayedValues != null) {
            // Allow text entry rather than strictly numeric entry.
            mInputText.setRawInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } else {
            mInputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        }*/
        updateInputTextView();
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return 1f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return 1f;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllCallbacks();
    }

    @CallSuper
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final Drawable selectionDivider = mSelectionDivider;
        if (selectionDivider != null && selectionDivider.isStateful()
                && selectionDivider.setState(getDrawableState())) {
            invalidateDrawable(selectionDivider);
        }
    }

    @CallSuper
    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        if (mSelectionDivider != null) {
            mSelectionDivider.jumpToCurrentState();
        }
    }

    /*@Override*/ @Keep
    public void onResolveDrawables(int layoutDirection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mSelectionDivider != null) {
            mSelectionDivider.setLayoutDirection(layoutDirection);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float x;
        switch (mSelectorWheelPaint.getTextAlign()) {
            case LEFT: x = getPaddingLeft(); break;
            case CENTER: x = getWidth() / 2; break;
            case RIGHT: x = getWidth() - getPaddingRight(); break;
            default: throw new AssertionError();
        }
        float y = mCurrentScrollOffset;

        // draw the selector wheel
        int[] selectorIndices = mSelectorIndices;
        int midIndex = selectorIndices.length / 2;
        float lim = 2 * midIndex * mSelectorElementHeight; // distance between top and bottom text baselines
        int originalAlpha = mSelectorWheelPaint.getAlpha();
        for (int i = 0; i < selectorIndices.length; i++) {
            canvas.save();
            float midLine = y - (lineHeight - fmi.descent) / 2f;
            float input = (midLine - mInitialScrollOffset - lim / 2) / lim;
            float scale = scaleInterpolator.getInterpolation(input);
            canvas.scale(1f, scale, x, midLine + (i < midIndex ? 1 - scale : -1 + scale) * mSelectorElementHeight);
            mSelectorWheelPaint.setAlpha((int) (originalAlpha * alphaInterpolator.getInterpolation(input)));
         // if (i != midIndex || mInputText.getVisibility() != VISIBLE)
                canvas.drawText(mSelectorIndexToStringCache.get(selectorIndices[i]), x, y, mSelectorWheelPaint);
            canvas.restore();
            y += mSelectorElementHeight;
        }
        mSelectorWheelPaint.setAlpha(originalAlpha);

        // draw the selection dividers
        if (mSelectionDivider != null) {
            int selectionDividerHeight = mSelectionDivider.getBounds().height();

            // draw the top divider
            int topOfTopDivider = mTopSelectionDividerTop;
            int bottomOfTopDivider = topOfTopDivider + selectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfTopDivider, getRight(), bottomOfTopDivider);
            mSelectionDivider.draw(canvas);

            // draw the bottom divider
            int bottomOfBottomDivider = mBottomSelectionDividerBottom;
            int topOfBottomDivider = bottomOfBottomDivider - selectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfBottomDivider, getRight(), bottomOfBottomDivider);
            mSelectionDivider.draw(canvas);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || getAccessibilityDelegate() != null) {
            event.setClassName(android.widget.NumberPicker.class.getName());
            event.setScrollable(true);
            event.setScrollY((mMinValue + mValue) * mSelectorElementHeight);
            event.setMaxScrollY((mMaxValue - mMinValue) * mSelectorElementHeight);
        }
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (mAccessibilityNodeProvider == null) {
            mAccessibilityNodeProvider = new AccessibilityNodeProviderImpl();
        }
        return mAccessibilityNodeProvider;
    }

    /**
     * Makes a measure spec that tries greedily to use the max value.
     *
     * @param measureSpec The measure spec.
     * @param maxSize The max value for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private static int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        final int size = MeasureSpec.getSize(measureSpec);
        final int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return measureSpec;
            case MeasureSpec.AT_MOST:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), MeasureSpec.EXACTLY);
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY);
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec. Tries to respect the min size, unless a different size
     * is imposed by the constraints.
     *
     * @param minSize The minimal desired size.
     * @param measuredSize The currently measured size.
     * @param measureSpec The current measure spec.
     * @return The resolved size and state.
     */
    private static int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        return minSize == -1 ? measuredSize : resolveSizeAndState(Math.max(minSize, measuredSize), measureSpec, 0);
    }

    /**
     * Resets the selector indices and clear the cached string representation of
     * these indices.
     */
    private void initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear();
        int[] selectorIndices = mSelectorIndices;
        int current = mValue;
        int mid = selectorIndices.length/2;
        for (int i = 0; i < mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - mid);
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }

    /**
     * Sets the current value of this NumberPicker.
     *
     * @param current The new value of the NumberPicker.
     * @param notifyChange Whether to notify if the current value changed.
     */
    private void setValueInternal(int current, boolean notifyChange) {
        if (mValue == current) {
            return;
        }
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            current = getWrappedSelectorIndex(current);
        } else {
            current = Math.max(current, mMinValue);
            current = Math.min(current, mMaxValue);
        }
        int previous = mValue;
        mValue = current;
        // If we're flinging, we'll update the text view at the end when it becomes visible
        if (mScrollState != SCROLL_STATE_FLING) {
            updateInputTextView();
        }
        if (notifyChange) {
            notifyChange(previous);
        }
        initializeSelectorWheelIndices();
        invalidate();
    }

    /**
     * Changes the current value by one which is increment or
     * decrement based on the passed argument.
     * decrement the current value.
     *
     * @param increment True to increment, false to decrement.
     */
    public void changeValueByOne(boolean increment) {
        hideSoftInput();
        if (!moveToFinalScrollerPosition(mFlingScroller)) {
            moveToFinalScrollerPosition(mAdjustScroller);
        }
        mPreviousScrollerY = 0;
        mFlingScroller.startScroll(0, 0, 0, increment ? -mSelectorElementHeight : mSelectorElementHeight, 300);
        invalidate();
    }

    private final Paint.FontMetricsInt fmi = new Paint.FontMetricsInt();
    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int selectorCount = mSelectorIndices.length;
        mSelectorElementHeight = getHeight() / selectorCount;
        float textSize = mSelectorWheelPaint.getTextSize();
        float totalTextSize = selectorCount * textSize;
        float totalGapSize = getHeight() - totalTextSize;
        mSelectorTextGapHeight = (int) (totalGapSize / selectorCount);
        // Ensure that the middle item is positioned the same as the text in mInputText
        mSelectorWheelPaint.getFontMetricsInt(fmi);
        int editTextTextPosition = (int) ((getHeight() + lineHeight) / 2f - fmi.descent); // mInputText.getBaseline() + mInputText.getTop();
        mInitialScrollOffset = editTextTextPosition - (selectorCount /2 * mSelectorElementHeight);
        mCurrentScrollOffset = mInitialScrollOffset;
        updateInputTextView();
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(getHeight() / 2);
    }

    /**
     * Callback invoked upon completion of a given <code>scroller</code>.
     */
    private void onScrollerFinished(Scroller scroller) {
        if (scroller == mFlingScroller) {
            ensureScrollWheelAdjusted();
            updateInputTextView();
            onScrollStateChange(SCROLL_STATE_IDLE);
        } else if (mScrollState != SCROLL_STATE_TOUCH_SCROLL) {
            updateInputTextView();
        }
    }

    /**
     * Handles transition to a given <code>scrollState</code>
     */
    private void onScrollStateChange(int scrollState) {
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChange(this, scrollState);
        }
    }

    /**
     * Flings the selector with the given <code>velocityY</code>.
     */
    private void fling(int velocityY) {
        mPreviousScrollerY = 0;

        if (velocityY > 0) {
            mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }

        invalidate();
    }

    /**
     * @return The wrapped index <code>selectorIndex</code> value.
     */
    int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue) % (mMaxValue - mMinValue) - 1;
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex) % (mMaxValue - mMinValue) + 1;
        }
        return selectorIndex;
    }

    /**
     * Increments the <code>selectorIndices</code> whose string representations
     * will be displayed in the selector.
     */
    private void incrementSelectorIndices(int[] selectorIndices) {
        System.arraycopy(selectorIndices, 1, selectorIndices, 0, selectorIndices.length - 1);
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    /**
     * Decrements the <code>selectorIndices</code> whose string representations
     * will be displayed in the selector.
     */
    private void decrementSelectorIndices(int[] selectorIndices) {
        System.arraycopy(selectorIndices, 0, selectorIndices, 1, selectorIndices.length - 1);
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    /**
     * Ensures we have a cached string representation of the given <code>
     * selectorIndex</code> to avoid multiple instantiations of the same string.
     */
    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = mSelectorIndexToStringCache;
        String scrollSelectorValue = cache.get(selectorIndex);
        if (scrollSelectorValue != null) {
            return;
        }
        if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
            scrollSelectorValue = "";
        } else {
            if (mDisplayedValues != null) {
                int displayedValueIndex = selectorIndex - mMinValue;
                scrollSelectorValue = mDisplayedValues[displayedValueIndex];
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
        }
        cache.put(selectorIndex, scrollSelectorValue);
    }

    String formatNumber(int value) {
        return (mFormatter != null) ? mFormatter.format(value) : formatNumberWithLocale(value);
    }

    void validateInputTextView(View v) {
        String str = String.valueOf(((TextView) v).getText());
        if (TextUtils.isEmpty(str)) {
            // Restore to the old value as we don't allow empty values
            updateInputTextView();
        } else {
            // Check the new value and ensure it's in range
            int current = getSelectedPos(str);
            setValueInternal(current, true);
        }
    }

    /**
     * Updates the view of this NumberPicker. If displayValues were specified in
     * the string corresponding to the index specified by the current value will
     * be returned. Otherwise, the formatter specified in {@link #setFormatter}
     * will be used to format the number.
     */
    private void updateInputTextView() {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct value in the displayed values for the current
         * number.
         */
        /*String text = (mDisplayedValues == null) ? formatNumber(mValue)
                : mDisplayedValues[mValue - mMinValue];
        if (!TextUtils.isEmpty(text)) {
            CharSequence beforeText = mInputText.getText();
            if (!text.equals(beforeText.toString())) {
                mInputText.setText(text);
                if (accessibilityManager().isEnabled()) {
                    AccessibilityEvent event = AccessibilityEvent.obtain(
                            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
                    mInputText.onInitializeAccessibilityEvent(event);
                    mInputText.onPopulateAccessibilityEvent(event);
                    event.setFromIndex(0);
                    event.setRemovedCount(beforeText.length());
                    event.setAddedCount(text.length());
                    event.setBeforeText(beforeText);
                    event.setSource(this, AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INPUT);
                    requestSendAccessibilityEvent(this, event);
                }
            }
        }*/
    }

    /**
     * Notifies the listener, if registered, of a change of the value of this
     * NumberPicker.
     */
    private void notifyChange(int previous) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(this, previous, mValue);
        }
    }

    /**
     * Posts a command for changing the current value by one.
     *
     * @param increment Whether to increment or decrement the value.
     */
    private void postChangeCurrentByOneFromLongPress(boolean increment, long delayMillis) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
        mChangeCurrentByOneFromLongPressCommand.setStep(increment);
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis);
    }

    /**
     * Removes the command for changing the current value by one.
     */
    private void removeChangeCurrentByOneFromLongPress() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
    }

    /**
     * Posts a command for beginning an edit of the current value via IME on
     * long press.
     */
    private void postBeginSoftInputOnLongPressCommand() {
        if (mBeginSoftInputOnLongPressCommand == null) {
            mBeginSoftInputOnLongPressCommand = new BeginSoftInputOnLongPressCommand();
        } else {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
        postDelayed(mBeginSoftInputOnLongPressCommand, ViewConfiguration.getLongPressTimeout());
    }

    /**
     * Removes the command for beginning an edit of the current value via IME.
     */
    private void removeBeginSoftInputCommand() {
        if (mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
    }

    /**
     * Removes all pending callback from the message queue.
     */
    private void removeAllCallbacks() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
        if (mSetSelectionCommand != null) {
            mSetSelectionCommand.cancel();
        }
        if (mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
        mPressedStateHelper.cancel();
    }

    /**
     * @return The selected index given its displayed <code>value</code>.
     */
    int getSelectedPos(String value) {
        // Ignore as if it's not a number we don't care
        if (mDisplayedValues != null) {
            for (int i = 0; i < mDisplayedValues.length; i++) {
                // Don't force the user to type in jan when ja will do
                value = value.toLowerCase();
                if (mDisplayedValues[i].toLowerCase().startsWith(value)) {
                    return mMinValue + i;
                }
            }

            /*
             * The user might have typed in a number into the month field i.e.
             * 10 instead of OCT so support that too.
             */
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Ignore as if it's not a number we don't care
        }
        return mMinValue;
    }

    /**
     * Posts a {@link SetSelectionCommand} from the given
     * {@code selectionStart} to {@code selectionEnd}.
     */
    void postSetSelectionCommand(int selectionStart, int selectionEnd) {
        /*if (mSetSelectionCommand == null) {
            mSetSelectionCommand = new SetSelectionCommand(mInputText);
        }
        mSetSelectionCommand.post(selectionStart, selectionEnd);*/
    }

    /**
     * The numbers accepted by the input text's {@link Filter}
     */
    static final char[] DIGIT_CHARACTERS = new char[] {
            // Latin digits are the common case
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            // Arabic-Indic
            '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668', '\u0669',
            // Extended Arabic-Indic
            '\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8', '\u06f9',
            // Hindi and Marathi (Devanagari script)
            '\u0966', '\u0967', '\u0968', '\u0969', '\u096a', '\u096b', '\u096c', '\u096d', '\u096e', '\u096f',
            // Bengali
            '\u09e6', '\u09e7', '\u09e8', '\u09e9', '\u09ea', '\u09eb', '\u09ec', '\u09ed', '\u09ee', '\u09ef',
            // Kannada
            '\u0ce6', '\u0ce7', '\u0ce8', '\u0ce9', '\u0cea', '\u0ceb', '\u0cec', '\u0ced', '\u0cee', '\u0cef'
    };

    /**
     * Filter for accepting only valid indices or prefixes of the string
     * representation of valid indices.
     */
    class InputTextFilter extends NumberKeyListener {

        // XXX This doesn't allow for range limits when controlled by a
        // soft input method!
        public int getInputType() {
            return InputType.TYPE_CLASS_TEXT;
        }

        @Override
        protected char[] getAcceptedChars() {
            return DIGIT_CHARACTERS;
        }

        @Override
        public CharSequence filter(
                CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            // We don't know what the output will be, so always cancel any
            // pending set selection command.
            if (mSetSelectionCommand != null) {
                mSetSelectionCommand.cancel();
            }

            if (mDisplayedValues == null) {
                CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }

                String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
                        + dest.subSequence(dend, dest.length());

                if ("".equals(result)) {
                    return result;
                }
                int val = getSelectedPos(result);

                /*
                 * Ensure the user can't type in a value greater than the max
                 * allowed. We have to allow less than min as the user might
                 * want to delete some numbers and then type a new number.
                 * And prevent multiple-"0" that exceeds the length of upper
                 * bound number.
                 */
                if (val > mMaxValue || result.length() > String.valueOf(mMaxValue).length()) {
                    return "";
                } else {
                    return filtered;
                }
            } else {
                CharSequence filtered = String.valueOf(source.subSequence(start, end));
                if (TextUtils.isEmpty(filtered)) {
                    return "";
                }
                String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
                        + dest.subSequence(dend, dest.length());
                String str = result.toLowerCase();
                for (String val : mDisplayedValues) {
                    String valLowerCase = val.toLowerCase();
                    if (valLowerCase.startsWith(str)) {
                        postSetSelectionCommand(result.length(), val.length());
                        return val.subSequence(dstart, val.length());
                    }
                }
                return "";
            }
        }
    }

    /**
     * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
     * middle element is in the middle of the widget.
     */
    private void ensureScrollWheelAdjusted() {
        // adjust to the closest value
        int deltaY = mInitialScrollOffset - mCurrentScrollOffset;
        if (deltaY != 0) {
            mPreviousScrollerY = 0;
            if (Math.abs(deltaY) > mSelectorElementHeight / 2) {
                deltaY += (deltaY > 0) ? -mSelectorElementHeight : mSelectorElementHeight;
            }
            mAdjustScroller.startScroll(0, 0, 0, deltaY, 800);
            invalidate();
        }
    }

    private InputMethodManager imm() {
        return (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    AccessibilityManager accessibilityManager() {
        return (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
    }
    private Rect tmpRect;
    private Point tmpPoint;
    protected boolean isVisibleToUser(Rect boundInView) {
        // Attached to invisible window means this view is not visible.
        if (getWindowVisibility() != View.VISIBLE) {
            return false;
        }
        // An invisible predecessor or one with alpha zero means
        // that this view is not visible to the user.
        Object current = this;
        while (current instanceof View) {
            View view = (View) current;
            // We have attach info so this view is attached and there is no
            // need to check whether we reach to ViewRootImpl on the way up.
            if (view.getAlpha() <= 0 ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && view.getTransitionAlpha() <= 0) ||
                    view.getVisibility() != VISIBLE) {
                return false;
            }
            current = view.getParent();
        }
        // Check if the view is entirely covered by its predecessors.
        Rect visibleRect = tmpRect;
        Point offset = tmpPoint;
        if (visibleRect == null) {
            tmpRect = visibleRect = new Rect();
            tmpPoint = offset = new Point();
        }
        if (!getGlobalVisibleRect(visibleRect, offset)) {
            return false;
        }
        // Check if the visible portion intersects the rectangle of interest.
        if (boundInView != null) {
            visibleRect.offset(-offset.x, -offset.y);
            return boundInView.intersect(visibleRect);
        }
        return true;
    }

    class PressedStateHelper implements Runnable {
        public static final int BUTTON_INCREMENT = 1;
        public static final int BUTTON_DECREMENT = 2;

        private static final int MODE_PRESS = 1;
        private static final int MODE_TAPPED = 2;

        private int mManagedButton;
        private int mMode;

        public void cancel() {
            mMode = 0;
            mManagedButton = 0;
            FlexibleNumberPicker.this.removeCallbacks(this);
            if (mIncrementVirtualButtonPressed) {
                mIncrementVirtualButtonPressed = false;
                invalidate();
            }
            mDecrementVirtualButtonPressed = false;
        }

        public void buttonPressDelayed(int button) {
            cancel();
            mMode = MODE_PRESS;
            mManagedButton = button;
            FlexibleNumberPicker.this.postDelayed(this, ViewConfiguration.getTapTimeout());
        }

        public void buttonTapped(int button) {
            cancel();
            mMode = MODE_TAPPED;
            mManagedButton = button;
            FlexibleNumberPicker.this.post(this);
        }

        @Override
        public void run() {
            switch (mMode) {
                case MODE_PRESS: {
                    switch (mManagedButton) {
                        case BUTTON_INCREMENT: {
                            mIncrementVirtualButtonPressed = true;
                            invalidate();
                        } break;
                        case BUTTON_DECREMENT: {
                            mDecrementVirtualButtonPressed = true;
                            invalidate();
                        }
                    }
                } break;
                case MODE_TAPPED: {
                    switch (mManagedButton) {
                        case BUTTON_INCREMENT: {
                            if (!mIncrementVirtualButtonPressed) {
                                FlexibleNumberPicker.this.postDelayed(this,
                                        ViewConfiguration.getPressedStateDuration());
                            }
                            mIncrementVirtualButtonPressed ^= true;
                            invalidate();
                        } break;
                        case BUTTON_DECREMENT: {
                            if (!mDecrementVirtualButtonPressed) {
                                FlexibleNumberPicker.this.postDelayed(this,
                                        ViewConfiguration.getPressedStateDuration());
                            }
                            mDecrementVirtualButtonPressed ^= true;
                            invalidate();
                        }
                    }
                } break;
            }
        }
    }

    /**
     * Command for setting the input text selection.
     */
    private static class SetSelectionCommand implements Runnable {
        private final EditText mInputText;

        private int mSelectionStart;
        private int mSelectionEnd;

        /** Whether this runnable is currently posted. */
        private boolean mPosted;

        public SetSelectionCommand(EditText inputText) {
            mInputText = inputText;
        }

        public void post(int selectionStart, int selectionEnd) {
            mSelectionStart = selectionStart;
            mSelectionEnd = selectionEnd;

            if (!mPosted) {
                mInputText.post(this);
                mPosted = true;
            }
        }

        public void cancel() {
            if (mPosted) {
                mInputText.removeCallbacks(this);
                mPosted = false;
            }
        }

        @Override
        public void run() {
            mPosted = false;
            mInputText.setSelection(mSelectionStart, mSelectionEnd);
        }
    }

    /**
     * Command for changing the current value from a long press by one.
     */
    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        void setStep(boolean increment) {
            mIncrement = increment;
        }

        @Override
        public void run() {
            changeValueByOne(mIncrement);
            postDelayed(this, mLongPressUpdateInterval);
        }
    }

    @SuppressLint("AppCompatCustomView")
    public static class CustomEditText extends EditText {

        public CustomEditText(Context context) {
            super(context);
        }

        @Override
        public void onEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
            if (actionCode == EditorInfo.IME_ACTION_DONE) {
                clearFocus();
            }
        }
    }

    /**
     * Command for beginning soft input on long press.
     */
    class BeginSoftInputOnLongPressCommand implements Runnable {

        @Override
        public void run() {
            performLongClick();
        }
    }

    /**
     * Class for managing virtual view tree rooted at this picker.
     */
    class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int UNDEFINED = Integer.MIN_VALUE;

        private static final int VIRTUAL_VIEW_ID_INCREMENT = 1;

        private static final int VIRTUAL_VIEW_ID_INPUT = 2;

        private static final int VIRTUAL_VIEW_ID_DECREMENT = 3;

        private final Rect mTempRect = new Rect();

        private final int[] mTempArray = new int[2];

        private int mAccessibilityFocusedView = UNDEFINED;

        @Override
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            int selectionDividerHeight = mSelectionDivider == null ? 0 : mSelectionDivider.getBounds().height();
            switch (virtualViewId) {
                case View.NO_ID:
                    return createAccessibilityNodeInfoForNumberPicker(getScrollX(), getScrollY(),
                            getScrollX() + getWidth(), getScrollY() + getHeight());
                case VIRTUAL_VIEW_ID_DECREMENT:
                    return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_DECREMENT,
                            getVirtualDecrementButtonText(), getScrollX(), getScrollY(),
                            getScrollX() + getWidth(),
                            mTopSelectionDividerTop + selectionDividerHeight);
                /*case VIRTUAL_VIEW_ID_INPUT:
                    return createAccessibiltyNodeInfoForInputText(getScrollX(),
                            mTopSelectionDividerTop + selectionDividerHeight,
                            getScrollX() + getWidth(),
                            mBottomSelectionDividerBottom - selectionDividerHeight);*/
                case VIRTUAL_VIEW_ID_INCREMENT:
                    return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_INCREMENT,
                            getVirtualIncrementButtonText(), getScrollX(),
                            mBottomSelectionDividerBottom - selectionDividerHeight,
                            getScrollX() + getWidth(), getScrollY() + getHeight());
            }
            return super.createAccessibilityNodeInfo(virtualViewId);
        }

        @Override
        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String searched,
                                                                            int virtualViewId) {
            if (TextUtils.isEmpty(searched)) {
                return Collections.emptyList();
            }
            String searchedLowerCase = searched.toLowerCase();
            switch (virtualViewId) {
                case View.NO_ID: {
                    List<AccessibilityNodeInfo> result = new ArrayList<>(3);
                    addIncDecNodeInfo(searchedLowerCase, result, getVirtualDecrementButtonText(), VIRTUAL_VIEW_ID_DECREMENT);
                    addInputNodeInfo(searchedLowerCase, result);
                    addIncDecNodeInfo(searchedLowerCase, result, getVirtualIncrementButtonText(), VIRTUAL_VIEW_ID_INCREMENT);
                    return result;
                }
                case VIRTUAL_VIEW_ID_DECREMENT:
                case VIRTUAL_VIEW_ID_INCREMENT:
                case VIRTUAL_VIEW_ID_INPUT: {
                    List<AccessibilityNodeInfo> result = new ArrayList<>(1);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, virtualViewId, result);
                    return result;
                }
            }
            return super.findAccessibilityNodeInfosByText(searched, virtualViewId);
        }

        @Override
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            switch (virtualViewId) {
                case View.NO_ID: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                                return true;
                            }
                        } return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                performAccessibilityAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS, null);
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:
                        case android.R.id.accessibilityActionScrollDown: {
                            if (FlexibleNumberPicker.this.isEnabled()
                                    && (getWrapSelectorWheel() || getValue() < getMaxValue())) {
                                changeValueByOne(true);
                                return true;
                            }
                        } return false;
                        case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD:
                        case android.R.id.accessibilityActionScrollUp: {
                            if (FlexibleNumberPicker.this.isEnabled()
                                    && (getWrapSelectorWheel() || getValue() > getMinValue())) {
                                changeValueByOne(false);
                                return true;
                            }
                        } return false;
                    }
                } break;
                case VIRTUAL_VIEW_ID_INPUT: {
                    switch (action) {
                        /*case AccessibilityNodeInfo.ACTION_FOCUS: {
                            if (FlexibleNumberPicker.this.isEnabled() && !mInputText.isFocused()) {
                                return mInputText.requestFocus();
                            }
                        } break;*/
                        /*case AccessibilityNodeInfo.ACTION_CLEAR_FOCUS: {
                            if (FlexibleNumberPicker.this.isEnabled() && mInputText.isFocused()) {
                                mInputText.clearFocus();
                                return true;
                            }
                            return false;
                        }*/
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (FlexibleNumberPicker.this.isEnabled()) {
                                performClick();
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_LONG_CLICK: {
                            if (FlexibleNumberPicker.this.isEnabled()) {
                                performLongClick();
                                return true;
                            }
                            return false;
                        }
                        /*case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                mInputText.invalidate();
                                return true;
                            }
                        } return false;
                        case  AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                mInputText.invalidate();
                                return true;
                            }
                        } return false;
                        default: {
                            return mInputText.performAccessibilityAction(action, arguments);
                        }*/
                    }
                } return false;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (FlexibleNumberPicker.this.isEnabled()) {
                                FlexibleNumberPicker.this.changeValueByOne(true);
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_CLICKED);
                                return true;
                            }
                        } return false;
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                invalidate();
                                return true;
                            }
                        } return false;
                        case  AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                invalidate();
                                return true;
                            }
                        } return false;
                    }
                } return false;
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (FlexibleNumberPicker.this.isEnabled()) {
                                FlexibleNumberPicker.this.changeValueByOne(false);
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_CLICKED);
                                return true;
                            }
                        } return false;
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                invalidate();
                                return true;
                            }
                        } return false;
                        case  AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                invalidate();
                                return true;
                            }
                        } return false;
                    }
                } return false;
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            switch (virtualViewId) {
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    if (hasVirtualDecrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType,
                                getVirtualDecrementButtonText());
                    }
                } break;
                case VIRTUAL_VIEW_ID_INPUT: {
                    sendAccessibilityEventForVirtualText(eventType);
                } break;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    if (hasVirtualIncrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType,
                                getVirtualIncrementButtonText());
                    }
                } break;
            }
        }

        private void sendAccessibilityEventForVirtualText(int eventType) {
            if (accessibilityManager().isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                /*mInputText.onInitializeAccessibilityEvent(event);
                mInputText.onPopulateAccessibilityEvent(event);*/
                event.setSource(FlexibleNumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
                requestSendAccessibilityEvent(FlexibleNumberPicker.this, event);
            }
        }

        private void sendAccessibilityEventForVirtualButton(int virtualViewId, int eventType,
                                                            String text) {
            if (accessibilityManager().isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setClassName(Button.class.getName());
                event.setPackageName(getContext().getPackageName());
                event.getText().add(text);
                event.setEnabled(FlexibleNumberPicker.this.isEnabled());
                event.setSource(FlexibleNumberPicker.this, virtualViewId);
                requestSendAccessibilityEvent(FlexibleNumberPicker.this, event);
            }
        }

        private void findAccessibilityNodeInfosByTextInChild(String searchedLowerCase,
                                                             int virtualViewId, List<AccessibilityNodeInfo> outResult) {
            switch (virtualViewId) {
                case VIRTUAL_VIEW_ID_DECREMENT:
                    addIncDecNodeInfo(searchedLowerCase, outResult, getVirtualDecrementButtonText(), VIRTUAL_VIEW_ID_DECREMENT);
                    break;
                case VIRTUAL_VIEW_ID_INPUT:
                    addInputNodeInfo(searchedLowerCase, outResult);
                    break;
                case VIRTUAL_VIEW_ID_INCREMENT:
                    addIncDecNodeInfo(searchedLowerCase, outResult, getVirtualIncrementButtonText(), VIRTUAL_VIEW_ID_INCREMENT);
                    break;
            }
        }

        private void addIncDecNodeInfo(
                String searchedLowerCase, List<AccessibilityNodeInfo> outResult, String buttonText, int id) {
            if (!TextUtils.isEmpty(buttonText) && buttonText.toLowerCase().contains(searchedLowerCase)) {
                outResult.add(createAccessibilityNodeInfo(id));
            }
        }

        private void addInputNodeInfo(String searchedLowerCase, List<AccessibilityNodeInfo> outResult) {
            /*CharSequence text = mInputText.getText();
            if (!TextUtils.isEmpty(text) &&
                    text.toString().toLowerCase().contains(searchedLowerCase)) {
                outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT));
            } else if (!TextUtils.isEmpty(text = mInputText.getContentDescription()) && // CHANGED from getText()
                    text.toString().toLowerCase().contains(searchedLowerCase)) {
                outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT));
            }*/
        }

        /*private AccessibilityNodeInfo createAccessibiltyNodeInfoForInputText(
                int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = mInputText.createAccessibilityNodeInfo();
            info.setSource(FlexibleNumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
            info.setAccessibilityFocused(mAccessibilityFocusedView == VIRTUAL_VIEW_ID_INPUT);
            if (mAccessibilityFocusedView != VIRTUAL_VIEW_ID_INPUT) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == VIRTUAL_VIEW_ID_INPUT) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            setBounds(left, top, right, bottom, info);
            return info;
        }*/

        private void setBounds(int left, int top, int right, int bottom, AccessibilityNodeInfo info) {
            Rect boundsInScreen = mTempRect;
            boundsInScreen.set(left, top, right, bottom);
            info.setVisibleToUser(isVisibleToUser(boundsInScreen));
            int[] locationOnScreen = mTempArray;
            getLocationOnScreen(locationOnScreen);
            boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInScreen);
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForVirtualButton(int virtualViewId,
                                                                                  String text, int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(Button.class.getName());
            info.setPackageName(getContext().getPackageName());
            info.setSource(FlexibleNumberPicker.this, virtualViewId);
            info.setParent(FlexibleNumberPicker.this);
            info.setText(text);
            info.setClickable(true);
            info.setLongClickable(true);
            info.setEnabled(FlexibleNumberPicker.this.isEnabled());
            info.setAccessibilityFocused(mAccessibilityFocusedView == virtualViewId);
            setBounds(left, top, right, bottom, info);

            if (mAccessibilityFocusedView != virtualViewId) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == virtualViewId) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (FlexibleNumberPicker.this.isEnabled()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
            }

            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForNumberPicker(int left, int top,
                                                                                 int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(android.widget.NumberPicker.class.getName());
            info.setPackageName(getContext().getPackageName());
            info.setSource(FlexibleNumberPicker.this);

            if (hasVirtualDecrementButton()) {
                info.addChild(FlexibleNumberPicker.this, VIRTUAL_VIEW_ID_DECREMENT);
            }
            info.addChild(FlexibleNumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
            if (hasVirtualIncrementButton()) {
                info.addChild(FlexibleNumberPicker.this, VIRTUAL_VIEW_ID_INCREMENT);
            }

            info.setParent((View) getParentForAccessibility());
            info.setEnabled(FlexibleNumberPicker.this.isEnabled());
            info.setScrollable(true);
            info.setAccessibilityFocused(mAccessibilityFocusedView == View.NO_ID);

         // final float applicationScale = getContext().getResources().getCompatibilityInfo().applicationScale;

            Rect boundsInScreen = mTempRect;
            boundsInScreen.set(left, top, right, bottom);
         // boundsInScreen.scale(applicationScale);

            info.setVisibleToUser(isVisibleToUser(null));

            int[] locationOnScreen = mTempArray;
            getLocationOnScreen(locationOnScreen);
            boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
         // boundsInScreen.scale(applicationScale);
            info.setBoundsInScreen(boundsInScreen);

            if (mAccessibilityFocusedView != View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (FlexibleNumberPicker.this.isEnabled()) {
                if (getWrapSelectorWheel() || getValue() < getMaxValue()) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN);
                    }
                }
                if (getWrapSelectorWheel() || getValue() > getMinValue()) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
                    }
                }
            }

            return info;
        }

        private boolean hasVirtualDecrementButton() {
            return getWrapSelectorWheel() || getValue() > getMinValue();
        }

        private boolean hasVirtualIncrementButton() {
            return getWrapSelectorWheel() || getValue() < getMaxValue();
        }

        private String getVirtualDecrementButtonText() {
            int value = mValue - 1;
            if (mWrapSelectorWheel) {
                value = getWrappedSelectorIndex(value);
            }
            if (value >= mMinValue) {
                return (mDisplayedValues == null) ? formatNumber(value)
                        : mDisplayedValues[value - mMinValue];
            }
            return null;
        }

        private String getVirtualIncrementButtonText() {
            int value = mValue + 1;
            if (mWrapSelectorWheel) {
                value = getWrappedSelectorIndex(value);
            }
            if (value <= mMaxValue) {
                return (mDisplayedValues == null) ? formatNumber(value)
                        : mDisplayedValues[value - mMinValue];
            }
            return null;
        }
    }

    static private String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", value);
    }
}
