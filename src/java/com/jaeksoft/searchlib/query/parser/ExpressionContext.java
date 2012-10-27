/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.query.parser;

import com.jaeksoft.searchlib.query.parser.Expression.QueryOperator;
import com.jaeksoft.searchlib.query.parser.Expression.TermOperator;

public class ExpressionContext {

	final protected QueryOperator queryOp;
	final protected TermOperator termOp;
	final protected String field;
	final protected int phraseSlop;

	private ExpressionContext(QueryOperator queryOp, TermOperator termOp,
			String field, int phraseSlop) {
		this.queryOp = queryOp;
		this.termOp = termOp;
		this.field = field;
		this.phraseSlop = phraseSlop;
	}

	public ExpressionContext(QueryOperator queryOp, String field, int phraseSlop) {
		this(queryOp, TermOperator.ORUNDEFINED, field, phraseSlop);
	}

	final public ExpressionContext setTermOperator(TermOperator termOp) {
		return new ExpressionContext(queryOp, termOp, field, phraseSlop);
	}

	final public ExpressionContext setField(String field) {
		return new ExpressionContext(queryOp, termOp, field, phraseSlop);
	}

	final public ExpressionContext setQueryOperator(QueryOperator queryOp) {
		return new ExpressionContext(queryOp, termOp, field, phraseSlop);
	}

	final public ExpressionContext setPhraseSlop(int phraseSlop) {
		return new ExpressionContext(queryOp, termOp, field, phraseSlop);
	}
}
