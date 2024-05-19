package net.mikemobile.mikelauncher.ui.applist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.constant.Global

class AppAdapter(
    private val inflater: LayoutInflater,
    private val onClick: (view: View, info: AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder =
        AppViewHolder(inflater.inflate(R.layout.list_item_application, parent, false))

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val info = list[position]
        holder.itemView.setOnClickListener { onClick(holder.icon, info) } // <- ココ
        holder.icon.setImageDrawable(info.icon)
        holder.label.text = info.label
        holder.packageName.text = info.componentName.packageName

        if (Global.homeItemData.checkHomeInApps( info.componentName.packageName, info.componentName.className)) {
            holder.itemView.setBackgroundResource(R.drawable.select_app)
        } else {
            holder.itemView.setBackgroundDrawable(null)
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
        val packageName: TextView = itemView.findViewById(R.id.packageName)
    }

    private var list: List<AppInfo> = emptyList()

    fun updateList(newList: List<AppInfo>) {
        val diff = DiffUtil.calculateDiff(DiffCallback(list, newList), true)
        list = newList
        diff.dispatchUpdatesTo(this)
    }
    private class DiffCallback(
        private val old: List<AppInfo>,
        private val new: List<AppInfo>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size
        override fun areItemsTheSame(op: Int, np: Int): Boolean =
            old[op].componentName == new[np].componentName
        override fun areContentsTheSame(op: Int, np: Int): Boolean =
            old[op].label == new[np].label && old[op].icon == new[np].icon
    }
}
