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

import org.apache.commons.lang.StringUtils;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.function.token.DigitToken;
import com.jaeksoft.searchlib.function.token.NoSpaceNoControlToken;

public class GroupExpression extends Expression {

	protected ArrayList<Expression> expressions;

	protected GroupExpression(Expression parent, char[] chars, int pos,
			QueryOperator queryOp, TermOperator termOp, String field)
			throws SyntaxError {
		super(parent);
		expressions = new ArrayList<Expression>();
		Expression previous = null;
		while (pos < chars.length) {
			if (Character.isWhitespace(chars[pos])) {
				pos++;
				continue;
			}
			Expression exp = nextExpression(previous, chars, pos, queryOp,
					termOp, field);
			if (exp == null) {
				pos++;
				break;
			}
			expressions.add(exp);
			previous = exp;
			pos = exp.nextPos;
		}
		nextPos = pos;
	}

	private Expression nextExpression(Expression previous, char[] chars,
			int pos, QueryOperator queryOp, TermOperator termOp, String field)
			throws SyntaxError {
		if (pos >= chars.length)
			return null;
		char ch = chars[pos];
		if (ch == '(')
			return new GroupExpression(this, chars, pos + 1, queryOp, termOp,
					field);
		if (ch == ')')
			return null;
		if (ch == '+')
			return nextExpression(this, chars, pos + 1, queryOp,
					TermOperator.REQUIRED, field);
		if (ch == '-')
			return nextExpression(this, chars, pos + 1, queryOp,
					TermOperator.FORBIDDEN, field);
		if (ch == '"')
			return new PhraseExpression(this, chars, pos + 1, termOp, field);
		if (ch == '^') {
			DigitToken token = new DigitToken(chars, pos + 1, '.');
			if (previous != null)
				previous.setBoost(token.value);
			return nextExpression(this, chars, pos + token.size + 2, queryOp,
					termOp, field);
		}
		NoSpaceNoControlToken token = new NoSpaceNoControlToken(chars, pos,
				null, TermExpression.forbiddenChars);
		if (token.size == 0)
			throw new SyntaxError("Term or operator missing", chars, pos);
		int newPos = pos + token.size;
		if (newPos < chars.length) {
			if (chars[newPos] == ':')
				return nextExpression(this, chars, newPos + 1, queryOp, termOp,
						token.word);
			if (chars[newPos] == ' ') {
				if ("NOT".equals(token.word))
					return nextExpression(this, chars, newPos + 1, queryOp,
							TermOperator.FORBIDDEN, field);
				if ("OR".equals(token.word))
					return nextExpression(this, chars, newPos + 1,
							QueryOperator.OR, termOp, field);
				if ("AND".equals(token.word))
					return nextExpression(this, chars, newPos + 1,
							QueryOperator.AND, termOp, field);
			}
		}
		return new TermExpression(this, chars, pos, queryOp, termOp, field);
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append('(');
		sb.append(StringUtils.join(expressions, ' '));
		sb.append(')');
	}

	@Override
	public void setBoost(float value) {
		for (Expression expression : expressions)
			expression.setBoost(value);
	}

}
