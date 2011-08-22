/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.filter;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.TokenStream;

public class EdgeNGramFilter extends FilterFactory {

	public enum Side {
		FRONT, BACK;

		public static Side getSide(String value) {
			for (Side s : values())
				if (s.name().equalsIgnoreCase(value))
					return s;
			return null;
		}
	}

	public final static int DEFAULT_MIN_GRAM_SIZE = 1;
	public final static int DEFAULT_MAX_GRAM_SIZE = 64;
	public final static Side DEFAULT_SIDE = Side.FRONT;

	private int min;

	private int max;

	private Side side;

	private final static Object[] SIDE_VALUE_LIST = { Side.FRONT.name(),
			Side.BACK.name() };

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.MIN_GRAM,
				Integer.toString(DEFAULT_MIN_GRAM_SIZE), null);
		addProperty(ClassPropertyEnum.MAX_GRAM,
				Integer.toString(DEFAULT_MAX_GRAM_SIZE), null);
		addProperty(ClassPropertyEnum.SIDE, DEFAULT_SIDE.name(),
				SIDE_VALUE_LIST);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.MIN_GRAM)
			min = Integer.parseInt(value);
		else if (prop == ClassPropertyEnum.MAX_GRAM)
			max = Integer.parseInt(value);
		else if (prop == ClassPropertyEnum.SIDE)
			side = Side.getSide(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		// TODO Auto-generated method stub
		return null;
	}

}
