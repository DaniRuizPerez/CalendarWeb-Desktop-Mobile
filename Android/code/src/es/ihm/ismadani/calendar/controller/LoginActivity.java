package es.ihm.ismadani.calendar.controller;


import java.util.List;

import org.ektorp.DbAccessException;
import org.ektorp.android.util.EktorpAsyncTask;

import es.ihm.ismadani.calendar.R;
import es.ihm.ismadani.calendar.model.UserDAO;
import es.ihm.ismadani.calendar.model.UserVO;
import es.ihm.ismadani.calendar.utils.LoginManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

public class LoginActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		Bundle bundle = getIntent().getExtras();
		LoginManager lm = new LoginManager(this);
		try {
			if (bundle.getBoolean("logout",true))
				lm.logOut();
		} catch (NullPointerException npe){}
		if (lm.hasUserLogged()) {
			startActivity(makeIntent(this, lm));
		}
		else
			setContentView(R.layout.activity_login);
	}

	
	public void onLoginClicked (View view){
		
		//Pongo el spinner a funcionar
		TextView loading = (TextView) findViewById(R.id.loading);
		ProgressBar spinnerLoading = (ProgressBar) findViewById(R.id.spinnerLogin);
		loading.setVisibility(View.VISIBLE);
		spinnerLoading.setVisibility(View.VISIBLE);
		
		//obtengo los datos del usuario del edittext y creo el loginmanager
		EditText editUser = (EditText) findViewById(R.id.editUser);
		final String idUser = editUser.getText().toString();
		final LoginManager loginManager = new LoginManager(this);
		
		//si he llamado a esta actividad desde el loginmanager tengo que cerrar la sesion anterior
        try{
        	Bundle bundle = getIntent().getExtras();
            if (bundle.getBoolean("logout") == true){
            	loginManager.logOut();
            }
        }
        catch (Exception e){ 	
        }
		
		
		final Context context = this;
		EktorpAsyncTask getUsersTask = new EktorpAsyncTask() {
			List <UserVO> userList = null;
            @Override
        /*Obtener la lista de eventos*/
            protected void doInBackground() {  
        		 UserDAO userDAO = new UserDAO();
        		 userList = userDAO.getAll();
            }

            @Override
            /*Si no ha habido excepciones en el doInBackground, se ejecuta esto*/
            protected void onSuccess() {
     			TextView loading = (TextView) findViewById(R.id.loading);
    			ProgressBar spinnerLoading = (ProgressBar) findViewById(R.id.spinnerLogin);
    			
    			//compruebo si el usuario esta o no en la bd y ejecuto acciones correspondientes
            	if (loginManager.logIn(userList, idUser) == true){   	
            		Toast.makeText(context, "AUTENTIFICACION EXITOSA",Toast.LENGTH_SHORT).show();
       
        			loading.setVisibility(View.GONE);
        			spinnerLoading.setVisibility(View.GONE);	   

        			//le paso al monthviewactivity las asignaturas y el nombre de usuario
        			startActivity(makeIntent(context, loginManager));	
        		}
        		else {
        			Toast.makeText(context, "USUARIO O COTNRASEÑA INCORRECTOS",Toast.LENGTH_SHORT).show();
        			loading.setVisibility(View.GONE);
        			spinnerLoading.setVisibility(View.GONE);
        		}	
            }

            @Override
            /*Esto se ejecuta si ha habido alguna excepción*/
            protected void onDbAccessException(DbAccessException dbAccessException) {
            	//se para el spinner
        		TextView loading = (TextView) findViewById(R.id.loading);
        		ProgressBar spinnerLoading = (ProgressBar) findViewById(R.id.spinnerLogin);
        		loading.setVisibility(View.GONE);
        		spinnerLoading.setVisibility(View.GONE);
        		
            	//logs y tostadas
                Log.e("Calendar/MainActivity", "DbAccessException in background", dbAccessException);
                Toast.makeText(getApplicationContext(),
                        "Error establishing a server connection!", Toast.LENGTH_LONG).show();
                
            }
        };
        getUsersTask.execute();
        
	}
		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	        Intent intent = new Intent(this, AboutMenuActivity.class);
	        this.startActivity(intent);
	        return true;
	}
	
	//hago que salga de la aplicacion al darle atrás
	public void onBackPressed() {
		   Intent intent = new Intent(Intent.ACTION_MAIN);
		   intent.addCategory(Intent.CATEGORY_HOME);
		   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   startActivity(intent);
		 }
	
	//cuando se activa el boton de invitado, se pasa como nombre de usuario
	public void onGestClicked(View view) {
		Intent intent = new Intent(this, MonthViewActivity.class);
	    intent.putExtra("user",getString(R.string.Guest));
	    intent.putExtra("subjects", "");
	    intent.putExtra("type", "student");
		startActivity(intent);	
	}
	
	
	public Intent makeIntent(Context context, LoginManager loginManager) {
		Intent intent = new Intent(context, MonthViewActivity.class);
	    intent.putExtra("subjects",loginManager.getSubjectsOfUserLogged());
	    intent.putExtra("user",loginManager.getUserLogged());
	    intent.putExtra("type", loginManager.getTypeOfUserLogged());
		return intent; 
	}
}

