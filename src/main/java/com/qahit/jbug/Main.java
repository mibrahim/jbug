/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

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
    static String getUsers(HttpServletRequest request) throws SQLException
    {
	HashSet<String> users = new HashSet<>();
	ResultSet rs = SQL.query("select distinct(assigned_to) from bugs");
	while (rs.next())
	{
	    users.add(rs.getString("assigned_to"));
	}
	rs.close();

	rs = SQL.query("select distinct(reporter) from bugs");
	while (rs.next())
	{
	    users.add(rs.getString("reporter"));
	}
	rs.close();

	StringBuilder res = new StringBuilder();
	for (String user : users)
	{
	    if (res.length() > 0)
	    {
		res.append(",");
	    }
	    res.append("'").append(user).append("'");
	}

	return res.toString();
    }

    /**
     * Returns the count of open bugs
     *
     * @param request
     * @return
     * @throws SQLException
     */
    static String getOpenBugCount(HttpServletRequest request) throws SQLException
    {
	ResultSet rs = SQL.query("select count(*) as count from bugs where status in (0,1,2,3,4,5,6)");
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
    static String getClosedBugCount(HttpServletRequest request) throws SQLException
    {
	ResultSet rs = SQL.query("select count(*) as count from bugs where status in (7,8)");
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
    static String getOpenBugsIds(HttpServletRequest request) throws SQLException
    {
	ResultSet rs = SQL.query("select bug_id from bugs where status in (0,1,2,3,4,5,6) order by severity,priority,creation_ts");
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
    static String getClosedBugsIds(HttpServletRequest request) throws SQLException
    {
	ResultSet rs = SQL.query("select bug_id from bugs where status in (7,8) order by creation_ts");
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
    static String getBug(HttpServletRequest request) throws SQLException
    {
	String pfor = request.getParameter("for");
	ResultSet rs = SQL.query("select * from bugs where bug_id=" + pfor);
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
    static String getBugs(HttpServletRequest request) throws SQLException
    {
	String pfor = request.getParameter("for");
	ResultSet rs = SQL.query("select * from bugs where bug_id in (" + pfor + ")");
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

    static String getBugsSummaries(HttpServletRequest request) throws SQLException
    {
	String pfor = request.getParameter("for");
	ResultSet rs = SQL.query("select bug_id,title,description,assigned_to,reporter,severity,status,creation_ts,description,priority from bugs where bug_id in (" + pfor + ")");
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

    static String getBugIds(HttpServletRequest request) throws SQLException
    {
	String condition = request.getParameter("condition");
	String orderby = request.getParameter("orderby");
	String sql = "select bug_id from bugs";

	if (condition != null)
	{
	    sql += " where " + condition;
	}

	if (orderby != null)
	{
	    sql += " order by " + orderby;
	}
	ResultSet rs = SQL.query(sql);
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

    static String updateBug(HttpServletRequest request) throws SQLException
    {
	StringBuilder sql=new StringBuilder();
	
	String bugid=request.getParameter("bugid");
	boolean updating=true;
	if (bugid.equalsIgnoreCase("new"))
	{
	    updating=false;
	}
	
	if (updating)
	{
	    sql.append("update bugs set ")
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
	String pget = request.getParameter("get");

	switch (pget)
	{
	    // Gets
	    case "users":
		return getUsers(request);
	    case "openbugcount":
		return getOpenBugCount(request);
	    case "closedbugcount":
		return getClosedBugCount(request);
	    case "openbugids":
		return getOpenBugsIds(request);
	    case "closedbugids":
		return getClosedBugsIds(request);
	    case "bug":
		return getBug(request);
	    case "bugs":
		return getBugs(request);
	    case "bugssummaries":
		return getBugsSummaries(request);
	    case "bugids":
		return getBugIds(request);

	    // Sets and updates
	    case "updatebug":
		return updateBug(request);
	    default:
		return "Unkown request: " + pget;
	}
    }
}
