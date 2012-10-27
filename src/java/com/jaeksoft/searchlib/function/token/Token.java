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

package com.jaeksoft.searchlib.function.token;

public abstract class Token {

	public int size;

	private char[] additionalChars;

	private char[] forbiddenChars;

	protected Token(char[] chars, int pos, char[] additionalChars,
			char[] forbiddenChars) {
		this.additionalChars = additionalChars;
		this.forbiddenChars = forbiddenChars;
		StringBuffer token = new StringBuffer();
		size = 0;
		boolean escaped = false;
		while (pos < chars.length) {
			char ch = chars[pos++];
			if (!escaped) {
				if (ch == '\\') {
					escaped = true;
					continue;
				}
				if (!charIsValid(ch))
					break;
			}
			token.append(ch);
			size++;
		}
		set(token);
	}

	protected Boolean charIsValid(char ch) {
		if (additionalChars != null) {
			for (char c : additionalChars)
				if (c == ch)
					return true;
		}
		if (forbiddenChars != null) {
			for (char c : forbiddenChars)
				if (c == ch)
					return false;
		}
		return null;
	}

	protected abstract void set(StringBuffer token);

}
