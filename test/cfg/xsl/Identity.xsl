<?xml version="1.0" encoding="UTF-8"?>

<!-- This sytylesheet performs a identity transformation from the source           -->
<!-- to the destination XML file, that is it copies the source to the destination. -->
<!-- see: http://www.ektron.com/support/ewebeditprokb.cfm?doc_id=2485              -->
<!-- 2003.11.16: Thomas Haklaender                                                 -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>