// DroidMate, an automated execution generator for Android apps.
// Copyright (C) 2012-2018. Saarland University
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// Current Maintainers:
// Nataniel Borges Jr. <nataniel dot borges at cispa dot saarland>
// Jenny Hotzkow <jenny dot hotzkow at cispa dot saarland>
//
// Former Maintainers:
// Konrad Jamrozik <jamrozik at st dot cs dot uni-saarland dot de>
//
// web: www.droidmate.org

package org.droidmate.exploration.modelFeatures

import kotlinx.coroutines.experimental.CoroutineName
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.NonCancellable.isActive
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.newCoroutineContext
import org.droidmate.deviceInterface.exploration.ExplorationAction
import org.droidmate.deviceInterface.exploration.Rectangle
import org.droidmate.explorationModel.config.ConcreteId
import org.droidmate.explorationModel.config.ModelConfig
import org.droidmate.explorationModel.interaction.StateData
import org.droidmate.explorationModel.interaction.Widget
import org.droidmate.explorationModel.config.ConfigProperties
import org.droidmate.explorationModel.visibleOuterBounds
import org.droidmate.misc.deleteDir
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.CoroutineContext

/** use this function to create a sequence of screen images in which the interacted target is highlighted by a red boarder */
class ImgTraceMF(val cfg: ModelConfig) : ModelFeature() {
	override val context: CoroutineContext = newCoroutineContext(context = CoroutineName("ImgTraceMF"), parent = job)

	private val targetDir = (cfg.baseDir.resolve("ModelFeatures/imgTrace"))
	init {
		job = Job(parent = (this.job)) // we don't want to wait for other modelFeatures (or having them wait for us), therefore create our own (child) job
		targetDir.deleteDir()
		Files.createDirectories(targetDir)
	}

	var i: AtomicInteger = AtomicInteger(0)
	override suspend fun onNewInteracted(traceId: UUID, actionIdx: Int, action: ExplorationAction, targetWidgets: List<Widget>, prevState: StateData, newState: StateData) {
		// check if we have any screenshots to process
		if(!cfg[ConfigProperties.ModelProperties.imgDump.states]) return

		val step = i.incrementAndGet()-1
		val screenFile = java.io.File(cfg.statePath(prevState.stateId, fileExtension = ".png"))

		while(isActive && !screenFile.exists()) delay(100)
		while(isActive && !screenFile.canRead()) delay(100)
		if(!isActive) return
		if(!screenFile.exists()) return // thread was canceled but no file to process is ready yet

		val targetFile = File("${targetDir.toAbsolutePath()}${File.separator}$step-${action.id}-$action.png")

		screenFile.copyTo(targetFile, overwrite = true) // copy file to trace directory
		if(prevState.widgets.isEmpty()) return  // no widgets exist which we could highlight

		val stateImg = ImageIO.read(targetFile)
		val visibleAppElems = prevState.widgets.filter { !it.isKeyboard && it.visibleBoundaries.isNotEmpty() }
		textColor = Color.white
		shapeColor = Color.LIGHT_GRAY
		highlightWidget(stateImg, visibleAppElems.filter { !it.hasUncoveredArea }, 0)
		shapeColor = Color.GRAY
		highlightWidget(stateImg, visibleAppElems.filter { it.hasUncoveredArea }, 0)


		textColor = Color.orange
		shapeColor = Color.orange
		highlightWidget(stateImg, prevState.actionableWidgets, 0, visibleAppElems)

		if(targetWidgets.isNotEmpty()) {
			shapeColor = Color.red
			textColor = Color.magenta
			highlightWidget(stateImg, targetWidgets, step)
		}
		ImageIO.write(stateImg,"png",targetFile)
	}
}

fun Widget.completeVisibleBounds(visibleWidgets: List<Widget>): List<Rectangle>{
	val areas = LinkedList<Rectangle>().apply { this.addAll(visibleBoundaries) }
	visibleWidgets.forEach { if(it.parentHash==idHash) areas.addAll(it.visibleBoundaries)}
	return areas
}

var shapeColor: Color = Color.red
var textColor: Color = Color.magenta
fun highlightWidget(stateImg: BufferedImage, targetWidgets: List<Widget>, idxOffset: List<Int>, visibleWidgets: List<Widget> = emptyList()){
	stateImg.createGraphics().apply{
		stroke = BasicStroke(10F)
		font = Font("TimesRoman", Font.PLAIN, 60)

		val targetsPerAction = targetWidgets.mapIndexed{ i,t -> Pair(idxOffset[i],t)}.groupBy { it.first }

		val targetCounter: MutableMap<ConcreteId,LinkedList<Pair<Int,Int>>> = HashMap() // used to compute offsets in the number string
		// compute the list of indicies for each widget-target (for labeling)
		targetsPerAction.forEach{ (idx, targets) ->
			targets.forEachIndexed{ index, (_,t) ->
				targetCounter.compute(t.id) { _, indicies -> (indicies ?: LinkedList()).apply { add(Pair(idx,index)) }}
			}
		}
		// highlight all targets and add text labels
		targetWidgets.forEach{ w: Widget ->
			paint = shapeColor// reset color for next shape drawing
			with(visibleOuterBounds(w.completeVisibleBounds(visibleWidgets))) {
				drawOval(this)
				// draw the label number for the element
				val text = targetCounter[w.id]!!.joinToString(separator = ", ") { if (it.first != 0) "${it.first}.${it.second}" else "${it.second}" }
				if (text.length > 20) font = Font("TimesRoman", Font.PLAIN, 20)
				paint = textColor// for better visibility use a different color then the boarder
				drawString(text, leftX + (width / 10), topY + (height / 10))
			}
			font = Font("TimesRoman", Font.PLAIN, 60) // reset font to bigger font
		}
	}
}
fun highlightWidget(stateImg: BufferedImage, targetWidgets: List<Widget>, idxOffset: Int = 0, visibleWidgets: List<Widget> = emptyList())
	= highlightWidget(stateImg, targetWidgets, (0 until targetWidgets.size).map { idxOffset }, visibleWidgets)

private fun Int.resize() = if(this<5) 10 else this

fun Graphics.drawOval(bounds: Rectangle){
	this.drawOval(bounds.leftX,bounds.topY,bounds.width.resize(),bounds.height.resize())
}
