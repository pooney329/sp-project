<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>오류 발생</title>
    <style>
        body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f5f5f5; }
        .box { background: #fff; padding: 40px 60px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.12); text-align: center; }
        .status { font-size: 64px; font-weight: bold; color: #e74c3c; }
        .code { font-size: 14px; color: #888; margin: 4px 0 16px; }
        .message { font-size: 18px; color: #333; }
        a { display: inline-block; margin-top: 24px; color: #3498db; text-decoration: none; }
    </style>
</head>
<body>
<div class="box">
    <div class="status">${statusCode}</div>
    <div class="code">${errorCode}</div>
    <div class="message">${errorMessage}</div>
    <a href="/">홈으로 돌아가기</a>
</div>
</body>
</html>
