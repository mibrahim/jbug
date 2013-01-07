/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
                        res.append("\n");
                    }
                    res.append(user);
                }

                return res.toString();

            case "openbugcount":
                rs = SQL.query("select count(*) as count from bugs where status in ('unconfirmed','new','assigned','reopen','ready')");
                String count = "0";
                if (rs.next())
                {
                    count = "" + rs.getInt("count");
                }
                rs.close();
                return count;

            case "closedbugcount":
                rs = SQL.query("select count(*) as count from bugs where status in ('resolved','verified')");
                count = "0";
                if (rs.next())
                {
                    count = "" + rs.getInt("count");
                }
                rs.close();
                return count;

            case "openbugids":
                rs = SQL.query("select bug_id from bugs where status in ('unconfirmed','new','assigned','reopen','ready') order by creation_ts");
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
                rs = SQL.query("select bug_id from bugs where status in ('resolved','verified') order by creation_ts");
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
                rs = SQL.query("select * from bugs where bug_id="+pfor);
                b = new StringBuilder();
                if (rs.next())
                {
                    ResultSetMetaData metaData=rs.getMetaData();
                    System.out.println("We found: "+metaData.getColumnCount()+" coulmns");
                    for(int i=1;i<=metaData.getColumnCount();i++)
                    {
                        if (b.length()>0)
                        {
                            b.append(",\n");
                        }
                        
                        if (b.length()==0)
                        {
                            b.append("{\n");
                        }
                        
                        String columnName=metaData.getColumnName(i);
                        String type=metaData.getColumnTypeName(i);
                        System.out.println("Column #"+i+": "+columnName+" type:"+type);
                        
                        b
                                .append("\"")
                                .append(columnName)
                                .append("\":\"");
                        
                        switch(type)
                        {
                            case "LONG VARCHAR":
                            case "VARCHAR":
                                String s=rs.getString(columnName);
                                b.append((s==null)?"":s);
                                break;
                                
                            case "INTEGER":
                                b.append(rs.getInt(columnName));
                                break;

                            case "BIGINT":
                                b.append(rs.getLong(columnName));
                                break;

                            case "CLOB":
                                Clob clob=rs.getClob(columnName);
                                b.append((clob==null)?"":clob.toString());
                                break;

                            default:
                                throw new SQLException("Unknown column type: "+type);
                        }
                        
                        b.append("\"");
                    }
                    b.append("\n}");
                }
                else
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
