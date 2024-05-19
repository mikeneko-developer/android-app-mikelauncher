package net.mikemobile.mikelauncher.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class OverlayView: ConstraintLayout {
    private val DEBUG_MODE = true
    private val MASK_INVISIBLE = true

    private val paint = Paint()

    constructor(context: Context): super(context){}
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs){}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr){}

    private var listener: OverlayViewListener? = null
    fun setOnOverlayViewListener(l: OverlayViewListener) {
        listener = l
    }

//    var data: MaskImageData? = null
//
//    fun setMaskImageData(data: MaskImageData?) {
//        this.data = data
//        scalePoint = 1f
//        invalidate()
//    }

    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

//        data?.let {
//            if (!DEBUG_MODE || DEBUG_MODE) {
//                setLayerType(LAYER_TYPE_HARDWARE, null)
//                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
//
//                val scaleWidth = it.bitmap.width * scalePoint
//                val scaleHeight = it.bitmap.height * scalePoint
//                val positionX = it.x + ((it.bitmap.width - scaleWidth) / 2)
//                val positionY = it.y + ((it.bitmap.height - scaleHeight) / 2)
//
//                val scaleWidth = it.bitmap.width
//                val scaleHeight = it.bitmap.height
//                val positionX = it.x
//                val positionY = it.y
//
//                val matrix = Matrix()
//                //matrix.setScale(scalePoint, scalePoint)
//                matrix.postTranslate(positionX, positionY)
//                //paint.alpha = alphaPoint
//
//                canvas.drawBitmap(it.bitmap, matrix, paint)
//
//            }
//
//            if (DEBUG_MODE) {
//                val touchPaint = Paint()
//                touchPaint.strokeWidth = 10f
//                touchPaint.style = Paint.Style.STROKE
//                touchPaint.color = Color.RED
//
//                canvas.drawRect(
//                    it.touchPointX,
//                    it.touchPointY,
//                    it.touchPointX + it.touchPointWidth,
//                    it.touchPointY + it.touchPointHeight,
//                    touchPaint
//                )
//
//                touchPaint.style = Paint.Style.FILL
//                touchPaint.textSize = 40f
//
//                canvas.drawText("タッチ範囲",
//                    it.touchPointX,
//                    it.touchPointY - 30f,
//                    touchPaint
//                )
//
//                canvas.drawText("Alpha:" + (alpha / 255),
//                    10f,
//                    300f,
//                    touchPaint
//                )
//
//                canvas.drawText("Position x:" + (it.touchPointX) + " y:" + (it.touchPointY),
//                    10f,
//                    350f,
//                    touchPaint
//                )
//
//                canvas.drawText("Size: width:" + (it.bitmap.width) + " height:" + (it.bitmap.height),
//                    10f,
//                    400f,
//                    touchPaint
//                )
//
//                if (!MASK_INVISIBLE) {
//                    canvas.drawBitmap(it.bitmap, 10f, 450f, touchPaint)
//                }
//            }
//        }
    }

    private fun checkTouchPoint(motionEvent: MotionEvent): Boolean {
//        if (data != null) {
//            val width = data!!.touchPointWidth
//            val height = data!!.touchPointHeight
//            val pX = motionEvent.x
//            val pY = motionEvent.y
//            if (data!!.touchPointX <= pX && data!!.touchPointY <= pY) {
//                if (pX <= data!!.touchPointX + width && pY <= data!!.touchPointY + height) {
//                    return true
//                }
//            }
//        }

        return false
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    var touchDownEnablePoint = false
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownEnablePoint = checkTouchPoint(motionEvent)
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchDownEnablePoint && !checkTouchPoint(motionEvent)) {
                    touchDownEnablePoint = false
                }
            }
            MotionEvent.ACTION_UP -> {
                if (touchDownEnablePoint) {
                    if (checkTouchPoint(motionEvent)) {
                        listener?.onClick(this)
                    } else {
                        touchDownEnablePoint = false
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                touchDownEnablePoint = false
            }
        }

        if (!touchDownEnablePoint) {
            return true
        }

        return super.onTouchEvent(motionEvent)
    }

    interface OverlayViewListener {
        fun onClick(view: View?)
        fun onChangeViewParam(scale: Float, alpha: Float)
        fun onAnimationEnd()
    }

    private var animation = false
    private var scalePoint = 0f
    private var alphaPoint = 0

    private val WEIGHT_TIME = 20L
    private fun getFrameLate(time: Long): Float {
        return 1f / (time / WEIGHT_TIME).toFloat()
    }

    private var cancelAnim = false
    fun cancelAnimation() {
        cancelAnim = true
    }

    fun startAlphaAnimation(start: Int, end: Int, time: Long, delay: Long = 0L) {
        if (animation) {
            animation = false
            return
        }

        alphaPoint = 0
        animation = true
//        if (data == null) {
//            return
//        }

        val toBack = start > end

        val framelate = getFrameLate(time)

        var loop = true
        var count = 0

        Thread {
            Thread.sleep(delay)
            do {

                var alphat = if (toBack) {
                    start - ((framelate * count) * 210)
                } else  {
                    start - ((framelate * count) * 210)
                }

                if (cancelAnim) {
                    alpha = end.toFloat()
                    loop = false
                } else if (toBack) {
                    if (alpha < end) {
                        alpha = end.toFloat()
                        loop = false
                    }
                } else {
                    if (alpha > end) {
                        alpha = end.toFloat()
                        loop = false
                    }
                }

                alphaPoint = alpha.toInt()

                if (!cancelAnim) {
                    delayed(0) {
                        listener?.onChangeViewParam(scalePoint, alphaPoint / 255f)
                        invalidate()
                    }
                }

                count++

                Thread.sleep(WEIGHT_TIME)

            } while(loop && animation && !cancelAnim)

            if (!cancelAnim) {
                listener?.onAnimationEnd()
            }

            cancelAnim = false
            animation = false

        }.start()
    }

    private fun delayed(
        time: Long,
        callback: () -> Unit
        ) = Handler(Looper.getMainLooper()).postDelayed({
            callback.invoke()
        } , time
    )
}