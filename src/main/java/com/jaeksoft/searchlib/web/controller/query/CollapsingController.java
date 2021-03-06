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

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.collapse.CollapseParameters;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;

public class CollapsingController extends AbstractQueryController {

	private transient List<String> indexedFields;

	public CollapsingController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		indexedFields = null;
	}

	public CollapseParameters.Mode[] getCollapseModes() {
		return CollapseParameters.Mode.values();
	}

	public CollapseParameters.Type[] getCollapseTypes() {
		return CollapseParameters.Type.values();
	}

	public List<String> getIndexedFields() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (indexedFields != null)
				return indexedFields;
			indexedFields = new ArrayList<String>();
			indexedFields.add(null);
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.checkIndexed(Indexed.YES))
					indexedFields.add(field.getName());
			return indexedFields;
		}
	}

	@Command
	@Override
	public void reload() throws SearchLibException {
		synchronized (this) {
			indexedFields = null;
			super.reload();
		}
	}

	@GlobalCommand
	@Override
	public void eventSchemaChange(Client client) throws SearchLibException {
		reload();
	}
}
