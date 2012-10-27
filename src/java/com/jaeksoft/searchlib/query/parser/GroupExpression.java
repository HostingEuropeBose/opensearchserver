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

import java.util.ArrayList;

import com.jaeksoft.searchlib.function.expression.SyntaxError;

public class GroupExpression extends Expression {

	protected ArrayList<Expression> expressions;

	protected GroupExpression(RootExpression root, char[] chars, int pos)
			throws SyntaxError {
		super(root);
		expressions = new ArrayList<Expression>();
		while (pos < chars.length) {
			char ch = chars[pos];
			if (Character.isWhitespace(ch)) {
				pos++;
				continue;
			}
			Expression exp = nextExpression(ch, chars, pos);
			if (exp == null) {
				pos++;
				break;
			}
			expressions.add(exp);
			pos = exp.nextPos;
		}
		nextPos = pos;
	}

	private Expression nextExpression(char ch, char[] chars, int pos)
			throws SyntaxError {
		if (pos >= chars.length)
			return null;
		if (ch == '(')
			return new GroupExpression(root, chars, pos + 1);
		if (ch == ')')
			return null;
		if (ch == '+')
			return new RequireExpression(root, chars, pos + 1);
		if (ch == '-')
			return new ProhibitExpression(root, chars, pos + 1);
		if (ch == '"')
			return new QuotedExpression(root, chars, pos + 1);
		if (ch == '^')
			return new BoostExpression(root, chars, pos + 1);
		if (Character.isJavaIdentifierStart(ch))
			return new FieldExpression(root, chars, pos + 1);
		throw new SyntaxError("Syntax error", chars, pos);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('(');
		for (Expression expression : expressions)
			sb.append(expression.toString());
		sb.append(')');
		return sb.toString();
	}

}
