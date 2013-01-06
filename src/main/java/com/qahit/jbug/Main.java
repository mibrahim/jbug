/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.jdbc.EmbeddedDriver;

/**
 *
 * @author mosama
 */
public class Main
{
    // Static initialization

    static
    {
        init();
    }
    /**
     * Store the database connection
     */
    public static Connection dbConnection;

    /**
     * Checks and upgrades the database version if necessary
     */
    static void checkDBVersion()
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

    public static void upgradeToV001()
    {
        try
        {
            System.out.println("Upgrading to v001");
            SQL.queryNoRes(
                    "CREATE TABLE bugs"
                    + "("
                    + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + "title VARCHAR(24) NOT NULL,"
                    + "description VARCHAR(1024),"
                    + "CONSTRAINT primary_key PRIMARY KEY (id)"
                    + ")");
            SQL.setStringVar("dbversion", "1");
        }
        catch (Exception ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void init()
    {
        try
        {
            if (dbConnection == null || dbConnection.isClosed())
            {
                System.out.println("Attempting to connect ....");
//                EmbeddedDriver driver = new EmbeddedDriver();
                String protocol = "jdbc:derby:";
                dbConnection = DriverManager.getConnection(protocol + "jBug;create=true");
                System.out.println("Connected to/created database jBug");

                // Since we just connected, then check the DB version and upgrade if necessary
                checkDBVersion();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static String getConnectionInfo()
    {
        init();
        return dbConnection.toString();
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        dbConnection.close();
    }
}
