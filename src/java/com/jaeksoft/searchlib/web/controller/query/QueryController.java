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

package com.jaeksoft.searchlib.web.controller.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.web.AbstractServlet;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.PushEvent;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public final class QueryController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3182630816725436838L;

	private transient String requestName;

	private transient Entry<String, SearchRequest> selectedRequest;

	public QueryController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedRequest = null;
		setRequest(null);
		setResult(null);
	}

	public String getRequestApiCall() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return null;
		SearchRequest request = getRequest();
		if (request == null)
			return null;
		StringBuffer sb = AbstractServlet.getApiUrl(getBaseUrl(), "/select",
				client, getLoggedUser());
		String requestName = request.getRequestName();
		if (requestName != null && requestName.length() > 0) {
			sb.append("&qt=");
			sb.append(URLEncoder.encode(requestName, "UTF-8"));
		}
		String q = request.getQueryString();
		if (q == null || q.length() == 0)
			q = "*:*";
		sb.append("&q=");
		sb.append(URLEncoder.encode(q, "UTF-8"));
		return sb.toString();
	}

	@Override
	public void eventRequestListChange() throws SearchLibException {
		reloadPage();
	}

	public void setRequest(SearchRequest request) {
		ScopeAttribute.QUERY_SEARCH_REQUEST.set(this, request);
	}

	public boolean isEditing() throws SearchLibException {
		return getRequest() != null;
	}

	public boolean isNotEditing() throws SearchLibException {
		return !isEditing();
	}

	public boolean isNotSelectedRequest() {
		return !isSelectedRequest();
	}

	public boolean isSelectedRequest() {
		return selectedRequest != null;
	}

	public Entry<String, SearchRequest> getSelectedRequest() {
		return selectedRequest;
	}

	public void setSelectedRequest(Entry<String, SearchRequest> entry)
			throws SearchLibException {
		selectedRequest = entry;
		reloadPage();
	}

	/**
	 * @param requestName
	 *            the requestName to set
	 */
	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	/**
	 * @return the requestName
	 */
	public String getRequestName() {
		return requestName;
	}

	private boolean checkRequestName() throws InterruptedException,
			SearchLibException {
		if (requestName == null || requestName.length() == 0) {
			new AlertController("Please enter a name");
			return false;
		}
		Client client = getClient();
		if (client.getSearchRequestMap().get(requestName) != null) {
			new AlertController("This name is already used.");
			return false;
		}
		return true;
	}

	private void newEdit(SearchRequest newRequest) {
		newRequest.setRequestName(requestName);
		setRequest(newRequest);
		reloadPage();
		PushEvent.QUERY_EDIT_REQUEST.publish(newRequest);
	}

	public void onNew() throws SearchLibException, InterruptedException {
		if (!checkRequestName())
			return;
		newEdit(getClient().getNewSearchRequest());
	}

	public void onNewCopy() throws InterruptedException, SearchLibException {
		if (!checkRequestName())
			return;
		newEdit(getClient().getNewSearchRequest(selectedRequest.getKey()));
	}

	@SuppressWarnings("unchecked")
	private Entry<String, SearchRequest> getRequestEntry(Component comp) {
		return (Entry<String, SearchRequest>) getRecursiveComponentAttribute(
				comp, "requestentry");
	}

	public void doEditQuery(Component comp) throws SearchLibException {
		Entry<String, SearchRequest> entry = getRequestEntry(comp);
		if (entry == null)
			return;
		setRequest(getClient().getNewSearchRequest(entry.getKey()));
		PushEvent.QUERY_EDIT_REQUEST.publish(getRequest());
	}

	public void doDeleteQuery(Component comp) throws SearchLibException,
			InterruptedException {
		Entry<String, SearchRequest> entry = getRequestEntry(comp);
		if (entry == null)
			return;
		if (!isSchemaRights())
			throw new SearchLibException("Not allowed");
		new RemoveAlert(entry.getKey());
	}

	public Set<Entry<String, SearchRequest>> getRequests()
			throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getSearchRequestMap().getRequests();
	}

	public void setResult(Result result) {
		ScopeAttribute.QUERY_SEARCH_RESULT.set(this, result);
	}

	public void onCancel() throws SearchLibException {
		reset();
		reloadPage();
	}

	public void onSave() throws SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		if (!isSchemaRights())
			throw new SearchLibException("Not allowed");
		Client client = getClient();
		SearchRequest request = getRequest();
		client.getSearchRequestMap().put(request);
		client.saveRequests();
		onCancel();
		PushEvent.REQUEST_LIST_CHANGED.publish(client);
	}

	private class RemoveAlert extends AlertController {

		private String selectedRequest;

		public RemoveAlert(String selectedRequest) throws InterruptedException {
			super("Please, confirm you want to remove the request: "
					+ selectedRequest, Messagebox.CANCEL | Messagebox.YES,
					Messagebox.QUESTION);
			this.selectedRequest = selectedRequest;
		}

		@Override
		public void onYes() throws SearchLibException {
			Client client = getClient();
			client.getSearchRequestMap().remove(selectedRequest);
			client.saveRequests();
			PushEvent.REQUEST_LIST_CHANGED.publish(client);
		}

	}

	public void onSearch() throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, SearchLibException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		SearchRequest request = getRequest();

		if (request.getQueryString() == null)
			request.setQueryString("*:*");

		request.reset();
		setResult(getClient().search(request));
		PushEvent.QUERY_EDIT_RESULT.publish(getResult());
	}

}
