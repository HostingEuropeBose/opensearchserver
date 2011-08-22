/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;
import java.util.BitSet;

import com.jaeksoft.searchlib.index.Collector;
import com.jaeksoft.searchlib.index.IndexReader;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.Query;

public class FilterHits extends Filter {

	protected BitSet docSet;

	protected FilterHits() {
		docSet = null;
	}

	protected void and(FilterHits filterHits) {
		if (docSet == null)
			docSet = (BitSet) filterHits.docSet.clone();
		else
			docSet.and(filterHits.docSet);
	}

	public FilterHits(Query query, boolean negative, ReaderLocal reader)
			throws IOException, ParseException {
		FilterCollector collector = new FilterCollector(reader.maxDoc());
		reader.search(query, null, collector);
		docSet = collector.bitSet;
		if (negative)
			docSet.flip(0, docSet.size());

	}

	private class FilterCollector extends Collector {

		private BitSet bitSet;

		private FilterCollector(int size) {
			this.bitSet = new BitSet(size);
		}

		@Override
		public void collect(int docId) {
			bitSet.set(docId);
		}

	}

	public BitSet getDocIdSet(IndexReader reader) throws IOException {
		return this.docSet;
	}

}
