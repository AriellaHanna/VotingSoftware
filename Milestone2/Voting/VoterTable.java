import java.util.*;
public class VoterTable{
	private ArrayList<Voter> voters;
	private TallyTable tt;
	
	public VoterTable(TallyTable t_table){
		voters = new ArrayList<Voter>();
		tt = new TallyTable(tt);
	}
	
	public boolean addVoter(String phone_num, String candidate){
		
		//Check if valid candidate ID, if not, don't add voter
		for (String s : tt.getCandidates()){
			if (!s.equals(candidate))
				return false;
		}
		
		//Check if voter has already voted
		for (int i = 0; i < voters.size(); i++){
			if (voters.get(i).getPhone().equals(phone_num))
				return false;
		}
		
		//New vote
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