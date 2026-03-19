<%!
    // Метод для перенаправления на страницу ошибки
    public static void redirectToError(JspWriter out, HttpServletResponse response,
                                       String message, Exception e) {
        try {
            // Устанавливаем атрибуты ошибки
            if (e != null) {
                // Если есть исключение, используем механизм error-page
                throw new ServletException(message, e);
            } else {
                // Если просто сообщение об ошибке
                response.sendRedirect("error.jsp?message=" +
                        java.net.URLEncoder.encode(message, "UTF-8"));
            }
        } catch (Exception ex) {
            try {
                // Фолбэк: простая переадресация
                response.sendRedirect("error.jsp");
            } catch (Exception ex2) {
                // Последний резерв: вывод ошибки в консоль
                System.err.println("Критическая ошибка: " + ex2.getMessage());
            }
        }
    }
%>