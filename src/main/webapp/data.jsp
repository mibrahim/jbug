<%
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
    httpResponse.setDateHeader("Expires", 0); // Proxies.
%><%@page import="com.qahit.jbug.Main"%><%@page contentType="text/html" pageEncoding="UTF-8"%><%=Main.getData(request)%>