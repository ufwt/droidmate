// DroidMate, an automated execution generator for Android apps.
// Copyright (C) 2012-2018 Konrad Jamrozik
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
// email: jamrozik@st.cs.uni-saarland.de
// web: www.droidmate.org
package org.droidmate.exploration.strategy.termination

import org.droidmate.exploration.strategy.WidgetContext

/**
 * Determines if exploration shall be terminated based on the explored widgets
 *
 * @author Nataniel P. Borges Jr.
 */
class AllWidgetsExploredTerminate : Terminate() {
    override fun getLogMessage(): String = ""

    override fun met(widgetContext: WidgetContext): Boolean {
        // All widgets have been explored, no need to continue exploration
        return !memory.isEmpty() && memory.areAllWidgetsExplored()
    }

    override fun start() {
        // Do nothing
    }

    override fun metReason(widgetContext: WidgetContext): String {
        return "All widgets have been explored at least once"
    }
}