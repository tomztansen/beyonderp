<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://stimulsoft.com/webviewer" prefix="stiwebviewer"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Stimulsoft Viewer</title>
    <style>
        html, body { height: 100%; width: 100%; margin: 0; padding: 0; overflow: hidden; font-family: sans-serif; }
    </style>
</head>
<body>
    <stiwebviewer:stiwebviewer report="${report}" options="${options}" />
</body>
</html>
