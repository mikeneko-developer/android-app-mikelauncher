package net.mikemobile.mikelauncher.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.constant.CELL_POINT_NAME
import net.mikemobile.mikelauncher.constant.DimenPoint
import net.mikemobile.mikelauncher.constant.GridSize
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.constant.GridCount
import net.mikemobile.mikelauncher.constant.GridPoint
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.util.getViewCapture

class GridViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
interface OnGridAdapterListener {

    fun onGridPositionView(viewType: CELL_POINT_NAME, view: LinearLayout, position: Int, row: Int, column: Int)
    fun onCellPositionView(viewType: CELL_POINT_NAME, view: LinearLayout, position: Int, row: Int, column: Int)
    fun onClickOpenApp(viewType: CELL_POINT_NAME, page: Int, row: Int, column: Int)
    fun onLongClickToEvent(view: View, bitmap: Bitmap, positionX: Float, positionY: Float)
    fun onLongClickToBlanc(row: Int, column: Int)
}

class GridAdapter(
    private val context: Context,
    private val viewPager: ViewPager2,
    private val viewType: CELL_POINT_NAME,
    private val startPageSize: Int = 5,
    private val rowCount: Int = Global.ROW_COUNT,
    private val columnCount: Int = Global.COLUMN_COUNT,
    private val listener: OnGridAdapterListener
): RecyclerView.Adapter<GridViewHolder>(), GridController.GridControllListener {

    private val TAG = "GridAdapter"
    private var pageSize = startPageSize

    private val screenWidth = context.resources.displayMetrics.widthPixels
    private val imageWidth = screenWidth / 2

    private val mViewHolderMap = SparseArray<RecyclerView.ViewHolder>()

    private var page: Int = 0
    private var enableViewSize = false
    private var cellSize: GridSize = GridSize(-1f, -1f)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.page, parent, false)
        return GridViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {

        val constraintLayout = holder.itemView.findViewById(R.id.constraint_grid_frame) as ConstraintLayout

        var page: GridController? = null
        if (constraintLayout?.tag == null) {
            page = GridController(context, position, constraintLayout)
            page.setOnGridControllListener(this)
        } else {
            page = constraintLayout?.tag as GridController
        }

        if (cellSize.width != -1f && cellSize.height != -1f) {
            page.setCellSize(cellSize)
            page.setFrame(columnCount, rowCount)
        }

        constraintLayout.tag = page

    }

    override fun getItemCount(): Int {
        if (!enableViewSize) return 0
        return pageSize
    }

    fun setCellSize(cellSize: GridSize) {
        this.cellSize = cellSize
        enableViewSize = true

        this.notifyDataSetChanged()
    }


    fun addGrid(view: View, gridCount: GridCount, item: HomeItem) {
        getPositionLayoutGridController(page)?.let {page ->
            page.updateGrid(view, gridCount, item.row, item.column)
        }
    }

    fun removePageItem(position: Int, row: Int, column: Int) {
        getPositionLayoutGridController(page)?.let {page ->
            page.clearGrid(row, column)
        }
    }

    fun dragAndDropSelect(row: Int, column: Int, select: Boolean) {
        getPositionLayoutGridController(page)?.let {page ->
            page.updateGridFrame(row, column, select)
        }
    }

    private fun getPositionLayoutGridController(position: Int): GridController? {
        val recyclerView = viewPager.getChildAt(0) as RecyclerView
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        if (viewHolder is GridViewHolder) {
            val myViewHolder: GridViewHolder? = viewHolder as GridViewHolder?
            myViewHolder?.let {
                val layout = it.itemView.findViewById(R.id.constraint_grid_frame) as ConstraintLayout

                layout.tag?.let {
                    val page = it as GridController

                    return page
                }
            }
        }

        return null
    }

    override fun onGridPositionView(view: LinearLayout, position: Int, row: Int, column: Int) {
        listener.onGridPositionView(viewType, view, position, row, column)
    }

    override fun onCellPositionView(view: LinearLayout, position: Int, row: Int, column: Int) {
        listener.onCellPositionView(viewType, view, position, row, column)



    }

    override fun onClickGrid(row: Int, column: Int) {
        if (isLongClick) {
            isLongClick = false
            return
        }

        listener.onClickOpenApp(viewType, page, row, column)
    }

    private var isLongClick = false

    override fun onLongClickGrid(view: View, row: Int, column: Int) {
        isLongClick = true

        val point = DimenPoint(view.x, view.y)

        val lastPoint = checkDisplayToPosition(view, point)
        val positionX = lastPoint.x
        val positionY = lastPoint.y

        android.util.Log.i(TAG,"positionX:" + positionX)
        android.util.Log.i(TAG,"positionY:" + positionY)


        val bitmap = getViewCapture(view)

        bitmap?.let {
            android.util.Log.i(TAG,"画像を生成しました")
            listener.onLongClickToEvent(view, bitmap, positionX, positionY)
        }
    }

    override fun onLongClickGridBlanc(row: Int, column: Int) {
        //listener.onLongClickToBlanc(row, column)
    }

    fun getGridPoint(newPoint: DimenPoint): GridPoint {
        val oneWidth = viewPager.width / columnCount
        val oneHeight = viewPager.height / rowCount

        val column = (newPoint.x / oneWidth).toInt()
        val row = (newPoint.y / oneHeight).toInt()

        return GridPoint(row, column)
    }

    fun selectItem(view: View, gridCount: GridCount, row: Int, column: Int, moveItem: Boolean) {

        if (moveItem) {
            // 最終的に移動した場所を始点に、残りの位置を更新する

            getPositionLayoutGridController(page)?.let {gridPage ->
                moveGird(gridPage, gridCount, view, row, column)
            }

        } else {
            getPositionLayoutGridController(page)?.let {gridPage ->
                try {
                    gridPage.updateGrid(view, gridCount, row, column)
                }catch(e: Exception) {
                    android.util.Log.e(TAG,"e:" + e.toString())
                }
            }
        }
    }

    /**
     * 一箇所の移動に対して、一つずつずらして移動するロジック
     */
    private fun moveGird(gridPage: GridController,
                         gridCount: GridCount,
                         view: View,
                         row: Int, column: Int) {

        val nextView = gridPage.updateGrid(view, gridCount, row, column)

        if (nextView != null) {
            var newRow = row
            var newColumn = column + 1

            if (newColumn >= columnCount) {
                newColumn = 0
                newRow = row + 1
            }

            if (row < rowCount) {
                moveGird(gridPage, gridCount, nextView, newRow, newColumn)
            }
        }
    }



    fun changeGrid(view: View, gridCount: GridCount,item: HomeItem) {
        getPositionLayoutGridController(page)?.let {gridPage ->
            gridPage.updateGridFrame(item.row, item.column, false)
            gridPage.updateGrid(view, gridCount, item.row, item.column)
        }
    }



    fun setPage(page: Int) {
        this.page = page
    }

    fun setEnableClick() {
        isLongClick = false
    }

    private var prevPoint: GridPoint? = null


    fun updateGridPage(gridPoint: GridPoint) {

        val column = gridPoint.column
        val row = gridPoint.row

        getPositionLayoutGridController(page)?.let {page ->
            //page.updateGridFrame(row, column, true)
        }
    }

    fun getGridView(row: Int, column: Int): View? {
        getPositionLayoutGridController(page)?.let {gridPage ->
            return gridPage.getGridInlineView(row, column)
        }

        return null
    }



    fun touchEventClear() {
        isLongClick = false
    }
}