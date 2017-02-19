/**
 *
 * @author adamhayes
 */
public class XMLMessage {
    private String phoneNum;
    private String candidateID;
    public XMLMessage(){
        phoneNum = null;
        candidateID = null;
    }
    public XMLMessage(String number, String id){
        phoneNum = number;
        candidateID = id;
    }
    public void setNum(String number){
        phoneNum = number;
    }
    public void setID(String id){
        candidateID = id;
    }
    public String getNum(){
        return phoneNum;
    }
    public String getID(){
        return candidateID;
    }
}
