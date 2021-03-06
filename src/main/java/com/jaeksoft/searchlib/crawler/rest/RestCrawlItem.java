/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.rest;

import java.io.UnsupportedEncodingException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.process.FieldMapCrawlItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class RestCrawlItem extends
		FieldMapCrawlItem<RestCrawlItem, RestCrawlThread, RestCrawlMaster> {

	private String name;

	private String url;

	private CredentialItem credential;

	private LanguageEnum lang;

	private int bufferSize;

	private String pathDocument;

	public RestCrawlItem(RestCrawlMaster crawlMaster) {
		super(crawlMaster, new RestFieldMap());
		name = null;
		url = null;
		credential = new CredentialItem();
		pathDocument = null;
		lang = LanguageEnum.UNDEFINED;
		bufferSize = 100;
	}

	public RestCrawlItem(RestCrawlMaster crawlMaster, String name) {
		this(crawlMaster);
		this.name = name;
	}

	protected RestCrawlItem(RestCrawlItem crawl) {
		this((RestCrawlMaster) crawl.threadMaster);
		crawl.copyTo(this);
	}

	public RestCrawlItem duplicate() {
		return new RestCrawlItem(this);
	}

	@Override
	public void copyTo(RestCrawlItem crawl) {
		super.copyTo(crawl);
		crawl.setName(this.getName());
		crawl.url = this.url;
		crawl.pathDocument = this.pathDocument;
		this.credential.copyTo(crawl.credential);
		crawl.lang = this.lang;
		crawl.bufferSize = this.bufferSize;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the credential
	 */
	public CredentialItem getCredential() {
		return credential;
	}

	/**
	 * @param lang
	 *            the lang to set
	 */
	public void setLang(LanguageEnum lang) {
		this.lang = lang;
	}

	/**
	 * @return the lang
	 */
	public LanguageEnum getLang() {
		return lang;
	}

	/**
	 * @return the fieldMap
	 */
	@Override
	public RestFieldMap getFieldMap() {
		return (RestFieldMap) super.getFieldMap();
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize
	 *            the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	protected final static String REST_CRAWL_NODE_NAME = "restCrawl";
	protected final static String REST_CRAWL_ATTR_NAME = "name";
	protected final static String REST_CRAWL_NODE_CREDENTIAL = "credential";
	protected final static String REST_CRAWL_ATTR_URL = "url";
	protected final static String REST_CRAWL_ATTR_LANG = "lang";
	protected final static String REST_CRAWL_ATTR_BUFFER_SIZE = "bufferSize";
	protected final static String REST_CRAWL_NODE_NAME_MAP = "map";
	protected final static String REST_CRAWL_NODE_DOC_PATH = "documentPath";

	public RestCrawlItem(RestCrawlMaster crawlMaster, XPathParser xpp, Node item)
			throws XPathExpressionException {
		this(crawlMaster);
		setName(XPathParser.getAttributeString(item, REST_CRAWL_ATTR_NAME));
		setUrl(XPathParser.getAttributeString(item, REST_CRAWL_ATTR_URL));
		setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(item,
				REST_CRAWL_ATTR_LANG)));
		setBufferSize(XPathParser.getAttributeValue(item,
				REST_CRAWL_ATTR_BUFFER_SIZE));
		Node mapNode = xpp.getNode(item, REST_CRAWL_NODE_NAME_MAP);
		if (mapNode != null)
			getFieldMap().load(mapNode);
		Node pathNode = xpp.getNode(item, REST_CRAWL_NODE_DOC_PATH);
		if (pathNode != null)
			setPathDocument(StringEscapeUtils.unescapeXml(pathNode
					.getTextContent()));
		Node credNode = xpp.getNode(item, REST_CRAWL_NODE_CREDENTIAL);
		if (credNode != null)
			credential = CredentialItem.fromXml(credNode);

	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		xmlWriter.startElement(REST_CRAWL_NODE_NAME, REST_CRAWL_ATTR_NAME,
				getName(), REST_CRAWL_ATTR_URL, getUrl(), REST_CRAWL_ATTR_LANG,
				getLang().getCode(), REST_CRAWL_ATTR_BUFFER_SIZE,
				Integer.toString(getBufferSize()));
		xmlWriter.startElement(REST_CRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		xmlWriter.writeSubTextNodeIfAny(REST_CRAWL_NODE_DOC_PATH,
				xmlWriter.escapeXml(getPathDocument()));
		credential.writeXml(xmlWriter);
		xmlWriter.endElement();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(RestCrawlItem o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @return the pathDocument
	 */
	public String getPathDocument() {
		return pathDocument;
	}

	/**
	 * @param pathDocument
	 *            the pathDocument to set
	 */
	public void setPathDocument(String pathDocument) {
		this.pathDocument = pathDocument;
	}

}
