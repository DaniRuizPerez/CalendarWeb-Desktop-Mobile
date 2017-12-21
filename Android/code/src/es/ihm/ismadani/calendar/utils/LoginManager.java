package es.ihm.ismadani.calendar.utils;

import java.util.List;

import es.ihm.ismadani.calendar.model.UserVO;
import android.content.Context;
import android.content.SharedPreferences;

public class LoginManager {
	private SharedPreferences pref;
	private SharedPreferences.Editor prefEditor;
	private String SUBJECTS_KEY = "subjects";
	private String USER_KEY = "user";
	private String TYPE_KEY = "type";
	public enum userType {STUDENT, TEACHER};
	private String foo_value = "If you're seeing this, pray";
	
	
	public LoginManager(Context c) {
		this.pref = c.getSharedPreferences("log.conf", c.MODE_PRIVATE);
		this.prefEditor = pref.edit();
	}
	
	public boolean logIn(List<UserVO> knownUsers, String userLogin) {
		boolean encontrado = false;
		String type=null;
		List<String> subjects = null;
		String subjectsString = "";
		for (UserVO user:knownUsers)
			if (user.getDescription().equals(userLogin)) {
				encontrado = true;
				type = user.getSubtype();
				subjects = user.getSubjects();
				break;
			}
		if (encontrado) {
			this.prefEditor.putString(this.USER_KEY, userLogin);
			this.prefEditor.putString(this.TYPE_KEY, type);
			for (String s:subjects) 
				subjectsString += "- " + s;
			this.prefEditor.putString(this.SUBJECTS_KEY, subjectsString);
			this.prefEditor.commit();
		}
		return encontrado;		
	}
	
	public void logOut() {
		this.prefEditor.clear();
		this.prefEditor.commit();
	}
	
	public boolean hasUserLogged() {
		return (this.pref.getString(this.USER_KEY, null) != null);
	}
	
	public String getUserLogged() {
		return this.pref.getString(this.USER_KEY, this.foo_value);
	}
	
	public String getTypeOfUserLogged() {
		return this.pref.getString(this.TYPE_KEY, this.foo_value);
	}
	
	public String getSubjectsOfUserLogged() {
		return this.pref.getString(this.SUBJECTS_KEY, this.foo_value);
	}
	
}