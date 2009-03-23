/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.snippet.SnippetField;

public class SnippetController extends QueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1641413871487856522L;

	private String selectedSnippet = null;

	private List<String> snippetFieldLeft = null;

	private RowRenderer rowRenderer = null;

	public SnippetController() throws SearchLibException {
		super();
	}

	public RowRenderer getSnippetFieldRenderer() {
		synchronized (this) {
			if (rowRenderer != null)
				return rowRenderer;
			rowRenderer = new SnippetFieldRenderer();
			return rowRenderer;
		}
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			return getSnippetFieldLeft().size() > 0;
		}
	}

	public List<String> getSnippetFieldLeft() throws SearchLibException {
		synchronized (this) {
			if (snippetFieldLeft != null)
				return snippetFieldLeft;
			snippetFieldLeft = new ArrayList<String>();
			FieldList<SnippetField> snippetFields = getRequest()
					.getSnippetFieldList();
			for (SchemaField field : getClient().getSchema().getFieldList())
				if (field.isStored())
					if ("positions_offsets".equals(field.getTermVectorLabel()))
						if (snippetFields.get(field.getName()) == null) {
							if (selectedSnippet == null)
								selectedSnippet = field.getName();
							snippetFieldLeft.add(field.getName());
						}
			return snippetFieldLeft;
		}
	}

	public void onSnippetRemove(Event event) throws SearchLibException {
		synchronized (this) {
			SnippetField field = (SnippetField) event.getData();
			getRequest().getSnippetFieldList().remove(field);
			reloadPage();
		}
	}

	public void setSelectedSnippet(String value) {
		synchronized (this) {
			selectedSnippet = value;
		}
	}

	public String getSelectedSnippet() {
		synchronized (this) {
			return selectedSnippet;
		}
	}

	public void onSnippetAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedSnippet == null)
				return;
			getRequest().getSnippetFieldList().add(
					new SnippetField(selectedSnippet));
			reloadPage();
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			snippetFieldLeft = null;
			selectedSnippet = null;
			super.reloadPage();
		}
	}

}