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

public class RootExpression extends Expression {

	private GroupExpression group;

	protected RootExpression(String field, QueryOperator queryOp, String query)
			throws SyntaxError {
		super(null);
		group = new GroupExpression(this, query.toCharArray(), 0, queryOp,
				TermOperator.ORUNDEFINED, field);
	}

	@Override
	public void setBoost(float value) {
	}

	public static void main(String[] argv) {
		String query = "title:(this is -a test)^10 OR title:(\"this is a test\")^10 NOT noindex";
		try {
			System.out.println(new RootExpression("content", QueryOperator.AND,
					query));
		} catch (SyntaxError e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	protected void toString(StringBuffer sb) {
		group.toString(sb);
	}

}
