/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.synonym;

import com.jaeksoft.searchlib.analysis.TokenStream;

public class SynonymTokenFilter extends TokenStream {

	// private TermAttribute termAtt;

	// private AttributeSource.State current = null;

	// private PositionIncrementAttribute posIncrAtt = null;

	// private OffsetAttribute offsetAtt = null;

	private SynonymQueue synonymQueue;

	public SynonymTokenFilter(TokenStream input, SynonymQueue synonymQueue) {
		super(input);
		// this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		// this.posIncrAtt = (PositionIncrementAttribute)
		// addAttribute(PositionIncrementAttribute.class);
		// this.offsetAtt = (OffsetAttribute)
		// addAttribute(OffsetAttribute.class);

		this.synonymQueue = synonymQueue;
	}

	private final boolean createToken(String term, int posInc, int startOff,
			int endOff) {
		// restoreState(current);
		// termAtt.setTermBuffer(term);
		// posIncrAtt.setPositionIncrement(posInc);
		// offsetAtt.setOffset(startOff, endOff);
		return true;
	}

	private final boolean createToken(SynonymToken token) {
		if (token == null)
			return false;
		createToken(token.getTerm(), token.getPositionIncrement(),
				token.getStartOffset(), token.getEndOffset());
		return true;
	}

	private final boolean createToken(String synonymKey,
			SynonymQueue synonymQueue) {
		createToken(synonymKey, synonymQueue.getPositionIncrement(),
				synonymQueue.getStartOffset(), synonymQueue.getEndOffset());
		return true;
	}

	@Override
	public final boolean incrementToken() {
		// current = captureState();
		for (;;) {
			if (!input.incrementToken())
				return createToken(synonymQueue.popToken());
			// synonymQueue.addToken(new SynonymToken(termAtt.term(), posIncrAtt
			// .getPositionIncrement(), offsetAtt.startOffset(), offsetAtt
			// .endOffset()));
			// restoreState(current);
			String synonymKey = synonymQueue.findSynonym();
			if (synonymKey != null) {
				if (!createToken(synonymKey, synonymQueue))
					return false;
				synonymQueue.clean();
				return true;
			}
			if (synonymQueue.isFull())
				return createToken(synonymQueue.popToken());
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Create token: " + this);
		return sb.toString();
	}
}
