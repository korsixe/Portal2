<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Доступ запрещен</title>
    <style>
        body {font-family: Arial, sans-serif; background: #0f172a; color: #e5e7eb; margin: 0; display: flex; align-items: center; justify-content: center; height: 100vh;}
        .card {background: #111827; padding: 32px 40px; border-radius: 14px; box-shadow: 0 12px 30px rgba(0,0,0,0.35); text-align: center; max-width: 520px; width: 100%;}
        h1 {margin: 0 0 16px; font-size: 26px; color: #f87171;}
        p {margin: 0 0 28px; font-size: 18px;}
        a.btn {display: inline-block; padding: 10px 18px; background: #10b981; color: #0f172a; border-radius: 10px; text-decoration: none; font-weight: 600; transition: background 0.2s ease, transform 0.2s ease;}
        a.btn:hover {background: #0ea271; transform: translateY(-1px);}
    </style>
</head>
<body>
<div class="card">
    <h1>Доступ запрещен</h1>
    <p>Вы кто такие? Я вас не звал, идите матан ботать</p>
    <a class="btn" href="${pageContext.request.contextPath}/home.jsp">На главную</a>
</div>
</body>
</html>

