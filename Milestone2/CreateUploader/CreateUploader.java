
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
    
    // message writer
    static MsgEncoder encoder;
    // message reader
    static MsgDecoder decoder;

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
    static final String emailFailureTxt = "Unfortunately, we were unable to understand your email, and no votes were counted.";
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
                
               Store store = emailSession.getStore("pop3s");
                System.out.println("created email session");
                
                store.connect(POP3_HOST_NAME, emailFromAddress, password);
                System.out.println("connected to store");
               Folder emailFolder = store.getFolder("INBOX");
                System.out.println("Opened inbox");
                emailFolder.open(Folder.READ_ONLY);
                System.out.println("open email folder");
                
                Message[] msgs = emailFolder.getMessages();
                
                System.out.println("retrieved the message");
               
                ProcessMsg(msgs[0]);  
                System.out.println("processed message");
                emailFolder.close(false);
                System.out.println("closed email folder");
                store.close();
                System.out.println("closed store");

            }
            catch (Exception e)
            {   
                e.printStackTrace();
                // if anything goes wrong, try to re-establish the connection
                try{
                    
                } catch(Exception f){
                    f.printStackTrace();
                    
                }
                e.printStackTrace();
                try
                {
                    // wait for 1 second to retry
                    Thread.sleep(1000);
                }
                catch (InterruptedException e2)
                {
                }
                System.out.println("Try to reconnect");
                
            } 
           
        }
    }

    /*
     * Method for sending email for Alert Message
     */
    static void sendFailureMessage(String recipient) throws MessagingException{
                boolean debug = false;
       

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

        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(emailFromAddress);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[1];
        addressTo[0] = new InternetAddress(recipient);

        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.setSubject(emailSubjectBad);
        msg.setText(emailFailureTxt);
        Transport.send(msg);
        System.out.println("Informed user of error in vote tally.");
    }
    static void sendConfirmMessage(String recipient) throws MessagingException
    {
        boolean debug = false;

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

        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(emailFromAddress);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[1];
        addressTo[0] = new InternetAddress(recipient);

        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.setSubject(emailSubjectGood);
        msg.setText(emailSuccessTxt);
        Transport.send(msg);
        System.out.println("Informed voter of vote reception.");
    }

    // ============= end of sending email ====================

    private static void ProcessMsg(Message msg) throws Exception
    {
        Address[] sender = msg.getFrom();
        String subject = msg.getSubject();
        String vote[] = subject.toLowerCase().split(" ");
        if(!vote[0].equals("vote") || vote.length != 2){
            sendFailureMessage(sender[0].toString());
        } else {
            try{
            createXML(vote[1], sender[0]);
            sendConfirmMessage(sender[0].toString());
            } catch (Exception e){
                sendFailureMessage(sender[0].toString());
            }
        }
        delete();
        
    }
    private static void createXML(String candID, Address sender) throws Exception{
        try{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
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

         public static void delete() 
   {
      try 
      {
         // get the session object
         

         
         // emailSession.setDebug(true);

         // create the POP3 store object and connect with the pop server
         Store store = emailSession.getStore("pop3s");

         store.connect(POP3_HOST_NAME, emailFromAddress, password);

         // create the folder object and open it
         Folder emailFolder = store.getFolder("INBOX");
         emailFolder.open(Folder.READ_WRITE);

        
         // retrieve the messages from the folder in an array and print it
         Message[] messages = emailFolder.getMessages();
         
         
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
