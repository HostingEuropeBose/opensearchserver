/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class PropertyManager {

	private File propFile;

	private Properties properties;

	protected PropertyItem<Integer> indexDocumentBufferSize;
	protected PropertyItem<Boolean> crawlEnabled;
	protected PropertyItem<Integer> maxThreadNumber;

	protected PropertyManager(File file) throws IOException {
		propFile = file;
		properties = new Properties();
		if (propFile.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(propFile);
				properties.loadFromXML(inputStream);
			} catch (IOException e) {
				throw e;
			} finally {
				if (inputStream != null)
					inputStream.close();
			}
		}
		indexDocumentBufferSize = new PropertyItem<Integer>(this,
				"indexDocumentBufferSize", 1000);
		maxThreadNumber = newIntegerProperty("maxThreadNumber", 10);
		crawlEnabled = newBooleanProperty("crawlEnabled", false);
	}

	protected void save() throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(propFile);
			properties.storeToXML(fos, "");
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	protected PropertyItem<Integer> newIntegerProperty(String name,
			Integer defaultValue) throws NumberFormatException, IOException {
		PropertyItem<Integer> propertyItem = new PropertyItem<Integer>(this,
				name, defaultValue);
		String value = properties.getProperty(name);
		if (value != null)
			propertyItem.initValue(Integer.parseInt(value));
		return propertyItem;
	}

	protected PropertyItem<Boolean> newBooleanProperty(String name,
			Boolean defaultValue) {
		PropertyItem<Boolean> propertyItem = new PropertyItem<Boolean>(this,
				name, defaultValue);
		String value = properties.getProperty(name);
		if (value != null)
			propertyItem.initValue("1".equals(value)
					|| "true".equalsIgnoreCase(value)
					|| "yes".equalsIgnoreCase(value));
		return propertyItem;
	}

	protected PropertyItem<String> newStringProperty(String name,
			String defaultValue) {
		PropertyItem<String> propertyItem = new PropertyItem<String>(this,
				name, defaultValue);
		String value = properties.getProperty(name);
		if (value != null)
			propertyItem.initValue(value);
		return propertyItem;
	}

	public void put(PropertyItem<?> propertyItem) throws IOException {
		propertyItem.put(properties);
		save();
	}

	public PropertyItem<Boolean> getCrawlEnabled() {
		return crawlEnabled;
	}

	public PropertyItem<Integer> getIndexDocumentBufferSize() {
		return indexDocumentBufferSize;
	}

	public PropertyItem<Integer> getMaxThreadNumber() {
		return maxThreadNumber;
	}

}
