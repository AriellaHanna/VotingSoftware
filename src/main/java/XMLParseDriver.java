
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
/**
 *
 * @author adamhayes
 */
public class XMLParseDriver {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String xml = "untitled.xml";
        XMLMessage data = parseMessage(xml);
        if(data == null){
            System.out.println("Null returned by parsing.");
            System.exit(0);
        }
        System.out.println(data.getNum() + " | " + data.getID());
       //driver fnctn, read in message created by 
       //remote control, parse the info based on 
       //what we want
       
    }
    /*
        MAIN FUNCTION OF DRIVER
            purpose is to parse an XML message 
            sent by the remote control and separate out
            the 
    
    */
    public static XMLMessage parseMessage(String fileName){
        try{
            System.out.println("Checkpoint 1");
            DocumentBuilder builder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
            System.out.println("Checkpoint 2");
            Document xml = builder.parse(fileName);
            Element root = xml.getDocumentElement();
            String[] values = findValues(root);
            
            System.out.println("Checkpoint 7");
            return new XMLMessage(values[0], values[1]);
            
        }catch(Exception e){
            System.out.println("There was an error while parsing the message");
            return null;
        }
    }
    private static Element findNode(Element el, String name){
        NodeList nodes = el.getChildNodes();
        if(nodes != null){
            for(int i = 0; i < nodes.getLength(); i++){
                if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nodes.item(i);
                    if(e.getNodeName().equals(name)){
                        return e;
                    }
                }
            }
        } else {
            System.out.println("Specified element not found");
        }
        return null;
    }
  
    private static String[] findValues(Element root){
        String[] keys = new String[2];
        Element body = findNode(root, "Body");
        System.out.println("STEP");
        NodeList items = body.getChildNodes();
        boolean phoneFound = false, idFound = false;
        System.out.println("STEP");
        if(items != null){
            for(int i = 0; i < items.getLength(); i++){
                System.out.println("STEP");
                if(items.item(i).getNodeType() == Node.ELEMENT_NODE){
                    
                    Element e = (Element) items.item(i);
                    if(e.getNodeName().equals("Item")){
                        Node child;
                        NodeList children = e.getChildNodes();
                        for (int j = 0; j < children.getLength(); j++) {
                            child = children.item(j);
                            if(child.getTextContent().equals("VoterPhoneNo")){
                                System.out.println("PHONE");
                                keys[0] = children.item(j+2).getTextContent();
                                System.out.println("PHONE");
                                phoneFound = true;
                           }
                            if(child.getTextContent().equals("CandidateID")){
                                System.out.println("ID");
                                keys[1] = children.item(j+2).getTextContent();
                                System.out.println("ID");
                                idFound = true;
                           }
                        }
                        
                        
                    }
                }
            }
        }
       
       return keys;
    }
}
