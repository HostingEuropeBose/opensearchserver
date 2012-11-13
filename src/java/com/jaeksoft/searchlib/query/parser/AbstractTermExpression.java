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

public abstract class AbstractTermExpression extends Expression {

	protected String field = null;

	protected float boost = 1.0F;

	protected final TermOperator operator;

	public AbstractTermExpression(Expression parent, ExpressionContext context) {
		super(parent);
		this.field = context.field;
		this.operator = ResolveOp(context.termOp, context.queryOp);
	}

	@Override
	public void setBoost(float boost) {
		this.boost = boost;
	}

	@Override
	final public TermOperator getOperator() {
		return operator;
	}
}
