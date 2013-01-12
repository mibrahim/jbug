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

    public static String getData(HttpServletRequest request) throws SQLException
    {
	String pget = request.getParameter("get");
	String pfor = request.getParameter("for");
	String condition = request.getParameter("condition");
	String orderby = request.getParameter("orderby");

	switch (pget)
	{
	    case "users":
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

	    case "openbugcount":
		rs = SQL.query("select count(*) as count from bugs where status in (0,1,2,3,4,5,6)");
		String count = "0";
		if (rs.next())
		{
		    count = "" + rs.getInt("count");
		}
		rs.close();
		return count;

	    case "closedbugcount":
		rs = SQL.query("select count(*) as count from bugs where status in (7,8)");
		count = "0";
		if (rs.next())
		{
		    count = "" + rs.getInt("count");
		}
		rs.close();
		return count;

	    case "openbugids":
		rs = SQL.query("select bug_id from bugs where status in (0,1,2,3,4,5,6) order by severity,priority,creation_ts");
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

	    case "closedbugids":
		rs = SQL.query("select bug_id from bugs where status in (7,8) order by creation_ts");
		b = new StringBuilder();
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

	    case "bug":
		rs = SQL.query("select * from bugs where bug_id=" + pfor);
		b = new StringBuilder();
		if (rs.next())
		{
		    b.append(SQL.currentRowToJSON(rs));
		}
		else
		{
		    b.append("Not found");
		}
		rs.close();
		return b.toString();

	    case "bugs":
		rs = SQL.query("select * from bugs where bug_id in (" + pfor + ")");
		b = new StringBuilder();
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
		}
		else
		{
		    b.append("\n]}");
		}
		rs.close();
		return b.toString();

	    case "bugssummaries":
		rs = SQL.query("select bug_id,title,description,assigned_to,reporter,severity,status,creation_ts,description,priority from bugs where bug_id in (" + pfor + ")");
		b = new StringBuilder();
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
		}
		else
		{
		    b.append("\n]}");
		}
		rs.close();
		return b.toString();

	    case "bugids":
		String sql = "select bug_id from bugs";

		if (condition != null)
		{
		    sql += " where " + condition;
		}

		if (orderby != null)
		{
		    sql += " order by " + orderby;
		}
		rs = SQL.query(sql);
		b = new StringBuilder();
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
		
	    default:
		return "Unkown request: " + pget;
	}
    }
}
