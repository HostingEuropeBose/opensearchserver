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
import java.net.URI;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.index.term.TermDocs;
import com.jaeksoft.searchlib.index.term.TermEnum;
import com.jaeksoft.searchlib.index.term.TermFreqVector;
import com.jaeksoft.searchlib.query.MoreLikeThis;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.collector.AbstractCollector;
import com.jaeksoft.searchlib.schema.AnalyzerSelector;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;

public interface ReaderInterface {

	public abstract boolean sameIndex(ReaderInterface reader);

	public void close();

	public Collection<?> getFieldNames() throws SearchLibException;

	public int getDocFreq(Term term) throws SearchLibException;

	public TermEnum getTermEnum() throws SearchLibException;

	public TermEnum getTermEnum(String field, String term)
			throws SearchLibException;

	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException, SearchLibException;

	public abstract Query rewrite(Query query) throws SearchLibException;

	public abstract MoreLikeThis getMoreLikeThis() throws SearchLibException;

	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException;

	public String explain(AbstractRequest request, int docId, boolean bHtml)
			throws SearchLibException;

	public IndexStatistics getStatistics() throws IOException,
			SearchLibException;

	public void reload() throws SearchLibException;

	public long getVersion() throws SearchLibException;

	public abstract int numDocs() throws IOException, SearchLibException;

	public abstract void search(Query query, BitSet filter,
			AbstractCollector collector) throws SearchLibException;

	public abstract StringIndex getStringIndex(String name)
			throws SearchLibException;

	public abstract TermDocs getTermDocs(Term t) throws SearchLibException;

	public abstract int maxDoc() throws IOException, SearchLibException;

	public abstract FilterHits getFilterHits(SchemaField defaultField,
			Analyzer analyzer, FilterAbstract<?> filter, Timer timer)
			throws SearchLibException;

	public abstract DocSetHits newDocSetHits(SearchRequest searchRequest,
			Schema schema, SchemaField defaultField,
			AnalyzerSelector analyzerSelector, Timer timer)
			throws SearchLibException;

	public abstract Map<String, FieldValue> getDocumentFields(int docId,
			TreeSet<String> fieldSet, Timer timer) throws SearchLibException;

	public abstract DocSetHits searchDocSet(SearchRequest searchRequest,
			Timer timer) throws SearchLibException;

	public abstract SpellChecker getSpellChecker(String fieldName)
			throws SearchLibException;

}
