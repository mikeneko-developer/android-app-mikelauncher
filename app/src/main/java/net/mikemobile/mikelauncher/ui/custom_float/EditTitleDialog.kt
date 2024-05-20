package net.mikemobile.mikelauncher.ui.custom_float

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.ui.home.createEditTitleView
import net.mikemobile.mikelauncher.ui.home.createFolderInItemListView
import net.mikemobile.mikelauncher.ui.home.displaySize
import net.mikemobile.mikelauncher.ui.home.folder.FolderAdapter

class EditTitleDialog(
    context: Context,
    private val item: HomeItem,
    private val callback: (HomeItem) -> Unit) : BaseFloatingDialog(context) {

    override fun onCreate(context: Context): View? {

        val displaySize = displaySize(context)
        val width = displaySize.width / 10 * 8
        return createEditTitleView(context, width)
    }

    override fun onCreateView(context: Context, view: View) {

        val textView = view.findViewById<EditText>(R.id.edit_title)
        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            item.homeName = textView.text.toString()
            callback.invoke(item)
            close()
        }

        val text = if (item.homeName != ""){
            item.homeName
        } else {
            item.label
        }

        textView.setText(text)


    }

    fun open(ownerView: ConstraintLayout?) {
        super.open(ownerView, true)

    }
}