<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>포인트 내역</title>
    <style>
        body { font-family: sans-serif; max-width: 700px; margin: 40px auto; padding: 0 16px; }
        .balance { font-size: 22px; font-weight: bold; margin-bottom: 24px; }
        .balance span { color: #e74c3c; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px 14px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background: #f4f4f4; }
        .earn { color: #27ae60; font-weight: bold; }
        .use  { color: #e74c3c; font-weight: bold; }
    </style>
</head>
<body>
<h2>포인트 내역</h2>
<div class="balance">현재 포인트: <span><fmt:formatNumber value="${pointBalance}"/>P</span></div>

<table>
    <thead>
    <tr><th>일시</th><th>유형</th><th>금액</th><th>주문번호</th></tr>
    </thead>
    <tbody>
    <c:forEach var="h" items="${histories}">
    <tr>
        <td><fmt:formatDate value="${h.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
        <td>
            <c:choose>
                <c:when test="${h.type == 'EARN'}"><span class="earn">적립</span></c:when>
                <c:otherwise><span class="use">사용</span></c:otherwise>
            </c:choose>
        </td>
        <td>
            <c:choose>
                <c:when test="${h.amount > 0}"><span class="earn">+<fmt:formatNumber value="${h.amount}"/>P</span></c:when>
                <c:otherwise><span class="use"><fmt:formatNumber value="${h.amount}"/>P</span></c:otherwise>
            </c:choose>
        </td>
        <td>
            <c:if test="${not empty h.orderId}">
                <a href="/order/${h.orderId}">#${h.orderId}</a>
            </c:if>
        </td>
    </tr>
    </c:forEach>
    <c:if test="${empty histories}">
    <tr><td colspan="4" style="text-align:center;color:#999;">포인트 내역이 없습니다.</td></tr>
    </c:if>
    </tbody>
</table>
</body>
</html>
