package net.mikemobile.mikelauncher.ui.home

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import net.mikemobile.mikelauncher.MainActivity
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.constant.CELL_POINT_NAME
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.constant.GridPoint
import net.mikemobile.mikelauncher.constant.HomeItemType
import net.mikemobile.mikelauncher.constant.ITEM_MOVE
import net.mikemobile.mikelauncher.data.AppPreference
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.ui.custom.DragAndDropView
import net.mikemobile.mikelauncher.ui.custom.OverlayMenuView
import net.mikemobile.mikelauncher.ui.custom_float.AppMenuFloatDialog
import net.mikemobile.mikelauncher.ui.custom_float.EditTitleDialog
import net.mikemobile.mikelauncher.ui.custom_float.FolderFloatDialog
import net.mikemobile.mikelauncher.ui.custom_float.ToolItemListFloatDialog
import net.mikemobile.mikelauncher.ui.dialog.MenuDialog


class HomeFragment : Fragment(),
    DragAndDropView.OnDragAndDropViewListener, OnGridAdapterListener,
    OverlayMenuView.OverlayMenuViewListener {

    companion object {
        const val TAG = "HomeFragment"
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var desktopAdapter: GridAdapter
    private lateinit var dockAdapter: GridAdapter

    private var gridPage = 0

    private lateinit var pref: AppPreference

    // スライドアニメーション制御用
    private val ANIMATION_MODE_SLIDE = 1
    private val ANIMATION_MODE_SLIDE_SCALE_DOWN = 2
    private var ANIMATION_MODE = ANIMATION_MODE_SLIDE



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        enableViewCheck(view)
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        pref = AppPreference(requireContext())

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        setupWidget()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        onResultWidget(requestCode, resultCode, data)

    }


    private var overlayMenuView: OverlayMenuView? = null
    private var overlayMenuView2: OverlayMenuView? = null
    private var dragAndDropView: DragAndDropView? = null
    private var viewPager: ViewPager2? = null
    private var dockViewPager: ViewPager2? = null

    private var dotFrame: ConstraintLayout? = null

    private fun setViews(view: View) {
        android.util.Log.i(TAG,"setViews")
        dotFrame = view.findViewById<ConstraintLayout>(R.id.page_dot_frame)

        dragAndDropView = view.findViewById(R.id.dragAndDropView) as DragAndDropView
        dragAndDropView?.setSplitData(Global.ROW_COUNT, Global.COLUMN_COUNT)
        dragAndDropView?.setOnDragAndDropViewListener(this)

        overlayMenuView = view.findViewById(R.id.overlay_view) as OverlayMenuView
        overlayMenuView?.setOnOverlayMenuViewListener(this)
        overlayMenuView?.setOnClickListener {
            closeOverlayView()
        }

        overlayMenuView2 = view.findViewById(R.id.overlay_view2) as OverlayMenuView
        overlayMenuView2?.setOnOverlayMenuViewListener(this)
        overlayMenuView2?.setOnClickListener {
            closeOverLayView2()
        }


        dockViewPager = view.findViewById<ViewPager2>(R.id.dockViewPager)


        viewPager = view.findViewById<ViewPager2>(R.id.viewPager)

        dragAndDropView?.setLowerDesktopView(viewPager!!)


        desktopAdapter = GridAdapter(
            context = this.requireContext(),
            viewPager = viewPager!!,
            viewType = CELL_POINT_NAME.DESKTOP,
            startPageSize = 5,
            rowCount = Global.ROW_COUNT,
            columnCount = Global.COLUMN_COUNT,
            listener = this
        )

        viewPager!!.adapter = desktopAdapter
        viewPager!!.offscreenPageLimit = 5


        viewPager!!.setPageTransformer { page, position ->
            val absPosition = Math.abs(position)

            // ページの中心からの距離に基づいて拡大率を計算
            var scale = 1f

            if (ANIMATION_MODE == ANIMATION_MODE_SLIDE) {

            } else if (ANIMATION_MODE == ANIMATION_MODE_SLIDE_SCALE_DOWN) {
                scale = if (absPosition < 1) {
                    1f - absPosition / 2
                } else {
                    0.0f
                }
            } else {

            }

            // ページの中心からの距離に基づいて透明度を計算
            var alpha = 1f

            if (ANIMATION_MODE == ANIMATION_MODE_SLIDE) {

            } else if (ANIMATION_MODE == ANIMATION_MODE_SLIDE_SCALE_DOWN) {
                alpha = 1f - absPosition * 0.5f
            } else {

            }

            // ページの中心からの距離に基づいてX方向への移動量を計算
            var translationX = -position
            if (ANIMATION_MODE == ANIMATION_MODE_SLIDE) {

            } else if (ANIMATION_MODE == ANIMATION_MODE_SLIDE_SCALE_DOWN) {
                translationX = -position * (page.width / 2)
            } else {

            }

            // ページの表示を更新
            page.alpha = alpha
            page.scaleX = scale
            page.scaleY = scale
            page.translationX = translationX

            // 左のアイテム判定
            if (position < -0.5f) {
                page.elevation = 0f
            }

            // 中央のアイテム判定
            if (position >= -0.5f && position <= 0.5f) {
                page.elevation = 8f
            }

            // 右のアイテム判定
            if (position > 0.5f) {
                page.elevation = 0.0f
            }
        }

        viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(posi: Int) {
                gridPage = posi
                desktopAdapter.setPage(posi)
            }
        })



        dockViewPager = view.findViewById<ViewPager2>(R.id.dockViewPager)

        dragAndDropView?.setLowerDockView(dockViewPager!!)

        dockAdapter = GridAdapter(
            context = this.requireContext(),
            viewPager = dockViewPager!!,
            viewType = CELL_POINT_NAME.DOCK,
            startPageSize = 1,
            rowCount = 1,
            columnCount = Global.COLUMN_COUNT,
            listener = this
        )

        dockViewPager!!.adapter = dockAdapter
        dockViewPager!!.offscreenPageLimit = 1

        dockViewPager!!.setPageTransformer { page, position ->
            val absPosition = Math.abs(position)

            // ページの中心からの距離に基づいて拡大率を計算
            var scale = 1f

            if (ANIMATION_MODE == ANIMATION_MODE_SLIDE) {

            } else if (ANIMATION_MODE == ANIMATION_MODE_SLIDE_SCALE_DOWN) {
                scale = if (absPosition < 1) {
                    1f - absPosition / 2
                } else {
                    0.0f
                }
            } else {

            }

            // ページの中心からの距離に基づいて透明度を計算
            var alpha = 1f

            if (ANIMATION_MODE == ANIMATION_MODE_SLIDE) {

            } else if (ANIMATION_MODE == ANIMATION_MODE_SLIDE_SCALE_DOWN) {
                alpha = 1f - absPosition * 0.5f
            } else {

            }

            // ページの中心からの距離に基づいてX方向への移動量を計算
            var translationX = -position
            if (ANIMATION_MODE == ANIMATION_MODE_SLIDE) {

            } else if (ANIMATION_MODE == ANIMATION_MODE_SLIDE_SCALE_DOWN) {
                translationX = -position * (page.width / 2)
            } else {

            }

            // ページの表示を更新
            page.alpha = alpha
            page.scaleX = scale
            page.scaleY = scale
            page.translationX = translationX

            // 左のアイテム判定
            if (position < -0.5f) {
                page.elevation = 0f
            }

            // 中央のアイテム判定
            if (position >= -0.5f && position <= 0.5f) {
                page.elevation = 8f
            }

            // 右のアイテム判定
            if (position > 0.5f) {
                page.elevation = 0.0f
            }
        }

        dockViewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(posi: Int) {

            }
        })
    }

    private fun enableViewCheck(ownerView: View) {

        setViews(ownerView)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // アプリのリスト表示・登録関連

    private val itemObserver = Observer<HomeItem?> { item ->

        if (item == null) return@Observer

        //android.util.Log.i("TESTESTEST","item:" + item.label)

        Global.homeItemData.addItem(gridPage, item)
        pref.setAppsList()

        // 追加されたアイテムを対象のページのグリッド上に配置する
        addGrid(item)

        Global.selectItem.value = null
    }

    private fun closeObserve() {
        Global.selectItem.removeObserver(itemObserver)
    }









    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Widget関連

    var mAppWidgetHost: AppWidgetHost? = null

    val XXXX = 111111

    val REQUEST_CODE_ADD_APPWIDGET = 1
    val REQUEST_CODE_ADD_APPWIDGET_2 = 2

    var widgetWidth = 0
    var widgetHeight = 0

    var appWidgetId: Int = -1
    var widgetView: AppWidgetHostView? = null

    fun openWidget() {
        Log.i("TESTESTEST", "openWidget")

        var appWidgetId = mAppWidgetHost!!.allocateAppWidgetId()    //…(2)
        var appWidgetProviderInfoList = ArrayList<AppWidgetProviderInfo>()

        var bundleList = ArrayList<Bundle>()
        var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, appWidgetProviderInfoList)
            .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, bundleList)


        val res = this.startActivityForResult(intent, REQUEST_CODE_ADD_APPWIDGET)    //…(3)
    }

    private fun onResultWidget(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> when (requestCode) {
                REQUEST_CODE_ADD_APPWIDGET -> {
                    val appWidgetId = data!!.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetProviderInfo =
                        AppWidgetManager.getInstance(this.requireActivity().applicationContext).getAppWidgetInfo(appWidgetId) //…(2)

                    //…(3)
                    if (appWidgetProviderInfo.configure != null) {
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                            .setComponent(appWidgetProviderInfo.configure)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        startActivityForResult(intent, REQUEST_CODE_ADD_APPWIDGET_2)

                        //…(4)
                    } else {
                        onActivityResult(REQUEST_CODE_ADD_APPWIDGET_2,
                            AppCompatActivity.RESULT_OK, data)
                    }
                }
                REQUEST_CODE_ADD_APPWIDGET_2 -> {
                    val appWidgetId2 = data!!.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

                    val view = getWidgetView(this.requireActivity().applicationContext, mAppWidgetHost!!, appWidgetId2)
                    view?.let {
                        addWodiget(it, appWidgetId2)
                    }



                }
            }
            AppCompatActivity.RESULT_CANCELED -> when (requestCode) {
                REQUEST_CODE_ADD_APPWIDGET, REQUEST_CODE_ADD_APPWIDGET_2 -> if (data != null) {
                    val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetHost = AppWidgetHost(this.requireContext(), XXXX)
                    if (appWidgetId != -1) appWidgetHost.deleteAppWidgetId(appWidgetId) //…(8)
                }
            }
        }
    }

    private fun setupWidget() {
        mAppWidgetHost = AppWidgetHost(this.requireActivity().applicationContext, XXXX)    //…(1)
        mAppWidgetHost!!.startListening()

        // ウィジェットの復帰
        for (id in mAppWidgetHost!!.appWidgetIds) {
            Log.i("TESTTEST", "onCreate  id:" + id)
            //mAppWidgetHost!!.deleteAppWidgetId(id) //…(8)


            val appWidgetId2 = id
            val appWidgetProviderInfo2 =
                AppWidgetManager
                    .getInstance(this.requireActivity().applicationContext)
                    .getAppWidgetInfo(appWidgetId2)

            try {
                val widgetLabel = appWidgetProviderInfo2.label
                val widgetIcon = this.requireActivity().packageManager.getDrawable(
                    appWidgetProviderInfo2.provider.packageName,
                    appWidgetProviderInfo2.icon,
                    null
                )
                widgetWidth = appWidgetProviderInfo2.minWidth
                widgetHeight = appWidgetProviderInfo2.minHeight


                appWidgetId = appWidgetId2

                var hostView = mAppWidgetHost!!.createView(this.requireActivity().applicationContext, appWidgetId2, appWidgetProviderInfo2)

                //hostView.setMinimumHeight(appWidgetProviderInfo2.minHeight)
                if (Build.VERSION.SDK_INT > 15) {
                    //hostView.updateAppWidgetSize(null,
                    //    widgetWidth, appWidgetProviderInfo2.minHeight,
                    //    widgetHeight, appWidgetProviderInfo2.minHeight)
                }
                hostView.setAppWidget(appWidgetId2, appWidgetProviderInfo2)
                //addWodiget(hostView, id, true, 0, 0, 1, 1, widgetWidth, widgetHeight)
                //addWodiget(hostView, id, true, 0, 0, 1, 1, 200, 200)
            }catch(e: Exception) {
                Log.e("TESTTEST", "error:" + e.message)

                removeWidget(id)
            }
        }
    }

    fun addWodiget(child: View, widgetId: Int) {
        Log.i("TESTTEST", "addWodiget  appWidgetId:" + appWidgetId)

        if (!Global.homeItemData.checkWidget(gridPage, widgetId)) {
            val homeItem = HomeItem(
                -1,
                "",
                "",
                1,
                null,
                "",
                "",
                ""
            )
            homeItem.widgetId = widgetId


            Global.homeItemData.addItem(gridPage, homeItem)
            pref.setAppsList()

            desktopAdapter.addGrid(child, homeItem)
        } else {
            val homeItem = Global.homeItemData.getWidgetHomeItem(gridPage, widgetId)

            homeItem?.let {
                desktopAdapter.changeGrid(child, homeItem)
            }
        }


        //mWorkspace!!.addView(child, lp)

        //mWorkspace!!.layoutParams.width = width * 2
        //mWorkspace!!.layoutParams.height = height * 2

        //mWorkspace!!.addView(child, if (insert) 0 else -1, lp)
        //child.setOnLongClickListener(this)
    }

    fun removeWidget(appWidgetId: Int) {
        Log.i("TESTTEST", "removeWidget  appWidgetId:" + appWidgetId)

        if (appWidgetId != -1) mAppWidgetHost!!.deleteAppWidgetId(appWidgetId) //…(8)

        //mWorkspace!!.removeAllViews()
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////
    // GridAdapterのListener

    override fun onGridPositionView(viewType: CELL_POINT_NAME, layout: LinearLayout, position: Int, row: Int, column: Int) {

        val item: HomeItem? = if (viewType == CELL_POINT_NAME.DESKTOP) {
            Global.homeItemData.getItem(position, row, column)
        } else if (viewType == CELL_POINT_NAME.DOCK) {
            Global.dockItemData.getItem(position, row, column)
        } else {
            null
        }

        item?.let {
            if (it.toolId != -1) {

                if (it.toolId == 2) {
                    // データを追加する
                    val list = Global.folderManager.getList(it.folderId)

                    val folderView = createToolFolderView(requireContext(), it, list)

                    val parent = folderView.parent as ViewGroup
                    parent?.removeView(folderView)

                    layout.addView(folderView)
                } else {
                    val view = createItemView(requireContext(), it)
                    layout.addView(view)
                }

            } else if (it.widgetId != -1) {
                val view = getWidgetView(this.requireActivity().applicationContext, mAppWidgetHost!!, it.widgetId)
                if (view != null) {
                    layout.addView(view)
                }
            } else{
                val view = createItemView(requireContext(), it)
                layout.addView(view)
            }
        }
    }

    override fun onClickOpenApp(viewType: CELL_POINT_NAME, page: Int, row: Int, column: Int) {
        android.util.Log.i(TAG,"onClickOpenApp")


    }

    override fun onLongClickToEvent(view: View, bitmap: Bitmap, positionX: Float, positionY: Float) {

        //setDragAndDropData(view, bitmap, positionX, positionY)
    }

    override fun onLongClickToBlanc(row: Int, column: Int) {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // DragAndDropViewのListener


    var dragAndDrop = false
    var dragItem: HomeItem? = null
    var dragView: View? = null
    var startCellPointName = CELL_POINT_NAME.NONE
    var moveToPrevItemDelete = false

    /**
     * DragAndDrop開始
     */
    private fun setDragAndDropData(adapter: GridAdapter, cellPointName: CELL_POINT_NAME, point: DragAndDropView.DimensionPoint) {

        android.util.Log.i(TAG,"setDragAndDropData")

        val gridPoint = adapter.getGridPoint(point)



        val homeItem = if (cellPointName == CELL_POINT_NAME.DESKTOP) {
            Global.homeItemData.getItem(gridPage, gridPoint.row, gridPoint.column)
        } else if (cellPointName == CELL_POINT_NAME.DOCK) {
            gridPoint.row = 0
            Global.dockItemData.getItem(0, gridPoint.row, gridPoint.column)
        } else {
            null
        }

        if (homeItem == null) return

        // Viewを取得
        val view = adapter.getGridView(gridPoint.row, gridPoint.column) ?: return

        val positionX = point.x - (view.width / 2)
        val positionY = point.y - (view.height / 3)

        // Viewから画像を生成
        val bitmap = getViewCapture(view) ?: return

        if (dragAndDropView == null)return

        var dragDrop = dragAndDropView!!

        // フィールド変数に一時登録
        dragAndDrop = true
        dragView = view
        dragItem = homeItem
        startCellPointName = cellPointName
        moveToPrevItemDelete = true

        android.util.Log.i(TAG,"画像を登録します")
        dragDrop.setDragImage(bitmap, DragAndDropView.DimensionPoint(positionX, positionY))

        //adapter.removePageItem(gridPage,gridPoint.row, gridPoint.column)

        openIconMenu(cellPointName, homeItem, positionX, positionY)
    }

    /**
     * DragAndDrop終了
     */
    private fun endDragAndDrop(adapter: GridAdapter, cellPointName: CELL_POINT_NAME, point: DragAndDropView.DimensionPoint) {
        val item = dragItem ?: return
        val view =  dragView ?: return
        if (startCellPointName == CELL_POINT_NAME.NONE) return

        // 指定したポイントに指定したアイテムを配置する
        android.util.Log.i(TAG,"setGridItem >> item.label:" + item.label)
        android.util.Log.i(TAG,"setGridItem >> item.widgetId:" + item.widgetId)
        android.util.Log.i(TAG,"setGridItem >> item.toolId:" + item.toolId)

        // Gridの移動
        val viewSize = viewPager!!.getSize()
        val oneWidth = viewSize.width / Global.COLUMN_COUNT
        val oneHeight = viewSize.height / Global.ROW_COUNT

        if (startCellPointName == cellPointName) {

            val column = (point.x / oneWidth).toInt()
            val row = (point.y / oneHeight).toInt()

            val prevRow = item.row
            val prevColumn = item.column

            if (cellPointName == CELL_POINT_NAME.DESKTOP) {
                // 移動前のデータをリストから消す
                Global.homeItemData.removeHomeItem(gridPage, prevRow, prevColumn)

                if (Global.homeItemData.checkToolToFolder(gridPage, row, column)) {
                    // フォルダなので特殊な操作をする
                    if (item.widgetId != -1) {
                        //widgetなので元に戻す

                        item.row = prevRow
                        item.column = prevColumn
                        Global.homeItemData.setItem(gridPage, prevRow, prevColumn, item)
                        adapter.selectItem(view, prevRow, prevColumn, false)

                    } else {

                        val folderItem = Global.homeItemData.getFolderItem(gridPage, row, column)

                        if (folderItem != null) {
                            // テストのために一時的に戻す
                            setFolderInApp(folderItem, item)
                        } else {
                            // なぜかデータが取れなかったので元に戻す
                            item.row = prevRow
                            item.column = prevColumn
                            Global.homeItemData.setItem(gridPage, prevRow, prevColumn, item)
                            adapter.selectItem(view, prevRow, prevColumn, false)

                        }
                    }

                } else {
                    val moveItem = Global.homeItemData.addItem(gridPage, row, column, item)

                    if (moveItem == ITEM_MOVE.MOVE_NG) {
                        item.row = prevRow
                        item.column = prevColumn
                        Global.homeItemData.setItem(gridPage, prevRow, prevColumn, item)
                        adapter.selectItem(view, prevRow, prevColumn, false)
                    } else {
                        item.row = row
                        item.column = column
                        val moveItemEnable = moveItem == ITEM_MOVE.MOVING_ITEM_ENABLED
                        adapter.selectItem(view, row, column, moveItemEnable)
                    }
                }

            } else if (cellPointName == CELL_POINT_NAME.DOCK) {
                // 移動前のデータをリストから消す
                Global.dockItemData.removeHomeItem(0, 0, prevColumn)

                val moveItem = Global.dockItemData.addItem(0, 0, column, item)
                if (moveItem == ITEM_MOVE.MOVE_NG) {
                    item.row = 0
                    item.column = prevColumn
                    Global.dockItemData.setItem(0, 0, prevColumn, item)
                    adapter.selectItem(view, 0, column, false)
                } else {
                    item.row = 0
                    item.column = column
                    val moveItemEnable = moveItem == ITEM_MOVE.MOVING_ITEM_ENABLED
                    adapter.selectItem(view, 0, column, moveItemEnable)
                }
            }

        } else {
            // 移動先が違う
            if (cellPointName == CELL_POINT_NAME.DESKTOP) {
                val prevColumn = item.column

                // 移動前のデータをリストから消す
                Global.dockItemData.removeHomeItem(0, 0, prevColumn)

                val column = (point.x / oneWidth).toInt()
                val row = (point.y / oneHeight).toInt()

                val moveItem = Global.homeItemData.addItem(gridPage, row, column, item)

                if (moveItem == ITEM_MOVE.MOVE_NG) {
                    item.row = 0
                    item.column = prevColumn
                    Global.dockItemData.setItem(0, 0, prevColumn, item)
                    dockAdapter.selectItem(view, 0, column, false)
                } else {
                    item.row = row
                    item.column = column
                    val moveItemEnable = moveItem == ITEM_MOVE.MOVING_ITEM_ENABLED
                    adapter.selectItem(view, row, column, moveItemEnable)
                }

            } else if (cellPointName == CELL_POINT_NAME.DOCK) {
                val prevRow = item.row
                val prevColumn = item.column

                val column = (point.x / oneWidth).toInt()
                val row = 0

                // 移動前のDesktopのデータをリストから消す
                Global.homeItemData.removeHomeItem(gridPage, prevRow, prevColumn)

                val moveItem = Global.dockItemData.addItem(0, row, column, item)

                if (moveItem == ITEM_MOVE.MOVE_NG) {
                    item.row = prevRow
                    item.column = prevColumn
                    Global.homeItemData.setItem(gridPage, prevRow, prevColumn, item)
                    this.desktopAdapter.selectItem(view, prevRow, prevColumn, false)
                } else {
                    item.row = 0
                    item.column = column
                    val moveItemEnable = moveItem == ITEM_MOVE.MOVING_ITEM_ENABLED
                    adapter.selectItem(view, 0, column, moveItemEnable)
                }
            }
        }

        // 配置されたアイコンたちの情報を保存
        pref.setAppsList()


        dragAndDrop = false
        dragItem = null
        dragView = null

    }

///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Viewサイズを取得したら呼ばれるOverrideメソッド
     */
    override fun onDisplayEnable(width: Int, height: Int) {

        val dotHeight = (20).dpToPx(requireContext()).toInt()

        val oneWidth = width / Global.COLUMN_COUNT
        val oneHeight = (height - dotHeight) / (Global.ROW_COUNT + 1)

        android.util.Log.i(TAG,"onDisplayEnable >> dotHeight:" + dotHeight)
        android.util.Log.i(TAG,"onDisplayEnable >> oneWidth:" + oneWidth)
        android.util.Log.i(TAG,"onDisplayEnable >> oneHeight:" + oneHeight)

        dotFrame?.layoutParams?.height = dotHeight
        dockViewPager?.layoutParams?.height = oneHeight

        dragAndDropView?.setDotHeight(dotHeight)

    }


    override fun onTouchDown(point: DragAndDropView.DimensionPoint) {
        closeOverlayView()
    }
    override fun onTouchMove(point: DragAndDropView.DimensionPoint) {}

    override fun onTouchUp(cellPointName: CELL_POINT_NAME, point: DragAndDropView.DimensionPoint) {

        if (dragAndDrop) {
            if (cellPointName == CELL_POINT_NAME.DESKTOP) {
                endDragAndDrop(desktopAdapter, cellPointName, point)
            } else if (cellPointName == CELL_POINT_NAME.DOCK) {
                endDragAndDrop(dockAdapter, cellPointName, point)
            }
        }
    }

    /**
     * DragAndDropViewのクリック判定
     */
    override fun onTouchClick(cellPointName: CELL_POINT_NAME, point: DragAndDropView.DimensionPoint) {
        if (dragAndDrop) return

        val viewSize = viewPager!!.getSize()
        val oneWidth = viewSize.width / Global.COLUMN_COUNT
        val oneHeight = viewSize.height / Global.ROW_COUNT

        val column = (point.x / oneWidth).toInt()
        val row = (point.y / oneHeight).toInt()

        val item = if (cellPointName == CELL_POINT_NAME.DESKTOP) {
            Global.homeItemData.getItem(gridPage, row, column)
        } else if (cellPointName == CELL_POINT_NAME.DOCK) {
            Global.dockItemData.getItem(0, 0, column)
        } else {
            null
        }

        if (item == null || item.widgetId != -1) {
            // 何もしない
        } else if (item.toolId != -1) {
            openToolAction(item)
        } else {
            Global.launch(requireContext(), item, null)
        }
    }


    /**
     *
     */
    override fun onLongTouchDown(cellPointName: CELL_POINT_NAME, point: DragAndDropView.DimensionPoint) {
        android.util.Log.i(TAG,"onLongTouchDown　" + cellPointName)

        if (cellPointName == CELL_POINT_NAME.DESKTOP) {
            val gridPoint = desktopAdapter.getGridPoint(point)
            val homeItem = Global.homeItemData.getItem(gridPage, gridPoint.row, gridPoint.column)
            android.util.Log.i(TAG,"homeItem is null = " + (homeItem == null))
            if (homeItem == null) {
                openMenuDialog()
                return
            }

            setDragAndDropData(desktopAdapter, cellPointName, point)
        } else if (cellPointName == CELL_POINT_NAME.DOCK) {
            val gridPoint = dockAdapter.getGridPoint(point)
            val homeItem = Global.dockItemData.getItem(0, 0, gridPoint.column)
            android.util.Log.i(TAG,"homeItem is null = " + (homeItem == null))
            if (homeItem == null) {
                openMenuDialog()
                return
            }

            setDragAndDropData(dockAdapter, cellPointName, point)
        } else {
            android.util.Log.i(TAG,"cellPointName = " + cellPointName)
        }
    }

    var moveLog = false
    override fun onSelectGridPoint(
        gridPoint: GridPoint?,
        cellPointName: CELL_POINT_NAME,
        action: Int
    ) {
        if (gridPoint == null) return

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                moveLog = true
                android.util.Log.i(TAG,"onSelectGridPoint >> gridPoint: MotionEvent.ACTION_DOWN")
                closeOverlayView()
            }
            MotionEvent.ACTION_MOVE -> {
                if (moveLog)android.util.Log.i(TAG,"onSelectGridPoint >> gridPoint: MotionEvent.ACTION_MOVE")
                moveLog = false

                closeOverlayView()
                if (dragAndDrop) {
                    dragAndDropView?.setDragAnimationEnable()

                    if (moveToPrevItemDelete) {
                        moveToPrevItemDelete = false
                        if (cellPointName == CELL_POINT_NAME.DESKTOP) {
                            desktopAdapter.removePageItem(gridPage, dragItem!!.row, dragItem!!.column)
                        } else if (cellPointName == CELL_POINT_NAME.DOCK) {
                            dockAdapter.removePageItem(0, dragItem!!.row, dragItem!!.column)
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                dragAndDropView?.setDragAnimationDisable()
                if (!openIconMenuEnable) {
                    android.util.Log.i(TAG, "onSelectGridPoint >> gridPoint: MotionEvent.ACTION_UP")

                    when (cellPointName) {
                        CELL_POINT_NAME.DESKTOP -> {

                        }
                        CELL_POINT_NAME.DOCK -> {

                        }
                        CELL_POINT_NAME.DOT -> {
                            // 置き場所じゃないので元の場所に返す
                            dragItem?.let {
                                val gridPointPrev = GridPoint(it.row, it.column)
                            }
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * １マスのアイテムが追加されたら、現在表示しているページ上のGridに追加する
     */
    private fun addGrid(item: HomeItem) {
        // 配置するViewの生成
        val view = createItemView(requireContext(), item)

        // gridに配置する
        desktopAdapter.addGrid(view, item)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private fun closeOverlayView() {
        openIconMenuEnable = false
        overlayMenuView?.let {
            it.removeAllViews()
            it.visibility = View.GONE
        }
    }

    private fun closeOverLayView2() {
        hideKeyboard(requireContext())
        overlayMenuView2?.let {
            it.removeAllViews()
            it.visibility = View.GONE
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private var openIconMenuEnable = false
    private fun openIconMenu(cellPointName: CELL_POINT_NAME, item: HomeItem, positionX: Float, positionY: Float) {
        openIconMenuEnable = true

        val width = 600
        val height = 120.dpToPx(requireContext())

        val gridPoint = desktopAdapter.getGridPoint(DragAndDropView.DimensionPoint(positionX, positionY))

        val displaySize = viewPager!!.getSize()

        val oneWidth = displaySize.width / Global.COLUMN_COUNT
        val oneHeight = displaySize.height / Global.ROW_COUNT

        var startX = oneWidth / 2 + (oneWidth * gridPoint.column)
        var startY = (oneHeight * gridPoint.row) - height

        startX -= width / 2

        if (gridPoint.column == 0) {
            startX = 10
        } else if (gridPoint.column == Global.COLUMN_COUNT - 1) {
            startX = displaySize.width - width - 10
        }

        if (gridPoint.row < 2) {
            startY = (oneHeight * (gridPoint.row + 1)).toFloat() + 10
        }

        // 位置計算

        val appMenuFloatDialog = AppMenuFloatDialog(requireContext(),
            callbackDelete = {
                if(item.widgetId != -1) {

                    val widgetId = item.widgetId
                    removeWidget(widgetId)
                }

                if (cellPointName == CELL_POINT_NAME.DESKTOP) {
                    Global.homeItemData.removeHomeItem(gridPage, item.row, item.column)
                    desktopAdapter.removePageItem(gridPage, item.row, item.column)
                } else if (cellPointName == CELL_POINT_NAME.DOCK) {
                    Global.dockItemData.removeHomeItem(0, item.row, item.column)
                    dockAdapter.removePageItem(0, item.row, item.column)
                }

                if (item.toolId == 2) {
                    Global.folderManager.removeAllItem(item.folderId)
                }

                pref.setAppsList()
            },
            callbackInfo = {
                if(item.widgetId == -1) {
                    onClickAppSetting(item)
                }
            },
            callbackEdit = {

            }
        )

        appMenuFloatDialog.open(overlayMenuView, startX.toFloat(), startY)
    }


    override fun onTouchUp() {
        closeOverlayView()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    fun openToolDialog() {
        android.util.Log.i(TAG,"openToolDialog")
        val floatDdialog = ToolItemListFloatDialog(requireContext()) {

            addTool(it)
        }

        floatDdialog.open(overlayMenuView)
    }

    private fun addTool(homeItem: HomeItem) {
        Global.homeItemData.addItem(gridPage, homeItem)
        pref.setAppsList()

        val child = createItemView(requireContext(), homeItem)

        desktopAdapter.addGrid(child, homeItem)
    }

    private fun openToolAction(homeItem: HomeItem) {
        if (homeItem.toolId == 1) {
            onClickOpenApps()
        } else if (homeItem.toolId == 2) {
            openFolderInAppData(homeItem)
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * フォルダーへアイテムの追加
     */
    private fun setFolderInApp(folder: HomeItem, inItem: HomeItem) {
        // データを追加する
        Global.folderManager.addItem(folder.folderId, inItem)

        updateFolderApp(folder)

    }

    private fun updateFolderApp(folder: HomeItem) {
        val list = Global.folderManager.getList(folder.folderId)
        val folderView = createToolFolderView(requireContext(), folder, list)

        desktopAdapter.selectItem(folderView, folder.row, folder.column, false)
    }

    var folderDialog: FolderFloatDialog? = null
    private fun openFolderInAppData(folder: HomeItem) {

        val cellSize = dragAndDropView?.getGridSize()

        val list = Global.folderManager.getList(folder.folderId)
        folderDialog = FolderFloatDialog(
            context = requireContext(),
            folder = folder,
            list = list,
            callback = {
                Global.launch(requireContext(), it, null)
                folderDialog = null
            },
            callbackEditTitle = {
                openEditTitle(it) {editItem ->

                    Global.updateFolder(editItem)

                    folderDialog?.updateTitle(editItem)

                    updateFolderApp(folder)

                    pref.setAppsList()
                }
            }
        ) {
            openFolderToAppMenu(folder, it)
        }
        folderDialog?.open(overlayMenuView)

    }

    private fun openFolderToAppMenu(folder: HomeItem, item: HomeItem) {

        openIconMenuEnable = true

        val width = 600
        val height = 120.dpToPx(requireContext())

        val gridPoint = GridPoint(Global.ROW_COUNT / 2, Global.COLUMN_COUNT / 2)

        val displaySize = viewPager!!.getSize()

        val oneWidth = displaySize.width / Global.COLUMN_COUNT
        val oneHeight = displaySize.height / Global.ROW_COUNT

        var startX = oneWidth / 2 + (oneWidth * gridPoint.column)
        var startY = (oneHeight * gridPoint.row) - height

        startX -= width / 2

        if (gridPoint.column == 0) {
            startX = 10
        } else if (gridPoint.column == Global.COLUMN_COUNT - 1) {
            startX = displaySize.width - width - 10
        }

        if (gridPoint.row < 2) {
            startY = (oneHeight * (gridPoint.row + 1)).toFloat() + 10
        }

        val appMenuFloatDialog = AppMenuFloatDialog(requireContext(),
            callbackDelete = {
                folderDialog?.close()
                folderDialog = null

                Global.folderManager.removeItem(folder.folderId, item)
                pref.setAppsList()

                updateFolderApp(folder)
            },
            callbackInfo = {
                folderDialog?.close()
                folderDialog = null

                if(item.widgetId == -1) {
                    onClickAppSetting(item)
                }
            },
            callbackEdit = {
                folderDialog?.close()
                folderDialog = null


            }
        )

        appMenuFloatDialog.open(overlayMenuView2, startX.toFloat(), startY)
    }

///////////////////////////////////////////////////////////////////////////////////////////////

    private fun openEditTitle(item: HomeItem, callback:(HomeItem) -> Unit) {
        val dialog = EditTitleDialog(requireContext(), item) {
            callback.invoke(it)
        }
        dialog?.open(overlayMenuView2)
    }

///////////////////////////////////////////////////////////////////////////////////////////////

    private fun openMenuDialog() {
        android.util.Log.i(TAG,"openMenuDialog")
        val dialog = MenuDialog("MENU") {
            if (it == HomeItemType.APP) {
                onClickOpenApps()
            } else if (it == HomeItemType.WIDGET) {
                openWidget()
            } else if (it == HomeItemType.TOOL) {
                openToolDialog()
            }
        }

        dialog.show(this.parentFragmentManager, "")
    }

    private fun onClickOpenApps() {
        closeObserve()
        Global.selectItem.observe(viewLifecycleOwner, itemObserver)


        val activity = this.requireActivity() as MainActivity
        activity.openApplicationList()
    }

    private fun onClickSetting() {

        val intent = Intent()
            .setAction(Settings.ACTION_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        startActivity(intent)
    }

    private fun onClickAppSetting(item: HomeItem) {
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Settings.ACTION_APPLICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, item.packageName)
        startActivity(intent)
    }

    private fun onClickAppNotificationSetting(item: HomeItem) {
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, item.packageName)
        startActivity(intent)
    }
}