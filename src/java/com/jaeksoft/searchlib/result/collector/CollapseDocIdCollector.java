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

import java.util.BitSet;

import org.apache.commons.lang3.ArrayUtils;

public class CollapseDocIdCollector implements CollapseDocInterface,
		JoinDocInterface {

	protected final long[][] foreignDocIdsArray;
	protected final long[] sourceIds;
	protected final int collapseMax;
	protected int totalCollapseCount = 0;
	protected final long[] ids;
	protected final long[][] collapseDocsArray;
	protected final int[] collapseCounts;
	protected int currentPos;
	protected int maxDoc;
	protected BitSet bitSet;

	public CollapseDocIdCollector(DocIdInterface sourceCollector, int size,
			int collapseMax) {
		if (sourceCollector instanceof JoinDocInterface) {
			foreignDocIdsArray = ((JoinDocInterface) sourceCollector)
					.getForeignDocIdsArray();
		} else
			foreignDocIdsArray = null;
		this.sourceIds = sourceCollector.getIds();
		this.totalCollapseCount = 0;
		this.collapseMax = collapseMax;
		this.collapseDocsArray = new long[size][];
		this.collapseCounts = new int[size];
		this.ids = new long[size];
		this.currentPos = 0;
		this.maxDoc = sourceCollector.getMaxDoc();
		this.bitSet = null;
	}

	protected CollapseDocIdCollector(CollapseDocIdCollector src) {
		if (src.foreignDocIdsArray != null)
			foreignDocIdsArray = JoinDocCollector
					.copyForeignDocIdsArray(src.foreignDocIdsArray);
		else
			foreignDocIdsArray = null;
		this.sourceIds = src.sourceIds;
		this.totalCollapseCount = src.totalCollapseCount;
		this.collapseMax = src.collapseMax;
		this.collapseDocsArray = new long[src.collapseDocsArray.length][];
		int i = 0;
		for (long[] collDocArray : src.collapseDocsArray)
			this.collapseDocsArray[i++] = ArrayUtils.clone(collDocArray);
		this.collapseCounts = ArrayUtils.clone(src.collapseCounts);
		this.ids = ArrayUtils.clone(src.ids);
		this.currentPos = src.currentPos;
		this.maxDoc = src.maxDoc;
		this.bitSet = null;
	}

	@Override
	public DocIdInterface duplicate() {
		return new CollapseDocIdCollector(this);
	}

	@Override
	public int collectDoc(int sourcePos) {
		collapseCounts[currentPos] = 0;
		ids[currentPos] = sourceIds[sourcePos];
		int pos = currentPos;
		currentPos++;
		return pos;
	}

	@Override
	public void collectCollapsedDoc(int sourcePos, int collapsePos) {
		totalCollapseCount++;
		int collCount = collapseCounts[collapsePos]++;
		if (collapseMax != 0 || collapseMax < collCount)
			return;
		if (collapseDocsArray[collapsePos] == null)
			collapseDocsArray[collapsePos] = new long[] { sourceIds[sourcePos] };
		else
			collapseDocsArray[collapsePos] = ArrayUtils.add(
					collapseDocsArray[collapsePos], sourceIds[sourcePos]);
	}

	@Override
	public int getCollapsedCount() {
		return totalCollapseCount;
	}

	@Override
	public void swap(int pos1, int pos2) {
		long id = ids[pos1];
		long[] colArray = collapseDocsArray[pos1];
		int colCount = collapseCounts[pos1];
		ids[pos1] = ids[pos2];
		collapseDocsArray[pos1] = collapseDocsArray[pos2];
		collapseCounts[pos1] = collapseCounts[pos2];
		ids[pos2] = id;
		collapseDocsArray[pos2] = colArray;
		collapseCounts[pos2] = colCount;
		if (foreignDocIdsArray != null)
			JoinDocCollector.swap(foreignDocIdsArray, pos1, pos2);
	}

	@Override
	public long[] getIds() {
		return ids;
	}

	@Override
	public int getSize() {
		return currentPos;
	}

	@Override
	final public BitSet getBitSet() {
		if (bitSet != null)
			return bitSet;
		bitSet = new BitSet(maxDoc);
		// TODO Long implementation
		for (long id : ids)
			bitSet.set((int) id);
		return bitSet;
	}

	@Override
	public int getMaxDoc() {
		return maxDoc;
	}

	@Override
	public int[] getCollapseCounts() {
		return collapseCounts;
	}

	@Override
	public long[] getCollapsedDocs(int pos) {
		return collapseDocsArray[pos];
	}

	@Override
	public void setForeignDocId(int pos, int joinResultPos, long foreignDocId) {
		throw new RuntimeException(
				"New join is not allowed on already collapsed documents");
	}

	@Override
	public long getForeignDocIds(int pos, int joinPosition) {
		return JoinDocCollector.getForeignDocIds(foreignDocIdsArray, pos,
				joinPosition);
	}

	@Override
	public long[][] getForeignDocIdsArray() {
		return foreignDocIdsArray;
	}

}
