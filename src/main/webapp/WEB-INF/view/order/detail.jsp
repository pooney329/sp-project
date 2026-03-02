<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>주문 상세</title>
    <style>
        body { font-family: sans-serif; max-width: 700px; margin: 40px auto; padding: 0 16px; }
        .summary { background: #f9f9f9; border-radius: 6px; padding: 16px; margin-bottom: 24px; }
        .summary p { margin: 6px 0; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px 14px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background: #f4f4f4; }
        a { color: #3498db; }
    </style>
</head>
<body>
<p><a href="/order/list">← 주문 내역</a></p>
<h2>주문 상세 #${order.id}</h2>

<div class="summary">
    <p>상태: <strong>${order.status}</strong></p>
    <p>총 금액: <fmt:formatNumber value="${order.totalAmount}"/>원</p>
    <p>포인트 사용: <fmt:formatNumber value="${order.pointUsed}"/>P</p>
    <p>결제 금액: <fmt:formatNumber value="${order.paymentAmount}"/>원</p>
    <p>주문일: <fmt:formatDate value="${order.orderedAt}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
</div>

<h3>주문 상품</h3>
<table>
    <thead>
    <tr><th>상품명</th><th>단가</th><th>수량</th><th>소계</th></tr>
    </thead>
    <tbody>
    <c:forEach var="item" items="${order.orderItems}">
    <tr>
        <td>${item.productName}</td>
        <td><fmt:formatNumber value="${item.price}"/>원</td>
        <td>${item.quantity}</td>
        <td><fmt:formatNumber value="${item.price * item.quantity}"/>원</td>
    </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
