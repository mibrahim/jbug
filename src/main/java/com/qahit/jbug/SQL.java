/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mosama
 */
public final class SQL
{
    // Static initialization

    public static SQL singleInstance = new SQL();
    /**
     * Store the database connection
     */
    static Connection dbConnection;

    public SQL()
    {
        System.out.println("Initializing SQL");
        try
        {
            if (dbConnection == null || dbConnection.isClosed())
            {
                dbConnection = DriverManager.getConnection("jdbc:derby:jBug;create=true");
                dbConnection.setAutoCommit(false);

                // Since we just connected, then check the DB version and upgrade if necessary
                checkDBVersion();
                System.out.println("dbconnection:"+dbConnection);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        dbConnection.close();
    }

    /**
     * Checks and upgrades the database version if necessary
     */
    void checkDBVersion() throws SQLException
    {
        // Does table vars exist ??
        try
        {
            SQL.query("select * from vars");
        }
        catch (Exception e)
        {
            try
            {
                System.out.println("Attempting to create the table vars");
                SQL.queryNoRes("create table vars(var varchar(32), val varchar(256))");
                SQL.queryNoRes("create index vars1 on vars(var)");
            }
            catch (Exception e2)
            {
                throw new RuntimeException(e2);
            }
        }

        if (SQL.getDBVersion() < 1)
        {
            upgradeToV001();
        }
    }

    /**
     * Upgrades the database to version 001. See https://bugzilla.mozilla.org/page.cgi?id=fields.html for field definitions
     */
    public void upgradeToV001() throws SQLException
    {
        System.out.println("Upgrading to v001");

        // Main table
        SQL.queryNoRes(
                "CREATE TABLE bugs"
                + "("
                + "bug_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," // Auto inc id
                + "assigned_to VARCHAR(64) NOT NULL," // email of the assignee
                + "severity INTEGER NOT NULL,"
                + "status INTEGER NOT NULL,"
                + "resolution INTEGER,"
                + "creation_ts BIGINT NOT NULL,"
                + "title VARCHAR(256) NOT NULL,"
                + "description LONG VARCHAR NOT NULL,"
                + "comments_json CLOB,"
                + "priority INTEGER NOT NULL,"
                + "product VARCHAR(64),"
                + "rep_platform VARCHAR(64),"
                + "reporter VARCHAR(64) NOT NULL,"
                + "version VARCHAR(64),"
                + "component VARCHAR(64),"
                + "target_milestone VARCHAR(64),"
                + "estimated_load VARCHAR(8),"
                + "CONSTRAINT primary_key PRIMARY KEY (bug_id)"
                + ")");

        // Secondary indexes
        SQL.queryNoRes("create index i01 on bugs(assigned_to)");
        SQL.queryNoRes("create index i02 on bugs(severity)");
        SQL.queryNoRes("create index i03 on bugs(status)");
        SQL.queryNoRes("create index i04 on bugs(creation_ts)");
        SQL.queryNoRes("create index i05 on bugs(priority)");
        SQL.queryNoRes("create index i06 on bugs(product)");
        SQL.queryNoRes("create index i07 on bugs(rep_platform)");
        SQL.queryNoRes("create index i08 on bugs(reporter)");
        SQL.queryNoRes("create index i09 on bugs(version)");
        SQL.queryNoRes("create index i10 on bugs(component)");
        SQL.queryNoRes("create index i12 on bugs(target_milestone)");
        SQL.queryNoRes("create index i13 on bugs(estimated_load)");
        SQL.queryNoRes("create index i14 on bugs(resolution)");

        SQL.setStringVar("dbversion", "1");
        
        System.out.println("Upgraded to V001");
    }

    public static ResultSet query(String sql) throws SQLException
    {
        ResultSet rs;
        Statement stmt = dbConnection.createStatement();
        rs = stmt.executeQuery(sql);
        dbConnection.commit();

        return rs;
    }

    public static boolean queryNoRes(String sql) throws SQLException
    {
        boolean result;
        Statement stmt = dbConnection.createStatement();
        result = stmt.execute(sql);
        dbConnection.commit();

        return result;
    }

    public static String getStringVar(String varName) throws SQLException
    {
        ResultSet rs = query("select val from vars where var='" + varName + "'");
        if (rs == null || rs.isClosed() || !rs.next())
        {
            return null;
        }
        String value = rs.getString("val");
        rs.close();
        return value;
    }

    public static void setStringVar(String name, String value) throws SQLException
    {
        queryNoRes("delete from vars where var='" + name + "'");
        queryNoRes("insert into vars(var,val) values ('" + name + "','" + value + "')");
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
    
    /**
     * Converts the current row to a json string.
     * @param rs a ResultSet having the cursor at a certain row
     * @return a String containing the JSON representation of the current row
     * @throws SQLException 
     */
    static String currentRowToJSON(ResultSet rs) throws SQLException
    {
        ResultSetMetaData metaData = rs.getMetaData();
        //System.out.println("We found: " + metaData.getColumnCount() + " coulmns");
        StringBuilder b = new StringBuilder();
        for (int i = 1; i <= metaData.getColumnCount(); i++)
        {
            if (b.length() > 0)
            {
                b.append(",\n");
            }

            if (b.length() == 0)
            {
                b.append("{\n");
            }

            String columnName = metaData.getColumnName(i);
            String type = metaData.getColumnTypeName(i);
            //System.out.println("Column #" + i + ": " + columnName + " type:" + type);

            b
                    .append("\"")
                    .append(columnName)
                    .append("\":\"");

            switch (type)
            {
                case "LONG VARCHAR":
                case "VARCHAR":
                    String s = rs.getString(columnName);
                    b.append((s == null) ? "" : s);
                    break;

                case "INTEGER":
                    b.append(rs.getInt(columnName));
                    break;

                case "BIGINT":
                    b.append(rs.getLong(columnName));
                    break;

                case "CLOB":
                    Clob clob = rs.getClob(columnName);
                    b.append((clob == null) ? "" : clob.toString());
                    break;

                default:
                    throw new SQLException("Unknown column type: " + type);
            }

            b.append("\"");
        }
        b.append("\n}");

        return b.toString();
    }
}
