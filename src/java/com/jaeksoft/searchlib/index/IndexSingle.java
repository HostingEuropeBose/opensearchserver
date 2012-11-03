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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.index.osse.OsseIndex;
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
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexSingle extends IndexAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private ReaderInterface reader = null;
	private WriterInterface writer = null;

	private volatile boolean online;

	private OsseIndex osseIndex = null;

	public IndexSingle(File configDir, IndexConfig indexConfig,
			boolean createIfNotExists) throws IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {
		super(indexConfig);
		online = true;
		osseIndex = new OsseIndex(new File(configDir, "index"), null,
				createIfNotExists);
		reader = new ReaderNativeOSSE(indexConfig, osseIndex);
		writer = new WriterNativeOSSE(osseIndex, indexConfig);
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			if (reader != null)
				reader.close();
			osseIndex.close(null);
		} finally {
			rwl.w.unlock();
		}
	}

	private void checkOnline(boolean online) throws SearchLibException {
		if (this.online != online)
			throw new SearchLibException("Index is offline");
	}

	private void checkReadOnly(boolean readOnly) throws SearchLibException {
		if (indexConfig.getReadWriteMode() == IndexMode.READ_ONLY)
			throw new SearchLibException("Index is read only");
	}

	@Override
	public void optimize() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			checkReadOnly(false);
			if (writer != null)
				writer.optimize();
		} finally {
			rwl.r.unlock();
		}
		reload();
	}

	@Override
	public boolean isOptimizing() {
		rwl.r.lock();
		try {
			if (writer != null)
				return writer.isOptimizing();
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int deleteDocument(Schema schema, String uniqueField)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			checkReadOnly(false);
			if (writer != null)
				return writer.deleteDocument(schema, uniqueField);
			else
				return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			checkReadOnly(false);
			if (writer != null)
				return writer.deleteDocuments(schema, uniqueFields);
			else
				return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void deleteAll() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			checkReadOnly(false);
			if (writer != null)
				writer.deleteAll();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int deleteDocuments(SearchRequest query) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			checkReadOnly(false);
			return writer.deleteDocuments(query);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void addBeforeUpdate(BeforeUpdateInterface beforeUpdate)
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (writer != null)
				writer.addBeforeUpdate(beforeUpdate);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void checkSchemaFieldList(SchemaFieldList schemaFieldList)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			checkReadOnly(false);
			if (writer != null)
				writer.checkSchemaFieldList(schemaFieldList);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			checkReadOnly(false);
			if (writer != null)
				return writer.updateDocument(schema, document);
			else
				return false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			checkReadOnly(false);
			if (writer != null)
				return writer.updateDocuments(schema, documents);
			else
				return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	private void reloadNoLock() throws SearchLibException {
		if (reader != null)
			reader.reload();
	}

	@Override
	public void reload() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			reloadNoLock();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.request(request);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String explain(AbstractRequest request, int docId, boolean bHtml)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.explain(request, docId, bHtml);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		rwl.r.lock();
		try {
			if (reader == this)
				return true;
			if (reader == this.reader)
				return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexStatistics getStatistics() throws IOException,
			SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getStatistics();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexSingle get(String name) {
		return this;
	}

	@Override
	public int getDocFreq(Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getDocFreq(term);
			return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getTermEnum();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum(String field, String term)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getTermEnum(field, term);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getTermFreqVector(docId, field);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean isOnline() {
		rwl.r.lock();
		try {
			return online;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexMode getReadWriteMode() {
		rwl.r.lock();
		try {
			return indexConfig.getReadWriteMode();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void setOnline(boolean v) {
		rwl.w.lock();
		try {
			online = v;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void setReadWriteMode(IndexMode mode) throws SearchLibException {
		rwl.w.lock();
		try {
			checkOnline(true);
			if (mode == indexConfig.getReadWriteMode())
				return;
			indexConfig.setReadWriteMode(mode);
			reloadNoLock();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public long getVersion() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader == null)
				return 0;
			return reader.getVersion();
		} finally {
			rwl.r.unlock();
		}
	}

	public SearchCache getSearchCache() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	public FilterCache getFilterCache() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	public FieldCache getFieldCache() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected void writeXmlConfigIndex(XmlWriter xmlWriter) throws SAXException {
		indexConfig.writeXmlConfig(xmlWriter);
	}

	@Override
	public Collection<?> getFieldNames() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getFieldNames();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.rewrite(query);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getMoreLikeThis();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void search(Query query, BitSet filter, AbstractCollector collector)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				reader.search(query, filter, collector);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public StringIndex getStringIndex(String name) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getStringIndex(name);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermDocs getTermDocs(Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getTermDocs(term);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			Analyzer analyzer, FilterAbstract<?> filter, Timer timer)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getFilterHits(defaultField, analyzer, filter,
						timer);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public DocSetHits newDocSetHits(SearchRequest searchRequest, Schema schema,
			SchemaField defaultField, AnalyzerSelector analyzerSelector,
			Timer timer) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.newDocSetHits(searchRequest, schema,
						defaultField, analyzerSelector, timer);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Map<String, FieldValue> getDocumentFields(int docId,
			TreeSet<String> fieldSet, Timer timer) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getDocumentFields(docId, fieldSet, timer);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public DocSetHits searchDocSet(SearchRequest searchRequest, Timer timer)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return searchDocSet(searchRequest, timer);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public SpellChecker getSpellChecker(String fieldName)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getSpellChecker(fieldName);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

}
