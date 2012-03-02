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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.filter.stop.WordArray;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.cache.SpellCheckerCache;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.index.term.TermDocs;
import com.jaeksoft.searchlib.index.term.TermEnum;
import com.jaeksoft.searchlib.index.term.TermFreqVector;
import com.jaeksoft.searchlib.index.term.TermPositions;
import com.jaeksoft.searchlib.query.MoreLikeThis;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.remote.UriWriteStream;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.DocumentRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class ReaderLocal extends ReaderAbstract implements ReaderInterface {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private IndexReader indexReader;

	private SearchCache searchCache;
	private FilterCache filterCache;
	private FieldCache fieldCache;
	private SpellCheckerCache spellCheckerCache;

	private File rootDir;
	private File dataDir;

	private boolean readOnly;
	private String similarityClass;

	private ReaderLocal(File rootDir, File dataDir, String similarityClass,
			boolean readOnly) throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		this.similarityClass = similarityClass;
		this.readOnly = readOnly;
		init(rootDir, dataDir);
	}

	private void init(File rootDir, File dataDir) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.w.lock();
		try {
			this.rootDir = rootDir;
			this.dataDir = dataDir;
			this.indexReader = new IndexReader(dataDir);
		} finally {
			rwl.w.unlock();
		}
	}

	private void init(ReaderLocal r) {
		rwl.w.lock();
		try {
			this.rootDir = r.rootDir;
			this.dataDir = r.dataDir;
			this.indexReader = r.indexReader;
		} finally {
			rwl.w.unlock();
		}
	}

	private void initCache(IndexConfig indexConfig) {
		rwl.w.lock();
		try {
			this.searchCache = new SearchCache(indexConfig);
			this.filterCache = new FilterCache(indexConfig);
			this.fieldCache = new FieldCache(indexConfig);
			// TODO replace value 100 by number of field in schema
			this.spellCheckerCache = new SpellCheckerCache(100);
		} finally {
			rwl.w.unlock();
		}
	}

	private void resetCache() {
		rwl.w.lock();
		try {
			searchCache.clear();
			filterCache.clear();
			fieldCache.clear();
			spellCheckerCache.clear();
		} finally {
			rwl.w.unlock();
		}
	}

	protected File getRootDir() {
		rwl.r.lock();
		try {
			return rootDir;
		} finally {
			rwl.r.unlock();
		}
	}

	protected File getDatadir() {
		rwl.r.lock();
		try {
			return dataDir;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public long getVersion() {
		rwl.r.lock();
		try {
			return indexReader.getVersion();
		} finally {
			rwl.r.unlock();
		}
	}

	public TermDocs getTermDocs(Term term) throws IOException {
		rwl.r.lock();
		try {
			return indexReader.termDocs(term);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException {
		rwl.r.lock();
		try {
			return indexReader.getTermFreqVector(docId, field);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int getDocFreq(Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			return indexReader.docFreq(term);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum() throws SearchLibException {
		rwl.r.lock();
		try {
			return indexReader.terms();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum(String field, String term)
			throws SearchLibException {
		rwl.r.lock();
		try {
			return indexReader.terms(new Term(field, term));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Collection<?> getFieldNames() {
		rwl.r.lock();
		try {
			return indexReader.getFieldNames();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public MoreLikeThis getMoreLikeThis() {
		rwl.r.lock();
		try {
			return new MoreLikeThis(indexReader);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		rwl.r.lock();
		try {
			return query.rewrite(indexReader);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	public void close(boolean bDeleteDirectory) {
		rwl.w.lock();
		try {
			if (indexReader != null) {
				indexReader.close();
				indexReader = null;
			}
			if (bDeleteDirectory)
				if (dataDir.exists())
					FileUtils.deleteDirectory(dataDir);
		} catch (IOException e) {
			Logging.warn(e.getMessage(), e);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void close() {
		close(false);
	}

	public int maxDoc() throws IOException {
		rwl.r.lock();
		try {
			return indexReader.maxDoc();
		} finally {
			rwl.r.unlock();
		}
	}

	public int numDocs() {
		rwl.r.lock();
		try {
			return indexReader.numDocs();
		} finally {
			rwl.r.unlock();
		}
	}

	public TopDocs search(Query query, Filter filter, SortList sort, int nTop)
			throws IOException {
		rwl.r.lock();
		try {
			if (sort == null) {
				if (filter == null)
					return indexReader.search(query, nTop);
				else
					return indexReader.search(query, filter, nTop);
			} else {
				return indexReader.search(query, filter, nTop, sort);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String explain(SearchRequest searchRequest, int docId, boolean bHtml)
			throws SearchLibException {
		rwl.r.lock();
		try {
			Explanation explanation = indexReader.explain(
					searchRequest.getQuery(), docId);
			return explanation.toString();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	public void search(Query query, Filter filter, Collector collector)
			throws IOException {
		rwl.r.lock();
		try {
			if (filter == null)
				indexReader.search(query, collector);
			else
				indexReader.search(query, filter, collector);
		} finally {
			rwl.r.unlock();
		}
	}

	public FilterHits getFilterHits(Field defaultField, Analyzer analyzer,
			com.jaeksoft.searchlib.filter.Filter filter) throws ParseException,
			IOException {
		rwl.r.lock();
		try {
			return filterCache.get(this, defaultField, analyzer, filter);
		} finally {
			rwl.r.unlock();
		}
	}

	protected void fastDeleteDocument(int docNum) throws IOException {
		indexReader.deleteDocument(docNum);
	}

	protected void fastDeleteDocument(String fieldName, String value)
			throws IOException {
		indexReader.deleteDocuments(new Term(fieldName, value));
	}

	public IndexDocument getDocFields(int docId, FieldSelector selector)
			throws IOException {
		rwl.r.lock();
		try {
			return indexReader.document(docId, selector);
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			rwl.r.unlock();
		}
	}

	public StringIndex getStringIndex(String fieldName) throws IOException {
		rwl.r.lock();
		try {
			return indexReader.getStringIndex(fieldName);
		} finally {
			rwl.r.unlock();
		}
	}

	public WordArray getWordArray(String fieldName) {
		rwl.r.lock();
		try {
			return indexReader.getWordArray(fieldName);
		} finally {
			rwl.r.unlock();
		}
	}

	public void xmlInfo(PrintWriter writer) {
		rwl.r.lock();
		try {
			writer.println("<index name=\"" + dataDir.getName() + "\" path=\""
					+ dataDir.getAbsolutePath() + "\"/>");
		} finally {
			rwl.r.unlock();
		}
	}

	private static ReaderLocal findMostRecent(File rootDir,
			String similarityClass, boolean readOnly) {
		ReaderLocal reader = null;
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			try {
				ReaderLocal r = new ReaderLocal(rootDir, f, similarityClass,
						readOnly);
				if (reader == null)
					reader = r;
				else if (r.getVersion() > reader.getVersion())
					reader = r;
			} catch (IOException e) {
				Logging.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				Logging.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				Logging.error(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				Logging.error(e.getMessage(), e);
			}
		}
		return reader;
	}

	private static ReaderLocal findVersion(File rootDir, long version,
			String similarityClass, boolean readOnly) {
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			try {
				ReaderLocal reader = new ReaderLocal(rootDir, f,
						similarityClass, readOnly);
				if (reader.getVersion() == version)
					return reader;
			} catch (IOException e) {
				Logging.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				Logging.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				Logging.error(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				Logging.error(e.getMessage(), e);
			}
		}
		return null;
	}

	private void deleteAllOthers() throws IOException {
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			if (f.equals(dataDir))
				continue;
			FileUtils.deleteDirectory(f);
		}
	}

	public static ReaderLocal fromConfig(File configDir,
			IndexConfig indexConfig, boolean createIfNotExists)
			throws IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		if (indexConfig.getRemoteUri() != null)
			return null;

		File indexDir = new File(configDir, "index");
		if (!indexDir.exists() && createIfNotExists)
			indexDir.mkdirs();

		ReaderLocal reader = ReaderLocal.findMostRecent(indexDir,
				indexConfig.getSimilarityClass(), indexConfig.getReadOnly());

		if (reader == null) {
			if (!createIfNotExists)
				return null;
			File dataDir = WriterLocal.createIndex(indexDir);
			reader = new ReaderLocal(indexDir, dataDir,
					indexConfig.getSimilarityClass(), indexConfig.getReadOnly());
		}

		reader.initCache(indexConfig);
		return reader;
	}

	@Override
	public void reload() throws SearchLibException {
		rwl.w.lock();
		try {
			close(false);
			init(rootDir, dataDir);
			resetCache();
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void swap(long version, boolean deleteOld) throws SearchLibException {
		ReaderLocal newReader = null;
		if (version > 0)
			newReader = ReaderLocal.findVersion(rootDir, version,
					similarityClass, readOnly);
		else
			newReader = ReaderLocal.findMostRecent(rootDir, similarityClass,
					readOnly);
		if (newReader == null)
			return;
		rwl.w.lock();
		try {
			close(false);
			init(newReader);
			resetCache();
			if (deleteOld)
				deleteAllOthers();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}

	}

	public DocSetHits searchDocSet(SearchRequest searchRequest)
			throws IOException, ParseException, SyntaxError,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.r.lock();
		try {

			Schema schema = searchRequest.getConfig().getSchema();
			Field defaultField = schema.getFieldList().getDefaultField();

			return searchCache.get(this, searchRequest, schema, defaultField);

		} finally {
			rwl.r.unlock();
		}
	}

	public DocSetHits newDocSetHits(SearchRequest searchRequest, Schema schema,
			Field defaultField, Analyzer analyzer) throws IOException,
			ParseException, SyntaxError, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SearchLibException {

		boolean isFacet = searchRequest.isFacet();

		FilterHits filterHits = searchRequest.getFilterList().getFilterHits(
				this, defaultField, analyzer);

		DocSetHits dsh = new DocSetHits(this, searchRequest.getQuery(),
				filterHits, searchRequest.getSortList(), isFacet);
		return dsh;
	}

	public FieldList<FieldValue> getDocumentFields(int docId,
			FieldList<Field> fieldList) throws IOException, ParseException,
			SyntaxError {
		rwl.r.lock();
		try {
			return fieldCache.get(this, docId, fieldList);
		} finally {
			rwl.r.unlock();
		}
	}

	public FieldList<FieldValue> getTermsVectorFields(int docId,
			FieldList<Field> fieldList) throws IOException {
		rwl.r.lock();
		try {
			FieldList<FieldValue> fieldValueList = new FieldList<FieldValue>();
			for (Field field : fieldList) {
				TermFreqVector termFreqVector = indexReader.getTermFreqVector(
						docId, field.getName());
				if (termFreqVector == null)
					continue;
				FieldValueItem[] fieldValueItem = termFreqVector.getTerms();
				if (fieldValueItem == null)
					continue;
				fieldValueList.add(new FieldValue(field, fieldValueItem));
			}
			return fieldValueList;
		} finally {
			rwl.r.unlock();
		}
	}

	public FieldList<FieldValue> getTerms(int docId, FieldList<Field> fieldList)
			throws IOException {
		rwl.r.lock();
		try {
			TermPositions termPosition = indexReader.termPositions();
			FieldList<FieldValue> fieldValueList = new FieldList<FieldValue>();
			for (Field field : fieldList) {
				String fieldName = field.getName();
				List<FieldValueItem> fieldValueItemList = new ArrayList<FieldValueItem>();
				TermEnum termEnum = indexReader.terms(new Term(fieldName, ""));
				Term term = termEnum.term();
				if (termEnum == null || !term.field().equals(fieldName))
					continue;
				do {
					term = termEnum.term();
					if (!term.field().equals(fieldName))
						break;
					termPosition.seek(term);
					if (!termPosition.skipTo(docId)
							|| termPosition.doc() != docId)
						continue;
					fieldValueItemList.add(new FieldValueItem(term.text()));
				} while (termEnum.next());
				termEnum.close();
				if (fieldValueItemList.size() > 0)
					fieldValueList
							.add(new FieldValue(field, fieldValueItemList));
			}
			return fieldValueList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public ResultDocument[] documents(DocumentsRequest documentsRequest)
			throws SearchLibException {
		rwl.r.lock();
		try {
			DocumentRequest[] requestedDocuments = documentsRequest
					.getRequestedDocuments();
			if (requestedDocuments == null)
				return null;
			ResultDocument[] documents = new ResultDocument[requestedDocuments.length];
			int i = 0;
			for (DocumentRequest documentRequest : requestedDocuments)
				documents[i++] = new ResultDocument(documentsRequest,
						documentRequest.doc, this);
			return documents;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
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
			if (reader == null)
				return true;
			return reader.sameIndex(this);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexStatistics getStatistics() {
		rwl.r.lock();
		try {
			return new IndexStatistics(indexReader);
		} finally {
			rwl.r.unlock();
		}
	}

	private void pushFile(File file, URI uri) throws URISyntaxException,
			IOException {
		StringBuffer query = new StringBuffer();
		query.append("?version=");
		query.append(getVersion());
		query.append("&fileName=");
		query.append(file.getName());
		uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
				uri.getPort(), uri.getPath() + "/push", query.toString(),
				uri.getFragment());
		UriWriteStream uws = null;
		try {
			uws = new UriWriteStream(uri, file);
		} finally {
			if (uws != null)
				uws.close();
		}
	}

	@Override
	public void push(URI dest) throws SearchLibException {
		rwl.r.lock();
		try {
			File[] files = dataDir.listFiles();
			for (File file : files) {
				if (file.getName().charAt(0) == '.')
					continue;
				pushFile(file, dest);
			}
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}

	}

	public SpellChecker getSpellChecker(String fieldName) throws IOException {
		rwl.r.lock();
		try {
			return spellCheckerCache.get(this, fieldName);
		} finally {
			rwl.r.unlock();
		}
	}

	protected SearchCache getSearchCache() {
		rwl.r.lock();
		try {
			return searchCache;
		} finally {
			rwl.r.unlock();
		}
	}

	protected FilterCache getFilterCache() {
		rwl.r.lock();
		try {
			return filterCache;
		} finally {
			rwl.r.unlock();
		}
	}

	protected FieldCache getFieldCache() {
		rwl.r.lock();
		try {
			return fieldCache;
		} finally {
			rwl.r.unlock();
		}
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

}
