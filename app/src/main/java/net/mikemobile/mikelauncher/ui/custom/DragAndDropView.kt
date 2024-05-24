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
import net.mikemobile.mikelauncher.constant.DimenPoint
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.constant.GridPoint
import net.mikemobile.mikelauncher.constant.GridScrollType
import net.mikemobile.mikelauncher.constant.ViewSize
import net.mikemobile.mikelauncher.util.processImageToFill

class DragAndDropView: ConstraintLayout {
    private val DEBUG_MODE = true
    private val MASK_INVISIBLE = true

    private val TAG = "DragAndDropView"

    private val paint = Paint()
    private val iconPaint = Paint()

    private val touchPaint = Paint()
    private val iconStartingPaint = Paint()

    var touchPoint: DimenPoint? = null

    constructor(context: Context): super(context){
        setPaintData()
    }
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs){
        setPaintData()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        setPaintData()
    }

    private fun setPaintData() {
        paint.color = Color.DKGRAY
        iconPaint.color = Color.DKGRAY
        iconPaint.alpha = 150

        touchPaint.color = Color.parseColor("#AA0066FF")
    }

    private var listener: OnDragAndDropViewListener? = null
    fun setOnDragAndDropViewListener(l: OnDragAndDropViewListener) {
        listener = l
    }

    //
    var column: Int = -1
    var row: Int = -1
    private var dotHeight = -1
    var calcEnable = false
    var oneCellSize = ViewSize(-1f,-1f)

    //
    var cellPointName: CELL_POINT_NAME = CELL_POINT_NAME.NONE
    var cellPoint: GridPoint? = null

    var outlineImage: Bitmap? = null

    var enableDisplay = false

    var onTouchEventDisable = false


    private var onDownTime = -1L

    // タッチイベント判定用
    private var onDownEnabled = false
    private var onLongTouchEnabled = false
    private var onTouchMoveEnable = false


    private var differenceDimenPoint = DimenPoint(0f, 0f)

    fun setDisableTouchEvent() {
        onTouchEventDisable = true
    }
    fun setEnableTouchEvent() {
        onTouchEventDisable = false
    }
    fun getEnableTouchEventFlag(): Boolean {
        return onTouchEventDisable
    }

    fun setSplitData(row: Int, column: Int) {
        this.row = row
        this.column = column
    }

    fun setDotHeight(dotHeight: Int) {
        this.dotHeight = dotHeight
    }

    fun setGridSize(oneCellSize: ViewSize) {
        this.oneCellSize = oneCellSize
        calcEnable = true
    }




    var data: Bitmap? = null
    var iconPoint: DimenPoint? = null
    fun setDragImage(data: Bitmap?, point: DimenPoint?) {
        android.util.Log.i(TAG,"setDragImage")
        android.util.Log.i(TAG,"Bitmap is null = " + (data == null))
        android.util.Log.i(TAG,"DimenPoint is null = " + (point == null))
        this.data = data
        this.iconPoint = point

        downPosition?.let {
            listener?.onTouchMove(cellPointName, DimenPoint(it.x, it.y))
        }

        data?.let {
            //outlineImage = processImageToOutline(data)
            outlineImage = processImageToFill(data)
        }

        updateView()
    }

    fun getPoint(): DimenPoint? {
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

    fun getCellSize(): ViewSize {
        return oneCellSize
    }


    private var dragAnimation = false
    fun setDragAnimationEnable() {
        dragAnimation = true
    }

    fun setDragAnimationDisable() {
        dragAnimation = false
    }

    fun getDragAnimationFlag(): Boolean {
        return dragAnimation
    }

    fun differenceTouch(differenceDimenPoint: DimenPoint) {
        this.differenceDimenPoint = differenceDimenPoint
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

        if (
            DEBUG_MODE
            //&& (onDownEnabled || onTouchMoveEnable)
        ) {
            drawTouchPoint(canvas)

//            if (oneCellSize.width != -1f && oneCellSize.height != -1f) {
//                for (rowId in 0 until row) {
//                    canvas.drawLine(
//                        0f,
//                        oneCellSize.height * rowId,
//                        oneCellSize.width * column,
//                        oneCellSize.height * rowId,
//                        paint
//                    )
//                }
//
//                for (columnId in 0 until column) {
//                    canvas.drawLine(
//                        oneCellSize.width * columnId,
//                        0f,
//                        oneCellSize.width * columnId,
//                        oneCellSize.height * row,
//                        paint
//                    )
//                }
//            }
        }

        if (!calcEnable) return


        if (onTouchEventDisable) {
            android.util.Log.i(TAG, "タッチ無効")
        } else if (dragAnimation) {

            drawShadowGridPoint(canvas)
            drawIcon(canvas)
        }
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        if (onTouchEventDisable) {
            return super.onTouchEvent(motionEvent)
        }
        android.util.Log.i(TAG,"onTouchEvent")

        // タッチ位置判定用
        touchPointEvent(motionEvent)

        // タッチ位置の取得と計算
        movePointEvent(motionEvent)

        // スクロール判定
        android.util.Log.i(TAG,"onTouchEvent dragAnimation:" + dragAnimation)
        if (dragAnimation && checkScrollActninEvent(motionEvent)) {
            return true
        }

        // Cell位置の計算
        selectCell(motionEvent)

        var clear = false
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                android.util.Log.i(TAG,"ACTION_DOWN")
                onDownEnabled = true
                onTouchMoveEnable = false

                startTimer()
                onDownTime = System.currentTimeMillis()
                listener?.onTouchDown(cellPointName, DimenPoint(motionEvent.x, motionEvent.y))
            }
            MotionEvent.ACTION_MOVE -> {
                if (onDownEnabled && checkMinMove()) {
                    android.util.Log.d("MINMOVETEST","checkMinMove over")
                    // 移動を始めたのでタイマーはキャンセルする
                    cancelTimer()
                    startFloatAnimation()
                    onDownEnabled = false
                    onTouchMoveEnable = true

                } else if (onDownEnabled){
                    android.util.Log.i("MINMOVETEST","TEST ACTION_MOVE")

                }

                if (onTouchMoveEnable) {
                    android.util.Log.i(TAG,"ACTION_MOVE")
                    listener?.onTouchMove(cellPointName, DimenPoint(motionEvent.x, motionEvent.y))
                    updateView()
                }

            }
            MotionEvent.ACTION_UP -> {
                android.util.Log.i(TAG,"ACTION_UP")
                onDownEnabled = false
                onLongTouchEnabled = false
                onTouchMoveEnable = false

                // 手を離したのでタイマーはキャンセルする
                cancelTimer()
                stopFloatAnimation()

                // クリック判定イベント
                onTouchClickEvent(motionEvent)

                // 手を離した判定イベント
                onTouchUpEvent()

                // データのクリア判定処理
                clear = (data != null)

            }
            MotionEvent.ACTION_CANCEL -> {
                android.util.Log.i(TAG,"ACTION_CANCEL")
                onDownEnabled = false
                onLongTouchEnabled = false
                onTouchMoveEnable = false

                // 違う場所を触り始めたのでタイマーを停止する
                cancelTimer()
                stopFloatAnimation()
                clear = true
            }
        }

        if (data != null) {
            if (clear) {
                clearData()
            }
            return true
        }

        if (motionEvent.action == MotionEvent.ACTION_DOWN) {

        } else if (onDownEnabled) {
            return true
        } else if (onLongTouchEnabled) {
            return true
        }

        if (!clear && lowerDesktopView != null && cellPointName == CELL_POINT_NAME.DESKTOP) {
            android.util.Log.d("MINMOVETEST","desktopに値を渡す")
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
        updateView()
    }

    var touchAction = MotionEvent.ACTION_CANCEL
    var downPosition: DimenPoint? = null
    var movePosition: DimenPoint? = null

    /**
     * タッチ箇所を保存・保持する
     */
    private fun touchPointEvent(motionEvent: MotionEvent) {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                touchPoint = DimenPoint(motionEvent.x, motionEvent.y)
                updateView()
            }
            MotionEvent.ACTION_MOVE -> {
                touchPoint = DimenPoint(motionEvent.x, motionEvent.y)
                updateView()

            }
            MotionEvent.ACTION_UP -> {
                touchPoint = null
                updateView()
            }
            MotionEvent.ACTION_CANCEL -> {
                touchPoint = null
                updateView()
            }
            else -> {
                touchPoint = null
                updateView()
            }
        }
    }

    private fun movePointEvent(motionEvent: MotionEvent) {
        touchAction = motionEvent.action
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                downPosition = DimenPoint(motionEvent.x, motionEvent.y)
                calcMovePoint(motionEvent)
            }
            MotionEvent.ACTION_MOVE -> {
                calcMovePoint(motionEvent)
            }
            MotionEvent.ACTION_UP -> {
                calcMovePoint(motionEvent)
            }
            MotionEvent.ACTION_CANCEL -> {}
        }
    }

    private fun calcMovePoint(motionEvent: MotionEvent) {
        downPosition?.let {
            movePosition = DimenPoint(motionEvent.x - it.x, motionEvent.y - it.y)
        }
    }

    private fun checkMinMove(): Boolean {
        if (movePosition == null) return false
        return checkMinMove(movePosition!!)
    }

    private fun checkMinMove(movePosition: DimenPoint): Boolean {
        if (oneCellSize.width == -1f || oneCellSize.height == -1f) return false

        val border = oneCellSize.width / 2f
        val borderHeight = oneCellSize.height / 3f

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
                val column = ((motionEvent.x + differenceDimenPoint.x) / oneCellSize.width).toInt()
                val row = ((motionEvent.y  + differenceDimenPoint.y) / oneCellSize.height).toInt()

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

    var leftScroll = false
    var rightScroll = false
    fun setScrollEventReset() {
        leftScroll = false
        rightScroll = false
        cancelOneWeitTimer()
    }
    private fun checkScrollActninEvent(motionEvent: MotionEvent): Boolean {
        if (oneCellSize.width == -1f || oneCellSize.height == -1f) return false
        val touchPointData = touchPoint?: return false
        android.util.Log.i(TAG + "_SCROLL_ACTION_EVENT","checkScrollActninEvent >>>>>>>>>>>>>>>")

        val touchLeftBorder = oneCellSize.width / 3
        val touchRightBorder = width - oneCellSize.width / 3

        when (motionEvent.action) {
            MotionEvent.ACTION_MOVE -> {
                android.util.Log.i(TAG + "_SCROLL_ACTION_EVENT","checkScrollActninEvent touchRightBorder:" + touchRightBorder + " / " + touchPointData.x)
                if (touchPointData.x < touchLeftBorder) {
                    if (!leftScroll){
                        leftScroll = true
                        oneWeitTimer(500) {
                            listener?.onScrollEvent(cellPointName, GridScrollType.LEFT)
                        }
                    }
                    return true
                } else if (touchRightBorder < touchPointData.x) {
                    if (!rightScroll) {
                        rightScroll = true
                        oneWeitTimer(500) {
                            listener?.onScrollEvent(cellPointName, GridScrollType.RIGHT)
                        }
                    }
                    return true
                } else {
                    setScrollEventReset()
                }
            }
            else -> {
                setScrollEventReset()
            }
        }

        return false
    }
    //////////////////////////////////////////////////////////////////////

    interface OnDragAndDropViewCallback {
        fun onTouchEvent(motionEvent: MotionEvent): Boolean
        fun onMeasure()
    }

    interface OnDragAndDropViewListener {
        fun onDisplayEnable(width: Int, height: Int)
        fun onTouchDown(cellPointName: CELL_POINT_NAME, point: DimenPoint)
        fun onTouchMove(cellPointName: CELL_POINT_NAME, point: DimenPoint)
        fun onTouchUp(cellPointName: CELL_POINT_NAME, point: DimenPoint)
        fun onTouchClick(cellPointName: CELL_POINT_NAME, point: DimenPoint)
        fun onLongTouchDown(cellPointName: CELL_POINT_NAME, point: DimenPoint)
        fun onSelectGridPoint(gridPoint: GridPoint?, cellPointName: CELL_POINT_NAME, action: Int)
        fun onScrollEvent(cellPointName: CELL_POINT_NAME, scrollType: GridScrollType)
    }

    //////////////////////////////////////////////////////////////////////
    private var oneTimer : Timer? = null
    private fun oneWeitTimer(time: Long, callback:() -> Unit) {
        android.util.Log.i(TAG,"oneWeitTimer")
        cancelOneWeitTimer()
        oneTimer = Timer(time) {
            callback.invoke()
            cancelOneWeitTimer()
        }
        oneTimer!!.start()
    }

    private fun cancelOneWeitTimer() {
        android.util.Log.i(TAG,"cancelOneWeitTimer")
        if (oneTimer != null) {
            oneTimer!!.cancel()
            oneTimer = null
        }
    }

    //////////////////////////////////////////////////////////////////////
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
        android.util.Log.i(TAG,"startTimer")
        longClickCheck = true

        if (timer != null && !timer!!.isCancel) {
            timer!!.cancel()
            timer = null
        }

        timer = Timer(600) {
            if (longClickCheck) {
                android.util.Log.i(TAG,"LONG_TOUCH_DOWN downPosition not null = " + (downPosition == null))
                downPosition?.let {
                    onLongTouchEnabled = true
                    listener?.onLongTouchDown(cellPointName, it)


                }
            } else {

            }

            longClickCheck = false
        }
        timer!!.start()
    }

    private fun cancelTimer() {
        android.util.Log.i(TAG,"cancelTimer")
        longClickCheck = false
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    //////////////////////////////////////////////////////////////////////
    private fun updateView() {
        if (!animation) return

        invalidate()
    }

    var animation = false
    private var animationTimer : Timer? = null
    private fun startFloatAnimation() {
        if (animation) return

        stopFloatAnimation()
        animation = true

        floatScale = 1.1f
        count = 0
        animationDraw()
    }

    var floatScale = 1.1f
    var scaleUpDown = 0.005f
    var maxCount = 40
    val floatLoopTime = 20L
    var count = 0
    private fun animationDraw() {
        animationTimer = Timer(floatLoopTime) {
            if (count < (maxCount / 2)) {
                floatScale = floatScale * (1f + scaleUpDown)
            } else {
                floatScale = floatScale * (1f - scaleUpDown)
            }

            if (floatScale < 1.1f) {
                floatScale = 1.1f
            }

            count++
            if (count >= maxCount) count = 0

            invalidate()

            if (animation) animationDraw()
        }
        animationTimer!!.start()
    }

    private fun stopFloatAnimation() {
        animation = false
        if (animationTimer != null) {
            animationTimer!!.cancel()
            animationTimer = null
        }
    }

    //////////////////////////////////////////////////////////////////////
    // DragAndDropで描画する画像に関するデータ
    private var imageStartPoint: DimenPoint? = null

    fun setImageStartPoint(startPoint: DimenPoint) {
        imageStartPoint = startPoint
    }

    /**
     * タッチポイント表示
     */
    private fun drawTouchPoint(canvas: Canvas) {
        touchPoint?.let { move ->

            if (onTouchMoveEnable) {
                touchPaint.color = Color.YELLOW
            } else if (onLongTouchEnabled) {
                touchPaint.color = Color.MAGENTA
            } else if (onDownEnabled) {
                touchPaint.color = Color.GREEN
            } else {
                touchPaint.color = Color.CYAN
            }

            val positionX = move.x
            val positionY = move.y

            //
            canvas.drawCircle(positionX, positionY, 130f, touchPaint)
        }
    }
    /**
     * DragAndDropで描画するデータ
     */
    private fun drawIcon(canvas: Canvas) {
        val image = data?: return
        val move = movePosition?: return
        val startPoint = imageStartPoint?: return
        val tPoint = touchPoint?: return


        val scale = floatScale

        val scaleChangeWidth = ((image.width * scale) - image.width) / 2
        val scaleChangeHeight = ((image.height * scale) - image.height) / 2

        val positionX = -scaleChangeWidth + startPoint.x + move.x
        var positionY = -scaleChangeHeight + startPoint.y + move.y


        val dotToBottomBorder = Global.ROW_COUNT * oneCellSize.height + dotHeight
        if (dotToBottomBorder <= tPoint.y) {
            positionY += dotHeight
        }


        val matrix = Matrix()
        matrix.setScale(scale, scale)

        matrix.postTranslate(positionX, positionY)

        //
        android.util.Log.i(TAG, "アイコン描画")
        canvas.drawBitmap(image, matrix, iconPaint)

    }
    /**
     * DragAndDropで描画するデータ
     */
    private fun drawShadowGridPoint(canvas: Canvas) {
        android.util.Log.i(TAG, "drawShadowGridPoint　画像の判定")
        val image = data?: return
        val tPoint = touchPoint?: return

        android.util.Log.i(TAG, "drawShadowGridPoint　影画像の判定")
        val shadowImage = outlineImage?: return

        android.util.Log.i(TAG, "drawShadowGridPoint　移動範囲の判定")
        val move = movePosition?: return

        android.util.Log.i(TAG, "drawShadowGridPoint　画像の最初の位置データの判定")
        val startPoint = imageStartPoint?: return

        val positionX = startPoint.x + move.x + (oneCellSize.width / 2)
        val positionY = startPoint.y + move.y + (oneCellSize.height / 2)

        val column = (positionX / oneCellSize.width).toInt()
        val row = (positionY / oneCellSize.height).toInt()

        val cellPositionX = column * oneCellSize.width
        var cellPositionY = row * oneCellSize.height


        val dotToBottomBorder = Global.ROW_COUNT * oneCellSize.height + dotHeight
        if (dotToBottomBorder <= tPoint.y) {
            cellPositionY += dotHeight
        } else if (Global.ROW_COUNT * oneCellSize.height <= tPoint.y) {
            return
        }

        val matrix = Matrix()
        matrix.postTranslate(cellPositionX, cellPositionY)

        android.util.Log.i(TAG, "影の描画")
        canvas.drawBitmap(shadowImage, matrix, iconPaint)

    }

    /**
     * 手を離した時にListenerを返すメソッド
     */
    private fun onTouchUpEvent() {
        android.util.Log.i(TAG + "MINMOVETEST", "drawShadowGridPoint　画像の判定")
        val image = data?: return

        android.util.Log.i(TAG, "drawShadowGridPoint　移動範囲の判定")
        val move = movePosition?: return

        android.util.Log.i(TAG, "drawShadowGridPoint　画像の最初の位置データの判定")
        val startPoint = imageStartPoint?: return

        val positionX = startPoint.x + move.x + (oneCellSize.width / 2)
        val positionY = startPoint.y + move.y + (oneCellSize.height / 2)

        val column = (positionX / oneCellSize.width).toInt()
        val row = (positionY / oneCellSize.height).toInt()

        val cellPositionX = column * oneCellSize.width
        var cellPositionY = row * oneCellSize.height

        listener?.onTouchUp(cellPointName, DimenPoint(positionX, positionY))
    }

    /**
     * 手を離した時にクリックイベント判定をし、Listenerを返すメソッド
     */
    private fun onTouchClickEvent(motionEvent: MotionEvent) {

        val tapTime = System.currentTimeMillis() - onDownTime
        android.util.Log.i(TAG,"tapTime : " + tapTime)

        val dimenPoint = DimenPoint(motionEvent.x + differenceDimenPoint.x, motionEvent.y + differenceDimenPoint.y)
        if (!checkMinMove() && ( 30 < tapTime && tapTime <= 130)) {
            android.util.Log.i(TAG,"ACTION_CLICK")
            listener?.onTouchClick(cellPointName, dimenPoint)
        }
    }
}