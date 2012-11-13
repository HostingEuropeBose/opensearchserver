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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.token.NoSpaceNoControlToken;
import com.jaeksoft.searchlib.index.osse.OsseQuery;
import com.jaeksoft.searchlib.query.ParseException;
import com.sun.jna.Pointer;

public class TermExpression extends AbstractTermExpression {

	private String term = null;

	public final static char[] forbiddenChars = { '(', ')', '+', '-', '"', '^',
			':' };

	protected TermExpression(Expression parent, char[] chars, int pos,
			ExpressionContext context) throws ParseException {
		super(parent, context);
		NoSpaceNoControlToken token = new NoSpaceNoControlToken(chars, pos,
				null, forbiddenChars);
		if (token.size == 0)
			throw new ParseException("Term expression expected");
		term = token.word;
		nextPos = pos + token.size;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append(operator);
		sb.append(field);
		sb.append(':');
		sb.append(term);
		sb.append('^');
		sb.append(boost);
	}

	@Override
	public void setPhraseSlop(int phraseSlop) {
	}

	@Override
	public Pointer execute(OsseQuery osseQuery) throws SearchLibException {
		return osseQuery.createTermCursor(field, term, operator);
	}

}
