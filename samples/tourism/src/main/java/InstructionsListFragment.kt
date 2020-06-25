import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.ionav.views.NavigationViaInstructionsFragment
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel

class InstructionsListFragment: NavigationViaInstructionsFragment() {
    override val model: TourismViewModel by activityViewModels()
}