package net.mikemobile.mikelauncher.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import net.mikemobile.android.view.CellLayout
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.data.HomeItem



@SuppressLint("InflateParams")
fun createItemView(context: Context, item: HomeItem): View {
    val layoutParams = LinearLayout.LayoutParams(
        MATCH_PARENT,
        MATCH_PARENT
    )

    val itemView = LayoutInflater.from(context).inflate(R.layout.home_item, null, false)
    itemView.layoutParams = layoutParams

    val imageView = itemView.findViewById<ImageView>(R.id.imageView)
    imageView.setImageDrawable(item.icon)

    val textView = itemView.findViewById<TextView>(R.id.textView)
    textView.text = item.label

    val innerLayout = getInnerLayout(context)
    innerLayout.addView(itemView)

    return innerLayout
}

@SuppressLint("MissingInflatedId")
fun createToolItemView(context: Context, item: HomeItem): View {
    val layoutParams = LinearLayout.LayoutParams(
        MATCH_PARENT,
        MATCH_PARENT
    )

    val itemView = LayoutInflater.from(context).inflate(R.layout.tool_item, null, false)
    itemView.layoutParams = layoutParams

    val imageView = itemView.findViewById<ImageView>(R.id.tool_image)
    imageView.setImageDrawable(item.icon)

    return itemView
}


@SuppressLint("MissingInflatedId")
fun createToolFolderView(context: Context, item: HomeItem, list: ArrayList<HomeItem>): View {
    val layoutParams = LinearLayout.LayoutParams(
        MATCH_PARENT,
        MATCH_PARENT
    )

    val itemView = LayoutInflater.from(context).inflate(R.layout.folder_icon, null, false)
    itemView.layoutParams = layoutParams

    val imageView = itemView.findViewById<ImageView>(R.id.icon_image)
    imageView.setImageDrawable(item.icon)

    val name = if (item.homeName != ""){
        item.homeName
    } else {
        item.label
    }

    val textView = itemView.findViewById<TextView>(R.id.icon_name)
    textView.text = name

    val imageView1 = itemView.findViewById<ImageView>(R.id.imageView1)
    val imageView2 = itemView.findViewById<ImageView>(R.id.imageView2)
    val imageView3 = itemView.findViewById<ImageView>(R.id.imageView3)
    val imageView4 = itemView.findViewById<ImageView>(R.id.imageView4)

    if (list.size > 0) {
        imageView1.setImageDrawable(list[0].icon)
    } else {
        imageView1.setImageDrawable(null)
    }
    if (list.size > 1) {
        imageView2.setImageDrawable(list[1].icon)
    } else {
        imageView2.setImageDrawable(null)
    }
    if (list.size > 2) {
        imageView3.setImageDrawable(list[2].icon)
    } else {
        imageView3.setImageDrawable(null)
    }
    if (list.size > 3) {
        imageView4.setImageDrawable(list[3].icon)
    } else {
        imageView4.setImageDrawable(null)
    }

    val innerLayout = getInnerLayout(context)
    innerLayout.addView(itemView)

    return itemView
}

fun getRowColumnToPosition(row: Int, column: Int): Int {
    var itemCount = -1
    var lastPosi = -1
    for(rowId in 0 until 10) {
        for(columnId in 0 until 5) {
            itemCount++

            if (rowId == row && columnId == column) {
                lastPosi = itemCount
                break
            }
        }
    }
    return lastPosi
}

fun checkPosition(view: View, point: ArrayList<Float>): ArrayList<Float> {
    val parentView = view.parent as? ViewGroup
    if (parentView != null) {
        point[0] += parentView.x
        point[1] += parentView.y

        return checkPosition(parentView, point)
    }

    return point
}

fun getViewCapture(view: View): Bitmap? {
    view.isDrawingCacheEnabled = true

    // Viewのキャッシュを取得
    val cache = view.drawingCache
    val screenShot = Bitmap.createBitmap(cache)
    view.isDrawingCacheEnabled = false
    return screenShot
}

