package cryodex.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;

public class WebServer {

   private Server server;
   
   private static WebServer webServer; 
   
   private WebServer() {
   }
   
   public static  WebServer getInstance() {
      if (webServer == null) {
         webServer = new WebServer();
      }
      
      return webServer;
   }
   
   public boolean start() {
      
      try {
         if (server == null || server.isStopped()) {
            server = new Server(9080);
            
            HandlerCollection handlers = new HandlerCollection();
            ContextHandler defaultContext = new ContextHandler();
            defaultContext.setContextPath("/");
            Handler handler = new HellowWorld();
            defaultContext.setHandler(handler);
            handlers.addHandler(defaultContext);
            
            server.setHandler(handlers); 
            server.start();
         }         
      } catch (Exception  ex) {
         return false;         
      }
      
      return true;
   }
   
   public boolean shutDown() {
      if (server != null && (server.isRunning() || server.isStarted() )) {
         try {
            server.stop();
            return true;
         } catch (Exception e) {
         }
      }
      return false;
   }
   
   public synchronized void addContextHandler(ContextHandler contextHandler) {
      
      if (server != null && server.isStarted()) {
         try {
            server.stop();
            HandlerCollection handlers = new HandlerCollection();
            handlers.setHandlers(server.getHandlers());
            handlers.addHandler(contextHandler);

            server.setHandler(handlers);
            server.start();
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      
   }

}
