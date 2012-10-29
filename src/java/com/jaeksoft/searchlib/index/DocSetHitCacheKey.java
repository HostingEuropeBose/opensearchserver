/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.IOException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.filter.FilterListCacheKey;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.BoostQuery;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.AnalyzerSelector;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.scoring.AdvancedScore;

public class DocSetHitCacheKey implements CacheKeyInterface<DocSetHitCacheKey> {

	private String query;
	private Boolean facet;
	private String sortListCacheKey;
	private FilterListCacheKey filterListCacheKey;
	private String boostQueryCacheKey;
	private String advancedScoreCacheKey;

	public DocSetHitCacheKey(SearchRequest searchRequest,
			SchemaField defaultField, AnalyzerSelector analyzerSelector)
			throws ParseException, SyntaxError, SearchLibException, IOException {
		query = searchRequest.getQuery().toString();
		facet = searchRequest.isFacet();
		sortListCacheKey = searchRequest.getSortFieldList().getCacheKey();
		filterListCacheKey = new FilterListCacheKey(
				searchRequest.getFilterList(), defaultField, analyzerSelector);
		boostQueryCacheKey = BoostQuery.getCacheKey(searchRequest
				.getBoostingQueries());
		advancedScoreCacheKey = AdvancedScore.getCacheKey(searchRequest
				.getAdvancedScore());
	}

	@Override
	public int compareTo(DocSetHitCacheKey r) {
		int c;
		if ((c = query.compareTo(r.query)) != 0)
			return c;
		if ((c = facet.compareTo(r.facet)) != 0)
			return c;
		if ((c = filterListCacheKey.compareTo(r.filterListCacheKey)) != 0)
			return c;
		if ((c = sortListCacheKey.compareTo(r.sortListCacheKey)) != 0)
			return c;
		if ((c = boostQueryCacheKey.compareTo(r.boostQueryCacheKey)) != 0)
			return c;
		if ((c = advancedScoreCacheKey.compareTo(r.advancedScoreCacheKey)) != 0)
			return c;
		return 0;
	}
}
