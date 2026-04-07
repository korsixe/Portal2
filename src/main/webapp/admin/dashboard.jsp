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
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .actions { display: flex; flex-direction: column; gap: 6px; }
        .btn { padding: 6px 10px; border: 1px solid #ccc; border-radius: 6px; background: #fff; cursor: pointer; }
        .btn-primary { background: #4f46e5; color: #fff; border-color: #4f46e5; }
        .btn-danger { background: #ef4444; color: #fff; border-color: #ef4444; }
        .alert { padding: 12px; border-radius: 8px; margin: 10px 0; }
        .alert.success { background: #dcfce7; color: #166534; }
        .alert.error { background: #fee2e2; color: #991b1b; }
        .nav { display: flex; gap: 10px; margin: 10px 0 20px; }
        .nav .btn { text-decoration: none; display: inline-block; }
    </style>
</head>
<body>
    <h1>Панель администратора</h1>
    <div class="nav">
        <a class="btn" href="${pageContext.request.contextPath}/home.jsp">На главную</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard.jsp">Личный кабинет</a>
        <a class="btn" href="${pageContext.request.contextPath}/moderator/moderation-bord.jsp">История модерации</a>
        <a class="btn" href="${pageContext.request.contextPath}/logout">Выйти</a>
    </div>

    <c:if test="${not empty message}">
        <div class="alert ${messageType}">${message}</div>
    </c:if>

    <div class="stats">
        <div class="stat-card">
            <h3>Всего пользователей</h3>
            <p>${stats.totalUsers}</p>
        </div>
        <div class="stat-card">
            <h3>Администраторов</h3>
            <p>${stats.adminCount}</p>
        </div>
        <div class="stat-card">
            <h3>Модераторов</h3>
            <p>${stats.moderatorCount}</p>
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
                <th>Монеты</th>
                <th>Действия</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${users}">
            <tr>
                <td>${user.id}</td>
                <td>${user.email}</td>
                <td>${user.name}</td>
                <td>
                    <c:forEach var="role" items="${user.roles}">
                        <span>${role.displayName}</span><br/>
                    </c:forEach>
                </td>
                <td>${user.coins}</td>
                <td>
                    <div class="actions">
                        <form method="post" action="${pageContext.request.contextPath}/admin/role">
                            <input type="hidden" name="targetUserId" value="${user.id}"/>
                            <input type="hidden" name="role" value="MODERATOR"/>
                            <input type="hidden" name="action" value="${user.moderator ? 'revoke' : 'assign'}"/>
                            <button class="btn" type="submit">${user.moderator ? 'Снять модератора' : 'Назначить модератором'}</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/admin/role">
                            <input type="hidden" name="targetUserId" value="${user.id}"/>
                            <input type="hidden" name="role" value="ADMIN"/>
                            <input type="hidden" name="action" value="${user.admin ? 'revoke' : 'assign'}"/>
                            <button class="btn btn-primary" type="submit">${user.admin ? 'Снять админа' : 'Назначить админом'}</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/admin/coins" style="display: flex; gap: 6px; align-items: center;">
                            <input type="hidden" name="targetUserId" value="${user.id}"/>
                            <input type="number" name="amount" min="1" value="50" style="width:80px;" required />
                            <input type="hidden" name="action" value="add"/>
                            <button class="btn" type="submit">+ Монеты</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/admin/coins" style="display: flex; gap: 6px; align-items: center;">
                            <input type="hidden" name="targetUserId" value="${user.id}"/>
                            <input type="number" name="amount" min="1" value="20" style="width:80px;" required />
                            <input type="hidden" name="action" value="deduct"/>
                            <button class="btn btn-danger" type="submit">- Монеты</button>
                        </form>
                    </div>
                </td>
            </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>
