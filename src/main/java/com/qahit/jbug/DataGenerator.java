/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

import de.svenjacobs.loremipsum.LoremIpsum;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author mosama
 */
public class DataGenerator {

    static String add10000Bugs(HttpServletRequest request, SQL sql)
            throws SQLException {
        Random rgen = new Random();

        LoremIpsum lr = new LoremIpsum();

        // Parse the given users
        ArrayList<String> emails = new ArrayList<>();
        String emailInput = request.getParameter("emails");
        for (String email : emailInput.split(" ")) {
            emails.add(email.trim());
        }

        for (int i = 0; i != 10000; i++) {
            String assigned_to = emails.get(rgen.nextInt(emails.size()));
            int status = rgen.nextInt(Bug.Status.values().length);
            long creation_ts = System.currentTimeMillis() + rgen.nextInt(60 * 24 * 60 * 60 * 1000) - 30 * 24 * 60 * 60 * 1000;    // Within +- 30 days
            String title = Utils.capitalizeInitials(lr.getWords(3, rgen.nextInt(50)));
            String description = lr.getWords(150, rgen.nextInt(50));
            int priority = rgen.nextInt(Bug.Priorities.values().length);
            String reporter = emails.get(rgen.nextInt(emails.size()));

            String product = "p" + rgen.nextInt(50);
            String target_milestone = "m" + rgen.nextInt(50);

            String stmt = "insert into bugs(assigned_to,status,creation_ts,modification_ts,title,description,priority,reporter,product,target_milestone) values ("
                    + "'" + assigned_to + "',"
                    + "" + status + ","
                    + "" + creation_ts + ","
                    + "" + creation_ts + ","
                    + "'" + title + "',"
                    + "'" + description + "',"
                    + "" + priority + ","
                    + "'" + reporter + "',"
                    + "'" + product + "',"
                    + "'" + target_milestone + "'" + //," +
                    ")";

            sql.queryNoRes(stmt);

            if (i % 100 == 0)
                System.out.println("i=" + i);
        }

        return "Added 10000 bugs";
    }

    public static String todo(HttpServletRequest request) throws SQLException {
        SQL sql = new SQL();

        try {
            String todo = request.getParameter("todo");
            if (todo != null && todo.equals("add1000bugs")) {
                return add10000Bugs(request, sql);

            }

            if (todo != null && todo.equals("deleteall")) {
                sql.queryNoRes("delete from bugs");
            }

            return null;
        } finally {
            sql.close();
        }
    }
}
