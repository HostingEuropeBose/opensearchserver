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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.token.DigitToken;
import com.jaeksoft.searchlib.function.token.NoSpaceNoControlToken;
import com.jaeksoft.searchlib.index.osse.OsseQuery;
import com.jaeksoft.searchlib.query.ParseException;
import com.sun.jna.Pointer;

public class GroupExpression extends Expression {

	protected ArrayList<Expression> expressions;

	protected GroupExpression(Expression parent, char[] chars, int pos,
			ExpressionContext context) throws ParseException {
		super(parent, context);
		expressions = new ArrayList<Expression>();
		Expression previous = null;
		while (pos < chars.length) {
			Expression exp = nextExpression(previous, chars, pos, context);
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
			int pos, ExpressionContext context) throws ParseException {
		if (pos >= chars.length)
			return null;
		char ch = chars[pos];
		if (Character.isWhitespace(ch))
			return nextExpression(previous, chars, pos + 1, context);
		if (ch == '(')
			return new GroupExpression(this, chars, pos + 1, context);
		if (ch == ')')
			return null;
		if (ch == '+')
			return nextExpression(this, chars, pos + 1,
					context.setTermOperator(TermOperator.REQUIRED));
		if (ch == '-')
			return nextExpression(this, chars, pos + 1,
					context.setTermOperator(TermOperator.FORBIDDEN));
		if (ch == '"')
			return new PhraseExpression(this, chars, pos + 1, context);
		if (ch == '^') {
			DigitToken token = new DigitToken(chars, pos + 1, '.');
			if (previous != null)
				previous.setBoost(token.value);
			return nextExpression(this, chars, pos + token.size + 1, context);
		}
		if (ch == '~') {
			DigitToken token = new DigitToken(chars, pos + 1, null);
			if (previous != null)
				previous.setPhraseSlop((int) token.value);
			return nextExpression(this, chars, pos + token.size + 1, context);
		}
		NoSpaceNoControlToken token = new NoSpaceNoControlToken(chars, pos,
				null, TermExpression.forbiddenChars);
		if (token.size == 0)
			throw new ParseException("Term or operator missing", chars, pos);
		int newPos = pos + token.size;
		if (newPos < chars.length) {
			if (chars[newPos] == ':')
				return nextExpression(this, chars, newPos + 1,
						context.setField(token.word));
			if (chars[newPos] == ' ') {
				if ("NOT".equals(token.word))
					return nextExpression(this, chars, newPos + 1,
							context.setTermOperator(TermOperator.FORBIDDEN));
				if ("OR".equals(token.word))
					return nextExpression(this, chars, newPos + 1,
							context.setQueryOperator(QueryOperator.OR));
				if ("AND".equals(token.word))
					return nextExpression(this, chars, newPos + 1,
							context.setQueryOperator(QueryOperator.AND));
			}
		}
		return new TermExpression(this, chars, pos, context);
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

	@Override
	public void setPhraseSlop(int value) {
		for (Expression expression : expressions)
			expression.setPhraseSlop(value);
	}

	@Override
	public Pointer execute(OsseQuery osseQuery) throws SearchLibException {
		Pointer cursor = null;
		for (Expression expression : expressions)
			cursor = osseQuery.combineCursor(cursor,
					expression.execute(osseQuery), expression.operator);
		return cursor;
	}
}
