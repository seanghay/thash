// Copyright 2019 Seanghay Yath

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//     http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.seanghay.thash

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleObserver
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
* ThashView is a minimal progress view for Android.
*/
@Keep
class ThashView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle), LifecycleObserver {
    
    /**
     * Listeners
     */
    private var progressListener: ((Float, Float) -> Unit)? = null

    /**
     *  Declaration of all public properties
     */

    var startAngle by prop(DEFAULT_START_ANGLE)
    var maxProgress by prop(DEFAULT_MAX_PROGRESS)

    private var _progress: Float = 0f

    var progress: Float
        set(value) {
            setProgress(progress, true)
            _progress = value
        }
        get() {
            return _progress
        }

    var indicatorColor by prop(DEFAULT_INDICATOR_COLOR)
    var indicatorFraction by prop(DEFAULT_INDICATOR_FRACTION)

    var progressColor: Int by prop(context.accentColor())

    var backgroundProgressColor by prop(DEFAULT_BACKGROUND_PROGRESS_COLOR)
    var progressWidth by prop(DEFAULT_PROGRESS_WIDTH_DIP.dip())
    var progressBackgroundWidth by prop(DEFAULT_BACKGROUND_PROGRESS_WIDTH_DIP.dip())
    var progressRoundedCap by prop(DEFAULT_PROGRESS_ROUNDED_CAP)
    var hideIndicator by prop(DEFAULT_HIDE_INDICATOR)
    var indicatorGravity by prop(INDICATOR_START)
    var indicatorAngleOffset by prop(DEFAULT_INDICATOR_ANGLE_OFFSET)

    private val rectF = RectF()

