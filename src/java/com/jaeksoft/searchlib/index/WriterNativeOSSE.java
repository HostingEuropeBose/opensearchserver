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

import java.util.Collection;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.OsseIndex;
import com.jaeksoft.searchlib.index.osse.OsseLibrary;
import com.jaeksoft.searchlib.index.osse.OsseTransaction;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class WriterNativeOSSE extends WriterAbstract {

	private OsseIndex index;

	protected WriterNativeOSSE(OsseIndex index, IndexConfig indexConfig) {
		super(indexConfig);
		this.index = index;
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

	public void createField(SchemaField schemaField) throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			transaction = new OsseTransaction(index);
			if (OsseLibrary.INSTANCE.OSSCLib_Transact_CreateField(
					transaction.getPointer(),
					new WString(schemaField.getName()),
					OsseLibrary.OSSCLIB_FIELD_UI32FIELDTYPE_STRING, 0, null,
					transaction.getErrorPointer()) == null)
				transaction.throwError();
			transaction.commit();
		} finally {
			if (transaction != null)
				transaction.release();
		}
	}

	public void deleteField(String fieldName) throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			WString[] wTerms = new WString[] { new WString(fieldName) };
			transaction = new OsseTransaction(index);
			if (OsseLibrary.INSTANCE.OSSCLib_Transact_DeleteFields(
					transaction.getPointer(), wTerms, 1,
					transaction.getErrorPointer()) != 1)
				transaction.throwError();
			transaction.commit();
		} finally {
			if (transaction != null)
				transaction.release();
		}
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			transaction = new OsseTransaction(index);
			Pointer docPtr = OsseLibrary.INSTANCE
					.OSSCLib_Transact_Document_New(transaction.getPointer(),
							transaction.getErrorPointer());
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
										transaction.getPointer(), docPtr,
										field, wTerms, wTerms.length,
										transaction.getErrorPointer());
						System.out
								.println("OSSCLib_Transact_Document_AddStringTerms returns "
										+ res + " / " + wTerms.length);
					}
				}
			}
			transaction.commit();
		} finally {
			if (transaction != null)
				transaction.release();
		}
		return true;
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
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
