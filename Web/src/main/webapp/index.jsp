<%--
  Created by IntelliJ IDEA.
  User: xxy
  Date: 2022/4/5
  Time: 12:44
  To change this template use File | Settings | File Templates.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%--<%= request.getAttribute("doctype") %>--%>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Makeup Searcher</title>
</head>
<body>
<p>What you are looking for?</p>
<form action="getMakeupInformation" method="GET">
    <label >Makeup Brand:</label>
    <input type="text" name="searchBrand" value="" /><br>
    <label >Makeup Type:</label>
    <input type="text" name="searchType" value="" /><br>
    <input type="submit" value="Submit" />
</form>
</body>
</html>


