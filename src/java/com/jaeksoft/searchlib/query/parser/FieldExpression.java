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

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.function.token.JavaIdentifierToken;

public class FieldExpression extends Expression {

	private String field;

	protected FieldExpression(RootExpression root, char[] chars, int pos)
			throws SyntaxError {
		super(root);
		JavaIdentifierToken token = new JavaIdentifierToken(chars, pos, null);
		field = token.word;
		pos += token.size;
		if (pos >= chars.length)
			throw new SyntaxError("Colon missing", chars, pos);
		if (chars[pos++] != ':')
			throw new SyntaxError("Colon missing", chars, pos);
		pos++;
		nextPos = pos;
	}

	public String getField() {
		return field;
	}

}
