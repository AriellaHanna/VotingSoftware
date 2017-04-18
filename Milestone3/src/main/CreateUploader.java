
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
    public static boolean canSend = true;
    private static int port = 993;
    private static int conPort = 53217;
    // scope of this component
    private static final String SCOPE = "SIS.Scope1";
	// name of this component
    private static final String NAME = "CreateUploader";
    // messages types that can be handled by this component
    private static final List<String> TYPES = new ArrayList<String>(
        Arrays.asList(new String[] { "Alert", "Emergency", "Confirm", "Setting" }));

  //  private static UploaderReading reading = new UploaderReading();
    // message writer
    static MsgEncoder encoder;
    // message reader
    static MsgDecoder decoder;
    // variables for sending emails
    static final String SMTP_HOST_NAME = "smtp.gmail.com";
    static final String SMTP_PORT = "465";
    static final String POP3_HOST_NAME = "pop.gmail.com";
    static final String POP3_PORT = "995";
    static final String emailSuccessTxt = "We have received your input.\nThanks for voting!";
    static final String emailFailureTxt = "Unfortunately, we were unable to understand your email, and no votes were counted."
            + "\nTo cast a vote, send an email with 'vote (candidate #)' in the subject line to this account.\n";

    static final String emailSubjectGood = "Vote Confirmation";
    static final String emailSubjectBad = "Error Processing Vote";
    static final String emailFromAddress = "hayeshanna1631@gmail.com";
    static final String password = "chang1631";
    static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    static Properties props = new Properties();
    static Session emailSession;  
    static final String cands = "1,2,3,4,5";
    static final String adminPass = "admin";
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
            try{
                // bind the message reader to inputstream of the socket
            decoder = new MsgDecoder(universal.getInputStream());
                // bind the message writer to outputstream of the socket
            encoder = new MsgEncoder(universal.getOutputStream());
            while(!registerComponent());
            KeyValueList conn = new KeyValueList();
            conn.putPair("Scope", SCOPE);
            conn.putPair("MessageType", "Connect");
	    conn.putPair("Role", "Basic");
            conn.putPair("Name", NAME);
            encoder.sendMsg(conn);
            
            while(true){
            try
            {
                                // try to establish a connection to SISServer

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
           
        } catch(Exception e){
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
                try
                {
                    universal = connect();
                }
                catch (IOException e1)
                {
                }
                
        }
        }
    }
    static boolean registerComponent() {
        try{
            KeyValueList reg = new KeyValueList();
            reg.putPair("MessageType","Register");
            reg.putPair("MsgId","21");
            reg.putPair("Scope",SCOPE);
            reg.putPair("Role","Basic");
            reg.putPair("Description","Create Uploader Component");
            reg.putPair("Passcode",adminPass);
            reg.putPair("SecurityLevel","3");
            reg.putPair("SourceCode","VS.jar");
            reg.putPair("Component Description","CreateUploader polls for votes in the gmail inbox"
                + " and then parses the email and sends a message to createVoting.");
            encoder.sendMsg(reg);
            return true;
        } catch (Exception e){
            return false;
        }   
    }

    /*
     * Method for sending email for vote registration failure
     */
    static void sendFailureMessage(String recipient, String why) throws MessagingException{
                boolean debug = false;
       //generate an email session to construct a response email for users
        System.out.println("Generating Failure message...");
        String errorExplain = "The reason for the error in vote tallying was as follows:\n"
                + why + "\nPlease note that only one vote can be counted per email account.";
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
 
        msg.setText(emailFailureTxt + errorExplain);
        //send the message
        Transport.send(msg);
        System.out.println("Informed user of error in vote tally.");
    }
    /*
     * Method for sending email for vote registration message creation success
     */
        /*
     * used for connect(reconnect) to SISServer
     */
    static Socket connect() throws IOException
    {
        Socket socket = new Socket("127.0.0.1", conPort);
        return socket;
    }
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
    private static void killVoting()throws Exception{
        KeyValueList message = new KeyValueList();
        genMessage(message);
                message.putPair("MessageType","Emergency");
        message.putPair("MsgId","22");
        message.putPair("Passcode",adminPass);
        encoder.sendMsg(message);
    }
    private static void endVoting(String numWinners)throws Exception{
        KeyValueList message = new KeyValueList();
        message.putPair("MessageType","Setting");
        genMessage(message);
        message.putPair("MsgId","702");
        message.putPair("Passcode",adminPass);
        message.putPair("N",numWinners);
        encoder.sendMsg(message);
        message.removePair("MsgId");
        message.putPair("MsgId","25");
        encoder.sendMsg(message);
    }
    private static void initVoting()throws Exception{
        KeyValueList message = new KeyValueList();
        genMessage(message);
        message.putPair("MessageType","Setting");
        message.putPair("MsgId","24");
        message.putPair("Passcode",adminPass);
        message.putPair("CandidateList",cands);
        encoder.sendMsg(message);
        KeyValueList msg = decoder.getMsg();
        
        message.removePair("MsgId");
        message.putPair("MsgId","703");
        encoder.sendMsg(message);
        
    }
    private static void genMessage(KeyValueList message){

                message.putPair("Sender",NAME);
                message.putPair("Scope",SCOPE);
                message.putPair("Receiver","VotingSoftware");
                
    }
    private static KeyValueList castVote(String candID, Address sender) throws Exception{
        String source = sender.toString();
        KeyValueList message = new KeyValueList();
        KeyValueList response;
        genMessage(message);

        
        message.putPair("MessageType","Reading");
        message.putPair("MsgId","701");
        message.putPair("Description","Cast Vote");
        message.putPair("VoterPhoneNo",source);
        message.putPair("CandidateID",candID);
        encoder.sendMsg(message);
        System.out.println("Wait for response...");
        response = decoder.getMsg();
        System.out.println(response.toString());
        if(response.getValue("MessageType").equals("ERROR")){
            throw new Exception();
        }
        return response;
    }
    private static void ProcessMsg(Message msg) throws Exception
    {
        //process the message,
        //meaning check the subject to see if it contains 
        //the correct format to be counted as a vote
        System.out.println("Processing message...");
        Address[] sender = msg.getFrom();
        String subject = msg.getSubject();
        String vote[] = subject.toLowerCase().split(" ");
        System.out.println("got subject line");
        //subject must contain vote
        if(!vote[0].equals("vote") || vote.length > 3){
            System.out.println("Error in message construction");
            sendFailureMessage(sender[0].toString(), "Bad subject line format");
        } else {
            if(vote[1].equals("init")){
                initVoting();
            } else if (vote[1].equals("end")){
                endVoting(vote[2]);
            } else if (vote[1].equals("kill")){
                killVoting();
                System.out.println("ending email parsing...");
                System.exit(1);
            } else {
            try{
                KeyValueList response = castVote(vote[1], sender[0]);
                if(!response.getValue("error").equals("none")){
                //duplicate email detected, reject vote
                    System.out.println("Error detected");
                    sendFailureMessage(sender[0].toString(), response.getValue("error"));
                } else {
                    System.out.println("XML message created successfully");
                    sendConfirmMessage(sender[0].toString());
                }
            } catch (Exception e){
                System.out.println("Error in message construction");
                sendFailureMessage(sender[0].toString(), e.toString());
            }
        }
        }
        //remove the email from the program's view so that 
        //it will only work on the oldest unprocessed email in the inbox
        delete();
    }

    //remove the most recently processed email in the inbox from the programs view
    //so that repeat emails will not be considered
    
    //SISServer waits for a response from teh SIS server,
    //if there is no response for designated amount of time, dont count vote

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
