<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${errorTitle != null ? errorTitle : 'Error'}</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f8fafc;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .error-container {
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            padding: 32px;
            max-width: 600px;
            width: 90%;
            border-top: 4px solid #ef4444;
        }
        h2 {
            color: #ef4444;
            margin-top: 0;
            font-size: 1.5rem;
        }
        .error-message {
            color: #334155;
            background-color: #f1f5f9;
            padding: 16px;
            border-radius: 4px;
            font-family: monospace;
            font-size: 0.875rem;
            white-space: pre-wrap;
            word-wrap: break-word;
            margin-top: 16px;
            border-left: 3px solid #64748b;
        }
        button {
            margin-top: 24px;
            background-color: #3b82f6;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 4px;
            font-weight: 600;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        button:hover {
            background-color: #2563eb;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <h2>${errorTitle != null ? errorTitle : 'An Error Occurred'}</h2>
        <p>There was a problem loading the report. Please check the technical details below:</p>
        <div class="error-message">${errorMessage}</div>
        <button onclick="window.close()">Close Window</button>
    </div>
</body>
</html>
