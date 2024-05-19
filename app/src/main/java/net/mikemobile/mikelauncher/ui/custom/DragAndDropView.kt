package net.mikemobile.mikelauncher.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import net.mikemobile.mikelauncher.constant.CELL_POINT_NAME
import net.mikemobile.mikelauncher.constant.CellSize
import net.mikemobile.mikelauncher.constant.GridPoint
import net.mikemobile.mikelauncher.util.processImageToOutline

class DragAndDropView: ConstraintLayout {
    private val DEBUG_MODE = true
    private val MASK_INVISIBLE = true

    private val TAG = "DragAndDropView"

    private val paint = Paint()

    constructor(context: Context): super(context){
        paint.color = Color.DKGRAY
    }
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs){
        paint.color = Color.DKGRAY
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        paint.color = Color.DKGRAY
    }

    private var listener: OnDragAndDropViewListener? = null
    fun setOnDragAndDropViewListener(l: OnDragAndDropViewListener) {
        listener = l
    }

    //
    var column: Int = -1
    var row: Int = -1
    var dotHeight = -1
    var calcEnable = false
    var oneCellSize = CellSize(-1f,-1f)

    //
    var cellPointName: CELL_POINT_NAME = CELL_POINT_NAME.NONE
    var cellPoint: GridPoint? = null

    var outlineImage: Bitmap? = null

    var enableDisplay = false

    fun setSplitData(row: Int, column: Int) {
        this.row = row
        this.column = column
    }

    fun setDotHeight(dotHeight: Int): Boolean {
        this.dotHeight = dotHeight

        return checkData()
    }

    private fun checkData(): Boolean {
        if (dotHeight == -1) return false

        val oneWidth = measuredWidth.toFloat() / column
        val oneHeight = (measuredHeight - dotHeight.toFloat()) / (row + 1)

        oneCellSize = CellSize(oneWidth, oneHeight)

        calcEnable = true
        return true
    }




    var data: Bitmap? = null
    var iconPoint: DimensionPoint? = null
    fun setDragImage(data: Bitmap?, point: DimensionPoint?) {
        android.util.Log.i(TAG,"setDragImage")
        android.util.Log.i(TAG,"Bitmap is null = " + (data == null))
        android.util.Log.i(TAG,"DimensionPoint is null = " + (point == null))
        this.data = data
        this.iconPoint = point

        downPosition?.let {
            listener?.onTouchMove(DimensionPoint(it.x, it.y))
        }

        data?.let {
            outlineImage = processImageToOutline(data)
        }

        invalidate()
    }

    fun getPoint(): DimensionPoint? {
        return movePosition
    }

    private var lowerDesktopView: View? = null
    fun setLowerDesktopView(view: View?) {
        this.lowerDesktopView = view
    }

    private var lowerDockView: View? = null
    fun setLowerDockView(view: View?) {
        this.lowerDockView = view
    }

    private var dragAnimation = false
    fun setDragAnimationEnable() {
        dragAnimation = true
    }

    fun setDragAnimationDisable() {
        dragAnimation = false
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (!enableDisplay && this.measuredWidth > 0 && this.measuredHeight > 0) {
            enableDisplay = true
            listener?.onDisplayEnable(this.measuredWidth, this.measuredHeight)

        }
    }


    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (!calcEnable) return

