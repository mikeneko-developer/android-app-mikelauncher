package net.mikemobile.mikelauncher.ui.home.folder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.data.HomeItem


class FolderAdapter(
    private val context: Context,
    var list: ArrayList<HomeItem>,
    private val callback: (HomeItem) -> Unit,
    private val callbackLongClick: (HomeItem) -> Unit
): RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    private val TAG = "FolderAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val icon = holder.itemView.findViewById(R.id.imageView) as ImageView
        val label = holder.itemView.findViewById(R.id.textView) as TextView

        icon.setImageDrawable(list[position].icon)
        label.text = list[position].label

        holder.itemView.setOnClickListener {
            callback.invoke(list[position])
        }
        holder.itemView.setOnLongClickListener {
            callbackLongClick.invoke(list[position])
            return@setOnLongClickListener true
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

}