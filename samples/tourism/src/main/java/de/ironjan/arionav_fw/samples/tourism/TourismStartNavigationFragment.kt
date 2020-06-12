package de.ironjan.arionav_fw.samples.tourism

import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.arionav.views.ArEnabledStartNavigationFragment
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel

class TourismStartNavigationFragment: ArEnabledStartNavigationFragment() {
    override val viewModel by activityViewModels<TourismViewModel>()

}