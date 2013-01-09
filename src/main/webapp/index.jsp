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
        <link rel="stylesheet" href="/css/main.css"/>
        <link rel="stylesheet" href="/css/font-awesome.css"/>
    </head>
    <body onresize="setMainContentHeight();">
        <div id="full">
            <jsp:include page="header.jsp"></jsp:include>
            <div id="main" class="main">Hello</div>
            <script type="text/javascript">
                checkUser();
                setMainContentHeight();
                updateBugStatusBar();
                showOpenBugs();
            </script>
        </div>
    </body>
</html>