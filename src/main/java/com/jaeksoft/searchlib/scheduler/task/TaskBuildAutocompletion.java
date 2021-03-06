/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.scheduler.task;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionItem;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;

public class TaskBuildAutocompletion extends TaskAbstract {

	final private TaskPropertyDef propBuffersize = new TaskPropertyDef(
			TaskPropertyType.textBox, "Buffer size", "Buffer size", null, 10);

	final private TaskPropertyDef propItemName = new TaskPropertyDef(
			TaskPropertyType.textBox, "Item name", "Item name", null, 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propBuffersize };

	@Override
	public String getName() {
		return "Build autocompletion";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propBuffersize)
			return "1000";
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		String p = properties.getValue(propBuffersize);
		int bufferSize = 1000;
		if (p != null && p.length() > 0)
			bufferSize = Integer.parseInt(p);
		if (bufferSize <= 0)
			bufferSize = 1000;
		String name = properties.getValue(propItemName);
		AutoCompletionItem autoCompItem = client.getAutoCompletionManager()
				.getItem(name);
		autoCompItem.build(14400, bufferSize, taskLog);
	}
}
