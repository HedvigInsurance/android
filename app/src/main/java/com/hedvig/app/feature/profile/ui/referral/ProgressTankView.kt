package com.hedvig.app.feature.profile.ui.referral

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatColor
import android.graphics.drawable.BitmapDrawable
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hedvig.app.util.extensions.compatFont
import com.hedvig.app.util.interpolateTextKey
import kotlin.math.ceil

class ProgressTankView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(context, attributeSet, defStyle)

    private var premium: Int = 100
    private var discountedPremium: Int = 80
    private var step = 10
    private var segments = premium / step

    //colors
    private val purple = context.compatColor(R.color.purple)
    private val darkPurple = context.compatColor(R.color.dark_purple)
    private val lightPurple = context.compatColor(R.color.referral_tank_lines)
    private val offBlackDark = context.compatColor(R.color.off_black_dark)
    private val white = context.compatColor(R.color.white)
    private val green = context.compatColor(R.color.green)

    // positions
    private var centerX = -1f
    private var centerY = -1f

    // dimens
    private val tankWidth = context.resources.getDimension(R.dimen.referral_progress_bar_width)
    private val tankWidthHalf = tankWidth / 2
    private var animationTopPadding = 0f

    private val labelsMarginFromTank = context.resources.getDimension(R.dimen.referral_progress_label_margin)

    private val roofHeight = context.resources.getDimension(R.dimen.referral_progress_roof_height)
    private val roofHeightHalf = roofHeight / 2

    private val sectionSpacing = context.resources.getDimension(R.dimen.referral_progress_segment_spacing)
    private val sectionSpacingHalf = sectionSpacing / 2

    private val textLabelRadius = context.resources.getDimension(R.dimen.referral_progress_text_label_radius)

    private val textSizeLabelLeft = context.resources.getDimension(R.dimen.text_xsmall)
    private val textSizeLabelRight = context.resources.getDimension(R.dimen.text_large)
    private val textPadding = context.resources.getDimension(R.dimen.referral_progress_text_padding)
    private val textLabelArrowSquareSize = context.resources.getDimension(R.dimen.referral_progress_arrow_square_size)

    // tile canvas
    private var polkaDrawable: BitmapDrawable
    private lateinit var tileResult: Bitmap
    private lateinit var tileCanvas: Canvas
    private lateinit var maskBitmap: Bitmap
    private lateinit var maskCanvas: Canvas

    //strings
    private val callToAction = context.getString(R.string.REFERRAL_PROGRESS_BAR_CTA)
    private val bottomLabelText = context.getString(R.string.REFERRAL_PROGRESS_FREE)
    // use by lazy to get premium on first draw instead of on init
    private val currentPremiumPrice by lazy {
        interpolateTextKey(
            context.getString(R.string.REFERRAL_PROGRESS_CURRENT_PREMIUM_PRICE),
            "CURRENT_PREMIUM_PRICE" to premium.toString()
        )
    }
    private val currentInvitedActiveValue by lazy {
        interpolateTextKey(
            context.getString(R.string.REFERRAL_INVITE_ACTIVE_VALUE),
            "REFERRAL_VALUE" to (premium - discountedPremium).toString()
        )
    }

    //font
    val font = context.compatFont(R.font.circular_bold)

    init {
        val polkaTile = BitmapFactory.decodeResource(context.resources, R.mipmap.polka_pattern_green_tile)
        polkaDrawable = BitmapDrawable(resources, polkaTile)
        polkaDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    //rect to recycle
    private val rect = Rect()
    private val rectF = RectF()
    private val path = Path()
    private val paint = Paint(ANTI_ALIAS_FLAG)

    private var isFirstDraw = true
    private var isInitialized = false
    private var hasDiscount = false

    private var isWaitingForAnimation = false

    fun initialize(premium: Int, discount: Int, step: Int) {
        this.premium = premium
        this.discountedPremium = premium - discount
        this.step = step
        segments = ceil(premium.toFloat() / step).toInt()
        isInitialized = true
        startAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInitialized || isWaitingForAnimation) return
        hasDiscount = discountedSegments != 0
        if (hasDiscount) {
            resetMask()
        }

        if (isFirstDraw) {
            centerX = getCenterX().toFloat()
            centerY = getCenterY().toFloat()
            polkaDrawable.setBounds(0, 0, tankWidth.toInt(), height)
            isFirstDraw = false
        }

        // Draw tank
        if (tankSpringAnimation.isRunning) {
            animationTopPadding = (height - roofHeight) * (tankFloatValueHolder.value / SPRING_START_VALUE)
        }
        val animatedSegmentHeight = (height - roofHeight - animationTopPadding - sectionSpacing) / segments
        drawSegments(canvas, animatedSegmentHeight)

        if (hasDiscount) {
            drawTiledFace(canvas)
        }

        val segmentHeight = (height - roofHeight - sectionSpacing) / segments

        // Draw bottom text label
        if (runBottomTextLabelAnimation) {
            if (bottomLabelSpringAnimation.isRunning) {
                val animationValue = bottomLabelFloatValueHolder.value / SPRING_START_VALUE
                drawTextLabelLeft(canvas, bottomLabelText, roofHeightHalf + (segmentHeight * segments), animationValue)
            }
        } else {
            drawTextLabelLeft(canvas, bottomLabelText, roofHeightHalf + (segmentHeight * segments))
        }

        // Draw text label right
        if (runRightTextLabelAnimation) {
            if (rightLabelSpringAnimation.isRunning) {
                val animationValue = rightLabelFloatValueHolder.value / SPRING_START_VALUE
                drawTextLabelRight(canvas, segmentHeight, animationValue)
            }
        } else {
            drawTextLabelRight(canvas, segmentHeight)
        }

        // Draw top text label
        if (runTopTextLabelAnimation) {
            if (topLabelSpringAnimation.isRunning) {
                val animationValue = topLabelFloatValueHolder.value / SPRING_START_VALUE
                drawTextLabelLeft(canvas, currentPremiumPrice, roofHeightHalf, animationValue)
            }
        } else {
            drawTextLabelLeft(canvas, currentPremiumPrice, roofHeightHalf)
        }

        if (animationIsRunning) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawSegments(canvas: Canvas, segmentHeight: Float) {
        for (i in segments - 1 downTo 0) {
            drawSegment(canvas, i, segmentHeight, isSegmentDiscounted(i))
        }
    }

    private fun drawSegment(canvas: Canvas, index: Int, segmentHeight: Float, isDiscounted: Boolean) {
        drawLeftFace(canvas, index, segmentHeight, isDiscounted)
        drawRightFace(canvas, index, segmentHeight, isDiscounted)
        // only draw roof on last
        if (index == 0)
            drawRoof(canvas, index, segmentHeight, isDiscounted)
    }

    private fun drawRoof(canvas: Canvas, index: Int, segmentHeight: Float, isDiscounted: Boolean) {
        path.reset()

        paint.color = if (isDiscounted) green else purple
        paint.style = Paint.Style.FILL

        path.moveTo(centerX, animationTopPadding + (index * segmentHeight))
        path.lineTo(centerX + tankWidthHalf, animationTopPadding + roofHeightHalf + (index * segmentHeight))
        path.lineTo(centerX, animationTopPadding + roofHeight + (index * segmentHeight))
        path.lineTo(centerX - tankWidthHalf, animationTopPadding + roofHeightHalf + (index * segmentHeight))
        path.close()

        canvas.drawPath(path, paint)
    }

    private fun drawLeftFace(canvas: Canvas, index: Int, segmentHeight: Float, isDiscounted: Boolean) {
        path.reset()

        val sectionLeft = if (isDiscounted) 0f else centerX - tankWidthHalf
        val sectionRight = if (isDiscounted) tankWidthHalf else centerX

        val sectionTop = animationTopPadding + (index * segmentHeight)
        val sectionBottom = sectionTop + segmentHeight

        path.moveTo(sectionLeft, sectionTop + roofHeightHalf)
        path.lineTo(sectionRight, sectionTop + roofHeight)
        path.lineTo(sectionRight, sectionBottom + roofHeight)
        path.lineTo(sectionLeft, sectionBottom + roofHeightHalf)
        path.close()

        paint.style = Paint.Style.FILL
        if (isDiscounted) {
            addPathToMask(path)
            drawVerticalGreenOutline(
                canvas,
                centerX - tankWidthHalf + sectionSpacingHalf,
                sectionTop + roofHeightHalf,
                sectionBottom + roofHeightHalf
            )
        } else {
            paint.color = purple
            canvas.drawPath(path, paint)
            paint.color = lightPurple
        }
        setUpPaintForLine()
        if (!isDiscounted || isLastDiscountedSegment(index)) {
            canvas.drawLine(
                centerX - tankWidthHalf,
                sectionBottom + roofHeightHalf + sectionSpacingHalf,
                centerX,
                sectionBottom + roofHeight + sectionSpacingHalf,
                paint
            )
        }
        if (index == 0) {
            canvas.drawLine(
                centerX - tankWidthHalf,
                sectionTop + roofHeightHalf + sectionSpacingHalf,
                centerX,
                sectionTop + roofHeight + sectionSpacingHalf,
                paint
            )
        }
    }

    private fun drawRightFace(canvas: Canvas, index: Int, segmentHeight: Float, isDiscounted: Boolean) {
        path.reset()

        val sectionLeft = if (isDiscounted) tankWidthHalf else centerX
        val sectionRight = if (isDiscounted) tankWidth else centerX + tankWidthHalf

        val sectionTop = animationTopPadding + (index * segmentHeight)
        val sectionBottom = sectionTop + segmentHeight

        path.moveTo(sectionLeft, sectionTop + roofHeight)
        path.lineTo(sectionRight, sectionTop + roofHeightHalf)
        path.lineTo(sectionRight, sectionBottom + roofHeightHalf)
        path.lineTo(sectionLeft, sectionBottom + roofHeight)
        path.close()

        paint.style = Paint.Style.FILL
        if (isDiscounted) {
            addPathToMask(path)
            setUpPaintForLine()
            drawVerticalGreenOutline(
                canvas,
                centerX + tankWidthHalf - sectionSpacingHalf,
                sectionTop + roofHeightHalf,
                sectionBottom + roofHeightHalf
            )
        } else {
            paint.color = darkPurple
            canvas.drawPath(path, paint)
            paint.color = lightPurple
        }
        setUpPaintForLine()
        if (!isDiscounted || isLastDiscountedSegment(index)) {
            canvas.drawLine(
                centerX,
                sectionBottom + roofHeight + sectionSpacingHalf,
                centerX + tankWidthHalf,
                sectionBottom + roofHeightHalf + sectionSpacingHalf,
                paint
            )
        }
        if (index == 0) {
            canvas.drawLine(
                centerX,
                sectionTop + roofHeight + sectionSpacingHalf,
                centerX + tankWidthHalf,
                sectionTop + roofHeightHalf + sectionSpacingHalf,
                paint
            )
        }
    }

    private fun drawVerticalGreenOutline(canvas: Canvas, xPosition: Float, yTop: Float, yBottom: Float) {
        setUpPaintForLine()
        paint.color = green
        canvas.drawLine(
            xPosition,
            yTop,
            xPosition,
            yBottom,
            paint
        )
    }

    private fun resetMask() {
        tileResult = Bitmap.createBitmap(tankWidth.toInt(), height, Bitmap.Config.ARGB_8888)
        tileCanvas = Canvas(tileResult)

        maskBitmap = Bitmap.createBitmap(tankWidth.toInt(), height, Bitmap.Config.ARGB_8888)
        maskCanvas = Canvas(maskBitmap)
    }

    private fun addPathToMask(path: Path) {
        paint.style = Paint.Style.FILL
        paint.color = green
        maskCanvas.drawPath(path, paint)
    }

    private fun drawTiledFace(canvas: Canvas) {
        polkaDrawable.draw(tileCanvas)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        tileCanvas.drawBitmap(maskBitmap, 0f, 0f, paint)
        paint.xfermode = null
        canvas.drawBitmap(tileResult, (centerX - tankWidthHalf).toFloat(), 0f, paint)
    }

    // Draw text labels
    private fun drawTextLabelLeft(canvas: Canvas, text: String, yPosition: Float, animationValue: Float = 0f) {
        paint.color = offBlackDark
        paint.style = Paint.Style.FILL
        paint.alpha = (255 - (animationValue * 255)).toInt()
        paint.typeface = font
        paint.textSize = textSizeLabelLeft
        paint.textAlign = Paint.Align.CENTER

        paint.getTextBounds(text, 0, text.length, rect)

        val textHeight = rect.height().toFloat()

        val labelWidth = paint.measureText(text) + textPadding
        val labelHeightHalf = (textHeight + textPadding) / 2
        rectF.set(
            (centerX - tankWidthHalf - labelsMarginFromTank - labelWidth),
            (yPosition - labelHeightHalf),
            centerX - tankWidthHalf - labelsMarginFromTank,
            (yPosition + labelHeightHalf)
        )

        canvas.drawRoundRect(rectF, textLabelRadius, textLabelRadius, paint)

        rect.set(
            rectF.right.toInt() - textLabelArrowSquareSize.toInt(),
            rectF.centerY().toInt() - textLabelArrowSquareSize.toInt(),
            rectF.right.toInt() + textLabelArrowSquareSize.toInt(),
            rectF.centerY().toInt() + textLabelArrowSquareSize.toInt()
        )

        canvas.save()
        canvas.rotate(45f, rect.exactCenterX(), rect.exactCenterY())
        canvas.drawRect(rect, paint)
        canvas.restore()

        paint.color = white
        canvas.drawText(text, rectF.centerX(), rectF.centerY() - ((paint.descent() + paint.ascent()) / 2), paint)
        paint.alpha = 255
    }

    private fun drawTextLabelRight(canvas: Canvas, segmentHeight: Float, animationValue: Float = 0f) {
        val text = if (hasDiscount) currentInvitedActiveValue else callToAction

        paint.color = if (hasDiscount) green else purple
        paint.style = Paint.Style.FILL
        paint.alpha = (255 - (animationValue * 255)).toInt()
        paint.typeface = font
        paint.textSize = textSizeLabelRight
        paint.textAlign = Paint.Align.CENTER

        paint.getTextBounds(text, 0, text.length, rect)

        val yPosition =
            roofHeightHalf + if (hasDiscount) (segmentHeight * discountedSegments) / 2 else segmentHeight / 2
        val textHeight = rect.height().toFloat()

        val labelWidth = rect.width() + textPadding
        val labelHeightHalf = (textHeight + textPadding) / 2
        rectF.set(
            (centerX + tankWidthHalf + labelsMarginFromTank),
            (yPosition - labelHeightHalf),
            (centerX + tankWidthHalf + labelsMarginFromTank + labelWidth),
            (yPosition + labelHeightHalf)
        )

        canvas.drawRoundRect(rectF, textLabelRadius, textLabelRadius, paint)

        rect.set(
            rectF.left.toInt() - textLabelArrowSquareSize.toInt(),
            rectF.centerY().toInt() - textLabelArrowSquareSize.toInt(),
            rectF.left.toInt() + textLabelArrowSquareSize.toInt(),
            rectF.centerY().toInt() + textLabelArrowSquareSize.toInt()
        )

        canvas.save()
        canvas.rotate(45f, rect.exactCenterX(), rect.exactCenterY())
        canvas.drawRect(rect, paint)
        canvas.restore()

        paint.color = if (hasDiscount) offBlackDark else white
        canvas.drawText(text, rectF.centerX(), rectF.centerY() - ((paint.descent() + paint.ascent()) / 2), paint)
        paint.alpha = 255
    }

    // Animation
    private fun startAnimation() {
        isWaitingForAnimation = true
        postDelayed({
            isWaitingForAnimation = false
            postInvalidateOnAnimation()
            tankSpringAnimation.setStartValue(SPRING_START_VALUE)
            tankSpringAnimation.start()
        }, INITIAL_ANIMATION_DELAY_MILLIS)
        postDelayed({
            postInvalidateOnAnimation()
            bottomLabelSpringAnimation.setStartValue(SPRING_START_VALUE)
            bottomLabelSpringAnimation.start()
        }, INITIAL_ANIMATION_DELAY_MILLIS + TEXT_LABEL_ANIMATION_DELAY_MILLIS)
        postDelayed({
            postInvalidateOnAnimation()
            rightLabelSpringAnimation.setStartValue(SPRING_START_VALUE)
            rightLabelSpringAnimation.start()
        }, INITIAL_ANIMATION_DELAY_MILLIS + (TEXT_LABEL_ANIMATION_DELAY_MILLIS * 2))
        postDelayed({
            postInvalidateOnAnimation()
            topLabelSpringAnimation.setStartValue(SPRING_START_VALUE)
            topLabelSpringAnimation.start()
        }, INITIAL_ANIMATION_DELAY_MILLIS + (TEXT_LABEL_ANIMATION_DELAY_MILLIS * 3))
    }

    // Helpers
    private fun isSegmentDiscounted(index: Int) = (premium - (index * step)) > discountedPremium

    private fun isLastDiscountedSegment(index: Int) =
        isSegmentDiscounted(index) && (premium - (index * step)) - step <= discountedPremium

    private val discountedSegments: Int
        get() = ceil((premium - discountedPremium).toFloat() / step).toInt()

    private fun setUpPaintForLine() {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = sectionSpacing
    }

    private fun getCenterX() = this.width / 2
    private fun getCenterY() = this.height / 2

    private val animationIsRunning
        get() = tankSpringAnimation.isRunning ||
            bottomLabelSpringAnimation.isRunning ||
            rightLabelSpringAnimation.isRunning ||
            topLabelSpringAnimation.isRunning

    // Companion
    companion object {
        private const val SPRING_START_VALUE = 100f
        private const val SPRING_MIN_VALUE = 0f
        private const val INITIAL_ANIMATION_DELAY_MILLIS = 200L
        private const val TEXT_LABEL_ANIMATION_DELAY_MILLIS = 125L
    }

    // Animation interpolators
    private val spring = SpringForce(SPRING_MIN_VALUE)
        .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)

    private val tankFloatValueHolder = FloatValueHolder(SPRING_MIN_VALUE)

    private val tankSpringAnimation = SpringAnimation(tankFloatValueHolder).also {
        it.setMaxValue(SPRING_START_VALUE)
        it.setMinValue(SPRING_MIN_VALUE)
        it.spring = spring
        it.setStartValue(SPRING_START_VALUE)
    }
    private val bottomLabelFloatValueHolder = FloatValueHolder(SPRING_MIN_VALUE)

    private val bottomLabelSpringAnimation = SpringAnimation(bottomLabelFloatValueHolder).also {
        it.setMaxValue(SPRING_START_VALUE)
        it.setMinValue(SPRING_MIN_VALUE)
        it.spring = spring
        it.setStartValue(SPRING_START_VALUE)
    }
    private val rightLabelFloatValueHolder = FloatValueHolder(SPRING_MIN_VALUE)

    private val rightLabelSpringAnimation = SpringAnimation(rightLabelFloatValueHolder).also {
        it.setMaxValue(SPRING_START_VALUE)
        it.setMinValue(SPRING_MIN_VALUE)
        it.spring = spring
        it.setStartValue(SPRING_START_VALUE)
    }
    private val topLabelFloatValueHolder = FloatValueHolder(SPRING_MIN_VALUE)

    private val topLabelSpringAnimation = SpringAnimation(topLabelFloatValueHolder).also {
        it.setMaxValue(SPRING_START_VALUE)
        it.setMinValue(SPRING_MIN_VALUE)
        it.spring = spring
        it.setStartValue(SPRING_START_VALUE)
    }

    private val runBottomTextLabelAnimation: Boolean
        get() = tankSpringAnimation.isRunning || bottomLabelSpringAnimation.isRunning
    private val runRightTextLabelAnimation: Boolean
        get() = tankSpringAnimation.isRunning || bottomLabelSpringAnimation.isRunning || rightLabelSpringAnimation.isRunning
    private val runTopTextLabelAnimation: Boolean
        get() = tankSpringAnimation.isRunning || bottomLabelSpringAnimation.isRunning || rightLabelSpringAnimation.isRunning || topLabelSpringAnimation.isRunning
}
