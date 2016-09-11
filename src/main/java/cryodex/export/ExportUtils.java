package cryodex.export;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportUtils {

    public static void displayHTML(String content, String filename) {
        String fancyCss = "table{border-collapse: collapse;margin: 20px;}th{color:white; background-color:DarkSlateGray; font-size:120%;} tr:nth-child(odd){    background-color:lightgray;}";
        String internationalCharacters = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />";
        String html = "<html><head><style type=\"text/css\">.pagebreak {page-break-after: always;}.smallFont{font-size:10px}"
                + fancyCss + "</style>" + internationalCharacters + "</head><body>" + content + "</body></html>";

        try {
            File file = File.createTempFile(filename, ".html");

            FileOutputStream stream = new FileOutputStream(file);

            stream.write(html.getBytes("UTF-8"));
            stream.flush();
            stream.close();

            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    
    public static void addTableStart(StringBuilder sb){
        sb.append("<table border=\"1\">");
    }

    public static void addTableHeader(StringBuilder sb, String ...strings){
        
        sb.append("<tr>");
        
        for(String s : strings){
            sb.append("<th>").append(s).append("</th>");
        }
        
        sb.append("</tr>");
    }
    
    public static void addTableRow(StringBuilder sb, String ...strings){

        sb.append("<tr>");
        
        for(String s : strings){
            sb.append("<td>").append(s).append("</td>");
        }
        
        sb.append("</tr>");
    }
    
    public static void addTableEnd(StringBuilder sb){
        sb.append("</table>");
    }
    
    
}
