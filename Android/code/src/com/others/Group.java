package com.others;



import java.util.ArrayList;
import java.util.List;
//Esta clase se usa para generar el expandableList
public class Group {

	public String string;
	public final List<String> children = new ArrayList<String>();

	public Group(String string) {
		this.string = string;
	}

}