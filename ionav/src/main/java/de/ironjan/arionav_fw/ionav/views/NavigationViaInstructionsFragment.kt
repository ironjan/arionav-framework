package de.ironjan.arionav_fw.ionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.ionav.views.debug.TextInstructionAdapter
import kotlinx.android.synthetic.main.fragment_navigation_via_text.*

open class NavigationViaInstructionsFragment : Fragment() {

    open val model: IonavViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_navigation_via_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val lifecycleOwner = viewLifecycleOwner

        val adapter = TextInstructionAdapter(lifecycleOwner, model)
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }
}