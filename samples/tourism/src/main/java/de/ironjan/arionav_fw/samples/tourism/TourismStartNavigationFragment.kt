package de.ironjan.arionav_fw.samples.tourism

import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.ionav.views.StartNavigationFragment
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel

class TourismStartNavigationFragment: StartNavigationFragment() {
    override val viewModel by activityViewModels<TourismViewModel>()

}