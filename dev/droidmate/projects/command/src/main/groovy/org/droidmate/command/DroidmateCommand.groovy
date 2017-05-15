// DroidMate, an automated execution generator for Android apps.
// Copyright (C) 2012-2016 Konrad Jamrozik
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
package org.droidmate.command

import groovy.util.logging.Slf4j
import org.droidmate.configuration.Configuration
import org.droidmate.misc.ThrowablesCollection

@Slf4j
abstract class DroidmateCommand
{

  abstract void execute(Configuration cfg) throws ThrowablesCollection

   static DroidmateCommand build(
    boolean report, boolean inline, boolean unpack, Configuration cfg)
  {
    assert [report, inline, unpack].count {it} <= 1

    if (report)
      return new ReportCommand()
    else if (inline)
      return InlineCommand.build()
    else if (unpack)
      return UnpackCommand.build()
    else
      return ExploreCommand.build(cfg)
  }
}
