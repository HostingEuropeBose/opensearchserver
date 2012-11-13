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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;
import java.util.BitSet;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.result.collector.AbstractCollector;
import com.jaeksoft.searchlib.util.Timer;

public class FilterHits {

	protected BitSet docSet;

	protected int count;

	protected FilterHits() {
		docSet = null;
		count = 0;
	}

	protected void and(BitSet targetDocSet) {
		if (docSet == null)
			return;
		targetDocSet.and(docSet);
	}

	public FilterHits(Query query, boolean negative, ReaderInterface reader,
			Timer timer) throws IOException, ParseException, SearchLibException {
		Timer t = new Timer(timer, "Filter hit: " + query.toString());
		docSet = new BitSet(reader.maxDoc());
		FilterCollector collector = new FilterCollector();
		reader.search(query, null, collector);
		if (negative)
			docSet.flip(0, docSet.size());
		t.duration();
	}

	private class FilterCollector extends AbstractCollector {

		private FilterCollector() {
		}

		@Override
		public void collect(long docId) {
			// TODO long implementation
			docSet.set((int) docId);
			count++;
		}

	}

}
