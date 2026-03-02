<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>로그인</title>
    <style>
        body { font-family: sans-serif; max-width: 400px; margin: 80px auto; padding: 0 16px; }
        h2 { margin-bottom: 24px; }
        .form-group { margin-bottom: 14px; }
        label { display: block; margin-bottom: 4px; font-size: 14px; color: #555; }
        input[type=email], input[type=password] {
            width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px;
        }
        button { width: 100%; padding: 10px; background: #2ecc71; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 15px; }
        .msg { padding: 10px; background: #d4edda; border-radius: 4px; margin-bottom: 14px; color: #155724; }
    </style>
</head>
<body>
<h2>로그인</h2>

<c:if test="${not empty message}">
    <div class="msg">${message}</div>
</c:if>

<form action="/member/login" method="post">
    <div class="form-group">
        <label>이메일</label>
        <input type="email" name="email" required/>
    </div>
    <div class="form-group">
        <label>비밀번호</label>
        <input type="password" name="password" required/>
    </div>
    <button type="submit">로그인</button>
</form>

<p style="text-align:center; margin-top:16px;">
    <a href="/member/register">계정이 없으신가요? 회원가입</a>
</p>
</body>
</html>
