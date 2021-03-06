/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.file.process.CrawlFileMaster;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;

public class TaskFileCrawlerStart extends TaskAbstract {

	final private TaskPropertyDef propRunOnce = new TaskPropertyDef(
			TaskPropertyType.listBox, "Run once", "Run once",
			"Choose if the crawler will run once or indefinitely", 10);

	final private TaskPropertyDef[] taskPropertyDefs = { propRunOnce };

	@Override
	public String getName() {
		return "File crawler - start";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties) {
		if (propertyDef == propRunOnce)
			return ClassPropertyEnum.BOOLEAN_LIST;
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propRunOnce)
			return ClassPropertyEnum.BOOLEAN_LIST[0];
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		CrawlFileMaster crawlMaster = client.getFileCrawlMaster();
		if (crawlMaster.isRunning()) {
			taskLog.setInfo("Already running");
			return;
		}
		boolean runOnce = Boolean
				.parseBoolean(properties.getValue(propRunOnce));
		crawlMaster.start(runOnce);
		if (!crawlMaster.waitForStart(600))
			taskLog.setInfo("Not started after 10 minutes");
		else
			taskLog.setInfo("Started");
		if (runOnce)
			crawlMaster.waitForEnd(60 * 24 * 30);
	}

}
