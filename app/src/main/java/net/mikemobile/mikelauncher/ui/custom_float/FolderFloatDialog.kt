package net.mikemobile.mikelauncher.ui.custom_float

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.ui.home.createFolderInItemListView
import net.mikemobile.mikelauncher.ui.home.displaySize
import net.mikemobile.mikelauncher.ui.home.folder.FolderAdapter

class FolderFloatDialog(
    context: Context,
    private val folder: HomeItem,
    private val list: ArrayList<HomeItem>,
    private val callback: (HomeItem) -> Unit,
    private val callbackEditTitle: (HomeItem) -> Unit,
    private val callbackLongClick: (HomeItem) -> Unit) : BaseFloatingDialog(context) {

    override fun onCreate(context: Context): View? {

        val displaySize = displaySize(context)
        val width = displaySize.width / 10 * 8
        val height = displaySize.height / 5 * 3
        return createFolderInItemListView(context, width, WRAP_CONTENT)
    }

    private var adapter: FolderAdapter? = null
    private var titleView: TextView? = null


    override fun onCreateView(context: Context, view: View) {

        val adapter = FolderAdapter(context, list,{
            close()
            callback.invoke(it)
        }) {
            callbackLongClick.invoke(it)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setLayoutManager(GridLayoutManager(context, 4)) // 2列のグリッド
        recyclerView.adapter = adapter

        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            close()
        }

        val textView = view.findViewById<TextView>(R.id.folder_name)

        val text = if (folder.homeName != ""){
            folder.homeName
        } else {
            folder.label
        }

        textView.text = text
        textView.setOnClickListener {
            callbackEditTitle.invoke(folder)
        }

        this.titleView = textView
        this.adapter = adapter

        // アイテムの最大数を設定
        val maxItemCount = 8
        setRecyclerViewHeightBasedOnItems(recyclerView, adapter, maxItemCount)
    }

    private fun setRecyclerViewHeightBasedOnItems(recyclerView: RecyclerView, adapter: FolderAdapter, maxItemCount: Int) {
        recyclerView.post {
            val itemCount = adapter.itemCount
            if (itemCount > maxItemCount) {
                // リサイクラービューの1アイテムあたりの高さを計算
                val itemView = adapter.createViewHolder(recyclerView, adapter.getItemViewType(0)).itemView
                itemView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val itemHeight = itemView.measuredHeight

                // 最大アイテム数に基づいてリサイクラービューの高さを設定
                val recyclerViewHeight = itemHeight * maxItemCount
                val layoutParams = recyclerView.layoutParams
                layoutParams.height = recyclerViewHeight
                recyclerView.layoutParams = layoutParams
            }
        }
    }

    fun open(ownerView: ConstraintLayout?) {
        super.open(ownerView, true)
    }

    fun update(list: ArrayList<HomeItem>) {
        adapter?.list = list
        adapter?.notifyDataSetChanged()
    }

    fun updateTitle(folder: HomeItem) {

        val text = if (folder.homeName != ""){
            folder.homeName
        } else {
            folder.label
        }

        titleView?.text = text
    }

}