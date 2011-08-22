/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.function.expression;

import com.jaeksoft.searchlib.query.CustomScoreQuery;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.query.ValueSourceQuery;

public class ScoreFunctionQuery extends CustomScoreQuery {

	private Query subQuery;

	private Expression expression;

	protected ScoreFunctionQuery(Query subQuery, Expression expression)
			throws SyntaxError {
		super(subQuery);
		this.subQuery = subQuery;
		this.expression = expression;
	}

	protected ScoreFunctionQuery(Query subQuery,
			ValueSourceQuery valueSourceQuery, Expression expression)
			throws SyntaxError {
		super(subQuery, valueSourceQuery);
		this.subQuery = subQuery;
		this.expression = expression;
	}

	protected ScoreFunctionQuery(Query subQuery,
			ValueSourceQuery[] valueSourceQueries, Expression expression)
			throws SyntaxError {
		super(subQuery, valueSourceQueries);
		this.subQuery = subQuery;
		this.expression = expression;
	}

	@Override
	public float customScore(int doc, float subQueryScore, float valSrcScore) {
		return expression.getValue(subQueryScore, valSrcScore);
	}

	@Override
	public float customScore(int doc, float subQueryScore, float[] valSrcScores) {
		return expression.getValue(subQueryScore, valSrcScores);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("score(");
		if (subQuery != null)
			sb.append(subQuery.toString());
		sb.append(')');
		if (expression != null)
			sb.append(expression.toString());
		return sb.toString();
	}
}
