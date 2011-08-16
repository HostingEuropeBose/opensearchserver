/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.Target;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class MappingController extends CrawlerController implements
		ListitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4912079093808208146L;

	private transient SchemaField selectedUrlField;

	private transient SchemaField selectedIndexField;

	public MappingController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		selectedUrlField = null;
		selectedIndexField = null;
	}

	public List<SchemaField> getUrlFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<SchemaField> list = client.getUrlManager().getUrlDbClient()
					.getSchema().getFieldList().getList();
			if (list.size() > 0 && selectedUrlField == null)
				selectedUrlField = list.get(0);
			return list;
		}
	}

	@Override
	protected void eventSchemaChange() throws SearchLibException {
		reloadPage();
	}

	public List<SchemaField> getIndexFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<SchemaField> list = client.getSchema().getFieldList()
					.getList();
			if (list.size() > 0 && selectedIndexField == null)
				selectedIndexField = list.get(0);
			return list;
		}
	}

	public void setSelectedUrlField(SchemaField field) {
		synchronized (this) {
			selectedUrlField = field;
		}
	}

	public SchemaField getSelectedUrlField() {
		synchronized (this) {
			return selectedUrlField;
		}
	}

	public void setSelectedIndexField(SchemaField field) {
		synchronized (this) {
			selectedIndexField = field;
		}
	}

	public SchemaField getSelectedIndexField() {
		synchronized (this) {
			return selectedIndexField;
		}
	}

	public FieldMap getFieldMap() {
		try {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getWebCrawlerFieldMap();
		} catch (SearchLibException e) {
			throw new RuntimeException(e);
		}
	}

	public void onAdd() throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (!isWebCrawlerParametersRights())
			throw new SearchLibException("Not allowed");
		if (selectedUrlField == null || selectedIndexField == null)
			return;
		FieldMap fieldMap = getFieldMap();
		fieldMap.add(selectedUrlField.getName(),
				new Target(selectedIndexField.getName()));
		fieldMap.store();
		reloadPage();
	}

	@SuppressWarnings("unchecked")
	public void onLinkRemove(Event event) throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (!isWebCrawlerParametersRights())
			throw new SearchLibException("Not allowed");
		GenericLink<String, Target> link = (GenericLink<String, Target>) event
				.getData();
		FieldMap fieldMap = getFieldMap();
		fieldMap.remove(link);
		fieldMap.store();
		reloadPage();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(Listitem item, Object data) throws Exception {
		GenericLink<String, Target> link = (GenericLink<String, Target>) data;
		new Listcell(link.getSource()).setParent(item);
		new Listcell(link.getTarget().getName()).setParent(item);
		Listcell listcell = new Listcell();
		Image image = new Image("/images/action_delete.png");
		image.addForward(null, this, "onLinkRemove", data);
		image.setParent(listcell);
		listcell.setParent(item);
	}
}
