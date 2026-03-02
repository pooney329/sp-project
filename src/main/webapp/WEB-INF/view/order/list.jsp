<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>주문 내역</title>
    <style>
        body { font-family: sans-serif; max-width: 800px; margin: 40px auto; padding: 0 16px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px 14px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background: #f4f4f4; }
        a { color: #3498db; text-decoration: none; }
    </style>
</head>
<body>
<h2>주문 내역</h2>
<table>
    <thead>
    <tr><th>주문번호</th><th>총금액</th><th>포인트사용</th><th>결제금액</th><th>상태</th><th>주문일</th><th></th></tr>
    </thead>
    <tbody>
    <c:forEach var="o" items="${orders}">
    <tr>
        <td>${o.id}</td>
        <td><fmt:formatNumber value="${o.totalAmount}"/>원</td>
        <td><fmt:formatNumber value="${o.pointUsed}"/>P</td>
        <td><fmt:formatNumber value="${o.paymentAmount}"/>원</td>
        <td>${o.status}</td>
        <td><fmt:formatDate value="${o.orderedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
        <td><a href="/order/${o.id}">상세</a></td>
    </tr>
    </c:forEach>
    <c:if test="${empty orders}">
    <tr><td colspan="7" style="text-align:center;color:#999;">주문 내역이 없습니다.</td></tr>
    </c:if>
    </tbody>
</table>
</body>
</html>
