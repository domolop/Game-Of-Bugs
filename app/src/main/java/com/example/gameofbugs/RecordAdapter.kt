package com.example.gameofbugs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordAdapter(
    private val context: Context,
    private var items: List<RecordItem>
) : BaseAdapter() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): RecordItem = items[position]

    override fun getItemId(position: Int): Long = items[position].userId

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_record, parent, false)

        val item = getItem(position)

        view.findViewById<TextView>(R.id.tvRecordName).text = item.displayName
        view.findViewById<TextView>(R.id.tvRecordScore).text = "Очки: ${item.bestScore} • Промахи: ${item.misses}"
        view.findViewById<TextView>(R.id.tvRecordDetails).text = buildString {
            append("Сложность: ${item.difficulty}")
            append(" • Курс: ${item.course}")
            append(" • Скорость: ${item.gameSpeed}")
            append(" • Жуков: ${item.maxBugs}")
            append(" • Раунд: ${item.roundDurationSeconds} сек.")
        }
        view.findViewById<TextView>(R.id.tvRecordDate).text =
            dateFormat.format(Date(item.finishedAt))

        return view
    }

    fun update(newItems: List<RecordItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
