package cryodex.modules.xwing.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import cryodex.CryodexController;
import cryodex.export.ExportUtils;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.XWingTournament;
import cryodex.modules.xwing.export.XWingExportController;

public class XWingExportRankingsHandler extends AbstractHandler {

   public XWingExportRankingsHandler() {
   }

   public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
      
      Tournament activeTournament = CryodexController.getActiveTournament();
      response.setContentType("text/html;charset=utf-8");
      
      if (activeTournament != null) {
         String content = XWingExportController.appendRankings((XWingTournament) activeTournament);

         String displayHTMLToString = ExportUtils.displayHTMLToString(content);
         response.setStatus(HttpServletResponse.SC_OK);
         response.getWriter().print(displayHTMLToString);

      } else {
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         response.getWriter().println("<H1>Tournanment has been started</H1>");
      }
      
      baseRequest.setHandled(true);
   }
}
