/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.QueryParser;

public class Main
{

	private static final Logger log = Logger.getLogger(Main.class);

	/**
	 * Returns a list of distinct emails.
	 *
	 * @param request HttpServletRequest reference to the request servlet
	 *
	 * @return a comma separated list of unique emails
	 *
	 * @throws SQLException
	 */
	static String getUsers(HttpServletRequest request, SQL sql) throws SQLException
	{
		HashSet<String> users = new HashSet<>(10);
		ResultSet rs = sql.query("select distinct(assigned_to) from bugs");
		while (rs.next())
		{
			users.add(rs.getString("assigned_to"));
		}
		rs.close();

		rs = sql.query("select distinct(reporter) from bugs");
		while (rs.next())
		{
			users.add(rs.getString("reporter"));
		}
		rs.close();

		StringBuilder res = new StringBuilder(100);
		for (String user : users)
		{
			String trimmedUser = user.trim();
			if (trimmedUser.length() > 0)
			{
				if (res.length() > 0)
				{
					res.append(",");
				}
				res
					.append("'")
					.append(trimmedUser)
					.append("'");
			}
		}

		return res.toString();
	}

	static String getDistinctColumn(HttpServletRequest request, String column, SQL sql) throws SQLException
	{
		StringBuilder res = new StringBuilder(256);
		ResultSet rs = sql.query("select distinct(" + column + ") from bugs");
		while (rs.next())
		{
			String value = rs.getString(column);
			if (value != null && value.length() == 0)
			{
				continue;
			}
			if (res.length() > 0)
			{
				res.append(",");
			}
			res
				.append("'")
				.append(value)
				.append("'");
		}
		rs.close();

		return res.toString();
	}

