package net.mikemobile.mikelauncher.ui.custom_float

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
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
    private val list: ArrayList<HomeItem>,
    private val callback: (HomeItem) -> Unit,
    private val callbackLongClick: (HomeItem) -> Unit) : BaseFloatingDialog(context) {

    override fun onCreate(context: Context): View? {

        val displaySize = displaySize(context)
        val width = displaySize.width / 10 * 8
        val height = displaySize.height / 5 * 3
        return createFolderInItemListView(context, width, WRAP_CONTENT)
    }

    private var adapter: FolderAdapter? = null
    override fun onCreateView(context: Context, view: View) {

        adapter = FolderAdapter(context, ArrayList<HomeItem>(),{
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
    }

    fun open(ownerView: ConstraintLayout?) {
        adapter?.list = list
        super.open(ownerView, true)

    }
}