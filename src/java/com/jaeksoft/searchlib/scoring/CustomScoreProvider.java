package com.jaeksoft.searchlib.scoring;

import java.io.IOException;

import com.jaeksoft.searchlib.index.Explanation;
import com.jaeksoft.searchlib.index.IndexReader;

public class CustomScoreProvider {

	public CustomScoreProvider(IndexReader reader) {
		// TODO Auto-generated constructor stub
	}

	public Explanation customExplain(int doc, Explanation subQueryExpl,
			Explanation valSrcExpl) {
		// TODO Auto-generated method stub
		return null;
	}

	public float customScore(int doc, float subQueryScore, float valSrcScore) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Explanation customExplain(int doc, Explanation subQueryExpl,
			Explanation[] valSrcExpls) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public float customScore(int doc, float subQueryScore, float[] valSrcScores) {
		// TODO Auto-generated method stub
		return 0;
	}

}
