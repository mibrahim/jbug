/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import java.util.ArrayList;
import de.svenjacobs.loremipsum.LoremIpsum;
import java.sql.SQLException;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author mosama
 */
public class DataGenerator
{

    static String add1000Bugs(HttpServletRequest request) throws SQLException
    {
        Random rgen = new Random();

        LoremIpsum lr = new LoremIpsum();

        // Parse the given users
        ArrayList<String> emails = new ArrayList<>();
        String emailInput = request.getParameter("emails");
        for (String email : emailInput.split(" "))
        {
            emails.add(email.trim());
        }

        for (int i = 0; i != 1000; i++)
        {
            String assigned_to = emails.get(rgen.nextInt(emails.size()));
            int status = rgen.nextInt(Bug.Status.values().length);
            long creation_ts = System.currentTimeMillis() + rgen.nextInt(60 * 24 * 60 * 60 * 1000) - 30 * 24 * 60 * 60 * 1000;    // Within +- 30 days
            String title = Utils.capitalizeInitials(lr.getWords(3, rgen.nextInt(50)));
            String description = lr.getWords(150, rgen.nextInt(50));
            int priority = rgen.nextInt(Bug.Priorities.values().length);
            String reporter = emails.get(rgen.nextInt(emails.size()));

            String sql = "insert into bugs(assigned_to,severity,status,creation_ts,title,description,priority,reporter) values ("
                         + "'" + assigned_to + "',"
                         + "" + status + ","
                         + "" + creation_ts + ","
                         + "'" + title + "',"
                         + "'" + description + "',"
                         + "" + priority + ","
                         + "'" + reporter + "')";

            SQL.queryNoRes(sql);
        }
        
        return "Added 1000 bugs";
    }

    public static String todo(HttpServletRequest request) throws SQLException
    {
        String todo = request.getParameter("todo");
        if (todo!=null && todo.equals("add1000bugs"))
        {
            return add1000Bugs(request);
        }

        if (todo!=null && todo.equals("deleteall"))
        {
            SQL.queryNoRes("delete from bugs");
        }

        return null;
    }
}
