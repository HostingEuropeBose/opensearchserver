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
import java.util.Iterator;

public class TokenStream implements Iterable<String> {

	public class TokenAttributes {

		public Integer offsetStart;
		public Integer offsetEnd;
		public Integer positionIncrement;

		private TokenAttributes(int currentIncrementPosition) {
			if (offsetStartList.size() > currentIncrementPosition)
				offsetStart = offsetStartList.get(currentIncrementPosition);
			else
				offsetStart = null;
			if (offsetEndList.size() > currentIncrementPosition)
				offsetEnd = offsetEndList.get(currentIncrementPosition);
			else
				offsetEnd = null;
			if (positionIncrementList.size() > currentIncrementPosition)
				positionIncrement = positionIncrementList
						.get(currentIncrementPosition);
			else
				positionIncrement = null;
		}
	}

	final private FilterFactory filterFactory;
	final protected TokenStream input;
	final private StringBuffer buffer = new StringBuffer(0);
	final private IntArrayList bufferPositionList = new IntArrayList(0);
	final private IntArrayList bufferLengthList = new IntArrayList(0);
	final private IntArrayList offsetStartList = new IntArrayList(0);
	final private IntArrayList offsetEndList = new IntArrayList(0);
	final private IntArrayList positionIncrementList = new IntArrayList(0);
	private int currentIncrementPosition = 0;
	private int currentTokenPosition = 0;
	private int currentTokenLength = 0;
	private String currentTerm = null;
	private TokenAttributes currentAttributes;

	public TokenStream(FilterFactory filterFactory, TokenStream input) {
		this.filterFactory = filterFactory;
		this.input = input;
	}

	public TokenStream(Reader reader) throws IOException {
		filterFactory = null;
		input = null;
		if (reader != null) {
			char[] cbuf = new char[65536];
			int l;
			while ((l = reader.read(cbuf, 0, cbuf.length)) != -1)
				buffer.append(cbuf, 0, l);
			l = buffer.length();
			if (l > 0) {
				bufferPositionList.add(0);
				bufferLengthList.add(l);
				offsetStartList.add(0);
				offsetEndList.add(l);
				positionIncrementList.add(1);
			}
		}
	}

	final private void addAttributes(int bufferPosition, int bufferLength,
			TokenAttributes attributes) {
		bufferPositionList.add(bufferPosition);
		bufferLengthList.add(bufferLength);
		if (attributes.offsetStart != null)
			offsetStartList.add(attributes.offsetStart);
		if (attributes.offsetEnd != null)
			offsetEndList.add(attributes.offsetEnd);
		if (attributes.positionIncrement != null)
			positionIncrementList.add(attributes.positionIncrement);
	}

	final protected void addToken(StringBuffer tokenBuffer,
			TokenAttributes attributes) {
		if (tokenBuffer == null)
			return;
		int len = tokenBuffer.length();
		if (len == 0)
			return;
		addAttributes(buffer.length(), len, attributes);
		buffer.append(tokenBuffer);
	}

	final protected void addToken(String token, TokenAttributes attributes) {
		if (token == null)
			return;
		int len = token.length();
		if (len == 0)
			return;
		addAttributes(buffer.length(), len, attributes);
		buffer.append(token);
	}

	final public int getCurrentTokenPosition() {
		return currentTokenPosition;
	}

	final public int getCurrentTokenLength() {
		return currentTokenLength;
	}

	final public TokenAttributes getAttributes() {
		return currentAttributes;
	}

	final public StringBuffer getBuffer() {
		return buffer;
	}

	public boolean incrementToken() throws IOException {
		if (currentIncrementPosition >= bufferPositionList.size())
			return false;
		currentTerm = null;
		currentTokenPosition = bufferPositionList
				.getInt(currentIncrementPosition);
		currentTokenLength = bufferLengthList.getInt(currentIncrementPosition);
		currentAttributes = new TokenAttributes(currentIncrementPosition);
		currentIncrementPosition++;
		return true;
	}

	public String getCurrentTerm() {
		if (currentTerm != null)
			return currentTerm;
		currentTerm = buffer.substring(currentTokenPosition,
				currentTokenPosition + currentTokenLength);
		return currentTerm;
	}

	final public FilterFactory getFilterFactory() {
		return filterFactory;
	}

	private class TermIterator implements Iterator<String> {

		private int position = 0;

		@Override
		public boolean hasNext() {
			return position < bufferPositionList.size();
		}

		@Override
		public String next() {
			int start = bufferPositionList.getInt(position);
			int end = start + bufferLengthList.getInt(position);
			StringBuffer sb = new StringBuffer(buffer.substring(start, end));
			TokenAttributes attributes = new TokenAttributes(position);
			sb.append('[');
			if (attributes.offsetStart != null)
				sb.append(attributes.offsetStart);
			sb.append('-');
			if (attributes.offsetEnd != null)
				sb.append(attributes.offsetEnd);
			sb.append(',');
			if (attributes.positionIncrement != null)
				sb.append(attributes.positionIncrement);
			sb.append(']');
			position++;
			return sb.toString();
		}

		@Override
		public void remove() {
			throw new RuntimeException("Not allowed");
		}

	}

	final public Iterator<String> iterator() {
		return new TermIterator();
	}

}
