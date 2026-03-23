<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Административная панель</title>
    <style>
        body { font-family: sans-serif; padding: 20px; }
        .stats { display: flex; gap: 20px; margin-bottom: 20px; }
        .stat-card { background: #f0f0f0; padding: 15px; border-radius: 8px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>Панель администратора</h1>

    <div class="stats">
        <div class="stat-card">
            <h3>Всего пользователей</h3>
            <p>${totalUsers}</p>
        </div>
        <div class="stat-card">
            <h3>Администраторов</h3>
            <p>${adminCount}</p>
        </div>
        <div class="stat-card">
            <h3>Модераторов</h3>
            <p>${moderatorCount}</p>
        </div>
    </div>

    <h2>Пользователи</h2>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Email</th>
                <th>Имя</th>
                <th>Роли</th>
                <th>Действия</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${users}">
            <tr>
                <td>${user.id}</td>
                <td>${user.email}</td>
                <td>${user.name}</td>
                <td>${user.roles}</td>
                <td>
                    <!-- Actions like Promote/Demote/Delete could go here -->
                    <button>Редактировать</button>
                </td>
            </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>

