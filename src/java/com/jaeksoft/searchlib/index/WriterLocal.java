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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzerWrapper;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;

public class WriterLocal extends WriterAbstract {

	private final ReentrantLock l = new ReentrantLock(true);

	private IndexWriter indexWriter;

	private ReaderLocal readerLocal;

	protected WriterLocal(IndexConfig indexConfig, ReaderLocal readerLocal)
			throws IOException {
		super(indexConfig);
		this.readerLocal = readerLocal;
		this.indexWriter = null;
	}

	private void close() {
		l.lock();
		try {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (IOException e) {
					Logging.error(e.getMessage(), e);
				}
				indexWriter = null;
			}
		} finally {
			l.unlock();
		}
	}

	private void open() throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		l.lock();
		try {
			if (indexWriter != null)
				return;
			indexWriter = openIndexWriter(readerLocal.getDatadir(), false);
		} finally {
			l.unlock();
		}
	}

	private static IndexWriter openIndexWriter(File dataDir, boolean create)
			throws IOException {
		return new IndexWriter(dataDir, create);
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
		} catch (IOException e) {
			throw e;
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
		l.lock();
		try {
			open();
			indexWriter.addDocument(document);
			close();
		} finally {
			l.unlock();
		}
	}

	private boolean updateDoc(Schema schema, IndexDocument document)
			throws IOException, NoSuchAlgorithmException, SearchLibException {
		if (!acceptDocument(document))
			return false;

		if (beforeUpdateList != null)
			for (BeforeUpdateInterface beforeUpdate : beforeUpdateList)
				beforeUpdate.update(schema, document);

		PerFieldAnalyzerWrapper pfa = schema.getIndexPerFieldAnalyzer(document
				.getLang());

		Field uniqueField = schema.getFieldList().getUniqueField();
		if (uniqueField != null) {
			String uniqueFieldName = uniqueField.getName();
			FieldValueItem uniqueFieldValue = document.getFieldValue(
					uniqueFieldName, 0);
			indexWriter.updateDocument(new Term(uniqueFieldName,
					uniqueFieldValue.getValue()), document, pfa);
		} else
			indexWriter.addDocument(document, pfa);
		return true;
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		l.lock();
		try {
			open();
			boolean updated = updateDoc(schema, document);
			close();
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
			close();
			l.unlock();
		}
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		l.lock();
		try {
			int count = 0;
			open();
			for (IndexDocument document : documents)
				if (updateDoc(schema, document))
					count++;
			close();
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
			close();
			l.unlock();
		}

	}

	@Override
	public void optimize() throws SearchLibException {
		l.lock();
		try {
			open();
			optimizing = true;
			indexWriter.optimize(true);
			close();
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
			close();
			l.unlock();
		}
	}

	@Override
	public boolean deleteDocument(Schema schema, String uniqueField)
			throws SearchLibException {
		Field uniqueSchemaField = schema.getFieldList().getUniqueField();
		if (uniqueSchemaField == null)
			return false;
		l.lock();
		try {
			open();
			indexWriter.deleteDocuments(new Term(uniqueSchemaField.getName(),
					uniqueField));
			close();
			readerLocal.reload();
			return true;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close();
			l.unlock();
		}
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws SearchLibException {
		Field uniqueSchemaField = schema.getFieldList().getUniqueField();
		if (uniqueSchemaField == null)
			return 0;
		String uniqueField = uniqueSchemaField.getName();
		Term[] terms = new Term[uniqueFields.size()];
		int i = 0;
		for (String value : uniqueFields)
			terms[i++] = new Term(uniqueField, value);
		l.lock();
		try {
			open();
			indexWriter.deleteDocuments(terms);
			close();
			if (terms.length > 0)
				readerLocal.reload();
			return terms.length;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close();
			l.unlock();
		}
	}

	@Override
	public int deleteDocuments(SearchRequest query) throws SearchLibException {
		l.lock();
		try {
			open();
			indexWriter.deleteDocuments(query.getQuery());
			close();
			readerLocal.reload();
			return 0;
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
			close();
			l.unlock();
		}
	}

	public void xmlInfo(PrintWriter writer) {
		// TODO Auto-generated method stub

	}

}
