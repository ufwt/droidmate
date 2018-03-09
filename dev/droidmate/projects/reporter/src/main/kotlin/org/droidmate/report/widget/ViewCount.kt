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

import org.droidmate.exploration.data_aggregators.IExplorationLog
import org.droidmate.report.IReporter
import org.droidmate.report.TableDataFile
import org.droidmate.report.misc.apkFileNameWithUnderscoresForDots
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

class ViewCount(private val includePlots: Boolean) : IReporter {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ViewCount::class.java)
    }

    fun getPath(reportDir: Path, data: IExplorationLog): Path {
        return reportDir.resolve("${data.apkFileNameWithUnderscoresForDots}_viewCount.txt")
    }

    override fun write(reportDir: Path, rawData: List<IExplorationLog>) {
        rawData.forEach { data ->
            val dataTable = ViewCountTable(data)
            val reportPath = getPath(reportDir, data)
            val report = TableDataFile(dataTable, reportPath)
            log.info("Writing out report $report")
            report.write()
            if (includePlots) {
                log.info("Writing out plot $report")
                report.writeOutPlot()
            }
        }
    }

}