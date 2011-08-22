/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.stopwords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;

public class StopWordsManager extends AbstractDirectoryManager {

	private Map<String, WordSet> wordsMap;

	public StopWordsManager(Config config, File directory) {
		super(config, directory);
		wordsMap = new TreeMap<String, WordSet>();
	}

	public WordSet getWords(String listname) throws SearchLibException {
		rwl.r.lock();
		try {
			WordSet words = wordsMap.get(listname);
			if (words != null)
				return words;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			WordSet words = wordsMap.get(listname);
			if (words != null)
				return words;
			words = getNewWordSet(listname);
			wordsMap.put(listname, words);
			return words;
		} finally {
			rwl.w.unlock();
		}
	}

	private WordSet getNewWordSet(String listname) throws SearchLibException {
		BufferedReader br = null;
		try {
			WordSet words = wordsMap.get(listname);
			if (words != null)
				return words;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					getFile(listname)), "UTF-8"));
			words = new WordSet(0, true);
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					words.add(line);
				}
			}
			return words;
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		} catch (FileNotFoundException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					Logging.warn(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void delete(String name) {
		rwl.w.lock();
		try {
			super.delete(name);
			wordsMap.remove(name);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void saveContent(String name, String content) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			super.saveContent(name, content);
			wordsMap.remove(name);
			wordsMap.put(name, getNewWordSet(name));
		} finally {
			rwl.w.unlock();
		}
	}
}
