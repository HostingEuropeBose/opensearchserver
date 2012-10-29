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

package com.jaeksoft.searchlib.query;

import java.io.StringReader;
import java.util.Set;

import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.index.IndexReader;

/*
 * TODO: Full implementation
 */
public class MoreLikeThis {

	public static final int DEFAULT_MIN_WORD_LENGTH = 0;
	public static final int DEFAULT_MAX_WORD_LENGTH = 0;
	public static final int DEFAULT_MIN_DOC_FREQ = 0;
	public static final int DEFAULT_MIN_TERM_FREQ = 0;
	public static final int DEFAULT_MAX_NUM_TOKENS_PARSED = 0;
	public static final int DEFAULT_MAX_QUERY_TERMS = 0;

	public MoreLikeThis(IndexReader indexReader) {
		// TODO Auto-generated constructor stub
	}

	public Query like(StringReader stringReader) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMinWordLen(int moreLikeThisMinWordLen) {
		// TODO Auto-generated method stub

	}

	public void setMaxWordLen(int moreLikeThisMaxWordLen) {
		// TODO Auto-generated method stub

	}

	public void setMinDocFreq(int moreLikeThisMinDocFreq) {
		// TODO Auto-generated method stub

	}

	public void setMinTermFreq(int moreLikeThisMinTermFreq) {
		// TODO Auto-generated method stub

	}

	public void setFieldNames(String[] arrayName) {
		// TODO Auto-generated method stub

	}

	public void setAnalyzer(Analyzer analyzer) {
		// TODO Auto-generated method stub

	}

	public void setStopWords(Set<String> words) {
		// TODO Auto-generated method stub

	}

	public Query like(int docId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMaxNumTokensParsed(int maxNumTokensParsed) {
		// TODO Auto-generated method stub

	}

	public void setMaxQueryTerms(int maxQueryTerms) {
		// TODO Auto-generated method stub

	}

}
