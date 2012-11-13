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

package com.jaeksoft.searchlib.result.collector;

import java.io.IOException;
import java.util.BitSet;

import org.apache.commons.lang3.ArrayUtils;

public class DocIdCollector extends AbstractCollector implements DocIdInterface {

	final protected int maxDoc;
	final protected long[] ids;
	private BitSet bitSet;
	protected int currentPos;

	final public static DocIdCollector EMPTY = new DocIdCollector(0, 0);

	public DocIdCollector(int maxDoc, int numFound) {
		this.maxDoc = maxDoc;
		currentPos = 0;
		bitSet = null;
		ids = new long[numFound];
	}

	protected DocIdCollector(DocIdCollector src) {
		this.maxDoc = src.maxDoc;
		this.ids = ArrayUtils.clone(src.ids);
		this.bitSet = src.bitSet;
		this.currentPos = src.currentPos;
	}

	@Override
	public DocIdInterface duplicate() {
		return new DocIdCollector(this);
	}

	@Override
	final public int getSize() {
		return ids.length;
	}

	@Override
	public void collect(long docId) throws IOException {
		ids[currentPos++] = docId;
	}

	final public boolean isBitSet() {
		return bitSet != null;
	}

	@Override
	final public BitSet getBitSet() {
		if (bitSet != null)
			return bitSet;
		bitSet = new BitSet(maxDoc);
		// TODO integer vs long
		for (long id : ids)
			bitSet.set((int) id);
		return bitSet;
	}

	@Override
	public void swap(int pos1, int pos2) {
		long id = ids[pos1];
		ids[pos1] = ids[pos2];
		ids[pos2] = id;
	}

	@Override
	public long[] getIds() {
		return ids;
	}

	@Override
	public int getMaxDoc() {
		return maxDoc;
	}

}