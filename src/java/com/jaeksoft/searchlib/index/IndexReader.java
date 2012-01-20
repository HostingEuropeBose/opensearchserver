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
import java.util.Collection;

import com.jaeksoft.searchlib.analysis.stopwords.WordSet;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.index.term.TermDocs;
import com.jaeksoft.searchlib.index.term.TermEnum;
import com.jaeksoft.searchlib.index.term.TermFreqVector;
import com.jaeksoft.searchlib.index.term.TermPositions;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.sort.SortList;

public class IndexReader {

	public IndexReader(File dataDir) {
		// TODO Auto-generated constructor stub
	}

	public long getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public TermDocs termDocs(Term term) {
		// TODO Auto-generated method stub
		return null;
	}

	public TermFreqVector getTermFreqVector(int docId, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	public int docFreq(Term term) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public TermEnum terms() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public TermEnum terms(Term term) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getFieldNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public int maxDoc() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int numDocs() {
		// TODO Auto-generated method stub
		return 0;
	}

	public TopDocs search(Query query, int nTop) {
		// TODO Auto-generated method stub
		return null;
	}

	public TopDocs search(Query query, Filter filter, int nTop) {
		// TODO Auto-generated method stub
		return null;
	}

	public TopDocs search(Query query, Filter filter, int nTop, SortList sort) {
		// TODO Auto-generated method stub
		return null;
	}

	public Explanation explain(Query query, int docId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void search(Query query, Collector collector) {
		// TODO Auto-generated method stub

	}

	public void search(Query query, Filter filter, Collector collector) {
		// TODO Auto-generated method stub

	}

	public void deleteDocument(int docNum) {
		// TODO Auto-generated method stub

	}

	public void deleteDocuments(Term term) {
		// TODO Auto-generated method stub

	}

	public IndexDocument document(int docId, FieldSelector selector) {
		// TODO Auto-generated method stub
		return null;
	}

	public StringIndex getStringIndex(String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	public WordSet getWordSet(String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	public TermPositions termPositions() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasDeletions() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOptimized() {
		// TODO Auto-generated method stub
		return false;
	}

	public int numDeletedDocs() {
		// TODO Auto-generated method stub
		return 0;
	}

}
