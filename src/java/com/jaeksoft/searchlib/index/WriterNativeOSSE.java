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
import java.util.Collection;

import com.jaeksoft.searchlib.index.osse.OsseLibrary;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class WriterNativeOSSE extends WriterAbstract {

	private ReaderNativeOSSE reader;

	protected WriterNativeOSSE(File configDir, IndexConfig indexConfig,
			ReaderNativeOSSE reader) {
		super(indexConfig);
		this.reader = reader;
	}

	@Override
	public boolean deleteDocument(Schema schema, String uniqueField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void optimize() {

	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document) {
		Pointer errorPtr = null;
		Pointer transactPtr = null;
		try {
			errorPtr = OsseLibrary.INSTANCE.OSSCLib_ExtErrInfo_Create();
			transactPtr = OsseLibrary.INSTANCE.OSSCLib_Transact_Begin(
					reader.getIndex(), errorPtr);
			Pointer docPtr = OsseLibrary.INSTANCE
					.OSSCLib_Transact_Document_New(transactPtr, errorPtr);
			for (FieldContent fieldContent : document) {
				WString field = new WString(fieldContent.getField());
				for (FieldValueItem valueItem : fieldContent.getValues()) {
					String value = valueItem.getValue();
					String[] terms = value.split("\\s");
					if (terms.length > 0) {
						WString[] wTerms = new WString[terms.length];
						int i = 0;
						for (String term : terms)
							wTerms[i++] = new WString(term);
						int res = OsseLibrary.INSTANCE
								.OSSCLib_Transact_Document_AddStringTerms(
										transactPtr, docPtr, field, wTerms,
										wTerms.length, errorPtr);
						System.out
								.println("OSSCLib_Transact_Document_AddStringTerms returns "
										+ res + " / " + wTerms.length);
					}
				}
			}
			OsseLibrary.INSTANCE.OSSCLib_Transact_Commit(transactPtr, null, 0,
					null, errorPtr);
			transactPtr = null;
		} finally {
			if (transactPtr != null)
				OsseLibrary.INSTANCE.OSSCLib_Transact_RollBack(transactPtr,
						errorPtr);
			if (errorPtr != null)
				OsseLibrary.INSTANCE.OSSCLib_ExtErrInfo_Delete(errorPtr);
		}
		return true;
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) {
		int i = 0;
		for (IndexDocument document : documents)
			if (updateDocument(schema, document))
				i++;
		return i;
	}

	@Override
	public int deleteDocuments(SearchRequest query) {
		throw new RuntimeException("Not yet implemented");

	}

}
