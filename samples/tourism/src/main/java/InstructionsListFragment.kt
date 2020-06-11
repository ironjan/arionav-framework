import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.ionav.views.TextInstructionListFragment
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel

class InstructionsListFragment: TextInstructionListFragment() {
    override val model: TourismViewModel by activityViewModels()
}