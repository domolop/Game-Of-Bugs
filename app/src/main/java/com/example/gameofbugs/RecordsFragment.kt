package com.example.gameofbugs

import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

class RecordsFragment : Fragment(R.layout.tab_records) {

    private var adapter: RecordAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<ListView>(R.id.lvRecords)
        val emptyView = view.findViewById<TextView>(R.id.tvRecordsEmpty)

        listView.emptyView = emptyView
        adapter = RecordAdapter(requireContext(), emptyList())
        listView.adapter = adapter

        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {
        GameRepository.getInstance(requireContext()).loadRecords { items ->
            adapter?.update(items)
        }
    }
}
