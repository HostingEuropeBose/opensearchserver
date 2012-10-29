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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.FieldContent;

public class CompiledAnalyzer extends Analyzer {

	private FilterFactory[] filters;

	protected CompiledAnalyzer(List<FilterFactory> sourceFilters,
			FilterScope scopeTarget) throws SearchLibException {
		super(null);
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

	public TokenStream tokenStream(Reader reader) throws IOException {
		TokenStream ts = new TokenStream(reader);
		for (FilterFactory filter : filters)
			ts = filter.create(ts);
		return ts;
	}

	public boolean isAnyToken(String value) throws IOException {
		return tokenStream(new StringReader(value)).incrementToken();
	}

	public List<TokenStream> test(String text) throws IOException {
		List<TokenStream> list = new ArrayList<TokenStream>();
		StringReader reader = new StringReader(text);
		TokenStream lastTokenFilter = new TokenStream(reader);
		for (FilterFactory filter : filters) {
			lastTokenFilter = filter.create(lastTokenFilter);
			list.add(lastTokenFilter);
		}
		while (lastTokenFilter.incrementToken())
			;
		return list;
	}

	public void extractTerms(String text, Set<String> termSet)
			throws IOException {
		StringReader reader = new StringReader(text);
		TokenStream ts = tokenStream(reader);
		ts = new TermSetTokenFilter(termSet, ts);
		ts.incrementToken();
	}

	public void populate(String text, FieldContent fieldContent)
			throws IOException {
		StringReader reader = new StringReader(text);
		TokenStream ts = tokenStream(reader);
		ts = new FieldContentPopulateFilter(fieldContent, ts);
		ts.incrementToken();
	}

}