//        android.util.Log.i(TAG,"dispatchDraw")
//        android.util.Log.i(TAG,"Bitmap is null = " + (data == null))
//        android.util.Log.i(TAG,"DimensionPoint is null = " + (iconPoint == null))


        if (oneCellSize.width != -1f && oneCellSize.height != -1f) {
            for(rowId in 0 until row) {
                canvas.drawLine(
                    0f,
                    oneCellSize.height * rowId,
                    oneCellSize.width * column,
                    oneCellSize.height * rowId,
                    paint
                )
            }

            for(columnId in 0 until column) {
                canvas.drawLine(
                    oneCellSize.width * columnId,
                    0f,
                    oneCellSize.width * columnId,
                    oneCellSize.height * row,
                    paint
                )
            }
        }

        if (dragAnimation) {
            data?.let { image ->

                if (cellPointName != CELL_POINT_NAME.DOT) {

                    cellPoint?.let { cell ->
                        outlineImage?.let { img ->
                            val positionX = cell.column * oneCellSize.width
                            var positionY = cell.row * oneCellSize.height

                            if (cellPointName == CELL_POINT_NAME.DOCK) {
                                positionY = oneCellSize.height * row + dotHeight
                            }

                            val matrix = Matrix()
                            matrix.postTranslate(positionX, positionY)

                            //
                            android.util.Log.i(TAG, "配置予定位置の描画")
                            canvas.drawBitmap(img, matrix, paint)
                        }
                    }
                }

                iconPoint?.let { iconPoint ->
                    movePosition?.let { move ->
                        val positionX = iconPoint.x + move.x
                        val positionY = iconPoint.y + move.y - (image.height / 3)

                        val matrix = Matrix()
                        matrix.postTranslate(positionX, positionY)

                        //
                        android.util.Log.i(TAG, "アイコン描画")
                        canvas.drawBitmap(image, matrix, paint)
                    }
                }
            }
        }
    }

    private var onDownTime = -1L
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {

        // タッチ位置の取得と計算
        touchPoint(motionEvent)

        // Cell位置の計算
        selectCell(motionEvent)

        var clear = false
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                android.util.Log.i(TAG,"ACTION_DOWN")
                startTimer()
                onDownTime = System.currentTimeMillis()
                listener?.onTouchDown(DimensionPoint(motionEvent.x, motionEvent.y))
            }
            MotionEvent.ACTION_MOVE -> {

                // 移動を始めたのでタイマーはキャンセルする
                cancelTimer()

                if (data != null && checkMinMove()) {
                    android.util.Log.i(TAG,"ACTION_MOVE")
                    listener?.onTouchMove(DimensionPoint(motionEvent.x, motionEvent.y))
                }
                invalidate()

            }
            MotionEvent.ACTION_UP -> {
                android.util.Log.i(TAG,"ACTION_UP")
                // 手を離したのでタイマーはキャンセルする
                cancelTimer()

                val tapTime = System.currentTimeMillis() - onDownTime
                android.util.Log.i(TAG,"tapTime : " + tapTime)
                if (!checkMinMove() && ( 30 < tapTime && tapTime <= 130)) {
                    android.util.Log.i(TAG,"ACTION_CLICK")
                    listener?.onTouchClick(cellPointName, DimensionPoint(motionEvent.x, motionEvent.y))
                }

                if (data != null) {
                    listener?.onTouchUp(cellPointName, DimensionPoint(motionEvent.x, motionEvent.y))
                    clear = true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                android.util.Log.i(TAG,"ACTION_CANCEL")

                // 違う場所を触り始めたのでタイマーを停止する
                cancelTimer()
                clear = true
            }
        }

        if (data != null) {
            if (clear) {
                clearData()
            }
            return true
        }
        if (!clear && lowerDesktopView != null && cellPointName == CELL_POINT_NAME.DESKTOP) {
            lowerDesktopView!!.dispatchTouchEvent(motionEvent)
        } else if (!clear && lowerDesktopView != null && cellPointName == CELL_POINT_NAME.DOT) {
            lowerDesktopView!!.dispatchTouchEvent(motionEvent)
        } else if (!clear && lowerDockView != null && cellPointName == CELL_POINT_NAME.DOCK) {
            lowerDockView!!.dispatchTouchEvent(motionEvent)
        }

        return true
    }

    private fun clearData() {
        android.util.Log.i(TAG,"clearData")
        outlineImage = null
        data = null
        iconPoint = null
        movePosition = null
        invalidate()
    }

    data class DimensionPoint(val x: Float, val y: Float)

    var touchAction = MotionEvent.ACTION_CANCEL
    var downPosition: DimensionPoint? = null

    private fun touchPoint(motionEvent: MotionEvent) {
        touchAction = motionEvent.action
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                downPosition = DimensionPoint(motionEvent.x, motionEvent.y)
                movePoint(motionEvent)
            }
            MotionEvent.ACTION_MOVE -> {
                movePoint(motionEvent)
            }
            MotionEvent.ACTION_UP -> {
                movePoint(motionEvent)
            }
            MotionEvent.ACTION_CANCEL -> {

            }
        }
    }

    var movePosition: DimensionPoint? = null
    private fun movePoint(motionEvent: MotionEvent) {
        downPosition?.let {
            movePosition = DimensionPoint(motionEvent.x - it.x, motionEvent.y - it.y)
        }
    }

    private fun checkMinMove(): Boolean {
        if (movePosition == null) return false
        return checkMinMove(movePosition!!)
    }

    private fun checkMinMove(movePosition: DimensionPoint): Boolean {
        if (oneCellSize.width == -1f || oneCellSize.height == -1f) return false

        val border = oneCellSize.width / 3f
        val borderHeight = oneCellSize.height / 5f

        android.util.Log.i("TESTESTEST","movePosition.x" + movePosition!!.x)

        if (movePosition!!.x > border || movePosition!!.y > borderHeight
            || movePosition!!.x < -border || movePosition!!.y < -borderHeight) {
            return true
        }
        return false
    }


    private fun selectCell(motionEvent: MotionEvent) {
        if (oneCellSize.width == -1f || oneCellSize.height == -1f) return

        movePosition?.let {

            if (motionEvent.y < height - dotHeight - oneCellSize.height) {
                android.util.Log.i("CELL_TEST","selectCell >> DESKTOP")
                // 通常のページ
                val column = (motionEvent.x / oneCellSize.width).toInt()
                val row = (motionEvent.y / oneCellSize.height).toInt()

                cellPoint = GridPoint(row, column)
                cellPointName = CELL_POINT_NAME.DESKTOP

                listener?.onSelectGridPoint(cellPoint, cellPointName, motionEvent.action)

            } else if (motionEvent.y < height - oneCellSize.height) {
                android.util.Log.i("CELL_TEST","selectCell >> DOT")
                // ページドットの範囲なので何もしない
                cellPointName = CELL_POINT_NAME.DOT
                listener?.onSelectGridPoint(null, cellPointName, motionEvent.action)
            } else {
                android.util.Log.i("CELL_TEST","selectCell >> DOCK")
                // Dockの範囲
                val column = (motionEvent.x / oneCellSize.width).toInt()
                val row = 0

                cellPoint = GridPoint(row, column)
                cellPointName = CELL_POINT_NAME.DOCK

                listener?.onSelectGridPoint(cellPoint, cellPointName, motionEvent.action)
            }
        }
    }


    interface OnDragAndDropViewCallback {
        fun onTouchEvent(motionEvent: MotionEvent): Boolean
        fun onMeasure()
    }

    interface OnDragAndDropViewListener {
        fun onDisplayEnable(width: Int, height: Int)
        fun onTouchDown(point: DimensionPoint)
        fun onTouchMove(point: DimensionPoint)
        fun onTouchUp(cellPointName: CELL_POINT_NAME, point: DimensionPoint)
        fun onTouchClick(cellPointName: CELL_POINT_NAME, point: DimensionPoint)
        fun onLongTouchDown(cellPointName: CELL_POINT_NAME, point: DimensionPoint)
        fun onSelectGridPoint(gridPoint: GridPoint?, cellPointName: CELL_POINT_NAME, action: Int)
    }

    class Timer(private val time: Long, private val timeCallback: () -> Unit) {
        private fun delayed(
            time: Long,
            callback: () -> Unit
        ): Handler {
            val handler = Handler(Looper.getMainLooper())

            handler.postDelayed({
                callback.invoke()
            } , time)

            return handler
        }

        private var handler: Handler? = null
        fun start() {
            handler = delayed(time) {
                if (!isCancel) {
                    timeCallback.invoke()
                }
            }
        }

        var isCancel = false
        fun cancel() {
            isCancel = true
            if (handler != null) {
                handler!!.removeCallbacks {  }
                handler = null
            }
        }
    }

    var longClickCheck = false
    private var timer : Timer? = null
    private fun startTimer() {
        longClickCheck = true

        if (timer != null && !timer!!.isCancel) {
            timer!!.cancel()
            timer = null
        }

        timer = Timer(800) {
            if (longClickCheck) {
                android.util.Log.i(TAG,"LONG_TOUCH_DOWN downPosition not null = " + (downPosition == null))
                downPosition?.let {
                    listener?.onLongTouchDown(cellPointName, it)
                }
            } else {

            }

            longClickCheck = false
        }
        timer!!.start()
    }

    private fun cancelTimer() {
        longClickCheck = false
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }
}