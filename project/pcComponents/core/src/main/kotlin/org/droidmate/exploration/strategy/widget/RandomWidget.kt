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
package org.droidmate.exploration.strategy.widget

import kotlinx.coroutines.experimental.joinChildren
import kotlinx.coroutines.experimental.runBlocking
import org.droidmate.configuration.Configuration
import org.droidmate.device.datatypes.statemodel.Widget
import org.droidmate.device.datatypes.statemodel.emptyId
import org.droidmate.device.datatypes.statemodel.features.ActionCounterMF
import org.droidmate.device.datatypes.statemodel.features.listOfSmallest
import org.droidmate.exploration.actions.ExplorationAction
import org.droidmate.exploration.strategy.ISelectableExplorationStrategy
import org.droidmate.exploration.strategy.StrategyPriority
import java.util.*

/**
 * Exploration strategy that select a (pseudo-)random widget from the screen.
 */
open class RandomWidget protected constructor(randomSeed: Long,
                                              private val priority: StrategyPriority = StrategyPriority.PURELY_RANDOM_WIDGET) : Explore() {
	protected val random = Random(randomSeed)
	private val counter: ActionCounterMF by lazy {
		(context.watcher.find { it is ActionCounterMF }
				?: ActionCounterMF().also { context.watcher.add(it) }) as ActionCounterMF
	}

	private fun mustRepeatLastAction(): Boolean {
		if (!this.context.isEmpty()) {

			// Last state was runtime permission
			return (currentState.isRequestRuntimePermissionDialogBox) &&
					// Has last action
					this.context.lastTarget != null &&
					// Has a state that is not a runtime permission
					this.context.getRecords().getStates()
							.filterNot { it.stateId == emptyId }
							.filterNot { it.isRequestRuntimePermissionDialogBox }
							.isNotEmpty() &&
					// Can re-execute the same action
					currentState.actionableWidgets
							.any { p -> context.lastTarget?.let { p.id == it.id } ?: false }
		}

		return false
	}

	private fun repeatLastAction(): ExplorationAction {
		val lastActionBeforePermission = currentState.let {
			!(it.isRequestRuntimePermissionDialogBox || it.stateId == emptyId)
		}

//        return lastActionBeforePermission.action
		TODO("extract WidgetId from recorded trace and look it up in current Context to choose as target")
	}

	protected open fun getAvailableWidgets(): List<Widget> {
		return currentState.actionableWidgets//.actionableWidgetsInfo
//                .filterNot { it.blackListed } //TODO
	}

	protected open fun chooseRandomWidget(): ExplorationAction {
		runBlocking { counter.job.joinChildren() }
		val candidates =
		// for each widget in this state the number of interactions
				counter.numExplored(currentState).entries.groupBy { it.value }.let {
					it.listOfSmallest()?.map { it.key }?.let { leastInState: List<Widget> ->
						// determine the subset of widgets which were least interacted with
						// if multiple widgets clicked with same frequency, choose the one least clicked over all states
						if (leastInState.size > 1) {
							leastInState.groupBy { counter.widgetCnt(it.uid) }.listOfSmallest()
						} else leastInState
					}
				}
						?: currentState.actionableWidgets

		assert(candidates.isNotEmpty())

		val chosenWidget = candidates[random.nextInt(candidates.size)]

		this.context.lastTarget = chosenWidget
		return chooseActionForWidget(chosenWidget)
	}

	protected open fun chooseActionForWidget(chosenWidget: Widget): ExplorationAction {

		while (!chosenWidget.canBeActedUpon()) {
			currentState.widgets.find { it.id == chosenWidget.parentId }
		}

		val actionList: MutableList<ExplorationAction> = mutableListOf()

		if (chosenWidget.longClickable)
			actionList.add(ExplorationAction.newWidgetExplorationAction(chosenWidget, longClick = true))

		if (chosenWidget.clickable)
			actionList.add(ExplorationAction.newWidgetExplorationAction(chosenWidget))

		if (chosenWidget.checked != null)
			actionList.add(ExplorationAction.newWidgetExplorationAction(chosenWidget))

		// TODO: Currently is doing a normal click. Replace for the swipe action (bellow)
		if (chosenWidget.scrollable)
			actionList.add(ExplorationAction.newWidgetExplorationAction(chosenWidget))

		/*if (chosenWidget.scrollable) {
				actionList.add(ExplorationAction.newWidgetExplorationAction(chosenWidget, 0, guiActionSwipe_right))
				actionList.add(ExplorationAction.newWidgetExplorationAction(chosenWidget, 0, guiActionSwipe_up))
				actionList.add(ExplorationAction.newWidgetExplorationAction(chosenWidget, 0, guiActionSwipe_left))
				actionList.add(ExplorationAction.newWidgetExplorationAction(chosenWidget, 0, guiActionSwipe_down))
		}*/

//        chosenWidget.actedUponCount++ //TODO this has to be implemented as Model Feature

		logger.debug("Chosen widget info: $chosenWidget")

		val maxVal = actionList.size
		assert(maxVal > 0)
		val randomIdx = random.nextInt(maxVal)
		return actionList[randomIdx]
	}

	override fun getFitness(): StrategyPriority {
		// Arbitrary established
		return this.priority
	}

	override fun chooseAction(): ExplorationAction {
		// Repeat previous action is last action was to click on a runtime permission dialog
		if (mustRepeatLastAction())
			return repeatLastAction()

		return chooseRandomWidget()
	}

	// region java overrides

	override fun equals(other: Any?): Boolean {
		if (other !is RandomWidget)
			return false

		return other.priority == this.priority
	}

	override fun hashCode(): Int {
		return this.priority.value.hashCode()
	}

	override fun toString(): String {
		return "${this.javaClass}\tPriority: ${this.priority}"
	}

	// endregion

	companion object {
		/**
		 * Creates a new exploration strategy instance
		 */
		fun build(cfg: Configuration): ISelectableExplorationStrategy {
			return RandomWidget(cfg.randomSeed.toLong())
		}
	}
}
