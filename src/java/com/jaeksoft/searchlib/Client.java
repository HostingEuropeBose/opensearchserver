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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpException;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;

public class Client extends Config {

	public Client(File initFileOrDir, boolean createIndexIfNotExists,
			boolean disableCrawler) throws SearchLibException {
		super(initFileOrDir, null, createIndexIfNotExists, disableCrawler);
	}

	public Client(File initFileOrDir, String resourceName,
			boolean createIndexIfNotExists) throws SearchLibException {
		super(initFileOrDir, resourceName, createIndexIfNotExists, false);
	}

	public Client(File initFile) throws SearchLibException {
		this(initFile, false, false);
	}

	public boolean updateDocument(IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Update document " + document.toString());
		try {
			return getIndex().updateDocument(getSchema(), document);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	public int updateDocuments(Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Update " + documents.size() + " documents");
		try {
			return getIndex().updateDocuments(getSchema(), documents);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	private int updateXmlDocuments(XPathParser xpp, int bufferSize,
			CredentialItem urlDefaultCredential)
			throws XPathExpressionException, NoSuchAlgorithmException,
			IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		NodeList nodeList = xpp.getNodeList("/index/document");
		int l = nodeList.getLength();
		Collection<IndexDocument> docList = new ArrayList<IndexDocument>(
				bufferSize);
		int docCount = 0;
		for (int i = 0; i < l; i++) {
			docList.add(new IndexDocument(getParserSelector(), xpp, nodeList
					.item(i), urlDefaultCredential));
			if (docList.size() == bufferSize) {
				docCount += updateDocuments(docList);
				Logging.info(docCount + " / " + l + " XML document(s) indexed.");
				docList.clear();
			}
		}
		if (docList.size() > 0) {
			docCount += updateDocuments(docList);
			Logging.info(docCount + " / " + l + " XML document(s) indexed.");
		}
		return docCount;
	}

	public int updateXmlDocuments(InputSource inputSource, int bufferSize,
			CredentialItem urlDefaultCredential)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, NoSuchAlgorithmException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		XPathParser xpp = new XPathParser(inputSource);
		return updateXmlDocuments(xpp, bufferSize, urlDefaultCredential);
	}

	public boolean deleteDocument(String uniqueField) throws IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {
		Timer timer = new Timer("Delete document " + uniqueField);
		try {
			return getIndex().deleteDocument(getSchema(), uniqueField);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public int deleteDocuments(Collection<String> uniqueFields)
			throws IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Delete " + uniqueFields.size() + " documents");
		try {
			return getIndex().deleteDocuments(getSchema(), uniqueFields);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public int deleteDocuments(SearchRequest searchRequest)
			throws SearchLibException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException,
			SyntaxError, URISyntaxException, InterruptedException {
		Timer timer = new Timer("Delete by query documents");
		try {
			return getIndex().deleteDocuments(searchRequest);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public void optimize() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		Timer timer = new Timer("Optimize");
		try {
			getIndex().optimize();
		} finally {
			getStatisticsList().addOptimize(timer);
		}
	}

	public boolean isOptimizing() {
		return getIndex().isOptimizing();
	}

	public void reload() throws SearchLibException {
		Timer timer = new Timer("Reload");
		try {
			getIndex().reload();
		} finally {
			getStatisticsList().addReload(timer);
		}
	}

	/*
	 * Older version compatibility
	 */
	@Deprecated
	public void reload(boolean deleteOld) throws IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {
		reload();
	}

	public Result search(SearchRequest searchRequest) throws SearchLibException {
		Timer timer = null;
		Result result = null;
		SearchLibException exception = null;
		try {
			searchRequest.init(this);
			timer = searchRequest.getTimer();
			result = getIndex().search(searchRequest);
			return result;
		} catch (SearchLibException e) {
			exception = e;
			throw e;
		} catch (Exception e) {
			exception = new SearchLibException(e);
			throw exception;
		} finally {
			if (timer != null) {
				if (exception != null)
					timer.setError(exception);
				getStatisticsList().addSearch(timer);
				getLogReportManager().log(searchRequest, timer, result);
			}
		}
	}

	public String explain(SearchRequest searchRequest, int docId)
			throws SearchLibException {
		Timer timer = null;
		SearchLibException exception = null;
		try {
			searchRequest.init(this);
			timer = searchRequest.getTimer();
			return getIndex().explain(searchRequest, docId);
		} catch (SearchLibException e) {
			exception = e;
		} finally {
			if (timer != null) {
				if (exception != null)
					timer.setError(exception);
				getStatisticsList().addSearch(timer);
			}
			if (exception != null)
				throw exception;
		}
		return null;
	}

	public ResultDocument[] documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, IllegalAccessException, InstantiationException {
		return getIndex().documents(documentsRequest);
	}

}
