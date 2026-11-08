package com.example.gameofbugs

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class GoldWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsync()

        GoldRepository.getInstance(context).loadRate { rate ->
            val views = RemoteViews(context.packageName, R.layout.widget_gold)
            val text = if (rate == null) "—" else String.format("%.0f", rate)
            views.setTextViewText(R.id.tvGoldRate, text)
            appWidgetManager.updateAppWidget(appWidgetIds, views)
            pending.finish()
        }
    }
}
