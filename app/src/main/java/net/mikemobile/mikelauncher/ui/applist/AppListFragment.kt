package net.mikemobile.mikelauncher.ui.applist

import android.content.*
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import net.mikemobile.mikelauncher.R

class AppListFragment : Fragment() {

    companion object {
        fun newInstance() = AppListFragment()
    }

    private lateinit var viewModel: AppListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.applist_fragment, container, false)
    }

    var recyclerView: RecyclerView? = null
    var adapter: AppAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AppListViewModel::class.java)
        // TODO: Use the ViewModel

        recyclerView = this.view?.findViewById(R.id.recyclerview)


        adapter = AppAdapter(layoutInflater) { view, info ->
            info.launch(requireContext(), view)
        }
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        //recyclerView?.layoutManager = GridLayoutManager(requireContext(), 5)
        //recyclerView?.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        //StaggeredGridLayoutManager


        recyclerView?.setOnApplyWindowInsetsListener { _, insets ->
            recyclerView?.setPadding(
                0,
                insets.systemWindowInsetTop,
                0,
                insets.systemWindowInsetBottom
            )
            insets
        }
        adapter?.updateList(viewModel.create(requireContext()))

        context?.registerReceiver(packageReceiver, IntentFilter().also {
            it.addAction(Intent.ACTION_PACKAGE_ADDED)
            it.addAction(Intent.ACTION_PACKAGE_REMOVED)
            it.addAction(Intent.ACTION_PACKAGE_CHANGED)
            it.addAction(Intent.ACTION_PACKAGE_REPLACED)
            it.addDataScheme("package")
        })

    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            adapter?.updateList(viewModel.create(requireContext()))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(packageReceiver)
    }



}