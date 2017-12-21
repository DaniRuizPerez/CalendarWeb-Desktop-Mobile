package es.ihm.ismadani.calendar.date;


public class Date{

	private int year;
	private int index; //month index
	
	public Date (int index, int year) {
		this.index = index;
		this.year = year;
	}
	
	public int [] next_month(){
		if (index != 11)
			index ++;
		else {
			index = 0;
			year++;
		}
			int [] toReturn = new int [2];
			toReturn[0] = index;
			toReturn[1] = year;
		return toReturn;
		
	}
	
	public int [] previous_month(){
		if (index != 0)
			index --;
		else {
			index = 11;
			year--;
		}
		int [] toReturn = new int [2];
		toReturn[0] = index;
		toReturn[1] = year;
	return toReturn;
		
	}
	
	public int [] target_month(int month){
		int [] toReturn = new int [2];
		toReturn[0] = month;
		toReturn[1] = year;
	return toReturn;
	}
	

	public int [] next_year(){
		year++;
		int [] toReturn = new int [2];
		toReturn[0] = index;
		toReturn[1] = year;
	return toReturn;
	}
	

	public int [] previous_year(){
		year--;
		int [] toReturn = new int [2];
		toReturn[0] = index;
		toReturn[1] = year;
	return toReturn;
	}
}
