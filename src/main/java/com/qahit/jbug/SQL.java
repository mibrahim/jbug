/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author mosama
 */
public class SQL
{

    public static ResultSet query(String sql)
    {
        try
        {
            Statement stmt = Main.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            stmt.close();
            return rs;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public static boolean queryNoRes(String sql)
    {
        try
        {
            Statement stmt = Main.dbConnection.createStatement();
            boolean result=stmt.execute(sql);
            stmt.close();
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String getStringVar(String varName)
    {
        try
        {
            ResultSet rs = query("select val from vars where var='" + varName + "'");
            if (!rs.next())
            {
                return null;
            }
            return rs.getString("val");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void setStringVar(String name, String value)
    {
        try
        {
            queryNoRes("delete from vars where var='"+name+"'");
            queryNoRes("insert into vars(var,val) values ('"+name+"','"+value+"')");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static public int getDBVersion()
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
