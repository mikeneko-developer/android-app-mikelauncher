package net.mikemobile.mikelauncher.ui.home

import android.view.MotionEvent
import net.mikemobile.mikelauncher.constant.GridSize
import net.mikemobile.mikelauncher.ui.custom.DragAndDropView

class CellCalcCallback: DragAndDropView.OnDragAndDropViewCallback {

    companion object {
        const val TAG = "CellCalcCallback"
    }

    //
    var column: Int = -1
    var row: Int = -1
    var dotHeight = -1f
    var calcEnable = false
    var oneCellSize = GridSize(-1f,-1f)


    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                android.util.Log.i(TAG,"ACTION_DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                android.util.Log.i(TAG,"ACTION_UP")
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }

        return false
    }

    override fun onMeasure() {
        TODO("Not yet implemented")
    }
}