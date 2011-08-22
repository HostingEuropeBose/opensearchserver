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

package com.jaeksoft.searchlib.web.controller.delete;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.web.AbstractServlet;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.PushEvent;

public class DeleteController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3721788615871232348L;

	private class DeleteAlert extends AlertController {

		protected DeleteAlert(int num) throws InterruptedException {
			super(
					"Please, confirm that you want to delete the documents matching this query: "
							+ request.getQueryString() + ". " + num
							+ " document(s) will be erased", Messagebox.YES
							| Messagebox.NO, Messagebox.QUESTION);
		}

		@Override
		protected void onYes() throws SearchLibException {
			try {
				request.reset();
				Client client = getClient();
				if (client == null)
					return;
				client.deleteDocuments(request);
				PushEvent.DOCUMENT_UPDATED.publish(client);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} catch (InstantiationException e) {
				throw new SearchLibException(e);
			} catch (IllegalAccessException e) {
				throw new SearchLibException(e);
			} catch (ClassNotFoundException e) {
				throw new SearchLibException(e);
			} catch (ParseException e) {
				throw new SearchLibException(e);
			} catch (SyntaxError e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			} catch (InterruptedException e) {
				throw new SearchLibException(e);
			}
		}

	}

	private transient SearchRequest request;

	private transient boolean isChecked;

	public DeleteController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		request = null;
		isChecked = false;
		Client client = getClient();
		if (client != null)
			request = getClient().getNewSearchRequest();
	}

	public SearchRequest getRequest() {
		return request;
	}

	public void onCheck() throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, SearchLibException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		request.reset();
		int numFound = getClient().search(request).getNumFound();
		isChecked = true;
		new AlertController(numFound + " document(s) found.",
				Messagebox.INFORMATION);
		reloadPage();
	}

	public void onDelete() throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, SearchLibException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		if (!isChecked)
			return;
		request.reset();
		int numFound = getClient().search(request).getNumFound();
		new DeleteAlert(numFound);
	}

	public void onQueryChange() {
		isChecked = false;
		reloadPage();
	}

	public boolean isNotChecked() {
		return !isChecked;
	}

	public String getRequestApiCall() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return null;
		StringBuffer sb = AbstractServlet.getApiUrl(getBaseUrl(), "/delete",
				client, getLoggedUser());
		String q = request.getQueryString();
		if (q == null)
			q = "";
		else
			q = q.replaceAll("\n", " ");
		sb.append("&q=");
		sb.append(URLEncoder.encode(q, "UTF-8"));
		return sb.toString();
	}

	@Override
	protected void eventClientChange() throws SearchLibException {
		reset();
		reloadPage();
	}

	@Override
	protected void eventFlushPrivileges() throws SearchLibException {
		reset();
		reloadPage();
	}

	@Override
	protected void eventDocumentUpdate() throws SearchLibException {
	}

	@Override
	protected void eventLogout() throws SearchLibException {
		reset();
		reloadPage();
	}

}
