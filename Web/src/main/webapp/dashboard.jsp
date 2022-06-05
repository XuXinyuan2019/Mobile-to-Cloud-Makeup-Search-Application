<%--
  Created by IntelliJ IDEA.
  User: xxy
  Date: 2022/4/7
  Time: 15:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="javax.swing.text.Document" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Dashboard</title>
</head>
<body>
    <h1>Makeup Search Dashboard</h1>

    <h3>Operations Analytics</h3>
    <p>Shortest search time (ms): <%= request.getAttribute("shortestSearchTime") %></p><br>
    <p>Top 1 Brand: <%= request.getAttribute("top1Brand") %></p><br>
    <p>Total search times: <%= request.getAttribute("totalSearchTimes") %></p><br>

    <h3>Log Data</h3>
    <% List<org.bson.Document> logData = new ArrayList<>(); %>
    <% logData = (List<org.bson.Document>) request.getAttribute("logData"); %>

    <table border=1, width="100%">
        <thead>
            <tr>
                <td>ID</td>
                <td>Device</td>
                <td>Search Brand</td>
                <td>Search Type</td>
                <td>Name</td>
                <td>Price</td>
                <td>Product URL</td>
                <td>Image URL</td>
                <td>Timestamp</td>
                <td>Search Time (ms)</td>
            </tr>
        </thead>
        <tbody>
            <% int i = 1; %>
            <% for (org.bson.Document document: logData) {%>
            <tr>
                <td><%= i %></td>
                <td><%= document.get("device") %></td>
                <td><%= document.get("search_brand") %></td>
                <td><%= document.get("search_type") %></td>
                <td><%= document.get("name") %></td>
                <td><%= document.get("price") %></td>
                <td><%= document.get("product_link") %></td>
                <td><%= document.get("image_link") %></td>
                <td><%= document.get("timestamp") %></td>
                <td><%= document.get("search_time") %></td>
            </tr>
            <% i++; %>
            <%} %>
        </tbody>
    </table>
</body>
</html>
