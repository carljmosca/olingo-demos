<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Olingo Demo</title>
        <style type="text/css">
            body { font-family: Arial, sans-serif; font-size: 13px; line-height: 18px;
                   color: #333333; background-color: #ffffff; }
            a { color: #0076cb; text-decoration: none; }
            a:focus { outline: thin dotted #0076cb; outline-offset: -1px; }
            a:hover, a:active { outline: 0; }
            a:hover { color: #004a7e; text-decoration: underline; }
            h1, h2, h3, h4, h5, h6 { margin: 9px 0; font-family: inherit; font-weight: bold;
                                     line-height: 1; color: #f0ab00; }
            h1 { font-size: 36px; line-height: 40px; }
            h2 { font-size: 30px; line-height: 40px; }
            h3 { font-size: 24px; line-height: 40px; }
            h4 { font-size: 18px; line-height: 20px; }
            h5 { font-size: 14px; line-height: 20px; }
            h6 { font-size: 12px; line-height: 20px; }
            .logo { float: right; }
            ul { padding: 0; margin: 0 0 9px 25px; }
            ul ul { margin-bottom: 0; }
            li { line-height: 18px; }
            hr { margin: 18px 0;
                 border: 0; border-top: 1px solid #cccccc; border-bottom: 1px solid #ffffff; }
            table { border-collapse: collapse; border-spacing: 10px; }
            th, td { border: 1px solid; padding: 20px; }
            .code { font-family: "Courier New", monospace; font-size: 13px; line-height: 18px; }
        </style>
    </head>
    <body>
        <h1>Olingo Demo</h1>
        <hr />
        <table>
            <tr>
                <td valign="top">
                    <h3>Service Document and Metadata</h3>
                    <ul>
                        <li><a href="OlingoDemo.svc?_wadl" target="_blank">wadl</a></li>
                        <li><a href="OlingoDemo.svc/" target="_blank">service
                                document</a></li>
                        <li><a href="OlingoDemo.svc/$metadata" target="_blank">metadata</a></li>
                    </ul>
                    <h3>EntitySets</h3>
                    <ul>
                        <li><a href="OlingoDemo.svc/Customers" target="_blank">Customers</a></li>
                        <li><a href="OlingoDemo.svc/DiscountCodes" target="_blank">DiscountCodes</a></li>
                        <li><a href="OlingoDemo.svc/Manufacturers" target="_blank">Manufacturers</a></li>
                    </ul>
                    <h3>Entities</h3>
                    <ul>
                        <li><a href="OlingoDemo.svc/Customers(1)"
                               target="_blank">Customers('1')</a></li>
                    </ul>
                </td>
                <td valign="top">
                    &nbsp;
                </td>
                <td valign="bottom">
                    <div class="code">
                        <%
                          String version = "gen/version.html";
                        %>
                        <%
                          try {
                        %>
                        <jsp:include page='<%=version%>' />
                        <%
                          } catch (Exception e) {
                        %>
                        <p>IDE Build</p>
                        <%
                          }
                        %>
                    </div>
                </td>
            </tr>
        </table>
    </body>
</html>
