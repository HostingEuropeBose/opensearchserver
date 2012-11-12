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

import com.jaeksoft.searchlib.index.IndexReader;
import com.jaeksoft.searchlib.scoring.CustomScoreProvider;

/*
 * TODO full implementation
 */
public class CustomScoreQuery extends Query {

	public CustomScoreQuery(Query subQuery) {
		super(null);
	}

	public CustomScoreQuery(Query subQuery, ValueSourceQuery valueSourceQuery) {
		super(null);
	}

	public CustomScoreQuery(Query subQuery,
			ValueSourceQuery[] valueSourceQueries) {
		super(null);
	}

	public float customScore(int doc, float subQueryScore, float valSrcScore) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float customScore(int doc, float subQueryScore, float[] valSrcScores) {
		// TODO Auto-generated method stub
		return 0;
	}

	public CustomScoreProvider getCustomScoreProvider(IndexReader reader) {
		// TODO Auto-generated method stub
		return null;
	}

	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

}
