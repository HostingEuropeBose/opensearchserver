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

package com.jaeksoft.searchlib.spellcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.SpellChecker;
import com.jaeksoft.searchlib.index.term.Term;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SpellCheckRequest;

public class SpellCheck implements Iterable<SpellCheckItem> {

	private List<SpellCheckItem> spellCheckItems;

	private String fieldName;

	public SpellCheck(ReaderInterface reader, SpellCheckRequest request,
			SpellCheckField spellCheckField) throws ParseException,
			SyntaxError, IOException, SearchLibException {
		fieldName = spellCheckField.getName();
		SpellChecker spellchecker = reader.getSpellChecker(fieldName);
		Set<String> wordSet = new LinkedHashSet<String>();
		Set<Term> set = request.getTermSet(fieldName);
		for (Term term : set)
			if (term.field().equals(fieldName))
				wordSet.add(term.text());
		int suggestionNumber = spellCheckField.getSuggestionNumber();
		float minScore = spellCheckField.getMinScore();
		synchronized (spellchecker) {
			spellchecker.setAccuracy(minScore);
			spellchecker.setStringDistance(spellCheckField.getStringDistance()
					.getNewInstance());
			spellCheckItems = new ArrayList<SpellCheckItem>();
			for (String word : wordSet) {
				String[] suggestions = spellchecker.suggestSimilar(word,
						suggestionNumber);
				if (suggestions != null && suggestions.length > 0) {
					SuggestionItem[] suggestionItems = new SuggestionItem[suggestions.length];
					int i = 0;
					for (String suggestion : suggestions)
						suggestionItems[i++] = new SuggestionItem(suggestion);
					spellCheckItems.add(new SpellCheckItem(word,
							suggestionItems));
				}
			}
		}
		for (SpellCheckItem spellcheckItem : spellCheckItems)
			spellcheckItem.computeFrequency(reader, fieldName);
	}

	public String getFieldName() {
		return fieldName;
	}

	public List<SpellCheckItem> getList() {
		return spellCheckItems;
	}

	@Override
	public Iterator<SpellCheckItem> iterator() {
		return spellCheckItems.iterator();
	}

}