    // Background Paint
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Progress Paint
    private val progressPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Dot Indicator Paint
    private val indicatorPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }


    private var _currentAnimation: ValueAnimator? = null


    init {
        val typedArray = context.theme.obtainStyledAttributes(attributeSet,
            R.styleable.ThashView, 0, 0)

        indicatorAngleOffset = typedArray.getFloat(R.styleable.ThashView_indicatorAngleOffset, DEFAULT_INDICATOR_ANGLE_OFFSET)
        indicatorGravity = typedArray.getInt(R.styleable.ThashView_indicatorGravity, INDICATOR_START)
        progressRoundedCap = typedArray.getBoolean(R.styleable.ThashView_progressRoundedCap, DEFAULT_PROGRESS_ROUNDED_CAP)
        hideIndicator = typedArray.getBoolean(R.styleable.ThashView_hideIndicator, DEFAULT_HIDE_INDICATOR)
        startAngle = typedArray.getFloat(R.styleable.ThashView_startAngle, DEFAULT_START_ANGLE)
        progress = typedArray.getFloat(R.styleable.ThashView_progress, DEFAULT_PROGRESS)
        maxProgress = typedArray.getFloat(R.styleable.ThashView_maxProgress, DEFAULT_MAX_PROGRESS)
        indicatorColor = typedArray.getColor(R.styleable.ThashView_indicatorColor, DEFAULT_INDICATOR_COLOR)
        indicatorFraction = typedArray.getFloat(R.styleable.ThashView_indicatorFraction, DEFAULT_INDICATOR_FRACTION)
        progressColor = typedArray.getColor(R.styleable.ThashView_progressColor, context.accentColor())
        backgroundProgressColor = typedArray.getColor(R.styleable.ThashView_progressBackgroundColor, DEFAULT_BACKGROUND_PROGRESS_COLOR)
        progressWidth = typedArray.getDimension(R.styleable.ThashView_progressWidth, DEFAULT_PROGRESS_WIDTH_DIP.dip())
        progressBackgroundWidth = typedArray.getDimension(R.styleable.ThashView_progressBackgroundWidth, DEFAULT_BACKGROUND_PROGRESS_WIDTH_DIP.dip())

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        val halfThickness = (progressWidth + progressBackgroundWidth) / 2

        // Background
        rectF.set(halfThickness, halfThickness, width.toFloat() - halfThickness, width.toFloat() - halfThickness)
        canvas.drawArc(rectF, 0f, 360f, false, paint.apply {
            color = backgroundProgressColor
            strokeWidth = progressBackgroundWidth
        })

        val progressAngle = ((progress / maxProgress) onlyInBetween (0f to 1f)) * 360f

        // Progress
        val left = progressWidth
        val right = width.toFloat() - progressWidth
        rectF.set(left, left, right, right)

        canvas.drawArc(rectF, startAngle, progressAngle, false, progressPaint.apply {
            color = progressColor
            strokeWidth = progressWidth
            strokeCap = if (progressRoundedCap) Paint.Cap.ROUND else Paint.Cap.SQUARE
        })

        // Draw indicator
        if (hideIndicator || progress == 0f) return

        val angleOffset = Math.abs(indicatorAngleOffset) onlyInBetween (0f to progressAngle)

        val indicatorAngle = if (indicatorGravity == INDICATOR_START) startAngle + angleOffset
        else (progressAngle + startAngle) - angleOffset

        val r = (width - (progressWidth * 2)) / 2
        val indicatorX = (width / 2f) + Math.cos(indicatorAngle * (Math.PI / 180)) * r
        val indicatorY = (width / 2f) + Math.sin(indicatorAngle * (Math.PI / 180)) * r
        val fraction = indicatorFraction onlyInBetween (0f to 1f)

        canvas.drawCircle(indicatorX.toFloat(), indicatorY.toFloat(),
            progressWidth * fraction / 2f, indicatorPaint.apply { color = indicatorColor })

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Square View
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
            measureDimension(desiredWidth, widthMeasureSpec))
    }


    /**
     * Save important state of the view.
     */
    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            progress = this@ThashView.progress
        }
    }


    /**
     * Restore states
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        progress = state.progress

    }

    fun animateAngle(block: (ValueAnimator.() -> Unit)? = null) {
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 750
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                startAngle = 270f + it.animatedValue as Float
            }
            block?.invoke(this)
            start()
        }
    }

    fun progressChanged(progressListener: ((progress: Float, fraction: Float) -> Unit)) {
        this.progressListener = progressListener
        progressListener.invoke(progress, progress / maxProgress)
    }


    fun animateProgress(progress: Float, block: (ValueAnimator.() -> Unit)? = null) {
        _currentAnimation?.cancel()
        _currentAnimation = ValueAnimator.ofFloat(this.progress, progress).apply {
            duration = 250
            interpolator = DecelerateInterpolator()
            block?.invoke(this)
            addUpdateListener {
                this@ThashView.progress = it.animatedValue as Float
            }
        }
        _currentAnimation?.start()
    }

    /**
     * Measure the size of the view
     */
    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(desiredSize)

        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> Math.min(desiredSize, size)
            MeasureSpec.UNSPECIFIED -> desiredSize
            else -> desiredSize
        }
    }

    /***
     * Get Accent Color from context
     */
    @ColorInt
    @CheckResult
    private fun Context.accentColor(): Int {
        val typedValue = TypedValue()
        val typedArray = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
        return typedArray.getColor(0, 0).also {
            typedArray.recycle()
        }
    }


    // Limit Range of Float
    private infix fun Float.onlyInBetween(minMax: Pair<Float, Float>): Float {
        val (min, max) = minMax
        return Math.min(max, Math.max(min, this))
    }

    // Limit Range of Int
    private infix fun Int.onlyInBetween(minMax: Pair<Int, Int>): Int  {
        val (min, max) = minMax
        return Math.min(max, Math.max(min, this))
    }

    // Limit Range of Double
    private infix fun Double.onlyInBetween(minMax: Pair<Double, Double>): Double  {
        val (min, max) = minMax
        return Math.min(max, Math.max(min, this))
    }

    // Utility functions for converting dimension
    private fun Float.dip() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)
    private fun Float.dipInt() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics).toInt()
    private fun Double.dip() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics)
    private fun Double.dipInt() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics).toInt()
    private fun Int.dip() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics)
    private fun Int.dipInt() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics).toInt()


    /**
     * An alias of CircularProperty
     */
    private fun <T> prop(value: T, observer: ((T) -> Unit)? = null) = CircularProperty(value, observer)

    fun setProgress(progress: Float, notify: Boolean) {
        _progress = progress
        if (notify) {
            progressListener?.invoke(_progress, _progress / maxProgress)
        }
    }

    /**
     * Property delegate class
     * @param value value pass in to
     */
    class CircularProperty<T>(var value: T, private var observer: ((T) -> Unit)? = null) :
        ReadWriteProperty<ThashView, T> {
        override fun getValue(thisRef: ThashView, property: KProperty<*>): T {
            return value
        }

        override fun setValue(thisRef: ThashView, property: KProperty<*>, value: T) {
            thisRef.invalidate()
            this.value = value
            observer?.invoke(this.value)
        }
    }

    
    companion object {
        const val DEFAULT_START_ANGLE = 270f
        const val DEFAULT_PROGRESS = 0f
        const val DEFAULT_MAX_PROGRESS = 100f

        const val DEFAULT_PROGRESS_ROUNDED_CAP = true
        const val DEFAULT_PROGRESS_WIDTH_DIP = 20f
        const val DEFAULT_BACKGROUND_PROGRESS_WIDTH_DIP = 2f
        const val DEFAULT_BACKGROUND_PROGRESS_COLOR = Color.LTGRAY

        const val DEFAULT_INDICATOR_FRACTION = .5f
        const val DEFAULT_INDICATOR_COLOR = Color.WHITE
        const val DEFAULT_HIDE_INDICATOR = false
        const val DEFAULT_INDICATOR_ANGLE_OFFSET = 0f

        // INDICATOR_GRAVITY
        const val INDICATOR_START = 0
        const val INDICATOR_END = 1
    }
    
    /**
     * Save view states
     */
    internal class SavedState : BaseSavedState {
        
        var progress = 0f
        private var startAngle = 270f
        private var maxProgress = 100f
        private var progressThickness = 60f
        private var progressBackgroundThickness = 10f

        constructor(superState: Parcelable?) : super(superState)

        private constructor(inState: Parcel?) : super(inState) {
            if (inState == null) return
            val bundle = inState.readBundle(javaClass.classLoader) ?: return
            bundle.getFloat(PARAMS_PROGRESS).let { progress = it }
            bundle.getFloat(PARAMS_MAX_PROGRESS).let { maxProgress = it }
            bundle.getFloat(PARAMS_START_ANGLE).let { startAngle = it }
            bundle.getFloat(PARAMS_PROGRESS_THICKNESS).let { progressThickness = it }
            bundle.getFloat(PARAMS_PROGRESS_BACKGROUND_THICKNESS).let { progressBackgroundThickness = it }
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)

            val params = Bundle().apply {
                putFloat(PARAMS_PROGRESS, progress)
                putFloat(PARAMS_MAX_PROGRESS, maxProgress)
                putFloat(PARAMS_START_ANGLE, startAngle)
                putFloat(PARAMS_PROGRESS_THICKNESS, progressThickness)
                putFloat(PARAMS_PROGRESS_BACKGROUND_THICKNESS, progressBackgroundThickness)
            }

            out?.writeBundle(params)
        }
        
        private companion object {

            private const val PARAMS_PROGRESS = "progress"
            private const val PARAMS_START_ANGLE = "start-angle"
            private const val PARAMS_MAX_PROGRESS = "max-progress"
            private const val PARAMS_PROGRESS_THICKNESS = "progress-thickness"
            private const val PARAMS_PROGRESS_BACKGROUND_THICKNESS = "progress-background-thickness"

            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel?) = SavedState(parcel)
                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }
    }

}