	/**
	 * Returns the count of open bugs
	 *
	 * @param request
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	static String getOpenBugCount(HttpServletRequest request, SQL sql) throws SQLException
	{
		ResultSet rs = sql.query("select count(*) as count from bugs where status in (0,1,2)");
		String count = "0";
		if (rs.next())
		{
			count = "" + rs.getInt("count");
		}
		rs.close();
		return count;
	}

	/**
	 * Returns the count of closed bugs
	 *
	 * @param request
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	static String getClosedBugCount(HttpServletRequest request, SQL sql) throws SQLException
	{
		ResultSet rs = sql.query("select count(*) as count from bugs where status=3");
		String count = "0";
		if (rs.next())
		{
			count = "" + rs.getInt("count");
		}
		rs.close();
		return count;

	}

	/**
	 * Returns a comma separated list of open bug ids
	 *
	 * @param request
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	static String getOpenBugsIds(HttpServletRequest request, SQL sql) throws SQLException
	{
		try (ResultSet rs = sql.query(
			"select bug_id, priority from bugs where status in (0,1,2) order by priority, creation_ts desc"))
		{
			StringBuilder b = new StringBuilder(256);
			while (rs.next())
			{
				if (b.length() > 0)
				{
					b.append(",");
				}
				b.append(rs.getInt("bug_id"));
			}
			return b.toString();
		}
	}

	static String searchBugs(HttpServletRequest request, SQL sql) throws SQLException
	{
		String searchFilter = request.getParameter("q");
		if (searchFilter == null || searchFilter.trim().length() == 0)
		{
			ResultSet resultSet = sql.query("select bug_id from bugs");
			StringBuilder b = new StringBuilder(256);
			while (resultSet.next())
			{
				if (b.length() > 0)
				{
					b.append(",");
				}
				b.append(resultSet.getInt("bug_id"));
			}
			return b.toString();
		}

		ArrayList<String> bugIds = LuceneManager.search(searchFilter, 1000, QueryParser.Operator.AND);
		return StringUtils.join(bugIds, ",");
	}

	/**
	 * Returns a comma separated list of the ids of the closed bugs
	 *
	 * @param request
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	static String getClosedBugsIds(HttpServletRequest request, SQL sql) throws SQLException
	{
		ResultSet rs = sql.query("select bug_id from bugs where status=3 order by creation_ts");
		StringBuilder b = new StringBuilder(256);
		while (rs.next())
		{
			if (b.length() > 0)
			{
				b.append(",");
			}
			b.append(rs.getInt("bug_id"));
		}
		rs.close();
		return b.toString();
	}

	/**
	 * Returns a bug details as a JSON array given the bug id
	 *
	 * @param request
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	static String getBug(HttpServletRequest request, SQL sql) throws SQLException
	{
		String pfor = request.getParameter("for");
		ResultSet rs = sql.query("select * from bugs where bug_id=" + pfor);
		StringBuilder b = new StringBuilder(256);
		if (rs.next())
		{
			b.append(SQL.currentRowToJSON(rs));
		} else
		{
			b.append("Not found");
		}
		rs.close();
		return b.toString();
	}

	/**
	 * Returns the details of several bugs given their comma separated list of IDs. The result is formatted as a JSON
	 * array.
	 *
	 * @param request
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	static String getBugs(HttpServletRequest request, SQL sql) throws SQLException
	{
		String pfor = request.getParameter("for").trim();
		if (pfor.length() == 0)
		{
			return "{\"bugs\":[\n]}";
		}
		ResultSet rs = sql.query("select * from bugs where bug_id in (" + pfor + ")");
		StringBuilder b = new StringBuilder(256);
		b.append("{\"bugs\":[");
		boolean first = true;
		while (rs.next())
		{
			if (!first)
			{
				b.append(",\n");
			} else
			{
				first = false;
			}
			b.append(SQL.currentRowToJSON(rs));
		}
		b.append("\n]}");
		rs.close();
		return b.toString();
	}

	static String getBugsSummaries(HttpServletRequest request, SQL sql) throws SQLException
	{
		String pfor = request.getParameter("for");
		ResultSet rs = sql
			.query(
				"select bug_id,title,description,assigned_to,reporter,status,creation_ts,description,priority from bugs where bug_id in ("
				+ pfor + ")");
		StringBuilder b = new StringBuilder(256);
		while (rs.next())
		{
			if (b.length() > 0)
			{
				b.append(",\n");
			}
			if (b.length() == 0)
			{
				b.append("{\"bugs\":[");
			}
			b.append(SQL.currentRowToJSON(rs));
		}
		if (b.length() == 0)
		{
			b.append("Not found");
		} else
		{
			b.append("\n]}");
		}
		rs.close();
		return b.toString();
	}

	static String getBugIds(HttpServletRequest request, SQL sql) throws SQLException
	{
		String condition = request.getParameter("condition");
		String orderby = request.getParameter("orderby");
		String stmt = "select bug_id from bugs";

		if (condition != null)
		{
			stmt += " where " + condition;
		}

		if (orderby != null)
		{
			stmt += " order by " + orderby;
		}
		ResultSet rs = sql.query(stmt);
		StringBuilder b = new StringBuilder(256);
		while (rs.next())
		{
			if (b.length() > 0)
			{
				b.append(",");
			}
			b.append(rs.getInt("bug_id"));
		}
		if (b.length() == 0)
		{
			b.append("Not found");
		}
		rs.close();
		return b.toString();
	}

	static String createNewBug(HttpServletRequest request, SQL sql) throws SQLException
	{
		// Status is Assigned
		PreparedStatement stmt = sql.getConnection()
			.prepareStatement("insert into bugs"
				+ "(title , description , reporter , assigned_to , "
				+ "priority , product , component , version , target_milestone, "
				+ "creation_ts, modification_ts, status, easiness) values (?,?,?,?,?,?,?,?,?,?,?,?,?)");

		stmt.setString(1, request.getParameter("title"));
		stmt.setString(2, request.getParameter("description"));
		stmt.setString(3, request.getParameter("reporter")
			.trim());
		stmt.setString(4, request.getParameter("assigned_to")
			.trim());
		stmt.setInt(5, Integer.parseInt(request.getParameter("priority")));
		stmt.setString(6, request.getParameter("product")
			.toLowerCase()
			.trim());
		stmt.setString(7, request.getParameter("component")
			.toLowerCase()
			.trim());
		stmt.setString(8, request.getParameter("version")
			.toLowerCase()
			.trim());
		stmt.setString(9, request.getParameter("target_milestone")
			.toLowerCase()
			.trim());
		stmt.setLong(10, System.currentTimeMillis());
		stmt.setLong(11, System.currentTimeMillis());
		stmt.setInt(12, Bug.Status.OPEN.ordinal());
		stmt.setInt(13, Integer.parseInt(request.getParameter("easiness")));

		stmt.execute();

		// Find the new bug id
		ResultSet rs = sql.query("select MAX(bug_id) as mxv from bugs");
		rs.next();
		int newBugId = rs.getInt("mxv");
		rs.close();

		return "" + newBugId;
	}

	static String deleteBug(HttpServletRequest request, SQL sql) throws SQLException
	{
		String cmd = "delete from bugs where bug_id=" + request.getParameter("bugid");
		sql.queryNoRes(cmd);
		return "ok";
	}

	static String updateBug(HttpServletRequest request, SQL sql) throws SQLException
	{
		// Status is Assigned
		PreparedStatement stmt = sql.getConnection()
			.prepareStatement("update bugs "
				+ "set title=? , description=? , reporter=? , assigned_to=? , "
				+ "priority=? , product=? , component=? , version=? , target_milestone=?, "
				+ "status=?, modification_ts=?, easiness=? where bug_id=?");

		stmt.setString(1, request.getParameter("title").trim());
		stmt.setString(2, request.getParameter("description").trim());
		stmt.setString(3, request.getParameter("reporter")
			.trim());
		stmt.setString(4, request.getParameter("assigned_to")
			.trim());
		stmt.setInt(5, Integer.parseInt(request.getParameter("priority")));
		stmt.setString(6, request.getParameter("product")
			.toLowerCase()
			.trim());
		stmt.setString(7, request.getParameter("component")
			.toLowerCase()
			.trim());
		stmt.setString(8, request.getParameter("version")
			.toLowerCase()
			.trim());
		stmt.setString(9, request.getParameter("target_milestone")
			.toLowerCase()
			.trim());
		stmt.setInt(10, Integer.parseInt(request.getParameter("status")));
		stmt.setLong(11, System.currentTimeMillis());
		stmt.setInt(12, Integer.parseInt(request.getParameter("easiness")));
		stmt.setInt(13, Integer.parseInt(request.getParameter("bugid")));

		stmt.execute();

		return request.getParameter("bugid");
	}

	/**
	 * Updates or creates a new bug
	 *
	 * @param request
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	static String updateOrCreateBug(HttpServletRequest request, SQL sql) throws SQLException
	{
		String bugid = request.getParameter("bugid");
		if (bugid.equalsIgnoreCase("new"))
		{
			return createNewBug(request, sql);
		} else
		{
			return updateBug(request, sql);
		}
	}

	/**
	 * Looks up the parameter get, and calls the correct corresponding function
	 *
	 * @param request
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	public static String getData(HttpServletRequest request) throws SQLException
	{
		try (SQL sql = new SQL())
		{
			String pget = request.getParameter("get");

			switch (pget)
			{
				// Gets
				case "users":
					return getUsers(request, sql);
				case "products":
					return getDistinctColumn(request, "product", sql);
				case "components":
					return getDistinctColumn(request, "component", sql);
				case "target_milestones":
					return getDistinctColumn(request, "target_milestone", sql);
				case "versions":
					return getDistinctColumn(request, "version", sql);
				case "openbugcount":
					return getOpenBugCount(request, sql);
				case "closedbugcount":
					return getClosedBugCount(request, sql);
				case "openbugids":
					return getOpenBugsIds(request, sql);
				case "closedbugids":
					return getClosedBugsIds(request, sql);
				case "bug":
					return getBug(request, sql);
				case "bugs":
					return getBugs(request, sql);
				case "bugids":
					return getBugIds(request, sql);
				case "bugssummaries":
					return getBugsSummaries(request, sql);
				case "searchbugs":
					return searchBugs(request, sql);
				case "producttarget_milestones":
					return getProductTargetMilestones(request, sql);
				case "producttarget_milestonebugs":
					return getProductTargetMilestoneBugs(request, sql);
				case "getsubtasks":
					return getSubtasks(request, sql);
				case "getsupertasks":
					return getSupertasks(request, sql);

				// Sets and updates
				case "updatebug":
					return updateOrCreateBug(request, sql);

				case "deletebug":
					return deleteBug(request, sql);

				case "adddependency":
					return addDependency(request, sql);

				case "removedependency":
					return removeDependency(request, sql);

				default:
					return "Unkown request: " + pget;
			}
		}
		catch (Exception e)
		{
			System.out.append("ERROR while requesting URL: " + request.getRequestURI());
			throw new SQLException("ERROR while requesting URL: " + request.getRequestURI(), e);
		}
	}

	private static String getProductTargetMilestoneBugs(HttpServletRequest request, SQL sql) throws SQLException
	{
		String target_milestone = request.getParameter("target_milestone");
		String product = request.getParameter("product");
		ResultSet rs = sql.query("select bug_id, status from bugs where "
			+ "target_milestone='" + target_milestone + "' and product='" + product + "' "
			+ "order by status, creation_ts");
		StringBuilder b = new StringBuilder();
		while (rs.next())
		{
			if (b.length() > 0)
			{
				b.append(",");
			}
			b.append(rs.getString("bug_id"))
				.append("#")
				.append(rs.getString("status"));
		}
		return b.toString();
	}

	private static String getProductTargetMilestones(HttpServletRequest request, SQL sql) throws SQLException
	{
		String product = request.getParameter("product");
		ResultSet rs = sql.query("select distinct(target_milestone) as target_milestone from bugs where product='"
			+ product + "' order by target_milestone desc");
		StringBuilder b = new StringBuilder();
		while (rs.next())
		{
			if (b.length() > 0)
			{
				b.append(",");
			}
			b.append(rs.getString("target_milestone"));
		}
		return b.toString();
	}

	private static String getSubtasks(HttpServletRequest request, SQL sql) throws SQLException
	{
		String bugid = request.getParameter("for");
		try (ResultSet rs = sql.query("select subtask from dependencies where supertask=" + bugid))
		{
			StringBuilder b = new StringBuilder(256);
			while (rs.next())
			{
				if (b.length() > 0)
				{
					b.append(",");
				}
				b.append(rs.getInt("subtask"));
			}
			return b.toString();
		}
	}

	private static String getSupertasks(HttpServletRequest request, SQL sql) throws SQLException
	{
		String bugid = request.getParameter("for");
		try (ResultSet rs = sql.query("select supertask from dependencies where subtask=" + bugid))
		{
			StringBuilder b = new StringBuilder(256);
			while (rs.next())
			{
				if (b.length() > 0)
				{
					b.append(",");
				}
				b.append(rs.getInt("supertask"));
			}
			return b.toString();
		}
	}

	private static String addDependency(HttpServletRequest request, SQL sql) throws SQLException
	{
		String superTask = request.getParameter("supertask");
		String subTask = request.getParameter("subtask");

		if (!sql.queryNoRes("insert into dependencies(supertask,subtask) values(" + superTask + "," + subTask + ")"))
		{
			return "OK";
		} else
		{
			return "FAIL";
		}
	}

	private static String removeDependency(HttpServletRequest request, SQL sql) throws SQLException
	{
		String superTask = request.getParameter("supertask");
		String subTask = request.getParameter("subtask");

		if (!sql.queryNoRes("delete from dependencies where supertask=" + superTask + " and subtask=" + subTask))
		{
			return "OK";
		} else
		{
			return "FAIL";
		}
	}
}
