<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>상품 목록</title>
    <style>
        body { font-family: sans-serif; max-width: 900px; margin: 40px auto; padding: 0 16px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px 14px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background: #f4f4f4; }
        a { color: #3498db; text-decoration: none; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<h2>상품 목록</h2>
<table>
    <thead>
    <tr><th>상품명</th><th>가격</th><th>재고</th><th></th></tr>
    </thead>
    <tbody>
    <c:forEach var="p" items="${products}">
    <tr>
        <td><a href="/product/${p.id}">${p.name}</a></td>
        <td><fmt:formatNumber value="${p.price}"/>원</td>
        <td>${p.stock}</td>
        <td><a href="/product/${p.id}">상세보기</a></td>
    </tr>
    </c:forEach>
    <c:if test="${empty products}">
    <tr><td colspan="4" style="text-align:center;color:#999;">등록된 상품이 없습니다.</td></tr>
    </c:if>
    </tbody>
</table>
</body>
</html>
