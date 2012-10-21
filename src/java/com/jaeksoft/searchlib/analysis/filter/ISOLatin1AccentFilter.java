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

import com.ibm.icu.text.Transliterator;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.TokenStream;

public class ISOLatin1AccentFilter extends FilterFactory {

	private class ISOLatin1AccentTokenStream extends TokenStream {

		private final Transliterator accentsconverter;

		public ISOLatin1AccentTokenStream(FilterFactory filterFactory,
				TokenStream input) {
			super(filterFactory, input);
			accentsconverter = Transliterator
					.getInstance("NFD; [:Nonspacing Mark:] Remove; NFC");
		}

		public boolean incrementToken() throws IOException {
			if (!input.incrementToken())
				return false;
			addToken(accentsconverter.transliterate(input.getCurrentTerm()),
					input.getAttributes());
			return super.incrementToken();
		}
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new ISOLatin1AccentTokenStream(this, tokenStream);
	}

}
