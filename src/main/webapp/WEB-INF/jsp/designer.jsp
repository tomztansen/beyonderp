<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://stimulsoft.com/webdesigner" prefix="stiwebdesigner"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Stimulsoft Designer</title>
    <style>
        html, body { height: 100%; width: 100%; margin: 0; padding: 0; overflow: hidden; font-family: sans-serif; }
    </style>
</head>
<body>
    <stiwebdesigner:stiwebdesigner options="${options}" handler="${handler}" />
</body>
</html>
