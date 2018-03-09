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
package org.droidmate.report.widget

import org.droidmate.device.datatypes.IWidget
import org.droidmate.exploration.actions.ExplorationRecord
import org.droidmate.exploration.data_aggregators.IExplorationLog
import org.droidmate.misc.uniqueString
import org.droidmate.report.CountsPartitionedByTimeTable
import org.droidmate.report.misc.actionableWidgets
import org.droidmate.report.misc.clickedWidget
import org.droidmate.report.misc.itemsAtTime

class ViewCountTable(data: IExplorationLog) : CountsPartitionedByTimeTable(
        data.getExplorationTimeInMs(),
        listOf(
                headerTime,
                headerViewsSeen,
                headerViewsClicked
        ),
        listOf(
                data.uniqueSeenActionableViewsCountByTime,
                data.uniqueClickedViewsCountByTime
        )
) {

    companion object {

        const val headerTime = "Time_seconds"
        const val headerViewsSeen = "Actionable_unique_views_seen"
        const val headerViewsClicked = "Actionable_unique_views_clicked"

        private val IExplorationLog.uniqueSeenActionableViewsCountByTime: Map<Long, Iterable<String>>
            get() {
                return this.uniqueViewCountByPartitionedTime(
                        extractItems = { it.actionableWidgets }
                )
            }

        private val IExplorationLog.uniqueClickedViewsCountByTime: Map<Long, Iterable<String>>
            get() {
                return this.uniqueViewCountByPartitionedTime(extractItems = { it.clickedWidget })
            }

        private fun IExplorationLog.uniqueViewCountByPartitionedTime(
                extractItems: (ExplorationRecord) -> Iterable<IWidget>): Map<Long, Iterable<String>> {

            return this.logRecords.itemsAtTime(
                    startTime = this.explorationStartTime,
                    extractTime = { it.getAction().timestamp },
                    extractItems = extractItems
            ).mapValues {
                val widgets = it.value
                widgets.map { it.uniqueString }
            }
        }
    }
}
