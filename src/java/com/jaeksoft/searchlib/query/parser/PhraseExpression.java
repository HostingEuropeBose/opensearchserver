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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jaeksoft.searchlib.function.token.NoSpaceNoControlToken;

public class PhraseExpression extends AbstractTermExpression {

	private List<String> terms;

	private int phraseSlop;

	private char[] forbiddenCharacters = { '"' };

	protected PhraseExpression(Expression parent, char[] chars, int pos,
			ExpressionContext context) {
		super(parent, context);
		this.phraseSlop = context.phraseSlop;
		terms = new ArrayList<String>(0);
		for (;;) {
			NoSpaceNoControlToken token = new NoSpaceNoControlToken(chars, pos,
					null, forbiddenCharacters);
			if (token.size == 0) {
				nextPos = pos;
				return;
			}
			terms.add(token.word);
			pos += token.size + 1;
		}
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append(operator);
		sb.append(field);
		sb.append(":\"");
		sb.append(StringUtils.join(terms, ' '));
		sb.append("\"~");
		sb.append(phraseSlop);
		sb.append('^');
		sb.append(boost);
	}

	@Override
	public void setPhraseSlop(int phraseSlop) {
		this.phraseSlop = phraseSlop;
	}
}
