<%@page import="com.qahit.jbug.Main"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JBug</title>
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
        <script src="/js/jquery.cookie.js"></script>
        <script src="/js/jquery.md5.js"></script>
        <script src="/js/main.js"></script>
        <link rel="stylesheet" href="/css/jbug.css"/>
        <link rel="stylesheet" href="/css/forms.css"/>
        <link rel="stylesheet" href="/css/font-awesome.css"/>
    </head>
    <body>
        <jsp:include page="header.jsp"></jsp:include>
        <script type="text/javascript">checkUser();</script>
        <h1>Hello World!</h1>
        <%=Main.getConnectionInfo()%>
    </body>
</html>