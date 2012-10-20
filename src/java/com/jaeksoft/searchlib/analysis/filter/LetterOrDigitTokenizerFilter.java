/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

public class LetterOrDigitTokenizerFilter extends FilterFactory {

	public class LetterOrDigitTokenizer extends TokenStream {

		public LetterOrDigitTokenizer(TokenStream input) {
			super(input);
		}

		public boolean incrementToken() throws IOException {
			if (isTokenAvailable())
				return true;
			while (input.incrementToken()) {
				StringBuffer buffer = input.getBuffer();
				StringBuffer newToken = new StringBuffer(0);
				int start = input.getCurrentTokenPosition();
				int end = start + input.getCurrentTokenLength();
				for (int i = start; i < end; i++) {
					char ch = buffer.charAt(i);
					if (Character.isLetterOrDigit(ch)) {
						newToken.append(ch);
					} else {
						addToken(newToken);
						newToken = new StringBuffer(0);
					}
				}
				addToken(newToken);
				if (isTokenAvailable())
					return true;
			}
			return false;
		}
	}

	@Override
	public TokenStream create(TokenStream input) {
		return new LetterOrDigitTokenizer(input);
	}

}
