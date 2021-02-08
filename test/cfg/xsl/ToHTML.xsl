<?xml version="1.0" encoding="UTF-8" ?>

<!-- This sytylesheet converts a XML file containing a Dataset to a HTML file . -->
<!-- 2003.11.16: Thomas Haklaender                                              -->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" version="1.0" encoding="UTF-8"/>

    
    <xsl:template match="/">
        <html>
        	<head>
          	</head>
        	<body>
          		<xsl:apply-templates/>
          	</body>
        </html>
    </xsl:template>

    
    <xsl:template match="filemetainfo">
    	<h2>File Meta Information</h2>
        <table border="0">
        	<th align="left" valign="top"> Name   &#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;</th>
          	<th align="left" valign="top"> Tag    &#8195;&#8195;&#8195;&#8195;&#8195; </th>
          	<th align="left" valign="top"> VR     &#8195;&#8195; </th>
          	<th align="left" valign="top"> VM     &#8195;&#8195; </th>
          	<th align="left" valign="top"> Length &#8195;&#8195; </th>
          	<th align="left" valign="top"> Data </th>
          	
          	<xsl:apply-templates select="./elm" />
    	</table>
    	<br/>
    	<br/>
    </xsl:template>

    
    <xsl:template match="dataset">
    	<h2>Dataset</h2>
        <table border="0" width="100%">
        	<th align="left" valign="top"> Name   &#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;&#8195;</th>
          	<th align="left" valign="top"> Tag    &#8195;&#8195;&#8195;&#8195;&#8195; </th>
          	<th align="left" valign="top"> VR     &#8195;&#8195; </th>
          	<th align="left" valign="top"> VM     &#8195;&#8195; </th>
          	<th align="left" valign="top"> Length &#8195;&#8195; </th>
          	<th align="left" valign="top"> Data </th>
          	
          	<xsl:apply-templates select="./elm" />
    	</table>
    	<br/>
    	<br/>
    </xsl:template>

   
    <xsl:template match="/dicomfile/filemetainfo/elm">
    	<tr>
    		<td align="left" valign="top"> <xsl:value-of select="@name" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="@tag" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="@vr" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="val/@vm" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="val/@len" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="val/@data" /> </td>
    	</tr>
    </xsl:template>

   
    <xsl:template match="item/elm">
    	<tr>
    		<td align="left" valign="top"> &gt;&#160; <xsl:value-of select="@name" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="@tag" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="@vr" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="val/@vm" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="val/@len" /> </td>

      		<xsl:variable name="dat" select="val/@data" />
    		<td align="left" valign="top"> <xsl:value-of select="translate($dat, '\', ' ')" /> </td>
 		
          	<xsl:apply-templates select="./seq/item" />
    	</tr>
    </xsl:template>

   
    <xsl:template match="elm">
    	<tr>
    		<td align="left" valign="top"> <xsl:value-of select="@name" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="@tag" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="@vr" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="val/@vm" /> </td>
    		<td align="left" valign="top"> <xsl:value-of select="val/@len" /> </td>

      		<xsl:variable name="dat" select="val/@data" />
    		<td align="left" valign="top"> <xsl:value-of select="translate($dat, '\', ' ')" /> </td>
 		
          	<xsl:apply-templates select="./seq/item" />
    	</tr>
    </xsl:template>

   
    <xsl:template match="item">
    	<tr>
    		<td align="left"> Sequence ID= <xsl:value-of select="@id" /> </td>
          	<xsl:apply-templates select="./elm" />
    	</tr>
    </xsl:template>

</xsl:stylesheet> 