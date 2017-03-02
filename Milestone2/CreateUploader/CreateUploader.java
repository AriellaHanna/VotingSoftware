
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Calendar;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.StoreClosedException;
import javax.mail.NoSuchProviderException;
import javax.mail.Flags;
import javax.mail.Address;

public class CreateUploader
{
    // socket for connection to SISServer
    static Socket universal;
    private static int port = 993;
   
    // scope of this component
    private static final String SCOPE = "SIS.Scope1";
	// name of this component
    private static final String NAME = "Uploader";
    // messages types that can be handled by this component
    private static final List<String> TYPES = new ArrayList<String>(
        Arrays.asList(new String[] { "Alert", "Emergency", "Confirm", "Setting" }));

  //  private static UploaderReading reading = new UploaderReading();

    // variables for sending emails
    static final String SMTP_HOST_NAME = "smtp.gmail.com";
    static final String SMTP_PORT = "465";
    static final String POP3_HOST_NAME = "pop.gmail.com";
    static final String POP3_PORT = "995";
    static final String emailSuccessTxt = "We have received your input.\nThanks for voting!";
    static final String emailFailureTxt = "Unfortunately, we were unable to understand your email, and no votes were counted."
            + "\nTo cast a vote, send an email with 'vote (candidate #)' in the subject line to this account.";
    static final String emailSubjectGood = "Vote Confirmation";
    static final String emailSubjectBad = "Error Processing Vote";// title
    static final String emailFromAddress = "hayeshanna1631@gmail.com";
    static final String password = "chang1631";
    static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    static Properties props = new Properties();
    static Session emailSession;    
    /*
     * Main program
     */
    public static void main(String[] args)
    {
        
        //init email session
         emailSession = Session.getDefaultInstance(props,
                          new javax.mail.Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(
                           "hayeshanna1631@gmail.com", "chang1631");
            }
        });
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.put("mail.smtp.socketFactory.fallback", "true");
        props.put("mail.store.protocol", "pop3");
        props.put("mail.pop3s.host", POP3_HOST_NAME);
        props.put("mail.pop3s.port", "995");
        props.put("mail.pop3.starttls.enable", "true");
        while (true)
        {
            try
            {
                System.out.println("Polling...");
                Store store = emailSession.getStore("pop3s");
                //open a new session with the gmail inbox 
                //refreshing by opening and closing
                store.connect(POP3_HOST_NAME, emailFromAddress, password);
                //connect to the account
                Folder emailFolder = store.getFolder("INBOX");
                //open the inbox
                emailFolder.open(Folder.READ_ONLY);
                //read in the oldest unprocessed message
                Message msg = emailFolder.getMessages()[0];
                //process this message
                ProcessMsg(msg);  
                System.out.println("processed message");
                //close the email session to refresh
                emailFolder.close(false);
                store.close();

            }
            catch (Exception e)
            {   
                
                try
                {
                    // wait for 1 second to retry
                    Thread.sleep(1000);
                }
                catch (InterruptedException e2)
                {
                }
               
            } 
           
        }
    }

    /*
     * Method for sending email for vote registration failure
     */
    static void sendFailureMessage(String recipient) throws MessagingException{
                boolean debug = false;
       //generate an email session to construct a response email for users
        System.out.println("Generating Failure message...");
        Session session = Session.getDefaultInstance(props,
                          new javax.mail.Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(
                           "hayeshanna1631@gmail.com", "chang1631");
            }
        });
        session.setDebug(debug);
        //Create a new message
        Message msg = new MimeMessage(session);
        //send it from the project email account
        InternetAddress addressFrom = new InternetAddress(emailFromAddress);
        msg.setFrom(addressFrom);
        //send it back to the person who sent in the email we are currently processing
        InternetAddress[] addressTo = new InternetAddress[1];
        addressTo[0] = new InternetAddress(recipient);
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        //set the subject and text to the error response
        msg.setSubject(emailSubjectBad);
        msg.setText(emailFailureTxt);
        //send the message
        Transport.send(msg);
        System.out.println("Informed user of error in vote tally.");
    }
    /*
     * Method for sending email for vote registration message creation success
     */
    static void sendConfirmMessage(String recipient) throws MessagingException
    {
        System.out.println("Generating Confirm message...");
        boolean debug = false;
        //generate an email session to construct a response email for users
        Session session = Session.getDefaultInstance(props,
                          new javax.mail.Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(
                           "hayeshanna1631@gmail.com", "chang1631");
            }
        });
        session.setDebug(debug);
        //create a new message 
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(emailFromAddress);
        msg.setFrom(addressFrom);
        //send the message from the project account

        InternetAddress[] addressTo = new InternetAddress[1];
        addressTo[0] = new InternetAddress(recipient);
        //send it back to the person who sent in the email we are currently processing
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        //set the subject and text to the error response
        msg.setSubject(emailSubjectGood);
        msg.setText(emailSuccessTxt);
        //send the email
        Transport.send(msg);
        System.out.println("Informed voter of vote reception.");
    }

    // ============= end of sending email ====================

    private static void ProcessMsg(Message msg) throws Exception
    {
        //process the message,
        //meaning check the subject to see if it contains 
        //the correct format to be counted as a vote
        System.out.println("Processing message...");
        Address[] sender = msg.getFrom();
        String subject = msg.getSubject();
        String vote[] = subject.toLowerCase().split(" ");
        //subject must contain
        if(!vote[0].equals("vote") || vote.length != 2){
            System.out.println("Error in message construction");
            sendFailureMessage(sender[0].toString());
        } else {
            try{
            createXML(vote[1], sender[0]);
            System.out.println("XML message created successfully");
            sendConfirmMessage(sender[0].toString());
            } catch (Exception e){
                System.out.println("Error in message construction");
                sendFailureMessage(sender[0].toString());
            }
        }
        //remove the email from the program's view so that 
        //it will only work on the oldest unprocessed email in the inbox
        delete();
    }
    private static void createXML(String candID, Address sender) throws Exception{
        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //Generate an xml file with the format of message 701
            //aka the cast a vote format
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("Msg");
            doc.appendChild(root);
        
            Element head = doc.createElement("Head");
            root.appendChild(head);
        
            Element body = doc.createElement("Body");
            Element item1 = doc.createElement("Item");
            Element item2 = doc.createElement("Item");
       
            Element MsgId = doc.createElement("MsgID");
            MsgId.appendChild(doc.createTextNode("701"));
            head.appendChild(MsgId);
        
            Element desc = doc.createElement("Description");
            desc.appendChild(doc.createTextNode("Cast Vote"));
            head.appendChild(desc);
        
            Element VoterNo = doc.createElement("VoterPhoneNo");
            VoterNo.appendChild(doc.createTextNode(sender.toString()));
            item1.appendChild(VoterNo);
            body.appendChild(item1);
        
            Element CandidateID = doc.createElement("CandidateID");
            CandidateID.appendChild(doc.createTextNode(candID));
            item2.appendChild(CandidateID);
            body.appendChild(item2);
            root.appendChild(body);
        
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans = transFact.newTransformer();
            DOMSource source = new DOMSource(doc);
            String email = sender.toString().split("@")[0];
            StreamResult result = new StreamResult(email + ".xml");
            trans.transform(source, result);
        }  catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	} catch (TransformerException tfe) {
		tfe.printStackTrace();
	}
        
    }
    //remove the most recently processed email in the inbox from the programs view
    //so that repeat emails will not be considered
    public static void delete(){
      try 
      {
         // get the session object
         

         
         

         // create the POP3 store object and connect with the pop server
         Store store = emailSession.getStore("pop3s");

         store.connect(POP3_HOST_NAME, emailFromAddress, password);

         // create the folder object and open it
         Folder emailFolder = store.getFolder("INBOX");
         emailFolder.open(Folder.READ_WRITE);

        
         // retrieve the messages from the folder in an array 
         Message[] messages = emailFolder.getMessages();
         
            //operate only on the oldest message (aka the one we are currently processing
            Message message = messages[0];
            
            
	     // set the DELETE flag to true
	    message.setFlag(Flags.Flag.SEEN, true);
            message.setFlag(Flags.Flag.DELETED, true);
            
	    
            
         
         // expunges the folder to remove messages which are marked deleted
         emailFolder.close(true);
         store.close();

      } catch (NoSuchProviderException e) {
         e.printStackTrace();
      } catch (MessagingException e) {
         e.printStackTrace();
      } 
   }

}
