import java.util.*;
public class VoterTable{
	private ArrayList<Voter> voters;
	
	public VoterTable(){
		voters = new ArrayList<Voter>();
	}
	
	public boolean addVoter(String phone_num, String candidate){
		
		for (int i = 0; i < voters.size(); i++){
			if (voters.get(i).getPhone().equals(phone_num))
				return false;
		}
		Voter newVoter = new Voter(phone_num, candidate);
		voters.add(newVoter);
		return true;
	}
	
	class Voter{
		private final String phone;
		private final String candidate;
		
		public Voter(String phone_num, String candidate_id){
			phone = new String(phone_num);
			candidate = new String(candidate_id);
		}
		
		public String getPhone(){
			return phone;
		}
	}
}