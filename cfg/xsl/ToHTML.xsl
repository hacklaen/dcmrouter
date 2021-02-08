<?xml version="1.0" encoding="UTF-8"?>

<!-- This sytylesheet converts a XML file containing a Dataset to a HTML file  -->
<!-- Sequence items are currently not special marked  -->

<!-- 2004.11.16: Thomas Haklaender   -->

<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/TR/xhtml1/strict" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html"/>

	
	<xsl:template match="/">
		<html>
			<body>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>

	
	<xsl:template match="filemetainfo">
		<h2>File Meta Information</h2>
		<table border="0">
			<th align="left" valign="top">Name</th>
			<th align="center" valign="top">Tag</th>
			<th align="center" valign="top">VR</th>
			<th align="center" valign="top">VM</th>
			<th align="center" valign="top">Length</th>
			<th align="left" valign="top">Data</th>
	
			<xsl:apply-templates select="./attr"/>
		</table>
		<br/>
		<br/>
	</xsl:template>
	
	
	<xsl:template match="dataset">
		<h2>Dataset</h2>
		<table border="0" width="100%">
			<th align="left" valign="top">Name</th>
			<th align="center" valign="top">Tag</th>
			<th align="center" valign="top">VR</th>
			<th align="center" valign="top">VM</th>
			<th align="center" valign="top">Length</th>
			<th align="left" valign="top">Data</th>
	
			<xsl:apply-templates select="./attr"/>
		</table>
		<br/>
		<br/>
	</xsl:template>
	
	
	<xsl:template match="attr">
		<tr>
			<td align="left" valign="top">
				<xsl:value-of select="@name"/>
			</td>
			<td align="center" valign="top">
				<xsl:value-of select="@tag"/>
			</td>
			<td align="center" valign="top">
				<xsl:value-of select="@vr"/>
			</td>
			<td align="center" valign="top">
				<xsl:value-of select="@vm"/>
			</td>
			<td align="center" valign="top">
				<xsl:value-of select="@len"/>
			</td>
			<td align="left" valign="top">
				<xsl:value-of select="text()"/>
			</td>
		
			<xsl:apply-templates select="./seq/item"/>
		</tr>
	</xsl:template>
	
	
</xsl:stylesheet>
