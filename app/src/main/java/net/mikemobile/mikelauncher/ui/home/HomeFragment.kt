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
import android.util.Size
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
import net.mikemobile.mikelauncher.constant.GridSize
import net.mikemobile.mikelauncher.constant.DataManagement
import net.mikemobile.mikelauncher.constant.DimenPoint
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.constant.GridCount
import net.mikemobile.mikelauncher.constant.GridPoint
import net.mikemobile.mikelauncher.constant.HomeItemType
import net.mikemobile.mikelauncher.constant.ITEM_MOVE
import net.mikemobile.mikelauncher.constant.WidgetData
import net.mikemobile.mikelauncher.data.AppPreference
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.system.triggerVibration
import net.mikemobile.mikelauncher.ui.custom.DragAndDropView
import net.mikemobile.mikelauncher.ui.custom.OverlayMenuView
import net.mikemobile.mikelauncher.ui.custom_float.AppMenuFloatDialog
import net.mikemobile.mikelauncher.ui.custom_float.BaseFloatingDialog
import net.mikemobile.mikelauncher.ui.custom_float.FolderFloatDialog
import net.mikemobile.mikelauncher.ui.custom_float.ToolItemListFloatDialog
import net.mikemobile.mikelauncher.ui.dialog.MenuDialog
import net.mikemobile.mikelauncher.ui.dialog.TitleEditDialog
import net.mikemobile.mikelauncher.util.getViewCapture


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


    private lateinit var pref: AppPreference

    // スライドアニメーション制御用
    private val ANIMATION_MODE_SLIDE = 1
    private val ANIMATION_MODE_SLIDE_SCALE_DOWN = 2
    private var ANIMATION_MODE = ANIMATION_MODE_SLIDE


    private var firstLoad = false
    private var gridPage = 0


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
        firstLoad = true
        pref = AppPreference(requireContext())

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        setupWidget()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        onResultWidget(requestCode, resultCode, data)

    }

    override fun onResume() {
        super.onResume()
        if (!firstLoad) {}

        firstLoad = false
    }


    private var overlayMenuView: OverlayMenuView? = null
    private var overlayMenuView2: OverlayMenuView? = null
    private var dragAndDropView: DragAndDropView? = null
    private var viewPager: ViewPager2? = null
    private var dockViewPager: ViewPager2? = null

    private var dotFrame: ConstraintLayout? = null

    private fun setViews(view: View) {
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

        Global.homeItemData.addItem(gridPage, item)
        pref.setAppsList()

        // 追加されたアイテムを対象のページのグリッド上に配置する
        addGrid(item)

        Global.selectItem.value = null
    }

    private fun startObserve() {
        Global.selectItem.observe(viewLifecycleOwner, itemObserver)
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

                    val widgetData = getWidgetView(this.requireActivity().applicationContext, mAppWidgetHost!!, appWidgetId2)
                    widgetData?.let {
                        addWodiget(widgetData, appWidgetId2)
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

    fun addWodiget(widgetData: WidgetData, widgetId: Int) {
        Log.i("TESTTEST", "addWodiget  appWidgetId:" + appWidgetId)

        val gridCount = Global.calcSizeToGridCount(widgetData.width, widgetData.height)

        if (!Global.homeItemData.checkWidget(gridPage, widgetId)) {
            val homeItem = HomeItem.createWidget(widgetId, widgetData, gridCount)

            val addEnable = Global.homeItemData.addItem(gridPage, homeItem)
            if (addEnable) {
                pref.setAppsList()
                desktopAdapter.addGrid(widgetData.view, gridCount, homeItem)
            }
        } else {
            val homeItem = Global.homeItemData.getWidgetHomeItem(gridPage, widgetId)

            homeItem?.let {
                desktopAdapter.changeGrid(widgetData.view, gridCount, homeItem)
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

    override fun onGridPositionView(viewType: CELL_POINT_NAME, layout: LinearLayout, position: Int, row: Int, column: Int) {}

    override fun onCellPositionView(viewType: CELL_POINT_NAME, layout: LinearLayout, position: Int, row: Int, column: Int) {
        val item: HomeItem? = if (viewType == CELL_POINT_NAME.DESKTOP) {
            Global.homeItemData.getItem(position, row, column)
        } else if (viewType == CELL_POINT_NAME.DOCK) {
            Global.dockItemData.getItem(position, row, column)
        } else {
            null
        }


        item?.let {

            if (item.widgetField) {
                if (Global.homeItemData.checkNotWidgetData(item.fieldId)) {
                    // オリジナルのデータがない
                    Global.homeItemData.removeHomeItem(position, row, column)
                    pref.setAppsList()
                    return
                }

                val blankView = getView(this.requireContext(), net.mikemobile.mikelauncher.R.layout.home_item_blank)

                layout.addView(blankView)
                layout.layoutParams.width = (Global.gridSize.width).toInt()
                layout.layoutParams.height = (Global.gridSize.height).toInt()

            } else if (it.toolId != -1) {

                if (it.toolId == 2) {
                    // データを追加する
                    val list = Global.folderManager.getList(it.folderId)

                    val folderView = createToolFolderView(requireContext(), it, list)

                    val parent = folderView.parent as ViewGroup
                    parent?.removeView(folderView)

                    layout.addView(folderView)
                    layout.layoutParams.width = (Global.gridSize.width).toInt()
                    layout.layoutParams.height = (Global.gridSize.height).toInt()
                } else {
                    val view = createItemView(requireContext(), it)
                    layout.addView(view)
                    layout.layoutParams.width = (Global.gridSize.width).toInt()
                    layout.layoutParams.height = (Global.gridSize.height).toInt()
                }

            } else if (item.widgetId != -1) {

                Log.i(TAG + "-WIDGET","setDragAndDropData >> homeItem　data " +
                        "label:" + item.label + " / type:" + item.type + "\n" +
                        "widgetId:" + item.widgetId + " / widgetField:" + item.widgetField + "\n" +
                        "")


                val widgetData = getWidgetView(this.requireActivity().applicationContext, mAppWidgetHost!!, it.widgetId)
                if (widgetData != null) {
                    if (item.width == -1 || item.height == -1) {
                        item.width = widgetData.width
                        item.height = widgetData.height

                        Global.updateItem(item)
                        pref.setAppsList()
                    }


                    val gridCount = Global.calcSizeToGridCount(widgetData.width, widgetData.height)

                    android.util.Log.i(TAG + "-WIDGET","gridCount >> " + "\n" +
                            "widgetData label:" + widgetData.label + "" + "\n" +
                            "fieldRow:" + item.fieldRow + " / fieldColumn:" + item.fieldColumn + "\n" +
                            "rowCount:" + gridCount.rowCount + " / columnCount:" + gridCount.columnCount)

                    if (item.fieldColumn != gridCount.columnCount || item.fieldRow != gridCount.rowCount) {
                        item.fieldRow = gridCount.rowCount
                        item.fieldColumn = gridCount.columnCount
                        Global.updateItem(item)
                        pref.setAppsList()
                    }

                    layout.addView(widgetData.view)

                    layout.layoutParams.width = (Global.gridSize.width * (gridCount.columnCount)).toInt()
                    layout.layoutParams.height = (Global.gridSize.height * (gridCount.rowCount)).toInt()
                } else {
                    // 対象のデータは存在しないためリストから削除する

                    android.util.Log.e(TAG + "-WIDGET","「${item.label}/${item.widgetId}」はWidgetとして存在しないため削除対象")
                    if (viewType == CELL_POINT_NAME.DESKTOP) {
                        Global.homeItemData.removeHomeItem(item.page, item.row, item.column)
                    } else if (viewType == CELL_POINT_NAME.DOCK) {
                        Global.dockItemData.removeHomeItem(item.page, item.row, item.column)
                    }
                    pref.setAppsList()
                }


            } else{
                val view = createItemView(requireContext(), it)
                layout.addView(view)
                layout.layoutParams.width = (Global.gridSize.width).toInt()
                layout.layoutParams.height = (Global.gridSize.height).toInt()

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


    var dragAndDrop = false
    var dragItem: HomeItem? = null
    var dragView: View? = null
    var startCellPointName = CELL_POINT_NAME.NONE
    var moveToPrevItemDelete = false

    /**
     * DragAndDrop開始
     */
    private fun setDragAndDropData(adapter: GridAdapter, cellPointName: CELL_POINT_NAME, point: DimenPoint) {

        android.util.Log.i(TAG,"setDragAndDropData")

        val gridPoint = adapter.getGridPoint(point)

        var homeItem = if (cellPointName == CELL_POINT_NAME.DESKTOP) {
            Global.homeItemData.getItem(gridPage, gridPoint.row, gridPoint.column)
        } else if (cellPointName == CELL_POINT_NAME.DOCK) {
            gridPoint.row = 0
            Global.dockItemData.getItem(0, gridPoint.row, gridPoint.column)
        } else {
            null
        }

        if (homeItem != null && homeItem.widgetField) {
//            if (cellPointName == CELL_POINT_NAME.DESKTOP) {
//                homeItem = Global.homeItemData.getItem(homeItem.ownerId)
//            } else if (cellPointName == CELL_POINT_NAME.DOCK) {
//                homeItem = Global.dockItemData.getItem(homeItem.ownerId)
//            } else {
//                null
//            }
        }
        Log.i(TAG,"setDragAndDropData >> homeItem is null = " + (homeItem == null))
        if (homeItem == null) return

        val cellPointPoint = GridPoint(homeItem.row, homeItem.column)

        // Viewを取得
        Log.i(TAG,"setDragAndDropData >> homeItem　data " +
                "label:" + homeItem.label + " / type:" + homeItem.type + "\n" +
                "widgetId:" + homeItem.widgetId + " / widgetField:" + homeItem.widgetField + "\n" +
                "")


        Log.i(TAG,"setDragAndDropData >> Viewの生成 row:" + cellPointPoint.row + " / column:" + cellPointPoint.column)
        val view = adapter.getGridView(cellPointPoint.row, cellPointPoint.column) ?: return

        val positionX = point.x - (view.width / 2)
        val positionY = point.y - (view.height / 3)

        // Viewから画像を生成
        android.util.Log.i(TAG,"setDragAndDropData >> 画像の生成")
        val bitmap = getViewCapture(view) ?: return

        android.util.Log.i(TAG,"setDragAndDropData >> ドラッグ&ドロップ用Viewのチェック")
        if (dragAndDropView == null)return

        var dragDrop = dragAndDropView!!

        // フィールド変数に一時登録
        dragAndDrop = true
        dragView = view
        dragItem = homeItem
        startCellPointName = cellPointName
        moveToPrevItemDelete = true

        android.util.Log.i(TAG,"画像を登録します")
        dragDrop.setDragImage(bitmap, DimenPoint(positionX, positionY))

        // フロートメニューを表示する
        openIconMenu(cellPointName, homeItem, point.x, point.y)
    }

    /**
     * DragAndDrop終了
     */
    private fun endDragAndDrop(adapter: GridAdapter, cellPointName: CELL_POINT_NAME, point: DimenPoint) {
        val item = dragItem ?: return
        val view =  dragView ?: return
        if (startCellPointName == CELL_POINT_NAME.NONE) return

        // 指定したポイントに指定したアイテムを配置する

        // Gridの移動

        val gridCount = Global.calcSizeToGridCount(item.width, item.height)
        android.util.Log.i(TAG,"endDragAndDrop >> gridCount.rowCount:" + gridCount.rowCount)
        android.util.Log.i(TAG,"endDragAndDrop >> gridCount.columnCount:" + gridCount.columnCount)

        if (startCellPointName == cellPointName) {
            android.util.Log.i(TAG,"endDragAndDrop >> 同じスペース :" + cellPointName)

            android.util.Log.i(TAG,"endDragAndDrop >> x:" + point.x)
            android.util.Log.i(TAG,"endDragAndDrop >> y:" + point.y)
            val newPoint = Global.calcDimenToGridPoint(point)
            val prevPoint = GridPoint(item.row, item.column)


            android.util.Log.i(TAG,"endDragAndDrop >> gridPage :" + gridPage)
            android.util.Log.i(TAG,"endDragAndDrop >> row :" + newPoint.row)
            android.util.Log.i(TAG,"endDragAndDrop >> column :" + newPoint.column)


            if (cellPointName == CELL_POINT_NAME.DESKTOP) {
                // 移動元のデータを削除する
                Global.homeItemData.removeHomeItem(gridPage, prevPoint.row, prevPoint.column)

                if (Global.homeItemData.checkToolToFolder(gridPage, newPoint.row, newPoint.column, item)) {
                    android.util.Log.i(TAG,"endDragAndDrop >>   移動先がフォルダなので特殊な操作をする")
                    // フォルダなので特殊な操作をする

                    if (item.widgetId != -1) {
                        //widgetなので元に戻す

                        backGrid(item, view, gridPage, prevPoint.row, prevPoint.column, Global.homeItemData, gridCount, adapter)
                    } else {

                        // 移動先のフォルダーのデータを取得する
                        val folderItem = Global.homeItemData.getFolderItem(gridPage, newPoint.row, newPoint.column)

                        if (folderItem != null) {

                            setFolderInApp(folderItem, item)
                        } else {
                            // なぜかデータが取れなかったので元に戻す
                            backGrid(item, view, gridPage, prevPoint.row, prevPoint.column, Global.homeItemData, gridCount, adapter)
                        }
                    }

                } else {
                    android.util.Log.i(TAG,"endDragAndDrop >> setGrid-1")
                    setGrid(item,
                        view,
                        gridPage,
                        newPoint.row, newPoint.column,
                        Global.homeItemData,
                        gridPage,
                        prevPoint.row, prevPoint.column,
                        Global.homeItemData,  gridCount, adapter, adapter)
                }

            } else if (cellPointName == CELL_POINT_NAME.DOCK) {
                android.util.Log.i(TAG,"endDragAndDrop >> setGrid-2")
                // 移動元のデータを削除する
                Global.dockItemData.removeHomeItem(0, prevPoint.row, prevPoint.column)


                setGrid(item,
                    view,
                    0,
                    0,
                    newPoint.column,
                    Global.dockItemData,
                    0,
                    0,
                    prevPoint.column, Global.dockItemData, gridCount, adapter, adapter)
            }

        } else {
            android.util.Log.i(TAG,"endDragAndDrop >> 違うスペース :" + cellPointName)
            // 移動先が違う
            if (cellPointName == CELL_POINT_NAME.DESKTOP) {

                val newPoint = Global.calcDimenToGridPoint(point)
                val prevPoint = GridPoint(0, item.column)

                // 移動元のデータを削除する
                Global.dockItemData.removeHomeItem(0, prevPoint.row, prevPoint.column)

                if (Global.homeItemData.checkToolToFolder(gridPage, newPoint.row, newPoint.column, item)) {
                    if (item.widgetId != -1) {
                        //widgetなので元に戻す

                        backGrid(item, view, 0, prevPoint.row,
                            prevPoint.column, Global.dockItemData, gridCount, adapter)
                    } else {

                        // 移動先のフォルダーのデータを取得する
                        val folderItem = Global.homeItemData.getFolderItem(gridPage, newPoint.row, newPoint.column)

                        if (folderItem != null) {
                            // 移動元のデータを削除する
                            Global.dockItemData.removeHomeItem(0, prevPoint.row, prevPoint.column)

                            setFolderInApp(folderItem, item)
                        } else {
                            // なぜかデータが取れなかったので元に戻す
                            backGrid(item, view, 0, prevPoint.row,
                                prevPoint.column, Global.homeItemData, gridCount, adapter)
                        }
                    }
                } else {
                    android.util.Log.i(TAG,"endDragAndDrop >> setGrid-3")
                    setGrid(item,
                        view,
                        gridPage,
                        newPoint.row, newPoint.column,
                        Global.homeItemData,
                        0,
                        prevPoint.row,
                        prevPoint.column, Global.dockItemData, gridCount, adapter, dockAdapter)
                }



            } else if (cellPointName == CELL_POINT_NAME.DOCK) {

                val newPoint = Global.calcDimenToGridPoint(point)
                newPoint.row = 0
                val prevPoint = GridPoint(item.row, item.column)

                Global.homeItemData.removeHomeItem(gridPage, prevPoint.row, prevPoint.column)

                android.util.Log.i(TAG,"endDragAndDrop >> setGrid-4")
                setGrid(item,
                    view,
                    0,
                    newPoint.row,
                    newPoint.column,
                    Global.dockItemData,
                    gridPage,
                    prevPoint.row,
                    prevPoint.column, Global.homeItemData, gridCount, adapter, desktopAdapter)
            }
        }

        // 配置されたアイコンたちの情報を保存
        pref.setAppsList()


        dragAndDrop = false
        dragItem = null
        dragView = null

    }

    /**
     * TouchEventでCancelが呼ばれた時の処理
     */
    private fun cancelToReturen() {
        val item = dragItem ?: return
        val view =  dragView ?: return

        val gridCount = Global.calcSizeToGridCount(item.width, item.height)

        val prevRow = item.row
        val prevColumn = item.column

        if (startCellPointName == CELL_POINT_NAME.DESKTOP) {

            backGrid(item, view, gridPage, prevRow, prevColumn, Global.homeItemData, gridCount, desktopAdapter)

        } else if (startCellPointName == CELL_POINT_NAME.DOCK) {

            backGrid(item, view, 0, prevRow, prevColumn, Global.dockItemData, gridCount, dockAdapter)

        }

        // 配置されたアイコンたちの情報を保存
        pref.setAppsList()


        dragAndDrop = false
        dragItem = null
        dragView = null
    }

///////////////////////////////////////////////////////////////////////////////////////////////
    // DragAndDropViewのListener

    /**
     * Viewサイズを取得したら呼ばれるOverrideメソッド
     */
    override fun onDisplayEnable(width: Int, height: Int) {

        val dotHeight = (20).dpToPx(requireContext()).toInt()

        val oneWidth = width / Global.COLUMN_COUNT
        val oneHeight = (height - dotHeight) / (Global.ROW_COUNT + 1)

        Global.gridSize.width = oneWidth.toFloat()
        Global.gridSize.height = oneHeight.toFloat()

        desktopAdapter.setCellSize(GridSize(oneWidth.toFloat(), oneHeight.toFloat()))
        dockAdapter.setCellSize(GridSize(oneWidth.toFloat(), oneHeight.toFloat()))

        android.util.Log.i(TAG,"onDisplayEnable >> dotHeight:" + dotHeight)
        android.util.Log.i(TAG,"onDisplayEnable >> oneWidth:" + oneWidth)
        android.util.Log.i(TAG,"onDisplayEnable >> oneHeight:" + oneHeight)

        dotFrame?.layoutParams?.height = dotHeight
        dockViewPager?.layoutParams?.height = oneHeight

        dragAndDropView?.setDotHeight(dotHeight)

    }


    override fun onTouchDown(point: DimenPoint) {
        closeOverlayView()
    }
    override fun onTouchMove(point: DimenPoint) {}

    override fun onTouchUp(cellPointName: CELL_POINT_NAME, point: DimenPoint) {

        if (openIconMenuEnable) {
            dragAndDrop = false
        } else if (dragAndDrop) {
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
    override fun onTouchClick(cellPointName: CELL_POINT_NAME, point: DimenPoint) {
        if (dragAndDrop) return

        val viewSize = viewPager!!.getSize()
        val oneWidth = viewSize.width / Global.COLUMN_COUNT
        val oneHeight = viewSize.height / Global.ROW_COUNT

        val column = (point.x / oneWidth).toInt()
        val row = (point.y / oneHeight).toInt()

        var item = if (cellPointName == CELL_POINT_NAME.DESKTOP) {
            Global.homeItemData.getItem(gridPage, row, column)
        } else if (cellPointName == CELL_POINT_NAME.DOCK) {
            Global.dockItemData.getItem(0, 0, column)
        } else {
            null
        }

        if (item != null && item.widgetField) {
//            if (cellPointName == CELL_POINT_NAME.DESKTOP) {
//                item = Global.homeItemData.getItem(item.ownerId)
//            } else if (cellPointName == CELL_POINT_NAME.DOCK) {
//                item = Global.dockItemData.getItem(item.ownerId)
//            }
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
    override fun onLongTouchDown(cellPointName: CELL_POINT_NAME, point: DimenPoint) {
        android.util.Log.i(TAG,"onLongTouchDown　" + cellPointName)

        triggerVibration(requireContext())

        if (cellPointName == CELL_POINT_NAME.DESKTOP) {
            val gridPoint = desktopAdapter.getGridPoint(point)
            var homeItem = Global.homeItemData.getItem(gridPage, gridPoint.row, gridPoint.column)


            if (homeItem != null && homeItem.widgetField) {

//                if (Global.homeItemData.checkNotWidgetData(homeItem.ownerId)) {
//                    // オリジナルのデータがない
//                    Global.homeItemData.removeHomeItem(gridPage, homeItem.row, homeItem.column)
//                    homeItem =  null
//                } else {
//                    homeItem = Global.homeItemData.getItem(homeItem.ownerId)
//                }

            }
            android.util.Log.i(TAG,"homeItem is null = " + (homeItem == null))

            if (homeItem == null) {
                openMenuDialog()
                return
            }

            setDragAndDropData(desktopAdapter, cellPointName, point)
        } else if (cellPointName == CELL_POINT_NAME.DOCK) {
            val gridPoint = dockAdapter.getGridPoint(point)
            var homeItem = Global.dockItemData.getItem(0, 0, gridPoint.column)

            if (homeItem != null && homeItem.widgetField) {
                //homeItem = Global.dockItemData.getItem(homeItem.ownerId)
            }

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
                if (!openIconMenuEnable) {
                    closeOverlayView()
                }
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

                        if (dragItem!!.widgetId != -1) {
                            if (cellPointName == CELL_POINT_NAME.DESKTOP) {
                                Global.homeItemData.removeHomeItem(gridPage,  dragItem!!.row, dragItem!!.column)
                                desktopAdapter.removePageItem(gridPage, dragItem!!.row, dragItem!!.column)

//                                val list = Global.homeItemData.getWidgetFieldList(dragItem!!.id)
//                                for(fieldItem in list) {
//                                    desktopAdapter.removePageItem(gridPage, fieldItem!!.row, fieldItem!!.column)
//                                }

                            } else if (cellPointName == CELL_POINT_NAME.DOCK) {
                                Global.dockItemData.removeHomeItem(0,  dragItem!!.row, dragItem!!.column)
                                dockAdapter.removePageItem(0, dragItem!!.row, dragItem!!.column)

//                                val list = Global.dockItemData.getWidgetFieldList(dragItem!!.id)
//                                for(fieldItem in list) {
//                                    dockAdapter.removePageItem(gridPage, fieldItem.row, fieldItem.column)
//                                }

                            }
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
                } else {
                    dragAndDropView?.setDisableTouchEvent()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                dragAndDropView?.setDragAnimationDisable()
                cancelToReturen()
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
        val gridCount = Global.calcSizeToGridCount(item.width, item.height)

        // gridに配置する
        desktopAdapter.addGrid(view, gridCount, item)
    }

    private fun setGrid(
        item: HomeItem,
        view: View,
        page: Int,
        row: Int,
        column: Int,
        itemData: DataManagement,
        prevpage: Int,
        prevRow: Int,
        prevColumn: Int,
        deleteItemData: DataManagement,
        gridCount: GridCount,
        adapter: GridAdapter,
        prevAdapter: GridAdapter) {

        android.util.Log.i(TAG,"setGrid >>>>>>>>>>>>>>>>>>>>>>>>>>>>>")

        android.util.Log.i(TAG,"setGrid >> データを削除する" + "prevpage:" + prevpage + " / prevRow:" + prevRow + " / prevColumn:" + prevColumn)
        //deleteItemData.removeHomeItem(prevpage, prevRow, prevColumn)
        //deleteItemData.removeWidgetField(item.id)

        android.util.Log.i(TAG,"setGrid >> データを追加する" + "page:" + page + " / row:" + row + " / column:" + column)
        android.util.Log.i(TAG,"setGrid >> アイテム"
                + "\n id:" + item.id
                + "\n label" + item.label
                + "\n widgetId:" + item.widgetId
                + "\n toolId:" + item.toolId
                + "\n firldId:" + item.fieldId
        )

        val moveItem = itemData.addItem(page, row, column, item)

        android.util.Log.i(TAG,"setGrid >> 保存結果" + "moveItem:" + moveItem)


        if (moveItem == ITEM_MOVE.MOVE_NG) {
            android.util.Log.e(TAG,"setGrid >> 保存結果" + "データを戻す")
            item.row = prevRow
            item.column = prevColumn
            itemData.setItem(page, prevRow, prevColumn, item)
            prevAdapter.selectItem(view, gridCount, prevRow, prevColumn, false)
        } else {
            android.util.Log.i(TAG,"setGrid >> 保存結果" + "データを追加")
            item.row = row
            item.column = column
            val moveItemEnable = moveItem == ITEM_MOVE.MOVING_ITEM_ENABLED
            adapter.selectItem(view, gridCount, row, column, moveItemEnable)

            if (item.widgetId != -1) {
//                val list = itemData.getWidgetFieldList(item.id)
//
//                for(fieldItem in list) {
//                    val blankView = getView(this.requireContext(), net.mikemobile.mikelauncher.R.layout.home_item_blank)
//                    blankView?.let {
//                        val cellSize = GridPoint(0,0)
//                        adapter.addGrid(blankView, cellSize, fieldItem)
//                    }
//                }
            }
        }
    }

    /**
     * 前のGridの位置に戻る
     */
    private fun backGrid(
        item: HomeItem,
        view: View,
        page: Int,
        prevrow: Int,
        prevColumn: Int,
        itemData: DataManagement,
        gridCount: GridCount,
        adapter: GridAdapter

    ) {
        item.row = prevrow
        item.column = prevColumn
        itemData.setItem(page, prevrow, prevColumn, item)
        adapter.selectItem(view, gridCount, prevrow, prevColumn, false)


    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private fun openOverlayView(dialog: BaseFloatingDialog, isCenter: Boolean = true, isTouchLimit: Boolean = true) {
        if (isTouchLimit)dragAndDropView?.setDisableTouchEvent()

        dialog.open(overlayMenuView, isCenter)
    }
    private fun openOverlayView2(dialog: BaseFloatingDialog, isCenter: Boolean = true) {
        dialog.open(overlayMenuView2, isCenter)
    }

    private fun closeOverlayView() {
        openIconMenuEnable = false
        overlayMenuView?.let {
            it.removeAllViews()
            it.visibility = View.GONE
        }
        dragAndDropView?.setEnableTouchEvent()
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

        // 位置計算
        val gridPoint = desktopAdapter.getGridPoint(DimenPoint(positionX, positionY))

        android.util.Log.i("IconMenu","Grid row:" + gridPoint.row + " / column:" + gridPoint.column)

        val displaySize = viewPager!!.getSize()

        android.util.Log.i("IconMenu","displaySize width:" + displaySize.width + " / height:" + displaySize.height)

        val oneWidth = displaySize.width / Global.COLUMN_COUNT
        val oneHeight = displaySize.height / Global.ROW_COUNT

        android.util.Log.i("IconMenu","GridSize width:" + oneWidth + " / height:" + oneHeight)


        var startX = oneWidth / 2 + (oneWidth * gridPoint.column)
        var startY = (oneHeight * gridPoint.row) - height

        android.util.Log.i("IconMenu","Grid Center x:" + startX + " / startY:" + startY)


        startX -= width / 2

        if (gridPoint.column == 0) {
            startX = 10
        } else if (gridPoint.column == Global.COLUMN_COUNT - 1) {
            startX = displaySize.width - width - 10
        }

        if (gridPoint.row < 2) {
            startY = (oneHeight * (gridPoint.row + 1)).toFloat() + 10
        }

        android.util.Log.i("IconMenu","Dialog Position startX:" + startX + " / startY:" + startY)

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
        ) {
            closeOverlayView()
        }

        appMenuFloatDialog.setDialogSize(startX.toFloat(), startY)
        openOverlayView(appMenuFloatDialog, false, false)
    }


    override fun onTouchUp() {
        closeOverlayView()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    fun openToolDialog() {
        val floatDdialog = ToolItemListFloatDialog(requireContext(),{

            addTool(it)
        }) {
            closeOverlayView()
        }

        openOverlayView(floatDdialog)
    }

    private fun addTool(homeItem: HomeItem) {
        Global.homeItemData.addItem(gridPage, homeItem)
        pref.setAppsList()

        val child = createItemView(requireContext(), homeItem)
        val gridCount = Global.calcSizeToGridCount(homeItem.width, homeItem.height)

        desktopAdapter.addGrid(child, gridCount, homeItem)
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

        val gridCount = Global.calcSizeToGridCount(folder.width, folder.height)

        desktopAdapter.selectItem(folderView, gridCount, folder.row, folder.column, false)

    }

    var folderDialog: FolderFloatDialog? = null
    private fun openFolderInAppData(folder: HomeItem) {

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

                    Global.updateItem(editItem)

                    folderDialog?.updateTitle(editItem)

                    updateFolderApp(folder)

                    pref.setAppsList()
                }
            },
            {view, point, selectItem ->
                val size = Size(view.width, view.height)
                openFolderToAppMenu(folder, selectItem, size, point)
            }
        ) {
            closeOverlayView()
        }

        openOverlayView(folderDialog!!)

    }

    private fun openFolderToAppMenu(folder: HomeItem, item: HomeItem, size: Size, point: DimenPoint) {

        val width = 600
        val height = 150.dpToPx(requireContext())

        val displaySize = viewPager!!.getSize()

        var startX = point.x + (size.width/2)
        var startY = point.y - height

        startX -= width / 2

        if (startX < 10f) {
            startX = 10f
        } else if (displaySize.width < startX + width) {
            startX = displaySize.width - width - 10f
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
        ){
            closeOverLayView2()
        }

        openOverlayView2(appMenuFloatDialog, false)
        appMenuFloatDialog.setDialogSize(startX.toFloat(), startY)
    }

///////////////////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////////////////////

    private fun openEditTitle(item: HomeItem, callback:(HomeItem) -> Unit) {
        val dialog = TitleEditDialog(item) {
            callback.invoke(it)
        }
        dialog.show(this.parentFragmentManager, "")
    }


    private fun openMenuDialog() {
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
        startObserve()

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