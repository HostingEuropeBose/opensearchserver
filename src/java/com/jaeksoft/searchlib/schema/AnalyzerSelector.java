/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;

/**
 * Build to be fast. No lock.
 * 
 * @author ekeller
 * 
 */
public class AnalyzerSelector {

	private static class NameLang implements Comparable<NameLang> {

		final private String name;

		final private LanguageEnum lang;

		private NameLang(Analyzer analyzer) {
			this.name = analyzer.getName();
			this.lang = analyzer.getLang();
		}

		private NameLang(String name, LanguageEnum lang) {
			this.name = name;
			this.lang = lang;
		}

		@Override
		final public int compareTo(NameLang comp) {
			int c = name.compareTo(comp.name);
			if (c != 0)
				return c;
			return lang.compareTo(comp.lang);
		}
	}

	private static class CompiledPair {

		final private CompiledAnalyzer indexAnalyzer;
		final private CompiledAnalyzer queryAnalyzer;

		private CompiledPair(Analyzer analyzer) throws SearchLibException {
			indexAnalyzer = analyzer.getIndexAnalyzer();
			queryAnalyzer = analyzer.getQueryAnalyzer();
		}
	}

	private final Map<String, String> schemaAnalyzerMap;
	private final Map<NameLang, CompiledPair> nameLangMap;

	public AnalyzerSelector(SchemaFieldList schemaFieldList) {
		schemaAnalyzerMap = new TreeMap<String, String>();
		for (SchemaField schemaField : schemaFieldList)
			schemaAnalyzerMap.put(schemaField.getName(),
					schemaField.getIndexAnalyzer());
		nameLangMap = new TreeMap<NameLang, CompiledPair>();
	}

	final public void add(Analyzer analyzer) throws SearchLibException {
		nameLangMap.put(new NameLang(analyzer), new CompiledPair(analyzer));
	}

	final private CompiledPair getByAnalyzerName(String name, LanguageEnum lang) {
		NameLang nameLang;
		if (lang != null) {
			nameLang = new NameLang(name, lang);
			CompiledPair pair = nameLangMap.get(nameLang);
			if (pair != null)
				return pair;
		}
		nameLang = new NameLang(name, LanguageEnum.UNDEFINED);
		return nameLangMap.get(nameLang);
	}

	final private CompiledPair getByFieldName(String fieldName,
			LanguageEnum lang) {
		String analyzerName = schemaAnalyzerMap.get(fieldName);
		if (analyzerName == null)
			return null;
		return getByAnalyzerName(analyzerName, lang);
	}

	final public CompiledAnalyzer getIndexByFieldName(String fieldName,
			LanguageEnum lang) {
		CompiledPair pair = getByFieldName(fieldName, lang);
		if (pair == null)
			return null;
		return pair.indexAnalyzer;
	}

	final public CompiledAnalyzer getQueryByFieldName(String fieldName,
			LanguageEnum lang) {
		CompiledPair pair = getByFieldName(fieldName, lang);
		if (pair == null)
			return null;
		return pair.queryAnalyzer;
	}

	final public CompiledAnalyzer getIndexByAnalyzerName(String name,
			LanguageEnum lang) {
		CompiledPair pair = getByAnalyzerName(name, lang);
		if (pair == null)
			return null;
		return pair.indexAnalyzer;
	}

	final public CompiledAnalyzer getQueryByAnalyzerName(String name,
			LanguageEnum lang) {
		CompiledPair pair = getByAnalyzerName(name, lang);
		if (pair == null)
			return null;
		return pair.queryAnalyzer;
	}
}
