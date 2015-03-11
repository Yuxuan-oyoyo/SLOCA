<%@page import="sloca.model.User"%>
<%

    User user = (User) session.getAttribute("LoggedInUser");
    if (user == null) {
        response.sendRedirect("index.jsp");
        return;
    }
%>
