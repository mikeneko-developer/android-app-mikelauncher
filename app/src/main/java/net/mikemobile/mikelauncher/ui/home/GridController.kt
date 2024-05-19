package net.mikemobile.mikelauncher.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import net.mikemobile.mikelauncher.R

class GridController(private val context: Context, private val position: Int, private val view: LinearLayout) {

    private var column: Int = 1
    private var row: Int = 1



    private var listener: GridControllListener? = null
    fun setOnGridControllListener(l: GridControllListener) {
        listener = l
    }
    interface GridControllListener {
        fun onGridPositionView(view: LinearLayout, position: Int, row: Int, column: Int)
        fun onClickGrid(row: Int, column: Int)
        fun onLongClickGrid(view: View, row: Int, column: Int)
        fun onLongClickGridBlanc(row: Int, column: Int)
    }

    private var layoutMemory = HashMap<String, LinearLayout>()

    @SuppressLint("ResourceType")
    fun setFrame(column: Int, row: Int) {
        this.column = column
        this.row = row

        layoutMemory = HashMap<String, LinearLayout>()

        // Row追加処理
        for(rowId in 0 until row) {
            val rowLayout = LinearLayout(context)

            val layoutParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )

            rowLayout.layoutParams = layoutParam
            rowLayout.orientation = LinearLayout.HORIZONTAL

            // Column追加処理
            for(columnId in 0 until column) {
                val columnLayout = LinearLayout(context)

                val layoutParam = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )

                columnLayout.layoutParams = layoutParam
                if (rowId % 2 == 0) {
                    if (columnId % 2 == 0) {
                        columnLayout.setBackgroundColor(Color.BLACK)
                    } else {
                        columnLayout.setBackgroundColor(Color.BLUE)
                    }
                } else {
                    if (columnId % 2 == 0) {
                        columnLayout.setBackgroundColor(Color.CYAN)
                    } else {
                        columnLayout.setBackgroundColor(Color.YELLOW)
                    }
                }
                columnLayout.setBackgroundResource(R.drawable.grid_frame)

                columnLayout.setOnClickListener {
                    listener?.onClickGrid(rowId, columnId)
                }

//                columnLayout.setOnLongClickListener {
//                    val parent = it as ViewGroup
//                    val childCount = parent.childCount
//                    if (childCount > 0) {
//                        //listener?.onLongClickGrid(parent.getChildAt(0), rowId, columnId)
//                        //parent.removeAllViews()
//                    } else {
//                        //listener?.onLongClickGridBlanc(rowId, columnId)
//                    }
//                    false
//                }

                listener?.onGridPositionView(columnLayout, position, rowId, columnId)

                rowLayout.addView(columnLayout)

                layoutMemory["$rowId-$columnId"] = columnLayout
            }

            view.addView(rowLayout)
        }
    }

    fun updateGrid(view: View, row: Int, column: Int): View? {
        if (!layoutMemory.containsKey("$row-$column")) {
            return null
        }

        var prevView: View? = null

        layoutMemory["$row-$column"]?.let {
            if (it.childCount > 0) {
                prevView = it.getChildAt(0)
            }

            view.getParent()?.let {
                val parent = it as ViewGroup
                parent.removeAllViews()
            }

            it.removeAllViews()
            it.addView(view)
        }

        return prevView
    }

    fun reloadGrid(row: Int, column: Int) {
        if (!layoutMemory.containsKey("$row-$column")) {
            return
        }

        layoutMemory["$row-$column"]?.let {
            it.invalidate()
        }
    }

    fun clearGrid(row: Int, column: Int) {
        if (!layoutMemory.containsKey("$row-$column")) {
            return
        }

        layoutMemory["$row-$column"]?.let {
            if (it.childCount > 0) {
                it.removeAllViews()
            }
        }

    }


    fun updateGridFrame(row: Int, column: Int, select: Boolean) {
        if (!layoutMemory.containsKey("$row-$column")) {
            return
        }

        layoutMemory["$row-$column"]?.let {
            if (select) {
                it.setBackgroundResource(R.drawable.grid_frame_select)
            } else {
                it.setBackgroundResource(R.drawable.grid_frame)
            }
        }
    }

    fun getGridInlineView(row: Int, column: Int): View? {
        if (!layoutMemory.containsKey("$row-$column")) {
            return null
        }

        layoutMemory["$row-$column"]?.let {
            if (it.childCount > 0) {
                return it.getChildAt(0)
            }
        }

        return null
    }

}