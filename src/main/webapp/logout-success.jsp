<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Вы вышли</title>
    <style>
        body {font-family: Arial, sans-serif; background: #f5f7fb; margin: 0; display: flex; align-items: center; justify-content: center; height: 100vh;}
        .card {background: white; padding: 32px 40px; border-radius: 14px; box-shadow: 0 10px 25px rgba(0,0,0,0.08); text-align: center; max-width: 420px; width: 100%;}
        h1 {margin: 0 0 12px; color: #1f2937; font-size: 24px;}
        p {margin: 0 0 24px; color: #4b5563;}
        a.btn {display: inline-block; padding: 10px 18px; background: #667eea; color: white; border-radius: 10px; text-decoration: none; transition: background 0.2s ease, transform 0.2s ease;}
        a.btn:hover {background: #5565c9; transform: translateY(-1px);}
    </style>
</head>
<body>
<div class="card">
    <h1>Вы успешно вышли из аккаунта</h1>
    <p>Сессия завершена. Можно вернуться на главную или зайти снова.</p>
    <a class="btn" href="${pageContext.request.contextPath}/home.jsp">На главную</a>
</div>
</body>
</html>

