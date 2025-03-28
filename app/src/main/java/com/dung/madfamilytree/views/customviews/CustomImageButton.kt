package com.dung.madfamilytree.views.customviews

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintSet.Motion
import com.dung.madfamilytree.R
import kotlin.math.log
import kotlin.math.pow

class CustomImageButton : androidx.appcompat.widget.AppCompatImageButton {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setContent(attrs, context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setContent(attrs, context)
    }

    private var backGroundColor = resources.getColor(R.color.secondary_color)
    fun setContent(attrs: AttributeSet, context: Context) {
        val data = context.obtainStyledAttributes(attrs, R.styleable.CustomImageButton)
        backGroundColor = data.getColor(
            R.styleable.CustomImageButton_custom_background_color,
            resources.getColor(R.color.secondary_color)
        )
        backgroundPaint.color = backGroundColor
    }

    private var circleCenterX = 0f
    private var circleCenterY = 0f
    private var circleRadios = 0f
    private var currentAlpha = 255
    private var circleMaxRadios = 0f
    private var newNotiAnimator: ValueAnimator? = null
    private var restoreNotiAnimator: ValueAnimator? = null
    private var notiCenterX = 0f
    private var notiCenterY = 0f
    private var notiTextX = 0f
    private var notiTextY = 0f
    private var notiRadios = 0f
    private var isAnimating = false

    private val backgroundPaint = Paint()
    private val notiPaint = Paint()

    private var currentRotate = 0f
    private var _numOfNoti = 0
    var numOfNoti
        get() = _numOfNoti
        set(value) {
            _numOfNoti = value
            if(!isAnimating){
                startNewNotiAnimation(500)
            }
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circleCenterX = w / 2f
        circleCenterY = h / 2f
        circleMaxRadios = w / 2f

        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.isAntiAlias = true

        notiPaint.color = Color.RED
        notiPaint.textAlign = Paint.Align.CENTER
        notiPaint.textSize = dpToPx(10f).toFloat()
        notiPaint.style = Paint.Style.FILL
        notiPaint.isAntiAlias = true

        notiCenterX = circleCenterX + dpToPx(NOTI_OFFSET_PX)
        notiCenterY = circleCenterY - dpToPx(NOTI_OFFSET_PX)
        notiRadios = dpToPx(NOTI_RADIOS).toFloat()

        notiTextX = notiCenterX
        val rect = Rect()
        notiPaint.getTextBounds(_numOfNoti.toString(), 0, _numOfNoti.toString().length, rect)
        notiTextY = notiCenterY - (notiPaint.ascent() + notiPaint.descent()) / 2
        Log.d("ascent", "${notiPaint.descent()}")

    }

    private var clickValueAnimator: ValueAnimator? = null
    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        Log.d("CustomView", "Click")
    }

    fun startClickAnimation() {
        clickValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 10f.pow(3).div(2f).toLong()
            addUpdateListener {
                val fragtion = it.animatedValue as Float
                circleRadios = circleMaxRadios * fragtion
                currentAlpha = 255 - (fragtion * 255).toInt()
                if (fragtion == 1f) {
                    it.cancel()
                }
                postInvalidate()
            }
            start()
        }

    }

    fun startNewNotiAnimation(durationMs: Long) {
        isAnimating = true
        newNotiAnimator = ValueAnimator.ofFloat(-1f, 1f).apply {
            duration = durationMs
            repeatCount = 2
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                val fragtion = it.animatedValue as Float
                currentRotate = (fragtion * MAX_CORNER_ROTATE)
                postInvalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {

                }

                override fun onAnimationEnd(p0: Animator) {
                    restoreNotiAnimator?.start()
                }

                override fun onAnimationCancel(p0: Animator) {
                }

                override fun onAnimationRepeat(p0: Animator) {
                }

            })
            setCurrentFraction(0.5f)
            start()
        }
        restoreNotiAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = durationMs
            addUpdateListener {
                val fragtion = it.animatedValue as Float
                currentRotate = (fragtion * MAX_CORNER_ROTATE)
                postInvalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                }

                override fun onAnimationEnd(p0: Animator) {
                    isAnimating = false
                }

                override fun onAnimationCancel(p0: Animator) {
                }

                override fun onAnimationRepeat(p0: Animator) {
                }
            })
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            startClickAnimation()
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        backgroundPaint.alpha = currentAlpha
        canvas.drawCircle(circleCenterX, circleCenterY, circleRadios, backgroundPaint)
        canvas.save()
        canvas.rotate(currentRotate, circleCenterX, circleCenterY)
        super.onDraw(canvas)
        canvas.restore()
        if (_numOfNoti > 0) {
            drawNoti(canvas)
        }
    }

    fun drawNoti(canvas: Canvas) {
        notiPaint.color = Color.RED
        canvas.drawCircle(notiCenterX, notiCenterY, NOTI_RADIOS, notiPaint)
//        notiPaint.color = Color.WHITE
//        canvas.drawText(_numOfNoti.toString(), notiTextX, notiTextY, notiPaint)
    }

    companion object {
        const val MAX_CORNER_ROTATE = 30f
        const val NOTI_OFFSET_PX = 8f
        const val NOTI_RADIOS = 5f
    }

    fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
}