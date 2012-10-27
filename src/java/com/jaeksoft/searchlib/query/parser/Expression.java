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

public abstract class Expression {

	public static enum TermOperator {

		ORUNDEFINED(""), REQUIRED("+"), FORBIDDEN("-");

		private String chars;

		private TermOperator(String chars) {
			this.chars = chars;
		}

		@Override
		public String toString() {
			return chars;
		}
	}

	public static enum QueryOperator {
		AND, OR;
	}

	public final static TermOperator ResolveOp(TermOperator termOp,
			QueryOperator queryOp) {
		switch (queryOp) {
		case AND:
			switch (termOp) {
			case ORUNDEFINED:
				return TermOperator.REQUIRED;
			case REQUIRED:
				return TermOperator.REQUIRED;
			case FORBIDDEN:
				return TermOperator.FORBIDDEN;
			}
		case OR:
		default:
			return termOp;
		}
	}

	protected Expression parent;

	protected int nextPos;

	protected Expression(Expression parent) {
		this.parent = parent;
		this.nextPos = 0;
	}

	protected abstract void toString(StringBuffer sb);

	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	public abstract void setBoost(float value);

	public abstract void setPhraseSlop(int value);
}
