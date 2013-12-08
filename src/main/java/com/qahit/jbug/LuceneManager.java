/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/**
 * Creates and manages a Lucene index.
 *
 * @author mosama
 */
public class LuceneManager extends Thread
{

	private static final Logger log = Logger.getLogger(LuceneManager.class);
	private static LuceneManager instance = null;
	private static final AtomicBoolean exit = new AtomicBoolean(false);
	private static Directory index = null;
	private static IndexWriter indexWriter = null;
	private static final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
	private static SearcherManager searcherManager = null;

	/**
	 * Interval in ms to probe the database for changes
	 */
	private static final int interval = 1000;

	public static LuceneManager get()
	{
		if (instance == null)
		{
			instance = new LuceneManager();
			instance.start();
		}
		return instance;
	}

	public static void exit()
	{
		exit.set(true);
	}

	@Override
	public void run()
	{
		while (!exit.get())
		{
			try
			{
				sleep(interval);
			}
			catch (InterruptedException ex)
			{
				log.error("Interrupted while sleeping", ex);

				// Don't process data this turn, just continue
				continue;
			}

			try (SQL sql = new SQL(); ResultSet result = sql.query("select * from bugs where indexed=0"))
			{
				while (result.next())
				{
					String bug_id = "" + result.getInt("bug_id");
					String assigned_to = result.getString("assigned_to");
					String status = "" + Bug.Status.values()[result.getInt("status")];
					String title = result.getString("title");
					String comments = result.getClob("comments_json") != null ? result.getClob("comments_json").toString() : "";
					String priority = "" + Bug.Priorities.values()[result.getInt("priority")];
					String description = result.getString("description");
					String product = result.getString("product");
					String reporter = result.getString("reporter");
					String version = result.getString("version");
					String component = result.getString("component");
					String target_milestone = result.getString("target_milestone");
					String easiness = "" + Bug.Easiness.values()[result.getInt("easiness")];

					addBugToIndex(bug_id, assigned_to, status, title, comments, priority, description, product, reporter, version, component, target_milestone, easiness);

					sql.queryNoRes("update bugs set indexed=1 where bug_id=" + bug_id);
				}
			}
			catch (SQLException | IOException ex)
			{
				java.util.logging.Logger.getLogger(LuceneManager.class.getName()).log(Level.SEVERE, null, ex);
			}
			try
			{
				indexWriter.commit();
				searcherManager.maybeRefresh();
			}
			catch (IOException ex)
			{
				log.error("Error while refreshing", ex);
			}
		}

		try
		{
			searcherManager.close();
			indexWriter.close();
			index.close();
		}
		catch (IOException e)
		{
			log.error("Problems while attempting to close the index", e);
		}
		finally
		{
			LuceneManager.index = null;
		}
		LuceneManager.exit.set(false);
		LuceneManager.instance = null;
	}

