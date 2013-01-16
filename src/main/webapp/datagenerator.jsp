<%-- 
    Document   : datagenerator
    Created on : Jan 6, 2013, 8:04:56 PM
    Author     : mosama
--%>

<%@page import="com.qahit.jbug.DataGenerator"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Data generator</title>
    </head>
    <body>
        <form method="get">
            <input type="hidden" name="todo" value="add1000bugs"/>
            <textarea name="emails"></textarea>
            <input type="submit">
        </form>
        <a href="/datagenerator.jsp?todo=deleteallbugs">Delete All Bugs</a><br/><br/>
        <%=DataGenerator.todo(request)%>
    </body>
</html>
