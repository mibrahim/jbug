/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author mosama
 */
public class SQL
{

    public static ResultSet query(String sql) throws SQLException
    {
        ResultSet rs;
        Statement stmt = Main.dbConnection.createStatement();
        rs = stmt.executeQuery(sql);
        stmt.close();
        return rs;
    }
    
    public static boolean queryNoRes(String sql) throws SQLException
    {
        boolean result;
        try (Statement stmt = Main.dbConnection.createStatement())
        {
            result = stmt.execute(sql);
            stmt.close();
        }
        return result;
    }

    public static String getStringVar(String varName) throws SQLException
    {
        ResultSet rs = query("select val from vars where var='" + varName + "'");
        if (rs==null || rs.isClosed() || !rs.next())
        {
            return null;
        }
        return rs.getString("val");
    }

    public static void setStringVar(String name, String value) throws SQLException
    {
        queryNoRes("delete from vars where var='"+name+"'");
        queryNoRes("insert into vars(var,val) values ('"+name+"','"+value+"')");
    }

    static public int getDBVersion() throws SQLException
    {
        String dbVersionString = getStringVar("dbversion");
        if (dbVersionString == null)
        {
            return 0;
        }
        try
        {
            return Integer.parseInt(dbVersionString);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }
}
