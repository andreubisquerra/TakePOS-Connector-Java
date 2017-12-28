/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jposbox;

import java.awt.print.PrinterException;
import static java.lang.System.out;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.PrinterName;
import javax.swing.JEditorPane;

/**
 *
 * @author Windows10
 */
public class PosPrinter {
    String P="";
    boolean html;
    
    void lineaguion(){
        if (html==true) P=P+"<hr />";
        else P=P+"----------------------------------------\r\n";
    }
    
    void salto(){
        if (html==true) P=P+"<br>";
        else P=P+"\r\n";
    }
    
    
    String ResizeText(String texto, int caracteres){
        texto=String.format("%1$-"+caracteres+"s",texto); //alargamos si es necsario
        texto=texto.substring(0,caracteres); //recortamos si es necesario
        return texto;
    }
    
    
    void HeaderOfLines(){
        lineaguion();
        if (html==false) P=P+ResizeText("Description",27)+" "+ResizeText("Qty",6)+" "+ResizeText("Total",6);
        else P=P+"<table cellspacing=\"0px\"><tr><td>Description</td><td>Qty</td><td>Total</td></tr>";
        salto();
        if (html==false) lineaguion();
    }
    
    void HeaderOfLinesNoTotal(){
        lineaguion();
        if (html==false) P=P+ResizeText("Description",27)+" "+ResizeText("Qty",6);
        else P=P+"<table cellspacing=\"0px\"><tr><td>Description</td><td>Qty</td><td></td></tr>";
        salto();
        if (html==false) lineaguion();
    }
    
    void addline(String label, String qty, String total) {
        if (total.equals("       0.00")) {qty="";total="";}
        if (total.equals("0")) {qty="";total="";}
        if (total.equals("0.00")) {qty="";total="";}
        if (total.equals("0,00")) {qty="";total="";}
        if (html==false) P=P+ResizeText(label,27)+" "+ResizeText(qty,6)+" "+ResizeText(total,6);
        else P=P+"<tr><td>"+label+"</td><td align=\"right\">"+qty+"</td><td align=\"right\">"+total+"</td></tr>";
        salto();
    }
    
    void finishProductLines(){
        if (html==true) P=P+"</table>";
        lineaguion();
    }
    
    void totales(String tva, String total, String currency){
        //FUNCION ANTIGUA PARA UN SOLO IVA, NO SE USA NI DOLIBARR NI LOCAL
        if (html==true) P=P+"<div align=\"right\">";
        if (!tva.equals("")){
            P=P+"Total VAT: "+tva;
            if (html==true) P=P+" "+currency;
            salto();
        }
        if (html==true) P=P+"<b>";
        P=P+"Total: "+total;
        if (html==true) P=P+" "+currency+"</b>";
        salto();
        if (html==true) P=P+"</div>";
    }
    
    void total(String descripcion, String total, boolean negrita, String currency){
        if (html==true && negrita) P=P+"<b>";
        P=P+descripcion+": "+total;
        if (html==true) P=P+" "+currency;
        if (html==true && negrita) P=P+"</b>";
        
        salto();
    }
    
    void add(String add){
        P=P+add;
    }
    
    
    void print(String printer, int copies, String FontSize) {
        if (printer.equals("nada")) {out.println("Printer not configured"); return;}
        PrintService[] printServices; // PARA WINDOWS
        PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
        printServiceAttributeSet.add(new PrinterName(printer, null));
        printServices = PrintServiceLookup.lookupPrintServices(null, printServiceAttributeSet); //PARA WINDOWS
        PrintService printService=PrintServiceLookup.lookupDefaultPrintService();
        
        if (html==false){
            
            if (copies>1) P=P+P;
            if (copies>2) P=P+P;
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            DocPrintJob pj = printServices[0].createPrintJob();
            byte[] bytes = P.getBytes();
            Doc doc = new SimpleDoc(bytes, flavor, null);
            try {
                pj.print(doc, null);
            } catch (PrintException ex) {
                Logger.getLogger(PosBoxFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        else {
            try {
                MediaPrintableArea mpa=new MediaPrintableArea(0,0,200,275,MediaPrintableArea.MM);
                HashPrintRequestAttributeSet hpras=new HashPrintRequestAttributeSet(mpa);
                final JEditorPane ed = new JEditorPane(
                        "text/html",
                        "<html><head><style>body{font-size:"+FontSize+"px;}</style></head><body>"+P+"</body></html>");
                if (/*System.getProperty("os.name").contains("Windows")*/ 1==1){ //POR EL MOTIVO QUE SEA, AHORA ME HA FUNCIONADO CON LINUX
                    ed.print(null, null, false, printServices[0], hpras, false);
                    if (copies>1) ed.print(null, null, false, printServices[0], hpras, false);
                    if (copies>2) ed.print(null, null, false, printServices[0], hpras, false);
                }
                else{
                    ed.print(null, null, false, printService, hpras, false);
                    if (copies>1) ed.print(null, null, false, printService, hpras, false);
                    if (copies>2) ed.print(null, null, false, printService, hpras, false);
                }
            } catch (PrinterException ex) {
                Logger.getLogger(PosPrinter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
