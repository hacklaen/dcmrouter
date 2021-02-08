<?xml version="1.0" encoding="UTF-8"?>


<!-- 2004.11.17: Thomas Haklaender   -->

<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/TR/xhtml1/strict" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" method="xml"/>
	
	
<!-- Photometric Interpretation ############################################## -->
	
	<xsl:variable name="photometric-interpretation">
		<xsl:value-of select="//attr [@name=&quot;Photometric Interpretation&quot;]"/>
	</xsl:variable>
	

	<xsl:variable name="new-photometric-interpretation">
		<xsl:choose>
			<xsl:when test="substring($photometric-interpretation, 1, 11) = &apos;MONOCHROME1&apos;">MONOCHROME2</xsl:when>
			<xsl:when test="substring($photometric-interpretation, 1, 11) = &apos;MONOCHROME2&apos;">MONOCHROME1</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$photometric-interpretation"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<xsl:attribute-set name="new-photometric-attribute-set">
		<xsl:attribute name="tag">
			<xsl:value-of select="@tag"/>
		</xsl:attribute>
		
		<xsl:attribute name="vr">
			<xsl:value-of select="@vr"/>
		</xsl:attribute>
		<xsl:attribute name="pos">
			<xsl:value-of select="@pos"/>
		</xsl:attribute>
				
		<xsl:attribute name="name">
			<xsl:value-of select="@name"/>
		</xsl:attribute>
		
		<xsl:attribute name="vm">1</xsl:attribute>
				
		<xsl:attribute name="length">
			<xsl:value-of select="string-length($new-photometric-interpretation)"/>
		</xsl:attribute>
		
	</xsl:attribute-set>
	
	
	<!-- Change the Photometric Interpretation between MONOCHROME1 and MONOCHROME2 -->
	<xsl:template match="attr [@name=&quot;Photometric Interpretation&quot;]">
		<xsl:element name="attr" namespace="" use-attribute-sets="new-photometric-attribute-set">
			<xsl:value-of select="$new-photometric-interpretation"/>
		</xsl:element>
	</xsl:template>

	
<!-- Copy all other nodes  ############################################## -->
			
	<!-- Copy all other nodes -->
	<xsl:template match="@* | * | text() | processing-instruction() | comment()" priority="-2">
		<xsl:copy>
			<xsl:apply-templates select="@* | * | text() | processing-instruction() | comment()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
