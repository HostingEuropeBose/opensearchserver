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

public class RootExpression extends GroupExpression {

	protected RootExpression(String defaultField,
			QueryOperator defaultOperator, int phraseSlop, String query)
			throws SyntaxError {
		super(null, query.toCharArray(), 0, new ExpressionContext(
				defaultOperator, defaultField, phraseSlop));
	}

	@Override
	public void setBoost(float value) {
	}

	public static void main(String[] argv) {
		String query = "title:(this is -a test)^10 OR title:(\"this is a test\")^10~5 NOT noindex";
		try {
			System.out.println(new RootExpression("content", QueryOperator.AND,
					2, query));
		} catch (SyntaxError e) {
			System.err.println(e.getMessage());
		}
	}

}
