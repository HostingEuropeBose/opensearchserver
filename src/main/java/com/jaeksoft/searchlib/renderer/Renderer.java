/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.QueryUtils;
import com.jaeksoft.searchlib.renderer.plugin.AuthPluginEnum;
import com.jaeksoft.searchlib.renderer.plugin.AuthPluginInterface;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.RendererServlet;

public class Renderer implements Comparable<Renderer> {

	private final static String RENDERER_ITEM_ROOTNODE_NAME = "renderer";
	private final static String RENDERER_ITEM_ROOT_ATTR_NAME = "name";
	private final static String RENDERER_ITEM_ROOT_ATTR_REQUEST = "request";
	private final static String RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL = "searchButtonLabel";
	private final static String RENDERER_ITEM_ROOT_ATTR_NORESULTFOUNDTEXT = "noResultFoundText";
	private final static String RENDERER_ITEM_ROOT_ATTR_ONERESULTFOUNDTEXT = "oneResultFoundText";
	private final static String RENDERER_ITEM_ROOT_ATTR_RESULTSFOUNDTEXT = "resultsFoundText";
	private final static String RENDERER_ITEM_ROOT_ATTR_FACET_WIDTH = "facetWidth";
	private final static String RENDERER_ITEM_NODE_CSS = "css";
	private final static String RENDERER_ITEM_NODE_NAME_FIELD = "field";
	private final static String RENDERER_ITEM_NODE_HEADER = "header";
	private final static String RENDERER_ITEM_NODE_FOOTER = "footer";
	private final static String RENDERER_ITEM_ROOT_ATTR_LOGENABLED = "logEnabled";
	private final static String RENDERER_ITEM_NODE_LOG_FIELD = "logField";
	private final static String RENDERER_ITEM_ROOT_ATTR_FIELD_CONTENTTYPE = "contentTypeField";
	private final static String RENDERER_ITEM_ROOT_ATTR_FIELD_FILENAME = "filenameField";
	private final static String RENDERER_ITEM_ROOT_ATTR_FIELD_HOCR = "ocrField";
	private final static String RENDERER_ITEM_AUTH_NODE = "auth";
	private final static String RENDERER_ITEM_AUTH_ATTR_SERVER_HOST = "serverHostname";
	private final static String RENDERER_ITEM_AUTH_ATTR_USERNAME = "username";
	private final static String RENDERER_ITEM_AUTH_ATTR_PASSWORD = "password";
	private final static String RENDERER_ITEM_AUTH_ATTR_DOMAIN = "domain";
	private final static String RENDERER_ITEM_AUTH_ATTR_PLUGIN_CLASS = "authPluginClass";
	private final static String RENDERER_ITEM_AUTH_ATTR_USER_ALLOW_FIELD = "userAllowField";
	private final static String RENDERER_ITEM_AUTH_ATTR_USER_DENY_FIELD = "userDenyField";
	private final static String RENDERER_ITEM_AUTH_ATTR_GROUP_ALLOW_FIELD = "groupAllowField";
	private final static String RENDERER_ITEM_AUTH_ATTR_GROUP_DENY_FIELD = "groupDenyField";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	private String requestName;

	private String searchButtonLabel;

	private String resultsFoundText;

	private String oneResultFoundText;

	private String noResultFoundText;

	private String facetWidth;

	private List<RendererField> fields;

	private List<RendererLogField> logFields;

	private String footer;

	private String header;

	private String css;

	private boolean logEnabled;

	private String contentTypeField;

	private String filenameField;

	private String hocrField;

	private String authUsername;

	private String authPassword;

	private String authDomain;

	private String authServer;

	private String authPluginClass;

	private String authUserAllowField;

	private String authGroupAllowField;

	private String authUserDenyField;

	private String authGroupDenyField;

