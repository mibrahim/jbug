/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author mosama
 */
public class Main
{

    /**
     * Returns a list of distinct emails
     *
     * @param request HttpServletRequest reference to the request servlet
     * @return a comma separated list of unique emails
     * @throws SQLException
     */
    static String getUsers(HttpServletRequest request, SQL sql) throws SQLException
    {
	HashSet<String> users = new HashSet<>();
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

	StringBuilder res = new StringBuilder();
	for (String user : users)
	{
	    user = user.trim();
	    if (user.length() > 0)
	    {
		res.append(",");
	    }
	    res.append("'").append(user).append("'");
	}

	return res.toString();
    }

    static String getDistinctColumn(HttpServletRequest request, String column, SQL sql) throws SQLException
    {
	StringBuilder res = new StringBuilder();
	ResultSet rs = sql.query("select distinct(" + column + ") from bugs");
	while (rs.next())
	{
	    String value = rs.getString(column);
	    if (value.length() == 0)
	    {
		continue;
	    }
	    if (res.length() > 0)
	    {
		res.append(",");
	    }
	    res.append("'").append(value).append("'");
	}
	rs.close();

	return res.toString();
    }

    /**
     * Returns the count of open bugs
     *
     * @param request
     * @return
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
     * @return
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
     * @return
     * @throws SQLException
     */
    static String getOpenBugsIds(HttpServletRequest request, SQL sql) throws SQLException
    {
	ResultSet rs = sql.query("select bug_id, priority from bugs where status in (0,1,2) order by priority, creation_ts desc");
	StringBuilder b = new StringBuilder();
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
     * Returns a comma separated list of the ids of the closed bugs
     *
     * @param request
     * @return
     * @throws SQLException
     */
    static String getClosedBugsIds(HttpServletRequest request, SQL sql) throws SQLException
    {
	ResultSet rs = sql.query("select bug_id from bugs where status=3 order by creation_ts");
	StringBuilder b = new StringBuilder();
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
     * @return
     * @throws SQLException
     */
    static String getBug(HttpServletRequest request, SQL sql) throws SQLException
    {
	String pfor = request.getParameter("for");
	ResultSet rs = sql.query("select * from bugs where bug_id=" + pfor);
	StringBuilder b = new StringBuilder();
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
     * Returns the details of several bugs given their comma separated list of
     * IDs. The result is formatted as a JSON array.
     *
     * @param request
     * @return
     * @throws SQLException
     */
    static String getBugs(HttpServletRequest request, SQL sql) throws SQLException
    {
	String pfor = request.getParameter("for");
	ResultSet rs = sql.query("select * from bugs where bug_id in (" + pfor + ")");
	StringBuilder b = new StringBuilder();
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
	    b.append(sql.currentRowToJSON(rs));
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

    static String getBugsSummaries(HttpServletRequest request, SQL sql) throws SQLException
    {
	String pfor = request.getParameter("for");
	ResultSet rs = sql.query("select bug_id,title,description,assigned_to,reporter,status,creation_ts,description,priority from bugs where bug_id in (" + pfor + ")");
	StringBuilder b = new StringBuilder();
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
	StringBuilder b = new StringBuilder();
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
	PreparedStatement stmt = sql.getConnection().prepareStatement("insert into bugs"
		+ "(title , description , reporter , assigned_to , "
		+ "priority , product , component , version , target_milestone, "
		+ "creation_ts, modification_ts, status, easiness) values (?,?,?,?,?,?,?,?,?,?,?,?,?)");

	stmt.setString(1, request.getParameter("title"));
	stmt.setString(2, request.getParameter("description"));
	stmt.setString(3, request.getParameter("reporter").trim());
	stmt.setString(4, request.getParameter("assigned_to").trim());
	stmt.setInt(5, Integer.parseInt(request.getParameter("priority")));
	stmt.setString(6, request.getParameter("product").toLowerCase().trim());
	stmt.setString(7, request.getParameter("component").toLowerCase().trim());
	stmt.setString(8, request.getParameter("version").toLowerCase().trim());
	stmt.setString(9, request.getParameter("target_milestone").toLowerCase().trim());
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

    static String updateBug(HttpServletRequest request, SQL sql) throws SQLException
    {
	// Status is Assigned
	PreparedStatement stmt = sql.getConnection().prepareStatement("update bugs "
		+ "set title=? , description=? , reporter=? , assigned_to=? , "
		+ "priority=? , product=? , component=? , version=? , target_milestone=?, "
		+ "status=?, modification_ts=?, easiness=? where bug_id=?");

	stmt.setString(1, request.getParameter("title"));
	stmt.setString(2, request.getParameter("description"));
	stmt.setString(3, request.getParameter("reporter").trim());
	stmt.setString(4, request.getParameter("assigned_to").trim());
	stmt.setInt(5, Integer.parseInt(request.getParameter("priority")));
	stmt.setString(6, request.getParameter("product").toLowerCase().trim());
	stmt.setString(7, request.getParameter("component").toLowerCase().trim());
	stmt.setString(8, request.getParameter("version").toLowerCase().trim());
	stmt.setString(9, request.getParameter("target_milestone").toLowerCase().trim());
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
     * @return
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
     * @return
     * @throws SQLException
     */
    public static String getData(HttpServletRequest request) throws SQLException
    {
	SQL sql = new SQL();

	try
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
		case "bugssummaries":
		    return getBugsSummaries(request, sql);
		case "bugids":
		    return getBugIds(request, sql);

		// Sets and updates
		case "updatebug":
		    return updateOrCreateBug(request, sql);
		default:
		    return "Unkown request: " + pget;
	    }
	} finally
	{
	    sql.close();
	}
    }
}
