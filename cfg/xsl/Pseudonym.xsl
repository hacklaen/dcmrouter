<?xml version="1.0" encoding="UTF-8"?>

<!-- 2004.11.17: Thomas Haklaender   -->

<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/TR/xhtml1/strict" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" method="xml"/>
	
	
<!-- Patient's Name  ############################################## -->
	
	<xsl:variable name="birth-date">
		<xsl:value-of select="//attr [@name=&quot;Patient&apos;s Birth Date&quot;]"/>
	</xsl:variable>
	
	<xsl:variable name="old-patient-name">
		<xsl:value-of select="//attr [@name=&quot;Patient&apos;s Name&quot;]"/>
	</xsl:variable>
			
	<xsl:variable name="new-patient-name">
		<xsl:value-of select="concat(substring($old-patient-name, 1, 1),substring(substring-after($old-patient-name, &apos;^&apos;),1,1), &apos;_&apos;, substring($birth-date, 3, 6))"/>
	</xsl:variable>
	
	<xsl:attribute-set name="new-patient-name-attribute-set">
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
			<xsl:value-of select="string-length($new-patient-name)"/>
		</xsl:attribute>
		
	</xsl:attribute-set>
	
	
	<!-- Change the Patient's name to LS_YYMMDD -->
	<!-- L Last name, S Surename, Y Year of birth, M Month of birth, D Day of birth -->
	<xsl:template match="attr[@name=&quot;Patient&apos;s Name&quot;]">
		<xsl:element name="attr" namespace="" use-attribute-sets="new-patient-name-attribute-set">
			<xsl:value-of select="$new-patient-name"/>
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
