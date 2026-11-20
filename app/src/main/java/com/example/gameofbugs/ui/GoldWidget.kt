package com.example.gameofbugs.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.gameofbugs.R
import com.example.gameofbugs.data.GoldRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GoldWidget : AppWidgetProvider(), KoinComponent {

    private val goldRepository: GoldRepository by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsync()

        CoroutineScope(Dispatchers.Main).launch {
            val rate = goldRepository.loadRate()
            val views = RemoteViews(context.packageName, R.layout.widget_gold)
            val text = if (rate == null) "—" else String.format("%.0f", rate)
            views.setTextViewText(R.id.tvGoldRate, text)
            appWidgetManager.updateAppWidget(appWidgetIds, views)
            pending.finish()
        }
    }
}
