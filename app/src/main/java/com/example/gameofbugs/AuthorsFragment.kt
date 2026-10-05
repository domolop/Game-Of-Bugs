package com.example.gameofbugs

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.fragment.app.Fragment

class AuthorsFragment : Fragment(R.layout.tab_authors) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lvAuthors = view.findViewById<ListView>(R.id.lvAuthors)
        val authors = listOf(
            Author("Скуртяин Д.Е.", R.drawable.author_skuryatin),
            Author("Кумов Д.В.", R.drawable.author_kumov)
        )
        lvAuthors.adapter = AuthorAdapter(requireContext(), authors)
    }
}
