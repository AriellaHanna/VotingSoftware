import java.util.*;
public class TallyTable{
	private ArrayList<Entry> entries;
	
	//Empty constructor
	public TallyTable(){
		entries = new ArrayList<Entry>();
	}
	
	//Constructor with number of candidates
	public TallyTable(int n){
		entries = new ArrayList<Entry>(n);
	}
	
	/**
		Adds candidate Id to table
		If id is not already in table, add it and return true
		If id is in table, return false
		@param id Candidate ID to add
	*/
	public boolean addId(String id){
		if (entries.isEmpty()){
			Entry newEntry = new Entry(id);
			entries.add(newEntry);
			return true;
		}
		else{
			for (int i = 0; i < entries.size(); i++){
				if (entries.get(i).getId().equals(id))
					return false;
			}
			Entry newEntry = new Entry(id);
			entries.add(newEntry);
			return true;
		}
	}
	
	public String toString(){
		String result = new String();
		for (int i = 0; i < entries.size(); i++){
			result += entries.get(i).toString() + ";";
		}
		return result;
	}
	/**
		Votes for a candidate
		If ID is already in table, vote is added and returns true
		If ID is not in table, returns false
		@param id Candidate Id to vote for
	*/
	public boolean addVote(String newId){
		for (int i = 0; i < entries.size(); i++){
			if (entries.get(i).getId().equals(newId)){
				entries.get(i).addVote();
				return true;
			}
		}
		return false;
	}

	/**
		Makes a string of all of the table entries
		@return String of table entries
	*/
	public String getResults(int n){
		String result = new String();
		Entry winner = new Entry("-1");
		String[] winnerStrings = new String[n];
		Entry[] winners = new Entry[n];
		for (int i = 0; i < n; i++){
			winners[i] = winner;
		}
		for (int j = 0;j <entries.size(); j++){
			if (winners[0].getCount() < entries.get(j).getCount()){
				winners[0] = entries.get(j);
			}
			
		}
		winnerStrings[0] = winners[0].toString();
		if (n > 1){
			for (int i = 1; i < n; i++){
				for (int j = 0; j <entries.size(); j++){
					if (entries.get(j) != null){
						if (winners[i].getCount() >= entries.get(j).getCount() && !winners[i-1].equals(entries.get(j).toString())){
							winners[i] = entries.get(j);
						}
					}
					
				}
				winnerStrings[i]=winners[i].toString();
			}
		}
		for (int i = 0; i <n-1; i++){
			result += winnerStrings[i] + ";";
		}
		//result += winnerStrings[n-1];
		result = toString();
		return result;
	}
	
	//Class for entries in tally table
	class Entry{
		private String id; //ContestantId
		private int count; //Number of votes
		
		//Constructor
		public Entry(String name){
			id = new String(name);
			count = 0;	
		}
		
		public String getId(){
			return id;
		}
		
		//Add a vote
		public void addVote(){
			count++;
		}	
		
		public String toString(){
			String result = (id + "," + count);
			return result;
		}
		
		public int getCount(){
			return count;
		}
	}
}	