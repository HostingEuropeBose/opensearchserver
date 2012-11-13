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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.osse.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.OsseIndex;
import com.jaeksoft.searchlib.index.osse.OsseQuery;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.index.term.TermDocs;
import com.jaeksoft.searchlib.index.term.TermEnum;
import com.jaeksoft.searchlib.index.term.TermFreqVector;
import com.jaeksoft.searchlib.query.MoreLikeThis;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.collector.AbstractCollector;
import com.jaeksoft.searchlib.schema.AnalyzerSelector;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;

public class ReaderNativeOSSE extends ReaderAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private OsseIndex index;

	private OsseErrorHandler err;

	protected ReaderNativeOSSE(IndexConfig indexConfig, OsseIndex index)
			throws SearchLibException {
		super(indexConfig);
		this.index = index;
	}

	@Override
	public void close() {
		err.release();
	}

	@Override
	public int getDocFreq(Term term) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IndexStatistics getStatistics() throws IOException {
		return new IndexStatistics();
	}

	@Override
	public TermFreqVector getTermFreqVector(long docId, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TermEnum getTermEnum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TermEnum getTermEnum(String field, String term) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getFieldNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query rewrite(Query query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException {
		rwl.r.lock();
		try {
			return request.execute(this);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String explain(AbstractRequest request, long docId, boolean bHtml)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numDocs() throws IOException, SearchLibException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void search(Query query, BitSet filter, AbstractCollector collector)
			throws SearchLibException, IOException {
		OsseQuery osseQuery = new OsseQuery(index, query);
		osseQuery.collect(collector);
		osseQuery.free();
		// TODO filter
	}

	@Override
	public StringIndex getStringIndex(String name) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TermDocs getTermDocs(Term t) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int maxDoc() throws IOException, SearchLibException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			AnalyzerSelector analyzerSelector, FilterAbstract<?> filter,
			Timer timer) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocSetHits newDocSetHits(SearchRequest searchRequest, Schema schema,
			SchemaField defaultField, AnalyzerSelector analyzerSelector,
			Timer timer) throws SearchLibException, ParseException, IOException {

		BitSet bitSet = searchRequest.getFilterList().getBitSet(this,
				defaultField, analyzerSelector, timer);

		DocSetHits dsh = new DocSetHits(this, searchRequest.getQuery(), bitSet,
				searchRequest.getSortFieldList(), timer);
		return dsh;
	}

	@Override
	public Map<String, FieldValue> getDocumentFields(long docId,
			TreeSet<String> fieldSet, Timer timer) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocSetHits searchDocSet(SearchRequest searchRequest, Timer timer)
			throws SearchLibException, IOException, SyntaxError,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.r.lock();
		try {

			Schema schema = searchRequest.getConfig().getSchema();
			SchemaField defaultField = schema.getFieldList().getDefaultField();

			return newDocSetHits(searchRequest, schema, defaultField,
					schema.getAnalyzerSelector(), timer);

		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public SpellChecker getSpellChecker(String fieldName)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

}
