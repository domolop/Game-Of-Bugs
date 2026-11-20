package com.example.gameofbugs.ui

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gameofbugs.R
import com.example.gameofbugs.viewmodel.RecordsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordsFragment : Fragment(R.layout.tab_records) {

    private val records: RecordsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<ListView>(R.id.lvRecords)
        val emptyView = view.findViewById<TextView>(R.id.tvRecordsEmpty)

        val adapter = RecordAdapter(requireContext(), emptyList())
        listView.emptyView = emptyView
        listView.adapter = adapter

        records.records.observe(viewLifecycleOwner) { items ->
            adapter.update(items)
        }
    }

    override fun onResume() {
        super.onResume()
        records.loadRecords()
    }
}
