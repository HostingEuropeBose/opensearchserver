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

package com.jaeksoft.searchlib.autocompletion;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.InvalidPropertiesFormatException;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class AutoCompletionManager implements Closeable {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final Config config;

	private TreeSet<AutoCompletionItem> autoCompItems;

	private final static String autoCompletionSubDirectory = "autocompletion";

	private final File autoCompletionDirectory;

	public AutoCompletionManager(Config config) throws SearchLibException,
			InvalidPropertiesFormatException, IOException {
		this.config = config;
		autoCompletionDirectory = new File(config.getDirectory(),
				autoCompletionSubDirectory);
		if (!autoCompletionDirectory.exists())
			autoCompletionDirectory.mkdir();
		autoCompItems = new TreeSet<AutoCompletionItem>();
		String[] autoCompDirs = autoCompletionDirectory
				.list(DirectoryFileFilter.INSTANCE);
		if (autoCompDirs == null)
			return;
		for (String autoCompDir : autoCompDirs) {
			System.out.println(autoCompDir);
		}
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			if (autoCompItems == null)
				return;
			for (AutoCompletionItem item : autoCompItems)
				IOUtils.closeQuietly(item);
		} finally {
			rwl.w.unlock();
		}
	}

	public Collection<AutoCompletionItem> getItems() {
		rwl.r.lock();
		try {
			return autoCompItems;
		} finally {
			rwl.r.unlock();
		}
	}

	private AutoCompletionItem find(AutoCompletionItem searchItem) {
		AutoCompletionItem foundItem = autoCompItems.ceiling(searchItem);
		if (foundItem == null)
			return null;
		return searchItem.equals(foundItem) ? foundItem : null;
	}

	public AutoCompletionItem getItem(String name) throws SearchLibException {
		rwl.r.lock();
		try {
			return find(new AutoCompletionItem(config, name));
		} finally {
			rwl.r.unlock();
		}
	}

	public void add(AutoCompletionItem currentItem) throws SearchLibException {
		rwl.w.lock();
		try {
			if (find(currentItem) != null)
				throw new SearchLibException(
						"This name is already taken by another item");
			currentItem.save();
		} finally {
			rwl.w.unlock();
		}
	}

	public File getDirectory() {
		return autoCompletionDirectory;
	}
}
