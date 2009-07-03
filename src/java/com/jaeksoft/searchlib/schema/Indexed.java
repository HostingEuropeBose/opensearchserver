/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.schema;

import org.apache.lucene.document.Field.Index;

public enum Indexed {

	YES(
			"The content of the field is indexed, therefore a query can search on that field."),

	NO(
			"The content of the field is not indexed, therefore a query cannot search on that field.");

	private String description;

	private Indexed(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getValue() {
		return name().toLowerCase();
	}

	public static Indexed fromValue(String value) {
		for (Indexed fs : values())
			if (fs.name().equalsIgnoreCase(value))
				return fs;
		return Indexed.NO;
	}

	public Index getLuceneIndex(String indexAnalyzer) {
		return indexAnalyzer == null ? Index.NOT_ANALYZED : Index.ANALYZED;
	}

}