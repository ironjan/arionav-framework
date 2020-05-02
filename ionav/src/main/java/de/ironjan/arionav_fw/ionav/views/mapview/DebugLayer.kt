package de.ironjan.arionav_fw.ionav.views.mapview

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import org.oscim.backend.CanvasAdapter
import org.oscim.backend.canvas.Color
import org.oscim.backend.canvas.Paint
import org.oscim.core.GeoPoint
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.map.Map

class DebugLayer(map: Map?, val context: Context) : ItemizedLayer<MarkerItem>(map, null as MarkerSymbol?),
    ModelDrivenUiComponent<IonavViewModel> {
    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.route.observe(lifecycleOwner, Observer {
            updateLayer(it)
        })
    }

    private fun updateLayer(it: PathWrapper?) {

        val canvas = CanvasAdapter.newCanvas()
        val paint = CanvasAdapter.newPaint()
        paint.setTypeface(Paint.FontFamily.DEFAULT, Paint.FontStyle.NORMAL)
        paint.setTextSize(12 * CanvasAdapter.getScale())
        paint.strokeWidth = 2 * CanvasAdapter.getScale()
        paint.color = Color.BLACK

        val items = it?.instructions?.mapIndexed { instructionNumber, instr ->
            val bitmap = CanvasAdapter.newBitmap((180 * CanvasAdapter.getScale()).toInt(), (20 * CanvasAdapter.getScale()).toInt(), 0)
            canvas.setBitmap(bitmap)
            canvas.fillColor(Color.GRAY)

            instr.points.mapIndexed { pointNr, point ->


                val name = "$instructionNumber.$pointNr: ${instr.name}, ${instr.distance}"

                canvas.drawText(name, 3 * CanvasAdapter.getScale(), 17 * CanvasAdapter.getScale(), paint)
                val markerItem = MarkerItem(name, "", GeoPoint(point.lat, point.lon))
                val markerSymbol = MarkerSymbol(bitmap, 0.5f, 0f)
                markerItem.marker = markerSymbol

                markerItem
            }
        }?.flatten() ?: return

        val instructionHelper = InstructionHelper(context)
        val test = it?.instructions?.mapIndexed {instructionNumber, instr ->
            instr.points.mapIndexed { pointNr, point ->
                val textInstr  = instructionHelper.getTextFor(instr.sign)
                val name = "$instructionNumber.$pointNr: $textInstr ${instr.name}, ${instr.distance}"

                name
            }
        }?.flatten()?.joinToString ("\n")

        val foo =it?.instructions?.mapIndexed{ index, instruction ->
            instruction.points.count()
        }
        removeAllItems()
        addItems(items)
    }

}