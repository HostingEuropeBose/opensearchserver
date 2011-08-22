/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;

public class CompiledAnalyzer extends Analyzer {

	private FilterFactory[] filters;

	protected CompiledAnalyzer(List<FilterFactory> sourceFilters,
			FilterScope scopeTarget) throws SearchLibException {
		List<FilterFactory> ff = new ArrayList<FilterFactory>();
		if (scopeTarget == FilterScope.INDEX)
			buildIndexList(sourceFilters, ff);
		else if (scopeTarget == FilterScope.QUERY)
			buildQueryList(sourceFilters, ff);
		filters = new FilterFactory[ff.size()];
		ff.toArray(filters);
	}

	private static void buildQueryList(List<FilterFactory> source,
			List<FilterFactory> target) throws SearchLibException {
		for (FilterFactory filter : source) {
			FilterScope scope = filter.getScope();
			if (scope == FilterScope.QUERY || scope == FilterScope.QUERY_INDEX) {
				filter.checkProperties();
				target.add(filter);
			}
		}
	}

	private static void buildIndexList(List<FilterFactory> source,
			List<FilterFactory> target) throws SearchLibException {
		for (FilterFactory filter : source) {
			FilterScope scope = filter.getScope();
			if (scope == FilterScope.INDEX || scope == FilterScope.QUERY_INDEX) {
				filter.checkProperties();
				target.add(filter);
			}
		}
	}

	@Override
	public TokenStream tokenStream(String fieldname, Reader reader) {
		TokenStream ts = new TokenStream(reader);
		for (FilterFactory filter : filters)
			ts = filter.create(ts);
		return ts;
	}

	public boolean isAnyToken(String fieldName, String value)
			throws IOException {
		return tokenStream(fieldName, new StringReader(value)).incrementToken();
	}

	public List<DebugTokenFilter> test(String text) throws IOException {
		List<DebugTokenFilter> list = new ArrayList<DebugTokenFilter>();
		StringReader reader = new StringReader(text);
		DebugTokenFilter lastDebugTokenFilter = new DebugTokenFilter(null,
				new TokenStream(reader));
		for (FilterFactory filter : filters) {
			DebugTokenFilter newDebugTokenFilter = new DebugTokenFilter(filter,
					filter.create(lastDebugTokenFilter));
			newDebugTokenFilter.incrementToken();
			list.add(newDebugTokenFilter);
			lastDebugTokenFilter = newDebugTokenFilter;
		}
		return list;
	}
}
