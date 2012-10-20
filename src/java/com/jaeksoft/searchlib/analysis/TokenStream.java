/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;
import java.io.Reader;

public class TokenStream {

	final protected TokenStream input;
	final private StringBuffer buffer = new StringBuffer(0);
	final private IntArrayList positionList = new IntArrayList(0);
	final private IntArrayList lengthList = new IntArrayList(0);
	private int currentIncrementPosition = 0;
	private int currentTokenPosition = 0;
	private int currentTokenLength = 0;

	public TokenStream(TokenStream input) {
		this.input = input;
	}

	public TokenStream(Reader reader) throws IOException {
		input = null;
		if (reader != null) {
			char[] cbuf = new char[65536];
			int l;
			while ((l = reader.read(cbuf, 0, cbuf.length)) != -1)
				buffer.append(cbuf, 0, l);
			l = buffer.length();
			if (l > 0) {
				positionList.add(0);
				positionList.add(l);
			}
		}
	}

	final protected void addToken(StringBuffer tokenBuffer) {
		if (tokenBuffer == null)
			return;
		int len = tokenBuffer.length();
		if (len == 0)
			return;
		positionList.add(buffer.length());
		lengthList.add(len);
		buffer.append(tokenBuffer);
	}

	final public int getCurrentTokenPosition() {
		return currentTokenPosition;
	}

	final public int getCurrentTokenLength() {
		return currentTokenLength;
	}

	final protected boolean isTokenAvailable() {
		return currentIncrementPosition < positionList.size();
	}

	final public StringBuffer getBuffer() {
		return buffer;
	}

	public boolean incrementToken() throws IOException {
		if (!isTokenAvailable())
			return false;
		currentTokenPosition = positionList.getInt(currentIncrementPosition);
		currentTokenLength = lengthList.getInt(currentIncrementPosition);
		currentIncrementPosition++;
		return true;
	}
}
