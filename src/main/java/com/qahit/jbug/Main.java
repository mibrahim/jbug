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

    static String getDistinctColumn(HttpServletRequest request,String column) throws SQLException
    {
	StringBuilder res = new StringBuilder();
	ResultSet rs = SQL.query("select distinct("+column+") from bugs");
	while (rs.next())
	{
            String value=rs.getString(column);
            if (value.length()==0) continue;
            if (res.length()>0) res.append(",");
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
    static String getOpenBugCount(HttpServletRequest request) throws SQLException
    {
	ResultSet rs = SQL.query("select count(*) as count from bugs where status in (0,1,2)");
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
	ResultSet rs = SQL.query("select count(*) as count from bugs where status=3");
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
	ResultSet rs = SQL.query("select bug_id, priority from bugs where status in (0,1,2) order by priority, creation_ts desc");
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
	ResultSet rs = SQL.query("select bug_id from bugs where status=3 order by creation_ts");
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
	ResultSet rs = SQL.query("select bug_id,title,description,assigned_to,reporter,status,creation_ts,description,priority from bugs where bug_id in (" + pfor + ")");
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

    static String createNewBug(HttpServletRequest request) throws SQLException
    {
	// Status is Assigned
	PreparedStatement stmt= SQL.dbConnection.prepareStatement("insert into bugs"
		+ "(title , description , reporter , assigned_to , "
		+ "priority , product , component , version , target_milestone, "
		+ "creation_ts, modification_ts, status, easiness) values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
	
	stmt.setString(1, request.getParameter("title"));
	stmt.setString(2, request.getParameter("description"));
	stmt.setString(3, request.getParameter("reporter").trim());
	stmt.setString(4, request.getParameter("assigned_to").trim());
	stmt.setInt(5,Integer.parseInt(request.getParameter("priority")));
	stmt.setString(6, request.getParameter("product").toLowerCase().trim());
	stmt.setString(7, request.getParameter("component").toLowerCase().trim());
	stmt.setString(8, request.getParameter("version").toLowerCase().trim());
	stmt.setString(9, request.getParameter("target_milestone").toLowerCase().trim());
	stmt.setLong(10,System.currentTimeMillis());
	stmt.setLong(11,System.currentTimeMillis());
	stmt.setInt(12, Bug.Status.OPEN.ordinal());
	stmt.setInt(13,Integer.parseInt(request.getParameter("easiness")));
	
	stmt.execute();
	
	// Find the new bug id
	ResultSet rs=SQL.query("select MAX(bug_id) as mxv from bugs");
	rs.next();
	int newBugId=rs.getInt("mxv");
	rs.close();
	
	SQL.dbConnection.commit();
	
	return ""+newBugId;
    }
    
    static String updateBug(HttpServletRequest request) throws SQLException
    {
	// Status is Assigned
	PreparedStatement stmt= SQL.dbConnection.prepareStatement("update bugs "
		+ "set title=? , description=? , reporter=? , assigned_to=? , "
		+ "priority=? , product=? , component=? , version=? , target_milestone=?, "
		+ "status=?, modification_ts=?, easiness=? where bug_id=?");
	
	stmt.setString(1, request.getParameter("title"));
	stmt.setString(2, request.getParameter("description"));
	stmt.setString(3, request.getParameter("reporter").trim());
	stmt.setString(4, request.getParameter("assigned_to").trim());
	stmt.setInt(5,Integer.parseInt(request.getParameter("priority")));
	stmt.setString(6, request.getParameter("product").toLowerCase().trim());
	stmt.setString(7, request.getParameter("component").toLowerCase().trim());
	stmt.setString(8, request.getParameter("version").toLowerCase().trim());
	stmt.setString(9, request.getParameter("target_milestone").toLowerCase().trim());
	stmt.setInt(10, Integer.parseInt(request.getParameter("status")));
	stmt.setLong(11, System.currentTimeMillis());
	stmt.setInt(12,Integer.parseInt(request.getParameter("easiness")));
	stmt.setInt(13, Integer.parseInt(request.getParameter("bugid")));
	
	stmt.execute();
        SQL.dbConnection.commit();
	
	return request.getParameter("bugid");
    }
    
    /**
     * Updates or creates a new bug
     * @param request
     * @return
     * @throws SQLException 
     */
    static String updateOrCreateBug(HttpServletRequest request) throws SQLException
    {
	String bugid=request.getParameter("bugid");
	if (bugid.equalsIgnoreCase("new"))
	{
	    return createNewBug(request);
	}
	else
	{
	    return updateBug(request);
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
	    case "products":
		return getDistinctColumn(request,"product");
	    case "components":
		return getDistinctColumn(request,"component");
	    case "target_milestones":
		return getDistinctColumn(request,"target_milestone");
	    case "versions":
		return getDistinctColumn(request,"version");
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
		return updateOrCreateBug(request);
	    default:
		return "Unkown request: " + pget;
	}
    }
}
