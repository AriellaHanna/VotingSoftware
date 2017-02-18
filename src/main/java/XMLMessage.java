/**
 *
 * @author adamhayes
 */
public class XMLMessage {
    private int phoneNum;
    private int candidateID;
    public XMLMessage(){
        phoneNum = -1;
        candidateID = -1;
    }
    public XMLMessage(int number, int id){
        phoneNum = number;
        candidateID = id;
    }
    public void setNum(int number){
        phoneNum = number;
    }
    public void setID(int id){
        candidateID = id;
    }
    public int getNum(){
        return phoneNum;
    }
    public int getID(){
        return candidateID;
    }
}
