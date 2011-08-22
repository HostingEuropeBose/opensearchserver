/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;

import com.jaeksoft.searchlib.analysis.PerFieldAnalyzerWrapper;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.query.Query;

public class IndexWriter {

	public IndexWriter(File dataDir, boolean create) {
		// TODO Auto-generated constructor stub
	}

	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	public void addDocument(IndexDocument document) {
		// TODO Auto-generated method stub

	}

	public void updateDocument(Term term, IndexDocument document,
			PerFieldAnalyzerWrapper pfa) {
		// TODO Auto-generated method stub

	}

	public void addDocument(IndexDocument document, PerFieldAnalyzerWrapper pfa) {
		// TODO Auto-generated method stub

	}

	public void optimize(boolean b) {

	}

	public void deleteDocuments(Term term) {

	}

	public void deleteDocuments(Term[] terms) {
		// TODO Auto-generated method stub

	}

	public void deleteDocuments(Query query) {
		// TODO Auto-generated method stub

	}

}