	public Renderer() {
		name = null;
		requestName = null;
		searchButtonLabel = "Search";
		oneResultFoundText = "1 result found";
		resultsFoundText = "results found";
		noResultFoundText = "No results found";
		facetWidth = "200px";
		logEnabled = false;
		fields = new ArrayList<RendererField>();
		logFields = new ArrayList<RendererLogField>();
		footer = null;
		header = null;
		css = null;
		contentTypeField = null;
		filenameField = null;
		hocrField = null;
		authUsername = null;
		authPassword = null;
		authDomain = null;
		authServer = null;
		authPluginClass = null;
		authUserAllowField = "userAllow";
		authGroupAllowField = "groupAllow";
		authUserDenyField = "userDeny";
		authGroupDenyField = "groupDeny";
	}

	public Renderer(XPathParser xpp) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		this();
		Node rootNode = xpp.getNode(RENDERER_ITEM_ROOTNODE_NAME);
		if (rootNode == null)
			return;
		setName(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_NAME));
		setRequestName(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_REQUEST));
		setSearchButtonLabel(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL));
		setNoResultFoundText(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_NORESULTFOUNDTEXT));
		setOneResultFoundText(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_ONERESULTFOUNDTEXT));
		setResultsFoundText(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_RESULTSFOUNDTEXT));
		setContentTypeField(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_FIELD_CONTENTTYPE));
		setFilenameField(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_FIELD_FILENAME));
		setHocrField(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_FIELD_HOCR));

		Node authNode = xpp.getNode(rootNode, RENDERER_ITEM_AUTH_NODE);
		if (authNode != null) {
			setAuthUsername(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_USERNAME));
			setAuthPassword(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_PASSWORD));
			setAuthDomain(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_DOMAIN));
			setAuthServer(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_SERVER_HOST));
			setAuthPluginClass(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_PLUGIN_CLASS));
			setAuthUserAllowField(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_USER_ALLOW_FIELD));
			setAuthUserDenyField(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_USER_DENY_FIELD));
			setAuthGroupAllowField(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_GROUP_ALLOW_FIELD));
			setAuthGroupDenyField(XPathParser.getAttributeString(authNode,
					RENDERER_ITEM_AUTH_ATTR_GROUP_DENY_FIELD));
		}

		String p = XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_FACET_WIDTH);
		if (p == null || p.length() == 0)
			p = "200px";
		setFacetWidth(p);
		setHeader(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_HEADER,
				true));
		setFooter(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_FOOTER,
				true));
		setCss(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_CSS, true));
		NodeList nodeList = xpp.getNodeList(rootNode,
				RENDERER_ITEM_NODE_NAME_FIELD);
		for (int i = 0; i < nodeList.getLength(); i++)
			addField(new RendererField(xpp, nodeList.item(i)));
		NodeList nodeLogList = xpp.getNodeList(rootNode,
				RENDERER_ITEM_NODE_LOG_FIELD);
		for (int j = 0; j < nodeLogList.getLength(); j++)
			addLogField(new RendererLogField(xpp, nodeLogList.item(j)));
		setLogEnabled(Boolean.parseBoolean(XPathParser.getAttributeString(
				rootNode, RENDERER_ITEM_ROOT_ATTR_LOGENABLED)));
		if (css == null || css.length() == 0)
			css = getOldCss(xpp, rootNode);
	}

	public Renderer(Renderer source) {
		this();
		source.copyTo(this);
	}

	public void setDefaultCss() throws SearchLibException {
		InputStream is = getClass()
				.getResourceAsStream(
						"/com/jaeksoft/searchlib/template/common/renderers/default.xml");
		try {
			Renderer r = new Renderer(new XPathParser(is));
			setCss(r.css);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private void writeCss(PrintWriter pw, String name, String content) {
		if (content == null || content.length() == 0)
			return;
		pw.print(name);
		pw.print(" { ");
		pw.println(content);
		pw.println(" }");
	}

	/**
	 * Return the old CSS stylesheet
	 * 
	 * @return
	 * @throws XPathExpressionException
	 */
	private String getOldCss(XPathParser xpp, Node rootNode)
			throws XPathExpressionException {

		final String RENDERER_ITEM_NODE_COMMON_STYLE = "style";
		final String RENDERER_ITEM_NODE_INPUT_STYLE = "inputStyle";
		final String RENDERER_ITEM_NODE_BUTTON_STYLE = "buttonStyle";
		final String RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE = "autocompleteStyle";
		final String RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE = "autocompleteListStyle";
		final String RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE = "autocompleteLinkStyle";
		final String RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE = "autocompleteLinkHoverStyle";
		final String RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE = "documentFoundStyle";
		final String RENDERER_ITEM_NODE_PAGING_STYLE = "ossPagingStyle";
		final String RENDERER_ITEM_NODE_CURRENTPAGE_STYLE = "ossCurrentPageStyle";
		final String RENDERER_ITEM_NODE_ALINK = "alink";
		final String RENDERER_ITEM_NODE_AVISITED = "avisited";
		final String RENDERER_ITEM_NODE_AACTIVE = "aactive";
		final String RENDERER_ITEM_NODE_AHOVER = "ahover";
		final String RENDERER_ITEM_NODE_FACET_STYLE = "facetStyle";
		final String RENDERER_ITEM_NODE_RESULT_STYLE = "resultStyle";

		String commonStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_COMMON_STYLE, true);

		String inputStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_INPUT_STYLE, true);

		String buttonStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_BUTTON_STYLE, true);

		String documentFoundStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE, true);

		String pagingStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_PAGING_STYLE, true);

		String currentPageStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_CURRENTPAGE_STYLE, true);

		String autocompleteStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE, true);

		String autocompleteListStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE, true);

		String autocompleteLinkStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE, true);

		String autocompleteLinkHoverStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE, true);

		String aactive = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AACTIVE, true);

		String ahover = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AHOVER, true);

		String alink = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_ALINK, true);

		String avisited = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AVISITED, true);

		String facetStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_FACET_STYLE, true);

		String resultStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_RESULT_STYLE, true);

		rwl.w.lock();
		try {
			StringWriter sw = null;
			PrintWriter pw = null;
			try {
				sw = new StringWriter();
				pw = new PrintWriter(sw);
				writeCss(pw, ".osscmnrdr", commonStyle);
				writeCss(pw, ".ossinputrdr", inputStyle);
				writeCss(pw, ".ossbuttonrdr", buttonStyle);
				writeCss(pw, "a:link", alink);
				writeCss(pw, "a:hover", ahover);
				writeCss(pw, "a:visited", avisited);
				writeCss(pw, "a:active", aactive);
				writeCss(pw, "#ossautocomplete", autocompleteStyle);
				writeCss(pw, "#ossautocompletelist", autocompleteListStyle);
				writeCss(pw, ".ossautocomplete_link", autocompleteLinkStyle);
				writeCss(pw, ".ossautocomplete_link_over",
						autocompleteLinkHoverStyle);
				writeCss(pw, ".ossnumfound", documentFoundStyle);
				writeCss(pw, ".oss-paging", pagingStyle);
				writeCss(pw, ".oss-currentpage", currentPageStyle);
				writeCss(pw, ".oss-facet", facetStyle);
				writeCss(pw, ".oss-result", resultStyle);

				int i = 0;
				for (RendererField rendererField : fields) {
					i++;
					writeCss(pw, ".ossfieldrdr" + i,
							rendererField.getOldStyle());
				}
				return sw.toString();
			} finally {
				if (pw != null)
					IOUtils.closeQuietly(pw);
				if (sw != null)
					IOUtils.closeQuietly(sw);
			}
		} finally {
			rwl.w.unlock();
		}
	}

	public void copyTo(Renderer target) {
		rwl.r.lock();
		try {
			target.rwl.w.lock();
			try {
				target.name = name;
				target.requestName = requestName;
				target.searchButtonLabel = searchButtonLabel;
				target.noResultFoundText = noResultFoundText;
				target.oneResultFoundText = oneResultFoundText;
				target.resultsFoundText = resultsFoundText;
				target.facetWidth = facetWidth;
				target.fields.clear();
				target.logFields.clear();
				target.header = header;
				target.footer = footer;
				target.css = css;
				target.logEnabled = logEnabled;
				target.contentTypeField = contentTypeField;
				target.filenameField = filenameField;
				target.hocrField = hocrField;
				target.authUsername = authUsername;
				target.authPassword = authPassword;
				target.authDomain = authDomain;
				target.authServer = authServer;
				target.authPluginClass = authPluginClass;
				target.authUserAllowField = authUserAllowField;
				target.authUserDenyField = authUserDenyField;
				target.authGroupAllowField = authGroupAllowField;
				target.authGroupDenyField = authGroupDenyField;
				for (RendererField field : fields)
					target.addField(new RendererField(field));

				for (RendererLogField logField : logFields)
					target.addLogField(new RendererLogField(logField));
			} finally {
				target.rwl.w.unlock();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	public void addField(RendererField field) {
		rwl.w.lock();
		try {
			fields.add(field);
		} finally {
			rwl.w.unlock();
		}
	}

	public void removeField(RendererField field) {
		rwl.w.lock();
		try {
			fields.remove(field);
		} finally {
			rwl.w.unlock();
		}

	}

	/**
	 * Move field up
	 * 
	 * @param field
	 */
	public void fieldUp(RendererField field) {
		rwl.w.lock();
		try {
			int i = fields.indexOf(field);
			if (i == -1 || i == 0)
				return;
			fields.remove(i);
			fields.add(i - 1, field);
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Move field down
	 * 
	 * @param field
	 */
	public void fieldDown(RendererField field) {
		rwl.w.lock();
		try {
			int i = fields.indexOf(field);
			if (i == -1 || i == fields.size() - 1)
				return;
			fields.remove(i);
			fields.add(i + 1, field);
		} finally {
			rwl.w.unlock();
		}
	}

	public List<RendererField> getFields() {
		rwl.r.lock();
		try {
			return fields;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		rwl.w.lock();
		try {
			this.name = name;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		rwl.r.lock();
		try {
			return name;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param name
	 *            the requestName to set
	 */
	public void setRequestName(String requestName) {
		rwl.w.lock();
		try {
			this.requestName = requestName;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the requestName
	 */
	public String getRequestName() {
		rwl.r.lock();
		try {
			return requestName;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param searchButtonLabel
	 *            the searchButtonLabel to set
	 */
	public void setSearchButtonLabel(String searchButtonLabel) {
		rwl.w.lock();
		try {
			this.searchButtonLabel = searchButtonLabel;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the searchButtonLabel
	 */
	public String getSearchButtonLabel() {
		rwl.r.lock();
		try {
			return searchButtonLabel;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public final int compareTo(Renderer o) {
		rwl.r.lock();
		try {
			return name.compareTo(o.name);
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(RENDERER_ITEM_ROOTNODE_NAME,
					RENDERER_ITEM_ROOT_ATTR_NAME, name,
					RENDERER_ITEM_ROOT_ATTR_REQUEST, requestName,
					RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL,
					searchButtonLabel,
					RENDERER_ITEM_ROOT_ATTR_NORESULTFOUNDTEXT,
					noResultFoundText,
					RENDERER_ITEM_ROOT_ATTR_ONERESULTFOUNDTEXT,
					oneResultFoundText,
					RENDERER_ITEM_ROOT_ATTR_RESULTSFOUNDTEXT, resultsFoundText,
					RENDERER_ITEM_ROOT_ATTR_FACET_WIDTH, facetWidth,
					RENDERER_ITEM_ROOT_ATTR_LOGENABLED,
					Boolean.toString(logEnabled),
					RENDERER_ITEM_ROOT_ATTR_FIELD_CONTENTTYPE,
					contentTypeField, RENDERER_ITEM_ROOT_ATTR_FIELD_FILENAME,
					filenameField, RENDERER_ITEM_ROOT_ATTR_FIELD_HOCR,
					hocrField);

			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_HEADER, header);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_FOOTER, footer);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_CSS, css);
			for (RendererField field : fields)
				field.writeXml(xmlWriter, RENDERER_ITEM_NODE_NAME_FIELD);

			for (RendererLogField logReportField : logFields)
				logReportField
						.writeXml(xmlWriter, RENDERER_ITEM_NODE_LOG_FIELD);

			xmlWriter.startElement(RENDERER_ITEM_AUTH_NODE,
					RENDERER_ITEM_AUTH_ATTR_USERNAME, authUsername,
					RENDERER_ITEM_AUTH_ATTR_PASSWORD, authPassword,
					RENDERER_ITEM_AUTH_ATTR_DOMAIN, authDomain,
					RENDERER_ITEM_AUTH_ATTR_SERVER_HOST, authServer,
					RENDERER_ITEM_AUTH_ATTR_PLUGIN_CLASS, authPluginClass,
					RENDERER_ITEM_AUTH_ATTR_USER_ALLOW_FIELD,
					authUserAllowField,
					RENDERER_ITEM_AUTH_ATTR_USER_DENY_FIELD, authUserDenyField,
					RENDERER_ITEM_AUTH_ATTR_GROUP_ALLOW_FIELD,
					authGroupAllowField,
					RENDERER_ITEM_AUTH_ATTR_GROUP_DENY_FIELD,
					authGroupDenyField);
			xmlWriter.endElement();
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public String getApiUrl() throws UnsupportedEncodingException {
		rwl.r.lock();
		try {
			return RendererServlet.doRenderer(name, null);
		} finally {
			rwl.r.unlock();
		}
	}

	public String getIFrameHtmlCode(String width, String height)
			throws UnsupportedEncodingException {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("<iframe src=\"");
			sb.append(getApiUrl());
			sb.append("\" scrolling=\"auto\" frameborder=\"1\" width=\"");
			sb.append(width);
			sb.append("\" height=\"");
			sb.append(height);
			sb.append("\"><p>Your browser does not support iframes.</p></iframe>");
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public String getResultsFoundText() {
		rwl.r.lock();
		try {
			return resultsFoundText;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setResultsFoundText(String resultsFoundText) {
		rwl.w.lock();
		try {
			this.resultsFoundText = resultsFoundText;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getOneResultFoundText() {
		rwl.r.lock();
		try {
			return oneResultFoundText;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setOneResultFoundText(String oneResultFoundText) {
		rwl.w.lock();
		try {
			this.oneResultFoundText = oneResultFoundText;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getNoResultFoundText() {
		rwl.r.lock();
		try {
			return noResultFoundText;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * 
	 * @param noResultFoundText
	 */
	public void setNoResultFoundText(String noResultFoundText) {
		rwl.w.lock();
		try {
			this.noResultFoundText = noResultFoundText;
		} finally {
			rwl.w.unlock();
		}
	}

	final public String getResultFoundText(int resultsCount) {
		rwl.r.lock();
		try {
			switch (resultsCount) {
			case 0:
				return noResultFoundText;
			case 1:
				return oneResultFoundText;
			default:
				StringBuffer sb = new StringBuffer();
				sb.append(resultsCount);
				sb.append(' ');
				sb.append(resultsFoundText);
				return sb.toString();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the footer
	 */
	public String getFooter() {
		rwl.r.lock();
		try {
			return footer == null ? "" : footer;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param footer
	 *            the footer to set
	 */
	public void setFooter(String footer) {
		rwl.w.lock();
		try {
			this.footer = footer;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		rwl.r.lock();
		try {
			return header == null ? "" : header;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(String header) {
		rwl.w.lock();
		try {
			this.header = header;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the facetWidth
	 */
	public String getFacetWidth() {
		rwl.r.lock();
		try {
			return facetWidth;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param facetWidth
	 *            the facetWidth to set
	 */
	public void setFacetWidth(String facetWidth) {
		rwl.w.lock();
		try {
			this.facetWidth = facetWidth;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the css
	 */
	public String getCss() {
		rwl.r.lock();
		try {
			return css;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param css
	 *            the css to set
	 */
	public void setCss(String css) {
		rwl.w.lock();
		try {
			this.css = css;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isLogEnabled() {
		rwl.r.lock();
		try {
			return logEnabled;
		} finally {
			rwl.r.unlock();
		}

	}

	public void setLogEnabled(boolean logReportEnabled) {
		rwl.w.lock();
		try {
			this.logEnabled = logReportEnabled;
		} finally {
			rwl.w.unlock();
		}

	}

	public void addLogField(RendererLogField logField) {
		rwl.w.lock();
		try {
			logFields.add(logField);
		} finally {
			rwl.w.unlock();
		}
	}

	public void removeLogField(RendererLogField logField) {
		rwl.w.lock();
		try {
			logFields.remove(logField);
		} finally {
			rwl.w.unlock();
		}

	}

	public List<RendererLogField> getLogFields() {
		rwl.r.lock();
		try {
			return logFields;
		} finally {
			rwl.r.unlock();
		}

	}

	/**
	 * @return the contentTypeField
	 */
	public String getContentTypeField() {
		return contentTypeField;
	}

	/**
	 * @param contentTypeField
	 *            the contentTypeField to set
	 */
	public void setContentTypeField(String contentTypeField) {
		if (contentTypeField != null)
			if (contentTypeField.length() == 0)
				contentTypeField = null;
		this.contentTypeField = contentTypeField;
	}

	/**
	 * @return the filenameField
	 */
	public String getFilenameField() {
		return filenameField;
	}

	/**
	 * @param filenameField
	 *            the filenameField to set
	 */
	public void setFilenameField(String filenameField) {
		if (filenameField != null)
			if (filenameField.length() == 0)
				filenameField = null;
		this.filenameField = filenameField;
	}

	/**
	 * @return the hocrField
	 */
	public String getHocrField() {
		return hocrField;
	}

	/**
	 * @param hocrField
	 *            the hocrField to set
	 */
	public void setHocrField(String hocrField) {
		if (hocrField != null)
			if (hocrField.length() == 0)
				hocrField = null;
		this.hocrField = hocrField;
	}

	/**
	 * @return the authUsername
	 */
	public String getAuthUsername() {
		return authUsername;
	}

	/**
	 * @param authUsername
	 *            the authUsername to set
	 */
	public void setAuthUsername(String authUsername) {
		this.authUsername = authUsername;
	}

	/**
	 * @return the authPassword
	 */
	public String getAuthPassword() {
		return authPassword;
	}

	/**
	 * @param authPassword
	 *            the authPassword to set
	 */
	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}

	/**
	 * @return the authDomain
	 */
	public String getAuthDomain() {
		return authDomain;
	}

	/**
	 * @param authDomain
	 *            the authDomain to set
	 */
	public void setAuthDomain(String authDomain) {
		this.authDomain = authDomain;
	}

	/**
	 * @return the authServer
	 */
	public String getAuthServer() {
		return authServer;
	}

	/**
	 * @param authServer
	 *            the authServer to set
	 */
	public void setAuthServer(String authServer) {
		this.authServer = authServer;
	}

	/**
	 * @return the authUserAllowField
	 */
	public String getAuthUserAllowField() {
		return authUserAllowField;
	}

	/**
	 * @param authUserAllowField
	 *            the authUserAllowField to set
	 */
	public void setAuthUserAllowField(String authUserAllowField) {
		this.authUserAllowField = authUserAllowField;
	}

	/**
	 * @return the authGroupAllowField
	 */
	public String getAuthGroupAllowField() {
		return authGroupAllowField;
	}

	/**
	 * @param authGroupAllowField
	 *            the authGroupAllowField to set
	 */
	public void setAuthGroupAllowField(String authGroupAllowField) {
		this.authGroupAllowField = authGroupAllowField;
	}

	/**
	 * @return the authUserDenyField
	 */
	public String getAuthUserDenyField() {
		return authUserDenyField;
	}

	/**
	 * @param authUserDenyField
	 *            the authUserDenyField to set
	 */
	public void setAuthUserDenyField(String authUserDenyField) {
		this.authUserDenyField = authUserDenyField;
	}

	/**
	 * @return the authGroupDenyField
	 */
	public String getAuthGroupDenyField() {
		return authGroupDenyField;
	}

	/**
	 * @param authGroupDenyField
	 *            the authGroupDenyField to set
	 */
	public void setAuthGroupDenyField(String authGroupDenyField) {
		this.authGroupDenyField = authGroupDenyField;
	}

	public void setAuthType(String authTypeName) {
		AuthPluginEnum authPlugin = AuthPluginEnum.find(authTypeName);
		authPluginClass = authPlugin == null ? authTypeName : authPlugin
				.getClassName();
	}

	public String getAuthType() {
		AuthPluginEnum authPlugin = AuthPluginEnum.find(authPluginClass);
		return authPlugin == null ? authPluginClass : authPlugin.label;
	}

	private AuthPluginInterface getNewAuthPluginInterface()
			throws SearchLibException {
		if (authPluginClass == null || authPluginClass.length() == 0)
			return null;
		try {
			return (AuthPluginInterface) Class.forName(authPluginClass)
					.newInstance();
		} catch (InstantiationException e) {
			throw new SearchLibException(
					"Unable to instance the authentication plugin", e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(
					"Unable to instance the authentication plugin", e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(
					"Unable to instance the authentication plugin", e);
		}
	}

	public void configureAuthRequest(SearchRequest searchRequest,
			HttpServletRequest servletRequest) throws ParseException,
			IOException, SearchLibException {
		AuthPluginInterface authPlugin = getNewAuthPluginInterface();
		if (authPlugin == null)
			return;
		AuthPluginInterface.User user = authPlugin.getUser(servletRequest);
		String[] groups = null;
		if ((authGroupAllowField != null && authGroupAllowField.length() > 0)
				|| (authGroupDenyField != null && authGroupDenyField.length() > 0))
			if (user != null)
				groups = authPlugin.authGetGroups(this, user);

		StringBuffer sbPositiveFilter = new StringBuffer();
		if (authUserAllowField != null && authUserAllowField.length() > 0) {
			if (sbPositiveFilter.length() > 0)
				sbPositiveFilter.append(" OR ");
			sbPositiveFilter.append(authUserAllowField);
			sbPositiveFilter.append(':');
			AuthPluginInterface.User.usernamesToFilterQuery(user,
					sbPositiveFilter);
		}
		if (authGroupAllowField != null && authGroupAllowField.length() > 0
				&& groups != null && groups.length > 0) {
			if (sbPositiveFilter.length() > 0)
				sbPositiveFilter.append(" OR ");
			sbPositiveFilter.append(authGroupAllowField);
			sbPositiveFilter.append(":(");
			boolean bOr = false;
			for (String group : groups) {
				if (bOr)
					sbPositiveFilter.append(" OR ");
				else
					bOr = true;
				sbPositiveFilter.append('"');
				sbPositiveFilter.append(QueryUtils.escapeQuery(group));
				sbPositiveFilter.append('"');
			}
			sbPositiveFilter.append(')');
		}
		if (sbPositiveFilter.length() > 0)
			searchRequest.addFilter(sbPositiveFilter.toString(), false);

		if (authUserDenyField != null && authUserDenyField.length() > 0) {
			StringBuffer sbNegativeFilter = new StringBuffer();
			sbNegativeFilter.append(authUserDenyField);
			sbNegativeFilter.append(':');
			AuthPluginInterface.User.usernamesToFilterQuery(user,
					sbNegativeFilter);
			searchRequest.addFilter(sbNegativeFilter.toString(), true);
		}

		if (authGroupDenyField != null && authGroupDenyField.length() > 0
				&& groups != null && groups.length > 0) {
			StringBuffer sbNegativeFilter = new StringBuffer();
			sbNegativeFilter.append(authGroupDenyField);
			sbNegativeFilter.append(":(");
			boolean bOr = false;
			for (String group : groups) {
				if (bOr)
					sbNegativeFilter.append(" OR ");
				else
					bOr = true;
				sbNegativeFilter.append('"');
				sbNegativeFilter.append(QueryUtils.escapeQuery(group));
				sbNegativeFilter.append('"');
			}
			sbNegativeFilter.append(')');
			searchRequest.addFilter(sbNegativeFilter.toString(), true);
		}

	}

	/**
	 * @return the authPluginClass
	 */
	public String getAuthPluginClass() {
		return authPluginClass;
	}

	/**
	 * @param authPluginClass
	 *            the authPluginClass to set
	 */
	public void setAuthPluginClass(String authPluginClass) {
		this.authPluginClass = authPluginClass;
	}
}