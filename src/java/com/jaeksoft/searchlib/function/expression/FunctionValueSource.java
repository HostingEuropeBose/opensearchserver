/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.function.expression;

import com.jaeksoft.searchlib.function.source.ByteFieldSource;
import com.jaeksoft.searchlib.function.source.FloatFieldSource;
import com.jaeksoft.searchlib.function.source.IntFieldSource;
import com.jaeksoft.searchlib.function.source.OrdFieldSource;
import com.jaeksoft.searchlib.function.source.ReverseOrdFieldSource;
import com.jaeksoft.searchlib.function.source.ShortFieldSource;
import com.jaeksoft.searchlib.function.source.ValueSource;

public class FunctionValueSource {

	protected ValueSource valueSource;

	protected String func;

	protected String field;

	protected int pos;

	FunctionValueSource(String func, String field, int pos) throws SyntaxError {
		this.field = field;
		this.func = func;
		this.pos = pos;
		if ("ord".equals(func))
			valueSource = new OrdFieldSource(field);
		else if ("rord".equalsIgnoreCase(func))
			valueSource = new ReverseOrdFieldSource(field);
		else if ("byte".equalsIgnoreCase(func))
			valueSource = new ByteFieldSource(field);
		else if ("float".equalsIgnoreCase(func))
			valueSource = new FloatFieldSource(field);
		else if ("int".equalsIgnoreCase(func))
			valueSource = new IntFieldSource(field);
		else if ("short".equalsIgnoreCase(func))
			valueSource = new ShortFieldSource(field);
		if (valueSource == null)
			throw new SyntaxError("Unknown function");

	}

	public boolean sameFuncField(FunctionValueSource fvs) {
		if (!fvs.field.equals(field))
			return false;
		if (!fvs.func.equalsIgnoreCase(func))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(func);
		sb.append('(');
		sb.append(field);
		sb.append(')');
		return sb.toString();

	}

}
