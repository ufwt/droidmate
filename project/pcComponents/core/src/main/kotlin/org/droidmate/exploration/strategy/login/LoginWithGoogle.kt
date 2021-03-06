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
package org.droidmate.exploration.strategy.login

import org.droidmate.deviceInterface.guimodel.ExplorationAction
import org.droidmate.exploration.statemodel.Widget
import org.droidmate.errors.UnexpectedIfElseFallthroughError
import org.droidmate.exploration.actions.click
import org.droidmate.exploration.strategy.ISelectableExplorationStrategy
import org.droidmate.exploration.strategy.widget.ExplorationStrategy
import org.droidmate.misc.DroidmateException

@Suppress("unused")
class LoginWithGoogle : ExplorationStrategy() {
	private val DEFAULT_ACTION_DELAY = 1000
	private val RES_ID_PACKAGE = "com.google.android.gms"
	private val RES_ID_ACCOUNT = "$RES_ID_PACKAGE:uid/account_display_name"
	private val RES_ID_ACCEPT = "$RES_ID_PACKAGE:uid/accept_button"

	private var state = LoginState.Start

	private fun canClickGoogle(widgets: List<Widget>): Boolean {
		return (this.state == LoginState.Start) &&
				widgets.any { it.text.toLowerCase() == "google" }
	}

	private fun clickGoogle(widgets: List<Widget>): ExplorationAction {
		val widget = widgets.firstOrNull { it.text.toLowerCase() == "google" }

		if (widget != null) {
			this.state = LoginState.AccountDisplayed
			return widget.click()
		}

		throw DroidmateException("The exploration shouldn' have reached this point.")
	}

	private fun canClickAccount(widgets: List<Widget>): Boolean {
		return widgets.any { it.resourceId == RES_ID_ACCOUNT }
	}

	private fun clickAccount(widgets: List<Widget>): ExplorationAction {
		val widget = widgets.firstOrNull { it.resourceId == RES_ID_ACCOUNT }

		if (widget != null) {
			this.state = LoginState.AccountSelected
			return widget.click()
		}

		throw DroidmateException("The exploration shouldn' have reached this point.")
	}

	private fun canClickAccept(widgets: List<Widget>): Boolean {
		return widgets.any { it.resourceId == RES_ID_ACCEPT }
	}

	private fun clickAccept(widgets: List<Widget>): ExplorationAction {
		val widget = widgets.firstOrNull { it.resourceId == RES_ID_ACCEPT }

		if (widget != null) {
			this.state = LoginState.Done
			return widget.click()
		}

		throw DroidmateException("The exploration shouldn' have reached this point.")
	}

	/*override fun mustPerformMoreActions(): Boolean {
		return !eContext.getCurrentState().widgets
				.any {
					it.visible &&
							it.resourceId == RES_ID_ACCOUNT
				}
	}*/

	// TODO
	/*override fun getFitness(): StrategyPriority {
		// Not the correct app, or already logged in
		if (this.state == LoginState.Done)
			return StrategyPriority.NONE

		val widgets = eContext.getCurrentState().actionableWidgets

		if (canClickGoogle(widgets) ||
				canClickAccount(widgets) ||
				canClickAccept(widgets))
			return StrategyPriority.SPECIFIC_WIDGET

		return StrategyPriority.NONE
	}*/

	private fun getWidgetAction(widgets: List<Widget>): ExplorationAction {
		// Can click on login
		return when {
			canClickGoogle(widgets) -> clickGoogle(widgets)
			canClickAccount(widgets) -> clickAccount(widgets)
			canClickAccept(widgets) -> clickAccept(widgets)
			else -> throw UnexpectedIfElseFallthroughError("Should not have reached this point. $widgets")
		}
	}

	override fun chooseAction(): ExplorationAction {
		return if (eContext.getCurrentState().isRequestRuntimePermissionDialogBox) {
			val widget = eContext.getCurrentState().widgets.let { widgets ->
				widgets.firstOrNull { it.resourceId == "com.android.packageinstaller:id/permission_allow_button" }
						?: widgets.first { it.text.toUpperCase() == "ALLOW" }
			}
			widget.click()
		} else {
			val widgets = eContext.getCurrentState().widgets
			getWidgetAction(widgets)
		}
	}

	override fun equals(other: Any?): Boolean {
		return other is LoginWithGoogle
	}

	override fun hashCode(): Int {
		return this.RES_ID_PACKAGE.hashCode()
	}

	override fun toString(): String {
		return javaClass.simpleName
	}

	companion object {
		/**
		 * Creates a new exploration strategy instance to login using google
		 */
		fun build(): ISelectableExplorationStrategy {
			return LoginWithGoogle()
		}
	}

}