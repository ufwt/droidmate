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
package org.droidmate.report

import org.droidmate.exploration.data_aggregators.IExplorationLog
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

abstract class Reporter {
    protected val log: Logger = LoggerFactory.getLogger(Reporter::class.java)

    fun write(reportDir: Path, rawData: List<IExplorationLog>) {
        Files.createDirectories(reportDir)
        log.info("Writing out report ${this.javaClass.simpleName} to $reportDir")
        try {
            safeWrite(reportDir, rawData)
        } catch (e: Exception) {
            log.error("Unable to write the report ${this.javaClass.simpleName} to $reportDir. Exception: $e. Generating remaining reports.")
            log.error("Error stack trace:")
            e.printStackTrace()
        }
    }

    protected abstract fun safeWrite(reportDir: Path, rawData: List<IExplorationLog>)
}