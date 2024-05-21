package net.mikemobile.mikelauncher.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.constant.GridCount
import net.mikemobile.mikelauncher.constant.GridSize
import net.mikemobile.mikelauncher.constant.GridPoint

class GridController(
    private val context: Context,
    private val page: Int,
    private val constraintLayout: ConstraintLayout,
) {

    private var column: Int = 1
    private var row: Int = 1


    private var listener: GridControllListener? = null
    fun setOnGridControllListener(l: GridControllListener) {
        listener = l
    }
    interface GridControllListener {
        fun onGridPositionView(view: LinearLayout, position: Int, row: Int, column: Int)
        fun onCellPositionView(view: LinearLayout, position: Int, row: Int, column: Int)
        fun onClickGrid(row: Int, column: Int)
        fun onLongClickGrid(view: View, row: Int, column: Int)
        fun onLongClickGridBlanc(row: Int, column: Int)
    }

    private var cellLayoutMemory = HashMap<String, LinearLayout>()

    private var cellSize: GridSize = GridSize(-1f, -1f)
    fun setCellSize(cellSize: GridSize) {
        this.cellSize = cellSize

    }

    @SuppressLint("ResourceType")
    fun setFrame(column: Int, row: Int) {
        this.column = column
        this.row = row

        cellLayoutMemory = HashMap<String, LinearLayout>()

        for(rowId in 0 until row) {

            // Column追加処理
            for(columnId in 0 until column) {
                val cellLayout = LinearLayout(context)

                val layoutParam = LinearLayout.LayoutParams(
                    cellSize.width.toInt(),
                    cellSize.height.toInt()
                )

                cellLayout.layoutParams = layoutParam
                if (rowId % 2 == 0) {
                    if (columnId % 2 == 0) {
                        cellLayout.setBackgroundColor(Color.BLACK)
                    } else {
                        cellLayout.setBackgroundColor(Color.BLUE)
                    }
                } else {
                    if (columnId % 2 == 0) {
                        cellLayout.setBackgroundColor(Color.CYAN)
                    } else {
                        cellLayout.setBackgroundColor(Color.YELLOW)
                    }
                }
                cellLayout.setBackgroundResource(R.drawable.grid_frame)

                cellLayout.translationX = columnId * cellSize.width
                cellLayout.translationY = rowId * cellSize.height

                listener?.onCellPositionView(cellLayout, page, rowId, columnId)

                constraintLayout.addView(cellLayout)

                cellLayoutMemory["$rowId-$columnId"] = cellLayout
            }
        }
    }


    fun addGrid(view: View, gridCount: GridCount, row: Int, column: Int): View? {

        var prevView: View? = null

        cellLayoutMemory["$row-$column"]?.let {
            if (it.childCount > 0) {
                it.removeAllViews()
            }

            view.getParent()?.let {
                val parent = it as ViewGroup
                parent.removeAllViews()
            }

            it.removeAllViews()
            it.addView(view)

            it.layoutParams.width = (cellSize.width * (gridCount.columnCount)).toInt()
            it.layoutParams.height = (cellSize.height * (gridCount.rowCount)).toInt()

        }

        return prevView
    }

    fun updateGrid(view: View, gridCount: GridCount, row: Int, column: Int): View? {
        if (!cellLayoutMemory.containsKey("$row-$column")) {
            return addGrid(view, gridCount, row, column)
        }

        var prevView: View? = null

        cellLayoutMemory["$row-$column"]?.let {
            if (it.childCount > 0) {
                prevView = it.getChildAt(0)
            }

            view.getParent()?.let {
                val parent = it as ViewGroup
                parent.removeAllViews()
            }

            it.removeAllViews()
            it.addView(view)

            it.layoutParams.width = (cellSize.width * (gridCount.columnCount)).toInt()
            it.layoutParams.height = (cellSize.height * (gridCount.rowCount)).toInt()

        }

        return prevView
    }

    fun reloadGrid(row: Int, column: Int) {

        if (!cellLayoutMemory.containsKey("$row-$column")) {
            return
        }

        cellLayoutMemory["$row-$column"]?.let {
            it.invalidate()
        }
    }

    fun clearGrid(row: Int, column: Int) {
        if (!cellLayoutMemory.containsKey("$row-$column")) {
            return
        }

        cellLayoutMemory["$row-$column"]?.let {
            if (it.childCount > 0) {
                it.removeAllViews()
            }
        }
    }


    fun updateGridFrame(row: Int, column: Int, select: Boolean) {
        if (!cellLayoutMemory.containsKey("$row-$column")) {
            return
        }

        cellLayoutMemory["$row-$column"]?.let {
            if (select) {
                it.setBackgroundResource(R.drawable.grid_frame_select)
            } else {
                it.setBackgroundResource(R.drawable.grid_frame)
            }
        }
    }

    fun getGridInlineView(row: Int, column: Int): View? {

        if (!cellLayoutMemory.containsKey("$row-$column")) {
            return null
        }

        cellLayoutMemory["$row-$column"]?.let {
            if (it.childCount > 0) {
                return it.getChildAt(0)
            }
        }

        return null
    }


}