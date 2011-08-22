/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.util.BitSet;

import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class DocSetHits {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private int[] collectedDocs;
	private BitSet bitset;
	private ReaderLocal reader;
	private Query query;
	private Filter filter;
	private SortList sort;
	private ScoreDoc[] scoreDocs;
	private int docNumFound;
	private float maxScore;

	private class ScoreHitCollector extends Collector {

		private int lastDocId = -1;
		final private long bitSetSize = bitset.size();

		@Override
		final public void collect(int docId) {
			lastDocId = docId;
			collectedDocs[docNumFound++] = docId;
			bitset.set(docId);
		}

		@Override
		final public void setScorer(float score) throws IOException {
			if (lastDocId < 0)
				return;
			if (lastDocId >= bitSetSize)
				return;
			if (score > maxScore)
				maxScore = score;
		}
	}

	protected DocSetHits(ReaderLocal reader, Query query, Filter filter,
			SortList sort, boolean collect) throws IOException {
		rwl.w.lock();
		try {
			this.query = query;
			this.filter = filter;
			this.sort = sort;
			this.docNumFound = 0;
			this.maxScore = 0;
			this.scoreDocs = new ScoreDoc[0];
			this.reader = reader;
			this.collectedDocs = new int[0];
			this.bitset = new BitSet(reader.maxDoc());
			Collector collector = null;
			if (reader.numDocs() == 0)
				return;
			else if (collect)
				collector = new ScoreHitCollector();

			TopDocs topDocs = reader.search(query, filter, sort, 1);
			if (collector != null) {
				this.collectedDocs = new int[topDocs.totalHits];
				reader.search(query, filter, collector);
			} else {
				docNumFound = topDocs.totalHits;
				maxScore = topDocs.getMaxScore();
			}
		} finally {
			rwl.w.unlock();
		}
	}

	public ScoreDoc[] getScoreDocs(int rows) throws IOException {
		rwl.w.lock();
		try {
			if (rows > docNumFound)
				rows = docNumFound;
			if (rows <= scoreDocs.length)
				return scoreDocs;
			TopDocs topDocs = reader.search(query, filter, sort, rows);
			this.scoreDocs = topDocs.scoreDocs;
			return scoreDocs;
		} finally {
			rwl.w.unlock();
		}
	}

	public int getDocByPos(int pos) {
		rwl.r.lock();
		try {
			return scoreDocs[pos].doc;
		} finally {
			rwl.r.unlock();
		}
	}

	public float getScoreByPos(int pos) {
		rwl.r.lock();
		try {
			return scoreDocs[pos].score;
		} finally {
			rwl.r.unlock();
		}
	}

	public int[] facetMultivalued(String fieldName) throws IOException {
		rwl.r.lock();
		try {
			return Facet.computeMultivalued(reader, fieldName, bitset);
		} finally {
			rwl.r.unlock();
		}
	}

	public int[] facetSinglevalue(String fieldName) throws IOException {
		rwl.r.lock();
		try {
			return Facet.computeSinglevalued(reader, fieldName, collectedDocs);
		} finally {
			rwl.r.unlock();
		}
	}

	public float getMaxScore() {
		rwl.r.lock();
		try {
			return maxScore;
		} finally {
			rwl.r.unlock();
		}
	}

	public int getDocNumFound() {
		rwl.r.lock();
		try {
			return docNumFound;
		} finally {
			rwl.r.unlock();
		}
	}

}
