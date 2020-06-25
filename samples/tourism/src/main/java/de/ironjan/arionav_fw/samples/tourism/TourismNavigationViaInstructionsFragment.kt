package de.ironjan.arionav_fw.samples.tourism

import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.arionav.views.ArEnabledNavigationViaInstructionsFragment
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel

class TourismNavigationViaInstructionsFragment: ArEnabledNavigationViaInstructionsFragment() {
    override val viewModel by activityViewModels<TourismViewModel>()
}