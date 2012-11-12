package com.jaeksoft.searchlib.query;

public class BoostingQuery extends Query {

	public BoostingQuery(Query complexQuery, Query parse, float boost) {
		super(null);
	}

}
