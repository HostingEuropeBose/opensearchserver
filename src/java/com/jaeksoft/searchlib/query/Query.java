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

import java.io.IOException;
import java.util.Set;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexReader;
import com.jaeksoft.searchlib.index.osse.OsseQuery;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.query.parser.RootExpression;

public class Query {

	private final RootExpression rootExpression;

	public Query(RootExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	public RootExpression getRootExpression() {
		return this.rootExpression;
	}

	public Query rewrite(IndexReader indexReader) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void extractTerms(Set<Term> terms) {
		// TODO Auto-generated method stub
	}

	public void execute(OsseQuery osseQuery) throws SearchLibException {
		rootExpression.execute(osseQuery);
	}

}
