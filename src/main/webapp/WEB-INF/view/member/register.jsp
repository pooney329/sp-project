<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원가입</title>
    <style>
        body { font-family: sans-serif; max-width: 480px; margin: 60px auto; padding: 0 16px; }
        h2 { margin-bottom: 24px; }
        .form-group { margin-bottom: 14px; }
        label { display: block; margin-bottom: 4px; font-size: 14px; color: #555; }
        input[type=text], input[type=email], input[type=password] {
            width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px;
        }
        button { padding: 8px 20px; background: #3498db; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
        .msg { padding: 10px; background: #d4edda; border-radius: 4px; margin-bottom: 14px; color: #155724; }
        .section { border: 1px solid #ddd; border-radius: 6px; padding: 20px; margin-bottom: 20px; }
        .section h3 { margin-top: 0; }
    </style>
</head>
<body>
<h2>회원가입</h2>

<c:if test="${not empty message}">
    <div class="msg">${message}</div>
</c:if>

<!-- Step 1: 이메일 인증 코드 발송 -->
<div class="section">
    <h3>1단계 — 이메일 인증 코드 발송</h3>
    <form action="/auth/send-code" method="post">
        <div class="form-group">
            <label>이메일</label>
            <input type="email" name="email" value="${email}" required/>
        </div>
        <button type="submit">인증 코드 발송</button>
    </form>
</div>

<!-- Step 2: 인증 코드 확인 -->
<div class="section">
    <h3>2단계 — 인증 코드 확인</h3>
    <form action="/auth/verify-code" method="post">
        <div class="form-group">
            <label>이메일</label>
            <input type="email" name="email" value="${email}" required/>
        </div>
        <div class="form-group">
            <label>인증 코드 (6자리)</label>
            <input type="text" name="code" maxlength="6" required/>
        </div>
        <button type="submit">코드 확인</button>
    </form>
</div>

<!-- Step 3: 회원 정보 입력 -->
<c:if test="${verified}">
<div class="section">
    <h3>3단계 — 회원 정보 입력</h3>
    <form action="/member/register" method="post">
        <input type="hidden" name="email" value="${email}"/>
        <div class="form-group">
            <label>비밀번호</label>
            <input type="password" name="password" required/>
        </div>
        <div class="form-group">
            <label>이름</label>
            <input type="text" name="name" required/>
        </div>
        <button type="submit">가입하기</button>
    </form>
</div>
</c:if>

<p><a href="/member/login">이미 계정이 있으신가요? 로그인</a></p>
</body>
</html>
