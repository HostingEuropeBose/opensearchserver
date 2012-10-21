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
package com.jaeksoft.searchlib.analysis.filter;

import java.io.IOException;

import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.TokenStream;

public abstract class AbstractCharFilter extends TokenStream {

	public AbstractCharFilter(FilterFactory filterFactory, TokenStream input) {
		super(filterFactory, input);
	}

	public abstract boolean isTokenChar(char ch);

	public boolean incrementToken() throws IOException {
		if (super.incrementToken())
			return true;
		while (input.incrementToken()) {
			TokenAttributes attributes = input.getAttributes();
			StringBuffer buffer = input.getBuffer();
			StringBuffer newToken = new StringBuffer(0);
			int start = input.getCurrentTokenPosition();
			int end = start + input.getCurrentTokenLength();
			int offsetStart = attributes.offsetStart;
			int tokenLength = 0;
			for (int i = start; i < end; i++) {
				char ch = buffer.charAt(i);
				if (isTokenChar(ch)) {
					if (tokenLength == 0)
						attributes.offsetStart = offsetStart;
					newToken.append(ch);
					tokenLength++;
				} else {
					attributes.offsetEnd = attributes.offsetStart + tokenLength
							- 1;
					addToken(newToken, attributes);
					newToken = new StringBuffer(0);
					tokenLength = 0;
				}
				offsetStart++;
			}
			attributes.offsetEnd = attributes.offsetStart + tokenLength - 1;
			addToken(newToken, attributes);
			if (super.incrementToken())
				return true;
		}
		return false;
	}
}
