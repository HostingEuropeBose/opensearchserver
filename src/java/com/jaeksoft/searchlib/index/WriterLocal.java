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
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzerWrapper;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.SimpleLock;

public class WriterLocal extends WriterAbstract {

	private final SimpleLock lock = new SimpleLock();

	private ReaderLocal readerLocal;

	protected WriterLocal(IndexConfig indexConfig, ReaderLocal readerLocal)
			throws IOException {
		super(indexConfig);
		this.readerLocal = readerLocal;
	}

	private IndexWriter close(IndexWriter indexWriter) {
		if (indexWriter == null)
			return null;
		synchronized (indexWriterList) {
			if (indexWriterList.size() > 1)
				logIndexWriterList();
		}
		try {
			indexWriter.close();
			synchronized (indexWriterList) {
				indexWriterList.remove(indexWriter);
			}
			return null;
		} catch (IOException e) {
			Logging.warn(e.getMessage(), e);
			return indexWriter;
		}
	}

	public static class ThreadInfo {
		public String info;
		public StackTraceElement[] stackTrace;

		public ThreadInfo() {
			Thread thread = Thread.currentThread();
			info = thread.getId() + " - " + thread.getName();
			stackTrace = thread.getStackTrace();
		}
	}

	private final Map<IndexWriter, ThreadInfo> indexWriterList = new HashMap<IndexWriter, ThreadInfo>();

	private final void logIndexWriterList() {
		synchronized (indexWriterList) {
			Logging.warn("IndexWriterList size " + indexWriterList.size());
			if (indexWriterList.size() > 1)
				for (ThreadInfo threadInfo : indexWriterList.values())
					Logging.warn("IndexWriter " + threadInfo.info,
							threadInfo.stackTrace);
		}
	}

	private IndexWriter open() throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		IndexWriter indexWriter = openIndexWriter(readerLocal.getDatadir(),
				false);
		synchronized (indexWriterList) {
			if (indexWriterList.size() > 0)
				logIndexWriterList();
			indexWriterList.put(indexWriter, new ThreadInfo());
		}
		return indexWriter;
	}

	private static IndexWriter openIndexWriter(File directory, boolean create)
			throws IOException {
		return new IndexWriter(directory, create);
	}

	private final static SimpleDateFormat dateDirFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	protected static File createIndex(File rootDir) throws IOException {

		File dataDir = new File(rootDir, dateDirFormat.format(new Date()));

		IndexWriter indexWriter = null;
		try {
			dataDir.mkdirs();
			indexWriter = openIndexWriter(dataDir, true);
			return dataDir;
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (IOException e) {
					Logging.error(e.getMessage(), e);
				}
			}
		}
	}

	@Deprecated
	public void addDocument(IndexDocument document) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		IndexWriter indexWriter = null;
		lock.rl.lock();
		try {
			indexWriter = open();
			indexWriter.addDocument(document);
			indexWriter = close(indexWriter);
		} finally {
			close(indexWriter);
			lock.rl.unlock();
		}
	}

	private boolean updateDocNoLock(IndexWriter indexWriter, Schema schema,
			IndexDocument document) throws IOException,
			NoSuchAlgorithmException, SearchLibException {
		if (!acceptDocument(document))
			return false;

		if (beforeUpdateList != null)
			for (BeforeUpdateInterface beforeUpdate : beforeUpdateList)
				beforeUpdate.update(schema, document);

		PerFieldAnalyzerWrapper pfa = schema.getIndexPerFieldAnalyzer(document
				.getLang());

		SchemaField uniqueField = schema.getFieldList().getUniqueField();
		if (uniqueField != null) {
			String uniqueFieldName = uniqueField.getName();
			FieldValueItem uniqueFieldValue = document.getFieldValue(
					uniqueFieldName, 0);
			if (uniqueFieldValue == null)
				throw new SearchLibException("The unique value is missing ("
						+ uniqueFieldName + ")");
			indexWriter.updateDocument(new Term(uniqueFieldName,
					uniqueFieldValue.getValue()), document, pfa);
		} else
			indexWriter.addDocument(document, pfa);
		return true;
	}

	private boolean updateDocumentNoLock(Schema schema, IndexDocument document)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			boolean updated = updateDocNoLock(indexWriter, schema, document);
			indexWriter = close(indexWriter);
			if (updated)
				readerLocal.reload();
			return updated;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		lock.rl.lock();
		try {
			return updateDocumentNoLock(schema, document);
		} finally {
			lock.rl.unlock();
		}
	}

	private int updateDocumentsNoLock(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int count = 0;
			indexWriter = open();
			for (IndexDocument document : documents)
				if (updateDocNoLock(indexWriter, schema, document))
					count++;
			indexWriter = close(indexWriter);
			if (count > 0)
				readerLocal.reload();
			return count;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		lock.rl.lock();
		try {
			return updateDocumentsNoLock(schema, documents);
		} finally {
			lock.rl.unlock();
		}
	}

	private void optimizeNoLock() throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			optimizing = true;
			indexWriter.optimize(true);
			indexWriter = close(indexWriter);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			optimizing = false;
			close(indexWriter);
		}
	}

	@Override
	public void optimize() throws SearchLibException {
		lock.rl.lock();
		try {
			optimizeNoLock();
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentNoLock(String field, String value)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = readerLocal.getStatistics().getNumDeletedDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(new Term(field, value));
			indexWriter = close(indexWriter);
			readerLocal.reload();
			l = readerLocal.getStatistics().getNumDeletedDocs() - l;
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int deleteDocument(Schema schema, String uniqueField)
			throws SearchLibException {
		SchemaField uniqueSchemaField = schema.getFieldList().getUniqueField();
		if (uniqueSchemaField == null)
			return 0;
		lock.rl.lock();
		try {
			return deleteDocumentNoLock(uniqueSchemaField.getName(),
					uniqueField);
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentsNoLock(Schema schema, Term[] terms)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = readerLocal.getStatistics().getNumDeletedDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(terms);
			indexWriter = close(indexWriter);
			if (terms.length > 0)
				readerLocal.reload();
			l = readerLocal.getStatistics().getNumDeletedDocs() - l;
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws SearchLibException {
		SchemaField uniqueSchemaField = schema.getFieldList().getUniqueField();
		if (uniqueSchemaField == null)
			return 0;
		String uniqueField = uniqueSchemaField.getName();
		Term[] terms = new Term[uniqueFields.size()];
		int i = 0;
		for (String value : uniqueFields)
			terms[i++] = new Term(uniqueField, value);
		lock.rl.lock();
		try {
			return deleteDocumentsNoLock(schema, terms);
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentsNoLock(SearchRequest query)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = readerLocal.getStatistics().getNumDeletedDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(query.getQuery());
			indexWriter = close(indexWriter);
			readerLocal.reload();
			l = readerLocal.getStatistics().getNumDeletedDocs() - l;
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int deleteDocuments(SearchRequest query) throws SearchLibException {
		lock.rl.lock();
		try {
			return deleteDocumentsNoLock(query);
		} finally {
			lock.rl.unlock();
		}
	}

	private void deleteAllNoLock() throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			indexWriter.deleteAll();
			indexWriter = close(indexWriter);
			readerLocal.reload();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public void deleteAll() throws SearchLibException {
		lock.rl.lock();
		try {
			deleteAllNoLock();
		} finally {
			lock.rl.unlock();
		}
	}

	@Override
	public void checkSchemaFieldList(SchemaFieldList schemaFieldList)
			throws SearchLibException {
		// TODO Auto-generated method stub

	}

}
