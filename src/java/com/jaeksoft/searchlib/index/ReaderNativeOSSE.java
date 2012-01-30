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
import java.net.URI;
import java.util.Collection;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.OsseLibrary;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.index.term.TermEnum;
import com.jaeksoft.searchlib.index.term.TermFreqVector;
import com.jaeksoft.searchlib.query.MoreLikeThis;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class ReaderNativeOSSE extends ReaderAbstract {

	private Pointer index;

	private OsseErrorHandler err;

	protected ReaderNativeOSSE(File configDir, IndexConfig indexConfig)
			throws SearchLibException {
		err = new OsseErrorHandler();
		index = OsseLibrary.INSTANCE.OSSCLib_Index_Create(
				new WString(configDir.getPath()), err.getPointer());
		if (index == null)
			throw new SearchLibException(err.getError());
	}

	@Override
	public void close() {
		if (!OsseLibrary.INSTANCE.OSSCLib_Index_Close(index, err.getPointer()))
			Logging.warn(err.getError());
		err.release();
	}

	protected Pointer getIndex() {
		return index;
	}

	@Override
	public ResultDocument[] documents(DocumentsRequest documentsRequest) {
		// TODO Auto-generated method stub
		return null;
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
	public TermFreqVector getTermFreqVector(int docId, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void push(URI dest) {
		// TODO Auto-generated method stub

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
	public Result search(SearchRequest searchRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void swap(long version, boolean deleteOld) {
		// TODO Auto-generated method stub

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
	public String explain(SearchRequest searchRequest, int docId) {
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

}
