
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
           //create a document builder object to iterate through nodes
           //of xml file
            DocumentBuilder builder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
            
            Document xml = builder.parse(fileName);
            Element root = xml.getDocumentElement();
            //find the phone number and voter ID that should be stored
            //in this xml file
            String[] values = findValues(root);
            //return null if the values aren't present OR if an error occurs
            if(values == null){
                System.out.println("Invalid message type, null returned");
                return null;
            }
            return new XMLMessage(values[0], values[1]);
            
        }catch(Exception e){
            System.out.println("There was an error while parsing the message");
            return null;
        }
    }
    private static Element findNode(Element el, String name){
        //find a node based on a given parent element and 
        //the desired name of the node
        Element e;
        NodeList nodes = el.getChildNodes();
        if(nodes != null){
            //if the node has a child node
            for(int i = 0; i < nodes.getLength(); i++){
                //iterate through the element nodes of the child node list
                if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    //get element reference to node
                     e = (Element) nodes.item(i);
                     //if its the node we want ,return its element reference
                    if(e.getNodeName().equals(name)){
                        return e;
                    }
                }
            }
        } else {
            //node list was empty
            System.out.println("No child nodes");
        }
        System.out.println("Specified node not found");
        //if the node we wanted was not found
        return null;
    }
  
    private static String[] findValues(Element root){
        String[] keys = new String[2];
        Element body = findNode(root, "Body");
        Element e;
        //get the items under the body node
        NodeList items = body.getChildNodes();
        boolean phoneFound = false, idFound = false;
        //init bool flags
        if(items != null){
            //if there are child nodes
            for(int i = 0; i < items.getLength(); i++){
               //iterate through child nodes
                if(items.item(i).getNodeType() == Node.ELEMENT_NODE){
                    //if the node is the correct type
                    e = (Element) items.item(i);
                    if(e.getNodeName().equals("Item")){
                        //The key value pairs are listed under Item
                        Node child;
                        NodeList children = e.getChildNodes();
                        //get node list of the item
                        for (int j = 0; j < children.getLength(); j++) {
                            //if the current child node content is the voter phone no.
                            child = children.item(j);
                            
                            if(child.getTextContent().equals("VoterPhoneNo")){
                                //the value is stored 2 away in the child node list,
                                //so grab that value and store it in key[0]
                                keys[0] = children.item(j+2).getTextContent();
                               //set phone found flag to true
                                phoneFound = true;
                           }
                            if(child.getTextContent().equals("CandidateID")){
                                //same context as above
                                keys[1] = children.item(j+2).getTextContent();
                                //set id found flag to true
                                idFound = true;
                           }
                        }
                        
                        
                    }
                }
            }
        }
        //if both values were found, return the array
       if(phoneFound && idFound){
           return keys;
       } else {
           //else, return null to signify one or both were not present
           return null;
       }
    }
}

