/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.query;

import com.jaeksoft.searchlib.schema.AnalyzerSelector;

/*
 * TODO Full implementation
 */
public class QueryParser {

	private String defaultField;

	private AnalyzerSelector analyzerSelector;

	public enum Operator {
		AND, OR
	}

	public QueryParser(String defaultField, AnalyzerSelector analyzerSelector) {
		this.defaultField = defaultField;
	}

	public Query parse(String query) {

		// get field name

		return null;
	}

	public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
		// TODO Auto-generated method stub

	}

	public void setPhraseSlop(int phraseSlop) {
		// TODO Auto-generated method stub

	}

	public void setDefaultOperator(Operator defaultOperator) {
		// TODO Auto-generated method stub

	}

	public void setLowercaseExpandedTerms(boolean b) {
		// TODO Auto-generated method stub

	}

}
