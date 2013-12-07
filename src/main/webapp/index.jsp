<%@page import="com.qahit.jbug.Main"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JBug</title>
        <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css" />

        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">

        <!-- Optional theme -->
        <link rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap-theme.min.css">

		<link rel="stylesheet" href="http://netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.min.css"">

        <script src="http://code.jquery.com/jquery-2.0.3.js"></script>
        <script src="http://code.jquery.com/ui/1.9.2/jquery-ui.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="http://netdna.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
        <script src="js/jquery.cookie.js"></script>
        <script src="js/jquery.md5.js"></script>
        <script src="js/main.js"></script>
    </head>
    <body onresize="setMainContentHeight();">
        <div id="full">
            <jsp:include page="header.jsp"></jsp:include>
            <div id="main" class="main"></div>
            <script type="text/javascript">
				checkUser();
				setMainContentHeight();
				updateBugStatusBar();
				showOpenBugs();
            </script>
        </div>
    </body>
</html>