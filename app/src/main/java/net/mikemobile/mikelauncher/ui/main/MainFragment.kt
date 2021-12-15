package net.mikemobile.mikelauncher.ui.main

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.mikemobile.mikelauncher.R
import android.appwidget.AppWidgetHostView




class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }


    val XXXX = 111111
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel

        val appWidgetHost = AppWidgetHost(this.context, XXXX)
        appWidgetHost.startListening() //…(1)

        /**
        val appWidgetId: Int = appWidget.getAppWidgetId()
        val appWidgetProviderInfo = AppWidgetManager.getInstance(this.context).getAppWidgetInfo(appWidgetId)

//…(2)
        val appWidgetHostView = appWidgetHost.createView(this.context, appWidgetId, appWidgetProviderInfo)
        appWidgetHostView.layoutParams = layoutParams
        appWidgetHostView.setAppWidget(appWidgetId, appWidgetProviderInfo)
        */

    }

}