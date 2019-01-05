<html>
<head>
<title>Ashwin's Grid</title>
<style>
html, body {
    margin: 0;
    padding: 0;
    font-size: 65%;
}
#div1 {
    -moz-border-radius: 10px;
    -webkit-border-radius: 10px;
    border-radius: 10px;
	border: 1px solid #FF8855;
	padding: 5px;
	background-color:#ffffff;
	margin-top:2.5%;
	margin-left:2.5%;
	margin-right:2.5%;
}
#div2 {
	padding: 5px;
}
</style>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" media="screen" href="css/ui-lightness/jquery-ui-1.8.14.custom.css" />
<link rel="stylesheet" type="text/css" media="screen" href="css/ui.jqgrid.css" />
<script src="scripts/jqstuffs/jquery-1.5.2.min.js" type="text/javascript"></script>
<script src="scripts/jqstuffs/i18n/grid.locale-en.js" type="text/javascript"></script>
<script src="scripts/jqstuffs/jquery.jqGrid.min.js" type="text/javascript"></script>
<script src="scripts/script.js" type="text/javascript"></script>
</head>
<body style="background-image:url('images/bglines.png'); background-repeat:repeat">
<div id="div1">
<table width="90%" border="0" align="center" style="background-color:#ffffff; margin-top:1%;" cellpadding="10px">
	<tr>
		<td style="background-color:9C98BD;">
			<div id="div2">
				<table border="0" width="100%">
				<tr>
					<td><button onclick="showMessage();">Get Data</button></td>
				</tr>
				</table>
			</div>
		</td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr><td><div align="center"><table id="list"></table><div id="pager"></div></div></td></tr>
</table>
</div>
</body>
</html>
