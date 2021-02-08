<?xml version="1.0" encoding="UTF-8"?>


<!-- Copy all other elements and leave them unchanged -->
<!-- 2004.11.17: Thomas Haklaender   -->

<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/TR/xhtml1/strict" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" method="xml"/>
	
	
	

<!-- Copy all other nodes  ############################################## -->
			
	<!-- Copy all other nodes -->
	<xsl:template match="@* | * | text() | processing-instruction() | comment()" priority="-2">
		<xsl:copy>
			<xsl:apply-templates select="@* | * | text() | processing-instruction() | comment()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
