package net.mikemobile.mikelauncher.ui.custom_float

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.data.HomeItem

class ToolItemListHolder(itemView: View): RecyclerView.ViewHolder(itemView)

class ListAdapter(
    private val context: Context,
    private val list: ArrayList<HomeItem>,
    private val callback: (HomeItem) -> Unit
): RecyclerView.Adapter<ToolItemListHolder>() {

    private val TAG = "ToolItemListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolItemListHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.common_list_item, parent, false)
        return ToolItemListHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToolItemListHolder, position: Int) {

        val icon = holder.itemView.findViewById(R.id.icon) as ImageView
        val label = holder.itemView.findViewById(R.id.label) as TextView
        val detail = holder.itemView.findViewById(R.id.detail) as TextView

        icon.setImageDrawable(list[position].icon)
        label.text = list[position].label
        detail.text = list[position].detail

        holder.itemView.setOnClickListener {
            callback.invoke(list[position])
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

}