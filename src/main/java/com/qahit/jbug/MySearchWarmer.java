/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.util.Version;

/**
 * Warms up the {@link IndexReader} before sending it back
 */
class MySearchWarmer extends SearcherFactory
{

	private static final Logger log = Logger.getLogger(MySearchWarmer.class);
	private static final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

	@Override
	public IndexSearcher newSearcher(IndexReader reader) throws IOException
	{
		log.info("Creating a new searcher");
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_45, new String[]
		{
			"easiness", "targetmilestone", "version", "component",
			"reporter", "product", "description", "comments",
			"title", "status", "assignedto", "bug_id",
		}, analyzer);
		try (final SQL sql = new SQL(); final ResultSet resultSet = sql.query("select * from bug limit 100"))
		{
			// Warm up by searching for all the top categories
			while (resultSet.next())
			{
				Query query = queryParser.parse(resultSet.getString("title"));
				indexSearcher.search(query, 1000);
			}
		} catch (SQLException | ParseException ex)
		{
			log.error("Error while warming up the index searcher", ex);
		}
		return indexSearcher;
	}

}
