package de.ironjan.arionav_fw.samples.tourism

import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.arionav.views.ArEnabledNavigationViaMapFragment
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel

class TourismNavigationViaMapFragment: ArEnabledNavigationViaMapFragment() {
    override val viewModel by activityViewModels<TourismViewModel>()

}