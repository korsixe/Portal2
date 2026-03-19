<%@ page contentType="text/plain;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.announcementContent.ProfanityChecker" %>
<%
    // Handler for AJAX profanity check requests
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String action = request.getParameter("action");
        if ("checkProfanity".equals(action)) {
            String text = request.getParameter("text");
            
            if (text != null && !text.trim().isEmpty()) {
                ProfanityChecker checker = new ProfanityChecker();
                boolean hasProfanity = checker.containsProfanity(text);
                out.print(hasProfanity);
            } else {
                out.print("false");
            }
            return;
        }
    }
    out.print("false");
%>

