<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>История модерации</title>
</head>
<body>
<h1>История модерации</h1>

<div>
    <a href="${pageContext.request.contextPath}/moderator/dashboard">Назад</a>
    <a href="${pageContext.request.contextPath}/admin/dashboard">Админка</a>
    <a href="${pageContext.request.contextPath}/logout">Выйти</a>
</div>

<h3>История объявлений</h3>
<table border="1" cellpadding="6">
    <thead>
    <tr>
        <th>#</th>
        <th>Объявление</th>
        <th>Из</th>
        <th>В</th>
        <th>Модератор</th>
        <th>Время</th>
        <th>Причина</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="item" items="${history}" varStatus="st">
        <tr>
            <td>${st.count}</td>
            <td>${item.adId}</td>
            <td>${item.fromStatus != null ? item.fromStatus : '-'}</td>
            <td>${item.toStatus}</td>
            <td>
                <c:choose>
                    <c:when test="${item.moderatorId != null}">
                        ${moderatorNames[item.moderatorId]}
                    </c:when>
                    <c:otherwise>-</c:otherwise>
                </c:choose>
            </td>
            <td><fmt:formatDate value="${item.createdAt}" type="both" pattern="dd.MM.yyyy HH:mm:ss"/></td>
            <td>${item.reason}</td>
        </tr>
    </c:forEach>
    <c:if test="${empty history}">
        <tr><td colspan="7">Пока нет записей</td></tr>
    </c:if>
    </tbody>
</table>

<h3>Админские действия</h3>
<table border="1" cellpadding="6">
    <thead>
    <tr>
        <th>#</th>
        <th>Действие</th>
        <th>Цель</th>
        <th>Подробнее</th>
        <th>Кто</th>
        <th>Время</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="item" items="${adminActions}" varStatus="st">
        <tr>
            <td>${st.count}</td>
            <td>${item.actionType}</td>
            <td>${item.targetType} ${item.targetId}</td>
            <td>${item.details}</td>
            <td>
                <c:choose>
                    <c:when test="${item.actorId != null}">
                        ${moderatorNames[item.actorId]}
                    </c:when>
                    <c:otherwise>${item.actorEmail}</c:otherwise>
                </c:choose>
            </td>
            <td><fmt:formatDate value="${item.createdAt}" type="both" pattern="dd.MM.yyyy HH:mm:ss"/></td>
        </tr>
    </c:forEach>
    <c:if test="${empty adminActions}">
        <tr><td colspan="6">Пока нет записей</td></tr>
    </c:if>
    </tbody>
</table>
</body>
</html>