	public static ArrayList<String> search(String terms, int n,
		QueryParser.Operator defaultOperator)
	{
		IndexSearcher indexSearcher = null;

		try
		{
			indexSearcher = searcherManager.acquire();
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
				Version.LUCENE_45,
				new String[]
				{
					"easiness", "targetmilestone", "version", "component",
					"reporter", "product", "description", "comments",
					"title", "status", "assignedto", "bug_id", "priority"
				},
				analyzer);
			queryParser.setDefaultOperator(defaultOperator);
			Query query = queryParser.parse(terms);

			ScoreDoc[] scoreDocs = indexSearcher.search(query, interval).scoreDocs;
			ArrayList<String> result = new ArrayList<>();
			for (ScoreDoc scoreDoc : scoreDocs)
			{
				Document doc = indexSearcher.doc(scoreDoc.doc);
				result.add(doc.get("bug_id"));
			}
			return result;
		}
		catch (IOException | ParseException ex)
		{
			log.error("Error while searching", ex);
		}
		finally
		{
			try
			{
				searcherManager.release(indexSearcher);
			}
			catch (IOException ex)
			{
				log.error("Error while releasing indexSearcher", ex);
			}
		}
		return null;
	}

	public static void deleteBugFromIndex(String bugId)
	{
		log.info("Deleting bug " + bugId);

		try
		{
			Term term = new Term("bug_id", bugId);
			indexWriter.deleteDocuments(term);
		}
		catch (IOException e)
		{
			String err = "Error deleting key:" + bugId;
			log.error(err, e);
		}
	}

	public static void addBugToIndex(String bugId, String assignedTo, String status, String title,
		String comments, String priority, String description, String product, String reporter,
		String version, String component, String target_milestone, String easiness) throws IOException
	{
		// Before adding a document, delete it
		deleteBugFromIndex(bugId);

		log.info("Indexing bug " + bugId);

		Document doc = new Document();

		Field bug_id = new StringField("bug_id", bugId, Field.Store.YES);
		doc.add(bug_id);

		Field assignedto = new Field("assignedto", assignedTo, TextField.TYPE_NOT_STORED);
		doc.add(assignedto);

		Field status_ = new Field("status", status, TextField.TYPE_NOT_STORED);
		status_.setBoost(5);
		doc.add(status_);

		Field title_ = new Field("title", title.replace(":", " "), TextField.TYPE_NOT_STORED);
		status_.setBoost(20);
		doc.add(title_);

		Field comments_ = new Field("comments", comments, TextField.TYPE_NOT_STORED);
		doc.add(comments_);

		Field priority_ = new Field("priority", priority, TextField.TYPE_NOT_STORED);
		status_.setBoost(5);
		doc.add(priority_);

		Field description_ = new Field("description", description, TextField.TYPE_NOT_STORED);
		status_.setBoost(10);
		doc.add(description_);

		Field product_ = new Field("product", product, TextField.TYPE_NOT_STORED);
		status_.setBoost(5);
		doc.add(product_);

		Field reporter_ = new Field("reporter", reporter, TextField.TYPE_NOT_STORED);
		status_.setBoost(5);
		doc.add(reporter_);

		Field component_ = new Field("component", component, TextField.TYPE_NOT_STORED);
		status_.setBoost(5);
		doc.add(component_);

		Field version_ = new Field("version", version, TextField.TYPE_NOT_STORED);
		status_.setBoost(5);
		doc.add(version_);

		Field target_milestone_ = new Field("targetmilestone", target_milestone, TextField.TYPE_NOT_STORED);
		status_.setBoost(5);
		doc.add(target_milestone_);

		Field easiness_ = new Field("easiness", easiness, TextField.TYPE_NOT_STORED);
		status_.setBoost(5);
		doc.add(easiness_);

		indexWriter.addDocument(doc);
	}

	private LuceneManager()
	{
		File luceneIndexDirectory = new File("LuceneIndex");

		if (!luceneIndexDirectory.exists())
		{
			try
			{
				// Reset all indexed bugs
				(new SQL()).queryNoRes("update bugs set indexed=0");
			}
			catch (SQLException e)
			{
				throw new RuntimeException("SQL error !!!", e);
			}
		}

		log.info("Opening index at: " + luceneIndexDirectory.getAbsolutePath());
		log.info("Creating the index manager");
		try
		{
			index = new SimpleFSDirectory(luceneIndexDirectory);
		}
		catch (IOException ex)
		{
			try
			{
				// Ok, try to delete the directory and recreate the index
				FileUtils.deleteDirectory(luceneIndexDirectory);

				// Reset all indexed bugs
				(new SQL()).queryNoRes("update bugs set indexed=0");

				index = new SimpleFSDirectory(luceneIndexDirectory);
			}
			catch (IOException | SQLException ex1)
			{
				log.error("Error while initializing lucene, cannot continue", ex1);
				throw new RuntimeException("Error while initializing lucene, cannot continue", ex1);
			}
		}

		try
		{
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);

			indexWriter = new IndexWriter(index, config);
			searcherManager = new SearcherManager(indexWriter, true, new MySearchWarmer());
		}
		catch (IOException ex)
		{
			log.error("Error while creating the searcher manager", ex);
		}

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				LuceneManager.exit();
			}
		});
	}

}
