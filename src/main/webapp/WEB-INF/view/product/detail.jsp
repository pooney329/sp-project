<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${product.name}</title>
    <style>
        body { font-family: sans-serif; max-width: 600px; margin: 40px auto; padding: 0 16px; }
        .price { font-size: 24px; font-weight: bold; color: #e74c3c; margin: 8px 0; }
        .stock { color: #555; margin-bottom: 20px; }
        .desc { line-height: 1.6; color: #333; margin-bottom: 24px; }
        .form-row { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; }
        input[type=number] { width: 80px; padding: 6px; border: 1px solid #ccc; border-radius: 4px; }
        button { padding: 8px 24px; background: #e74c3c; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
        a { color: #3498db; }
    </style>
</head>
<body>
<p><a href="/product/list">← 상품 목록</a></p>
<h2>${product.name}</h2>
<div class="price"><fmt:formatNumber value="${product.price}"/>원</div>
<div class="stock">재고: ${product.stock}개</div>
<div class="desc">${product.description}</div>

<c:choose>
<c:when test="${not empty loginMember}">
    <form action="/order/create" method="post">
        <input type="hidden" name="productId" value="${product.id}"/>
        <div class="form-row">
            <label>수량</label>
            <input type="number" name="quantity" value="1" min="1" max="${product.stock}"/>
        </div>
        <div class="form-row">
            <label>포인트 사용</label>
            <input type="number" name="pointUsed" value="0" min="0"
                   max="${loginMember.pointBalance}"/>
            <span>(보유: <fmt:formatNumber value="${loginMember.pointBalance}"/>P)</span>
        </div>
        <button type="submit">주문하기</button>
    </form>
</c:when>
<c:otherwise>
    <p><a href="/member/login">로그인 후 주문할 수 있습니다.</a></p>
</c:otherwise>
</c:choose>
</body>
</html>
