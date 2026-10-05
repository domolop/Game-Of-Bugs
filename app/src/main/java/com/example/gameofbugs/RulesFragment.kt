package com.example.gameofbugs

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class RulesFragment : Fragment(R.layout.tab_rules) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvRules = view.findViewById<TextView>(R.id.tvRules)
        val rulesHtml = resources.openRawResource(R.raw.rules).bufferedReader().use { it.readText() }
        tvRules.text = Html.fromHtml(rulesHtml, Html.FROM_HTML_MODE_LEGACY)
    }
}
