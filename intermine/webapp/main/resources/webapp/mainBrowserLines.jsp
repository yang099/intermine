<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<!-- mainBrowserLines.jsp -->

<html:xhtml/>

<c:set var="indent" value="0"/>

<c:forEach var="node" items="${nodes}">
  <%-- This hideous stuff surrounds branches of the statically
    rendered tree with the right div ids. This kind of thing
    would be a lot easier if we were rendering a real tree
    rather than just a list of Nodes.. --%>
  <c:if test="${!noTreeIds && node.indentation > indent}">
    <div id="${previousNodePath}"><!--open div ${previousNodePath}-->
  </c:if>
  <c:if test="${!noTreeIds && node.indentation < indent}">
    </div><!--close div ${previousNodePath}-->
  </c:if>
  <c:set var="indent" value="${node.indentation}"/>
  <c:set var="node" value="${node}" scope="request"/>
  <!--browser line ${node.pathString} indent ${node.indentation}-->
  <tiles:insert page="/mainBrowserLine.jsp"/>
  <c:set var="previousNodePath" value="${node.pathString}"/>
</c:forEach>
<%-- Closes all opened divs --%>
<c:forEach begin="1" end="${indent}">
  </div>
</c:forEach>

<!-- /mainBrowserLines.jsp -->


