/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jposbox;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import static java.lang.System.out;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author Windows10
 */
public class Web {
    static Statement stmt,stmt2 = null;
    static ResultSet rs= null, rs2= null;
    String printername="";
    HttpServer server = null;
    
    
    public int StartServer(int port, String printer_name){
        printername=printer_name;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException ex) {
            return port;
        }
        out.println("Server started at port "+port);
        server.createContext("/hw_proxy/hello", new Hello());
        server.createContext("/hw_proxy/handshake", new Handshake());
        server.createContext("/hw_proxy/status_json", new StatusJson());
        server.createContext("/hw_proxy/print_xml_receipt", new PrintXMLReceipt());
        //Dolibarr
        server.createContext("/print", new PrintReceipt());
        server.createContext("/print2", new PrintReceipt2());
        //End Dolibarr
        server.setExecutor(null); // creates a default executor
        server.start();
        return 0;
    }
    
    
    public void StopServer(){
        // Stop server
    }
    
    
    
    public static Map<String, String> queryToMap(String query){
    Map<String, String> result = new HashMap<String, String>();
    for (String param : query.split("&")) {
        String pair[] = param.split("=");
        if (pair.length>1) {
            result.put(pair[0], pair[1]);
        }else{
            result.put(pair[0], "");
        }
    }
    return result;
    }
    

    
    
    static class Hello implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            out.println(t.getRequestURI());
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().set("Content-Type", "text/plain");
            t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
            String response = "ping";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            out.println("Respponse: "+response);
        }
    }
    
    
    
    
    
    
    
    public class Handshake implements HttpHandler {
         @Override
         public void handle(HttpExchange he) throws IOException {
                // parse request
                out.println("/hw_proxy/handshake");
                he.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                he.getResponseHeaders().set("Content-Type", "application/json");
                he.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
                he.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Debug-Mode");
                Map<String, Object> parameters = new HashMap<String, Object>();
                InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String query = br.readLine();
                parseQuery(query, parameters);

                // send response
                String response = "";
                for (String key : parameters.keySet())
                         response += key + " = " + parameters.get(key) + "\n";
                response=response.replace("{\"jsonrpc\":\"2.0\",\"method\":\"call\",\"params\":{},\"id\":", "");
                response=response.replace("} = null", "");
                response="{\"jsonrpc\": \"2.0\", \"id\": "+response+", \"result\": \"true\"}";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                out.print("Response::"+response);
                os.write(response.toString().getBytes());
                os.close();
         }
}
    
    

    
    
    public class StatusJson implements HttpHandler {
         @Override
         public void handle(HttpExchange he) throws IOException {
                // parse request
                out.println("/hw_proxy/status_json");
                he.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                he.getResponseHeaders().set("Content-Type", "application/json");
                he.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
                he.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Debug-Mode");
                Map<String, Object> parameters = new HashMap<String, Object>();
                InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String query = br.readLine();
                parseQuery(query, parameters);
                 // send response
                String response = "";
                for (String key : parameters.keySet())
                         response += key + " = " + parameters.get(key) + "\n";
                response=response.replace("{\"jsonrpc\":\"2.0\",\"method\":\"call\",\"params\":{},\"id\":", "");
                response=response.replace("} = null", "");
                response="{\"jsonrpc\": \"2.0\", \"id\": "+response+", \"result\": {\"scale\": {\"status\": \"disconnected\", \"messages\": [\"No RS-232 device found\"]}, \"scanner\": {\"status\": \"error\", \"messages\": [\"[Errno 2] No such file or directory: '/dev/input/by-id/'\"]}, \"escpos\": {\"status\": \"connected\", \"messages\": [\"Connected to TakePOS Printer\"]}}}";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();
         }
}
    
    
    
    
    
    
    
    
    
    
    public class PrintXMLReceipt implements HttpHandler {
         @Override
         public void handle(HttpExchange he) throws IOException {
            try{ 
                // parse request
                out.println("/print_xml_receipt");
                he.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                he.getResponseHeaders().set("Content-Type", "application/json");
                he.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
                he.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Debug-Mode");
                Map<String, Object> parameters = new HashMap<String, Object>();
                InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String text = br.readLine();
                String id="";
                String document="ticket";
                if (text!=null){ // Si nos manda vacio saltamos
                    out.println("Received "+text);
                    id=text.substring(text.length()-9, text.length()-1);
                    out.println("ID"+id);
                    String receipt = text.substring(text.indexOf("<receipt"), text.indexOf("</receipt>")+10);
                    receipt=receipt.replace("\\n", "");
                    receipt=receipt.replace("\\\"", "\"");
                    String LogoData="";
                    try{
                        LogoData=receipt.substring(receipt.indexOf("<img"), receipt.indexOf("\"/>")+3);
                        receipt=receipt.replace(LogoData, "");
                    } catch (IndexOutOfBoundsException e) {
                        //Si no hay imagen, es que es un pedido?
                        document="order";
                    }
                    out.println("Document: "+document);
                    out.println("Printing: "+receipt);
                    PosPrinter P= new PosPrinter();
                    P.html=true;
                    //P.add(LogoData);
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = db.parse(new InputSource(new StringReader(receipt)));
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    
                    //Header
                    XPathExpression expr = xpath.compile("/receipt/div/div");
                    NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    P.add("<center>");
                    for (int temp = 0; temp < nl.getLength(); temp++) {
                        Node nNode = nl.item(temp);
                        if (nNode.getTextContent().contains("----------")){ //Las lineas de servido por.... estan dentro de otro div
                            NodeList nl2= nNode.getChildNodes();
                            for (int temp2 = 0; temp2 < nl2.getLength(); temp2++) {
                                if (temp2==0 || temp2==2 ||temp2==6) continue; //Only spaces
                                Node nNode2 = nl2.item(temp2);
                                P.add(nNode2.getTextContent());
                                P.salto();
                            }
                        }
                        else{
                            P.add(nNode.getTextContent());
                            P.salto();
                        }
                    }
                    P.add("</center>");
                    
                    //End Header
                    
                        
                    //Start order
                    if (document.equals("order")){
                        expr = xpath.compile("/receipt/div");
                        nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                        for (int temp = 0; temp < nl.getLength(); temp++) {
                            Node nNode = nl.item(temp);
                            P.add(nNode.getTextContent());
                            P.salto();
                        }
                    }
                    //End header
                    
                    P.salto();
                    
                    //Invoice lines
                    boolean IsFactura=false;
                    P.add("<table width=\"90%\" cellpadding=0 cellspacing=0>");
                    expr = xpath.compile("/receipt/div[@line-ratio='0.6']/line");
                    nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    for (int temp = 0; temp < nl.getLength(); temp++) {
                        Node nNode = nl.item(temp);
                        NodeList nl2=nNode.getChildNodes();
                        for (int temp2=0; temp2 < nl2.getLength();temp2++){
                            IsFactura=true;
                            Node nNode2=nl2.item(temp2);
                            if (nNode2.getNodeName().equals("left")) P.add("<tr><td><div alight=\"left\">"+nNode2.getTextContent()+"</div></td>");
                            if (nNode2.getNodeName().equals("right")){
                                double value1 = Double.parseDouble(nNode2.getTextContent());
                                String value2=String.format("%.2f",value1);
                                P.add("<td><div align=\"right\">"+value2+"</div></td></tr>");
                            }
                        }
                    }
                    if (IsFactura) P.add("<td></td><td><div align\"right\">--------</div></td>");
                    P.add("</table>");
                    //End invoice lines
                    
                    //Order lines and footer
                    expr = xpath.compile("/receipt/line");
                    nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    for (int temp = 0; temp < nl.getLength(); temp++) {
                        Node nNode = nl.item(temp);
                        if (nNode.getTextContent().contains("--------")) continue;
                        if (nNode.hasChildNodes() && IsFactura){ //If is invoice and last lines
                            // If is left or right more cool
                            String negrita="";
                            if (nNode.getTextContent().contains("TOTAL")) negrita="<b>";
                            NodeList nl2=nNode.getChildNodes();
                            P.add("<table width=\"90%\" cellpadding=0 cellspacing=0>");
                            for (int temp2=0; temp2 < nl2.getLength();temp2++){
                                Node nNode2=nl2.item(temp2);
                                if (nNode2.getNodeName().equals("left")) P.add("<tr><td><div alight=\"left\">"+negrita+nNode2.getTextContent()+"</div></td>");
                                if (nNode2.getNodeName().equals("right")){
                                    double value1 = Double.parseDouble(nNode2.getTextContent());
                                    String value2=String.format("%.2f",value1);
                                    P.add("<td><div align=\"right\">"+negrita+value2+"</div></td></tr>");
                                }
                            }
                            P.add("</table>");
                        }
                        else P.add(nNode.getTextContent());
                        P.salto();
                    }
                    
                    P.print(printername,1,"7");
                }
                String response="{\"jsonrpc\": \"2.0\", \"id\": "+id.replace(":", "")+"}";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                out.print("Response:"+response);
                os.write(response.toString().getBytes());
                os.close();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
         }
}
    
    
    
    
    
        public class PrintReceipt implements HttpHandler {
         @Override
         public void handle(HttpExchange he) throws IOException {
            try{ 
                out.println("Data received");
                he.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                he.getResponseHeaders().set("Content-Type", "application/json");
                he.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
                he.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Debug-Mode");
                Map<String, Object> parameters = new HashMap<String, Object>();
                InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String text = br.readLine();
                if (text!=null){
                    PosPrinter P= new PosPrinter();
                    P.html=true;
                    P.P=text;
                    P.print(PosBoxFrame.ComboPrinter1.getSelectedItem().toString(), 1,"7");
                }
                String response="";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
         }
}
    

   
    
    
    public class PrintReceipt2 implements HttpHandler {
         @Override
         public void handle(HttpExchange he) throws IOException {
            try{ 
                out.println("Data received");
                he.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                he.getResponseHeaders().set("Content-Type", "application/json");
                he.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
                he.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Debug-Mode");
                Map<String, Object> parameters = new HashMap<String, Object>();
                InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String text = br.readLine();
                if (text!=null){
                    PosPrinter P= new PosPrinter();
                    P.html=true;
                    P.P=text;
                    P.print(PosBoxFrame.ComboPrinter2.getSelectedItem().toString(), 1,"7");
                }
                String response="";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
         }
}    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static void parseQuery(String query, Map<String,Object> parameters) throws UnsupportedEncodingException {
         if (query != null) {
                 String pairs[] = query.split("[&]");
                 for (String pair : pairs) {
                          String param[] = pair.split("[=]");
                          String key = null;
                          String value = null;
                          if (param.length > 0) {
                          key = URLDecoder.decode(param[0], 
                          	System.getProperty("file.encoding"));
                          }
                          if (param.length > 1) {
                                   value = URLDecoder.decode(param[1], 
                                   System.getProperty("file.encoding"));
                          }
                          if (parameters.containsKey(key)) {
                                   Object obj = parameters.get(key);
                                   if (obj instanceof List<?>) {
                                            List<String> values = (List<String>) obj;
                                            values.add(value);
                                   } else if (obj instanceof String) {
                                            List<String> values = new ArrayList<String>();
                                            values.add((String) obj);
                                            values.add(value);
                                            parameters.put(key, values);
                                   }
                          } else {
                                   parameters.put(key, value);
                          }
                 }
         }
    }
}
