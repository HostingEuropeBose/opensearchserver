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

import com.jaeksoft.searchlib.index.StringIndex;
import com.jaeksoft.searchlib.sort.AscStringIndexSorter;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

public class JoinDocCollector implements JoinDocInterface {

	public final static JoinDocCollector EMPTY = new JoinDocCollector();

	protected final int maxDoc;
	protected final long[] ids;
	protected final long[][] foreignDocIdsArray;
	protected BitSet bitSet;
	protected final int joinResultSize;

	protected JoinDocCollector() {
		maxDoc = 0;
		ids = new long[0];
		foreignDocIdsArray = new long[0][0];
		bitSet = null;
		joinResultSize = 0;
	}

	public JoinDocCollector(DocIdInterface docs, int joinResultSize) {
		this.bitSet = null;
		this.maxDoc = docs.getMaxDoc();
		this.ids = ArrayUtils.clone(docs.getIds());
		this.foreignDocIdsArray = new long[ids.length][];
		this.joinResultSize = joinResultSize;
	}

	protected JoinDocCollector(int joinResultSize, int idsLength, int maxDoc) {
		this.bitSet = null;
		this.joinResultSize = joinResultSize;
		this.ids = new long[idsLength];
		this.foreignDocIdsArray = new long[idsLength][];
		this.maxDoc = maxDoc;
	}

	/**
	 * Copy only the valid item (other than -1)
	 * 
	 * @param src
	 */
	protected JoinDocCollector(JoinDocCollector src) {
		this(src.joinResultSize, validSize(src.ids), src.maxDoc);
		int i1 = 0;
		int i2 = 0;
		for (long id : src.ids) {
			if (id != -1) {
				ids[i1] = id;
				foreignDocIdsArray[i1++] = ArrayUtils
						.clone(src.foreignDocIdsArray[i2]);
			}
			i2++;
		}
	}

	protected static final int validSize(long[] ids) {
		int i = 0;
		for (long id : ids)
			if (id != -1)
				i++;
		return i;
	}

	final public static long[][] copyForeignDocIdsArray(
			long[][] foreignDocIdsArray) {
		long[][] neworeignDocIdsArray = new long[foreignDocIdsArray.length][];
		int i = 0;
		for (long[] foreignIds : foreignDocIdsArray)
			neworeignDocIdsArray[i++] = ArrayUtils.clone(foreignIds);
		return neworeignDocIdsArray;
	}

	final public static void swap(long[][] foreignDocIdsArray, int pos1,
			int pos2) {
		long[] foreignDocIds = foreignDocIdsArray[pos1];
		foreignDocIdsArray[pos1] = foreignDocIdsArray[pos2];
		foreignDocIdsArray[pos2] = foreignDocIds;
	}

	@Override
	public DocIdInterface duplicate() {
		return new JoinDocCollector(this);
	}

	@Override
	public void swap(int pos1, int pos2) {
		long id = ids[pos1];
		ids[pos1] = ids[pos2];
		ids[pos2] = id;
		swap(foreignDocIdsArray, pos1, pos2);
	}

	@Override
	public long[] getIds() {
		return ids;
	}

	@Override
	public int getSize() {
		return ids.length;
	}

	@Override
	public BitSet getBitSet() {
		if (bitSet != null)
			return bitSet;
		bitSet = new BitSet(maxDoc);
		// TODO long bitset implementation
		for (long id : ids)
			bitSet.set((int) id);
		return bitSet;
	}

	@Override
	public int getMaxDoc() {
		return maxDoc;
	}

	@Override
	public void setForeignDocId(int pos, int joinResultPos, long foreignDocId,
			float foreignScore) {
		long[] foreignDocIds = foreignDocIdsArray[pos];
		if (foreignDocIds == null)
			foreignDocIds = new long[joinResultSize];
		foreignDocIds[joinResultPos] = foreignDocId;
		foreignDocIdsArray[pos] = foreignDocIds;
	}

	final public static long getForeignDocIds(long[][] foreignDocIdsArray,
			int pos, int joinPosition) {
		long[] foreignDocIds = foreignDocIdsArray[pos];
		if (foreignDocIds == null)
			return -1;
		if (joinPosition >= foreignDocIds.length)
			return -1;
		return foreignDocIds[joinPosition];
	}

	@Override
	public long getForeignDocIds(int pos, int joinPosition) {
		return getForeignDocIds(foreignDocIdsArray, pos, joinPosition);
	}

	final public static DocIdInterface getDocIdInterface(int maxDoc,
			int joinPosition, JoinDocCollector joinDocColletor)
			throws IOException {
		DocIdCollector docIdCollector = new DocIdCollector(maxDoc,
				joinDocColletor.getIds().length);
		for (long[] foreinDocs : joinDocColletor.getForeignDocIdsArray())
			docIdCollector.collect(foreinDocs[joinPosition]);
		return docIdCollector;
	}

	final public static DocIdInterface join(DocIdInterface docs,
			StringIndex doc1StringIndex, DocIdInterface docs2,
			StringIndex doc2StringIndex, int joinResultSize, int joinResultPos,
			Timer timer, boolean factorScore) {
		if (docs.getSize() == 0 || docs2.getSize() == 0)
			return docs instanceof ScoreDocInterface ? JoinScoreDocCollector.EMPTY
					: JoinDocCollector.EMPTY;

		Timer t = new Timer(timer, "copy & sort local documents");
		JoinDocInterface docs1 = docs instanceof ScoreDocInterface ? new JoinScoreDocCollector(
				(ScoreDocInterface) docs, joinResultSize)
				: new JoinDocCollector(docs, joinResultSize);
		new AscStringIndexSorter(docs1, doc1StringIndex).quickSort(t);
		t.duration();

		t = new Timer(timer, "copy & sort foreign documents");
		docs2 = docs2.duplicate();
		float scores2[] = null;
		if (docs2 instanceof ScoreDocInterface && factorScore) {
			if (factorScore)
				scores2 = ((ScoreDocInterface) docs2).getScores();

		}
		new AscStringIndexSorter(docs2, doc2StringIndex).quickSort(t);
		t.duration();

		t = new Timer(timer, "join operation");
		float score2 = 1.0F;
		int i1 = 0;
		int i2 = 0;
		long[] ids1 = docs1.getIds();
		long[] ids2 = docs2.getIds();
		while (i1 != ids1.length) {
			long id1 = ids1[i1];
			long id2 = ids2[i2];
			// TODO Long implementation
			String t1 = doc1StringIndex.lookup[doc1StringIndex.order[(int) id1]];
			String t2 = doc2StringIndex.lookup[doc2StringIndex.order[(int) id2]];
			int c = StringUtils.compareNullString(t1, t2);
			if (c < 0) {
				ids1[i1] = -1;
				i1++;
			} else if (c > 0) {
				i2++;
				if (i2 == ids2.length) {
					while (i1 != ids1.length)
						ids1[i1++] = -1;
				}
			} else {
				if (scores2 != null)
					score2 = scores2[i2];
				docs1.setForeignDocId(i1, joinResultPos, id2, score2);
				i1++;
			}
		}
		t.duration();

		return docs1.duplicate();
	}

	@Override
	public long[][] getForeignDocIdsArray() {
		return foreignDocIdsArray;
	}
}
