<%-- autocomplete.jsp --%>
<%@ page contentType="application/json;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.database.DatabaseConnection" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.Statement" %>
<%
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String query = request.getParameter("query");

    if (query == null || query.trim().length() < 2) {
        out.print("[]");
        return;
    }

    query = query.trim();

    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
        conn = DatabaseConnection.getConnection();

        String sql = "SELECT title FROM ads WHERE title ILIKE ? AND status = 'ACTIVE' LIMIT 8";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%" + query + "%");

        rs = stmt.executeQuery();

        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        while (rs.next()) {
            if (!first) json.append(",");
            String title = rs.getString("title");
            if (title != null) {
                json.append("\"").append(title.replace("\"", "\\\"")).append("\"");
                first = false;
            }
        }
        json.append("]");

        out.print(json.toString());

    } catch (Exception e) {
        System.err.println("Autocomplete error: " + e.getClass().getSimpleName());
        out.print("[]");

    } finally {
        if (rs != null) {
            try { rs.close(); } catch (Exception e) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (Exception e) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (Exception e) {}
        }
    }
%>