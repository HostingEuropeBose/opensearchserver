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
import java.util.Collection;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.index.term.TermEnum;
import com.jaeksoft.searchlib.index.term.TermFreqVector;
import com.jaeksoft.searchlib.query.MoreLikeThis;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocument;

public interface ReaderInterface {

	public abstract boolean sameIndex(ReaderInterface reader);

	public void close();

	public Collection<?> getFieldNames() throws SearchLibException;

	public int getDocFreq(Term term) throws SearchLibException;

	public TermEnum getTermEnum() throws SearchLibException;

	public TermEnum getTermEnum(String field, String term)
			throws SearchLibException;

	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException;

	public abstract Query rewrite(Query query) throws SearchLibException;

	public abstract MoreLikeThis getMoreLikeThis() throws SearchLibException;

	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException;

	public String explain(SearchRequest searchRequest, int docId, boolean bHtml)
			throws SearchLibException;

	public ResultDocument[] documents(DocumentsRequest documentsRequest)
			throws SearchLibException;

	public IndexStatistics getStatistics() throws IOException;

	public void reload() throws SearchLibException;

	public void swap(long version, boolean deleteOld) throws SearchLibException;

	public void push(URI dest) throws SearchLibException;

	public long getVersion();

}
