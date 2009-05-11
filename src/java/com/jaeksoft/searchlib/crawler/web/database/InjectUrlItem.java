/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.database;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import com.jaeksoft.searchlib.index.IndexDocument;

public class InjectUrlItem {

	public enum Status {
		UNDEFINED("Undefined"), INJECTED("Injected"), MALFORMATED(
				"Malformated url"), ALREADY("Already injected"), ERROR(
				"Unknown Error");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private String badUrl;
	private URL url;
	private Status status;

	private InjectUrlItem() {
		status = Status.UNDEFINED;
		badUrl = null;
	}

	public InjectUrlItem(PatternUrlItem patternUrl) {
		this();
		try {
			url = patternUrl.extractUrl(true);
		} catch (MalformedURLException e) {
			status = Status.MALFORMATED;
			badUrl = patternUrl.getPattern();
			url = null;
		}
	}

	public InjectUrlItem(String u) {
		this();
		try {
			url = new URL(u);
		} catch (MalformedURLException e) {
			status = Status.MALFORMATED;
			badUrl = u;
			url = null;
		}
	}

	public String getUrl() {
		if (url == null)
			return badUrl;
		return url.toExternalForm();
	}

	public URL getURL() {
		return url;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status v) {
		status = v;
	}

	public void populate(IndexDocument indexDocument) {
		indexDocument.set("url", getUrl());
		indexDocument.set("when", UrlItem.getWhenDateFormat()
				.format(new Date()));
		URL url = getURL();
		if (url != null)
			indexDocument.set("host", url.getHost());
		indexDocument.set("fetchStatus", FetchStatus.UN_FETCHED.value);
		indexDocument.set("parserStatus", ParserStatus.NOT_PARSED.value);
		indexDocument.set("indexStatus", IndexStatus.NOT_INDEXED.value);
	}

	public IndexDocument getIndexDocument() {
		IndexDocument indexDocument = new IndexDocument();
		populate(indexDocument);
		return indexDocument;
	}
}