fun getWidgetView(context: Context, mAppWidgetHost: AppWidgetHost, widgetId: Int): View? {

    val appWidgetProviderInfo2 =
        AppWidgetManager
            .getInstance(context)
            .getAppWidgetInfo(widgetId)

    try {
        val widgetLabel = appWidgetProviderInfo2.label
        val widgetIcon = context.packageManager.getDrawable(
            appWidgetProviderInfo2.provider.packageName,
            appWidgetProviderInfo2.icon,
            null
        )

        var hostView = mAppWidgetHost.createView(context, widgetId, appWidgetProviderInfo2)

        //hostView.setMinimumHeight(appWidgetProviderInfo2.minHeight)
        if (Build.VERSION.SDK_INT > 15) {
            //hostView.updateAppWidgetSize(null,
            //    widgetWidth, appWidgetProviderInfo2.minHeight,
            //    widgetHeight, appWidgetProviderInfo2.minHeight)
        }
        hostView.setAppWidget(widgetId, appWidgetProviderInfo2)

//        var lp: CellLayout.LayoutParams
//        if (hostView.layoutParams == null) {
//            lp = CellLayout.LayoutParams(0, 0, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
//        } else {
//            lp = hostView.layoutParams as CellLayout.LayoutParams
//            lp.cellX = 0
//            lp.cellY = 0
//            lp.cellHSpan = LinearLayout.LayoutParams.MATCH_PARENT
//            lp.cellVSpan = LinearLayout.LayoutParams.MATCH_PARENT
//        }

        var lp: LinearLayout.LayoutParams
        if (hostView.layoutParams == null) {
            lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        } else {
            lp = hostView.layoutParams as LinearLayout.LayoutParams
            lp.width = LinearLayout.LayoutParams.MATCH_PARENT
            lp.height = LinearLayout.LayoutParams.MATCH_PARENT
        }

        hostView.setPadding(0,0,0,0)

        hostView.layoutParams = lp



        val columnLayout = getInnerLayout(context)

        columnLayout.addView(hostView)

        return columnLayout
    }catch(e: Exception) {
        Log.e("TESTTEST", "error:" + e.message)

    }
    return null
}

private fun getInnerLayout(context: Context): LinearLayout {
    val columnLayout = LinearLayout(context)
    columnLayout.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
    )

    columnLayout.setPadding(5,5,5,5)
    //columnLayout.setBackgroundColor(Color.parseColor("#4039FFA3"))
    return columnLayout
}

@SuppressLint("InflateParams")
fun createMenuView(context: Context, width: Int): View {
    val layoutParams = LinearLayout.LayoutParams(
        width,
        WRAP_CONTENT
    )

    val itemView = LayoutInflater.from(context).inflate(R.layout.float_menu, null, false)
    itemView.layoutParams = layoutParams

    return itemView
}

@SuppressLint("InflateParams")
fun createToolListView(context: Context, width: Int): View {
    val layoutParams = LinearLayout.LayoutParams(
        width,
        WRAP_CONTENT
    )

    val itemView = LayoutInflater.from(context).inflate(R.layout.float_tool_list, null, false)
    itemView.layoutParams = layoutParams


    return itemView
}

@SuppressLint("InflateParams")
fun createFolderInItemListView(context: Context, width: Int, height: Int): View {
    val layoutParams = LinearLayout.LayoutParams(
        width,
        height
    )

    val itemView = LayoutInflater.from(context).inflate(R.layout.float_tool_list, null, false)
    itemView.layoutParams = layoutParams


    return itemView
}

// 拡張関数: Contextに対するdp変換関数
fun Context.pxToDp(px: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        px / resources.displayMetrics.density,
        resources.displayMetrics
    )
}

// 拡張関数: Intに対するdp変換関数
fun Int.pxToDp(context: Context): Float {
    return context.pxToDp(this.toFloat())
}

// 拡張関数: Floatに対するdp変換関数
fun Float.pxToDp(context: Context): Float {
    return context.pxToDp(this)
}


fun displaySize(context: Context) : Size {
    // DisplayMetricsオブジェクトを取得
    val displayMetrics = DisplayMetrics()

    // WindowManagerを取得してディスプレイのサイズを取得
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(displayMetrics)

    // 画面の幅と高さを取得
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels

    // 画面サイズを表示（例としてログ出力）
    println("Screen Width: $screenWidth px")
    println("Screen Height: $screenHeight px")

    return Size(screenWidth, screenHeight)
}

fun View.getSize(): Size {
    return Size(this.width, this.height)
}


// Contextに対する拡張関数
fun Context.dpToPx(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        resources.displayMetrics
    )
}

// Int型に対する拡張関数
fun Int.dpToPx(context: Context): Float {
    return context.dpToPx(this.toFloat())
}

// Float型に対する拡張関数
fun Float.dpToPx(context: Context): Float {
    return context.dpToPx(this)
}


fun hideKeyboard(context: Context) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = (context as Activity).currentFocus
    currentFocus?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}