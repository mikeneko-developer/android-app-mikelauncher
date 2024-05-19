package net.mikemobile.mikelauncher.ui.custom_float

import android.content.Context
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.constant.Global.Companion.generateId
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.ui.home.createToolListView
import net.mikemobile.mikelauncher.ui.home.displaySize

class ToolItemListFloatDialog(context: Context, private val callback: (HomeItem) -> Unit) : BaseFloatingDialog(context) {
    override fun onCreate(context: Context): View? {

        val displaySize = displaySize(context)
        val width = displaySize.width / 10 * 8
        return createToolListView(context, width)
    }

    override fun onCreateView(context: Context, view: View) {
        val list = ArrayList<HomeItem>()

        list.add(HomeItem(
            -1,"",null,2,
            icon = Global.getToolIcon(context, 1),
            label = "ドロワー",
            "","",
            detail = "アプリ一覧を表示するボタン",
            toolId = 1
        ))

        list.add(HomeItem(
            -1,"",null,2,
            icon = Global.getToolIcon(context, 2),
            label = "フォルダー",
            "","",
            detail = "アプリをまとめるフォルダー",
            toolId = 2,
            folderId = generateId()
        ))

        val adapter = ListAdapter(context, list) {
            close()
            callback.invoke(it)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            close()
        }
    }
}