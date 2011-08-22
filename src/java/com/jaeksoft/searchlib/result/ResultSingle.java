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

package com.jaeksoft.searchlib.result;

import java.io.IOException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.index.StringIndex;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.spellcheck.SpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheckField;

public class ResultSingle extends Result {

	private static final long serialVersionUID = -8289431499983379291L;

	transient private ReaderLocal reader;
	transient private StringIndex[] sortStringIndexArray;
	transient private StringIndex[] facetStringIndexArray;
	transient private DocSetHits docSetHits;

	public ResultSingle() {
	}

	/**
	 * The constructor executes the request using the searcher provided and
	 * computes the facets.
	 * 
	 * @param searcher
	 * @param request
	 * @throws IOException
	 * @throws ParseException
	 * @throws SyntaxError
	 * @throws SearchLibException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public ResultSingle(ReaderLocal reader, SearchRequest searchRequest)
			throws IOException, ParseException, SyntaxError,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(searchRequest);

		this.reader = reader;
		docSetHits = reader.searchDocSet(searchRequest);
		numFound = docSetHits.getDocNumFound();
		maxScore = docSetHits.getMaxScore();

		for (SpellCheckField spellCheckField : searchRequest
				.getSpellCheckFieldList())
			this.spellCheckList
					.addObject(new SpellCheck(this, spellCheckField));

		ResultScoreDoc[] docs;
		// Are we doing collapsing ?
		if (collapse != null) {
			docs = collapse.collapse(this);
			collapsedDocCount = collapse.getDocCount();
		} else
			docs = fetch();

		facetStringIndexArray = FacetField.newStringIndexArrayForCollapsing(
				searchRequest.getFacetFieldList(), reader);
		if (facetStringIndexArray != null)
			for (ResultScoreDoc doc : docs)
				doc.loadFacetValues(facetStringIndexArray);

		for (FacetField facetField : searchRequest.getFacetFieldList())
			this.facetList.addObject(facetField.getFacet(this));

		if (searchRequest.isWithSortValues()) {
			sortStringIndexArray = searchRequest.getSortList()
					.newStringIndexArray(reader);
			if (sortStringIndexArray != null)
				for (ResultScoreDoc doc : docs)
					doc.loadSortValues(sortStringIndexArray);
		}

		setDocs(docs);

		if (searchRequest.isWithDocument())
			setDocuments(reader.documents(new DocumentsRequest(this)));

		searchRequest.getTimer().setInfo(searchRequest.toString());

	}

	private ResultScoreDoc[] fetch() throws IOException, ParseException,
			SyntaxError {
		int end = searchRequest.getEnd();
		String collapseField = searchRequest.getCollapseField();
		StringIndex collapseFieldStringIndex = (collapseField != null) ? reader
				.getStringIndex(collapseField) : null;
		return ResultScoreDoc.appendResultScoreDocArray(this, getDocs(),
				docSetHits.getScoreDocs(end), end, collapseFieldStringIndex);
	}

	/**
	 * Returns the searcher used to build the result.
	 * 
	 * @return Searcher
	 */
	public ReaderLocal getReader() {
		return reader;
	}

	/**
	 * 
	 * @return DocSetHits.
	 */
	public DocSetHits getDocSetHits() {
		return this.docSetHits;
	}

}
