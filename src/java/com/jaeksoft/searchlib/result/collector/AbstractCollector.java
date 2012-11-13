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

import com.jaeksoft.searchlib.index.IndexReader;
import com.jaeksoft.searchlib.scoring.AbstractScorer;
import com.jaeksoft.searchlib.scoring.NullScorer;

public abstract class AbstractCollector {

	protected IndexReader reader = null;
	protected AbstractScorer scorer = NullScorer.INSTANCE;

	final public void setNextReader(IndexReader reader, long docId)
			throws IOException {
		this.reader = reader;
	}

	final public void setScorer(AbstractScorer scorer) throws IOException {
		this.scorer = scorer;
	}

	public abstract void collect(long docId) throws IOException;

}
