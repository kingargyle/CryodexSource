package cryodex.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class HellowWorld extends AbstractHandler {

   public HellowWorld() {
      // TODO Auto-generated constructor stub
   }

   public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
      
      if (target.contains("/xwing")) {
         baseRequest.setHandled(false);
         return;
      }
      
      response.setContentType("text/html;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);
      baseRequest.setHandled(true);
      response.getWriter().println("<h1>Hello World</h1>");
   }
}
