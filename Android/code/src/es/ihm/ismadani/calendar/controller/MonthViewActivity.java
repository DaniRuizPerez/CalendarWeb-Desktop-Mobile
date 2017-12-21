package es.ihm.ismadani.calendar.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

















import org.ektorp.DbAccessException;
import org.ektorp.android.util.EktorpAsyncTask;

import com.others.Group;
import com.others.MyExpandableListAdapter;

import es.ihm.ismadani.calendar.R;
import es.ihm.ismadani.calendar.date.Date;
import es.ihm.ismadani.calendar.date.DateUtils;
import es.ihm.ismadani.calendar.model.EventCouchDAO;
import es.ihm.ismadani.calendar.model.EventVO;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MonthViewActivity extends Activity {
	private Date date;
	private String user;
	private String type;
	private String subjects;
	private List <String> subjectsList;
	protected CharSequence[] subjectsOptions; 
	protected boolean[] subjectsOptionsSelected;
	protected int year = 0;
	protected int month = 0;
	protected List <EventVO> events;
	private String[] commandArray;
	private String[] sureArray;
	int eventClicked;
	boolean mod;
	final static int faltaPoco = 30;
	
	private boolean isTablet(){
	    Display display = getWindowManager().getDefaultDisplay();
	    DisplayMetrics displayMetrics = new DisplayMetrics();
	    display.getMetrics(displayMetrics);

	    int width = displayMetrics.widthPixels / displayMetrics.densityDpi;
	    int height = displayMetrics.heightPixels / displayMetrics.densityDpi;

	    double screenDiagonal = Math.sqrt( width * width + height * height );
	    return (screenDiagonal >= 5.0 );
	    //doy por hecho que si es mayor de 7, es una tablet
	}

	//este método devuelve true si está ejecutándose sobre una tablet una tablet
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		commandArray=new String[] {getString(R.string.Edit),getString(R.string.Delete)};
		sureArray=new String[] {getString(R.string.Yes),getString(R.string.No)};

		String [] months = {
				getString(R.string.January),
				getString(R.string.February),	
				getString(R.string.March),
				getString(R.string.April),
				getString(R.string.May),
				getString(R.string.June),
				getString(R.string.July),
				getString(R.string.August),
				getString(R.string.September),
				getString(R.string.October),
				getString(R.string.November),
				getString(R.string.December),
		};


		super.onCreate(savedInstanceState);
		if (isTablet()){
			setContentView(R.layout.activity_main_tablet);
		}
		else {
			setContentView(R.layout.activity_main);
		}
		
		//hago que la listview se vuelva invisible y no consuma recursos, para dejarle todo el sitio al spinner
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
		listView.setVisibility(View.GONE);
		
    	Calendar cal = Calendar.getInstance();

    	//cojo los parametros que se le paso a la funcion

        Bundle bundle = getIntent().getExtras();
        
        user = bundle.getString("user");
    	subjects = bundle.getString("subjects");
    	type = bundle.getString("type");
   	 	
   	 	if (!user.equals(getString(R.string.Guest))) {
   	 	subjectsList = new ArrayList();
	   	 	String [] subjectsArray = subjects.substring(1).split("-");   
		    for (int i = 0 ; i< subjectsArray.length; i++)
		        subjectsList.add(subjectsArray[i].trim());
		    subjectsOptions = subjectsList.toArray(new CharSequence[subjectsList.size()]);
		    subjectsOptionsSelected = bundle.getBooleanArray("selected");
		    if (subjectsOptionsSelected==null) {
		    	subjectsOptionsSelected = new boolean[ subjectsOptions.length ];
			    Arrays.fill(subjectsOptionsSelected,true);
		    }
   	 	}
    	
    	  
        if(bundle.getIntArray("date") == null){//primera vez que se llama a la actividad
        	//obtengo la fecha del del dia actual
        	month = cal.get(Calendar.MONTH);
    	    year = cal.get(Calendar.YEAR);            
        }
        
        else {//rellamada a si mismo
        	//obtengo la fecha de los parametros
        	month = bundle.getIntArray("date")[0];
        	year = bundle.getIntArray("date")[1];       	
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
        } 
        date = new Date (month,year); 
        
        //actualizo vista
        TextView welcome = (TextView) findViewById(R.id.welcome);
        welcome.setText(getString(R.string.welcome) + " " + user + "!");
        
        TextView actualDate = (TextView) findViewById(R.id.mes);
        actualDate.setText(year + " , " + months[month]);


  /*Los atributos de la actividad no pueden ser accedidos en una AsyncTask si son propensos a cambios.
   * Hay que declarar una constante que tome el valor de dichas variables una vez terminadas de setear.*/
        final int cmonth = month; 
        final int cyear = year;
        final Activity cact= this;
        
  /*el siguiente fragmento de código se lanza en BackGround mediante una AsyncTask*/
        EktorpAsyncTask getUsersTask = new EktorpAsyncTask() {

            @Override
        /*Obtener la lista de eventos*/
            protected void doInBackground() {   


                EventCouchDAO eventDAO = new EventCouchDAO();
                String first = DateUtils.generateDateString(cyear, cmonth, 1);
                String last = DateUtils.generateDateString(cyear, cmonth, DateUtils.lastDayOfMonth(cyear, cmonth-1));

                //si el usuario es el invitado, se muestran todos los eventos
                if (user.equals(getString(R.string.Guest)))
                    events = eventDAO.listEventsByDate(first,last);
                else 
                	//si no, solo descargo de la BD los eventos de las asignaturas correspondientes          
                    events = eventDAO.listEventsByDate(first,last,subjectsList);    
                notification(cact);
            }

            @Override
            /*Si no ha habido excepciones en el doInBackground, se ejecuta esto*/
            protected void onSuccess() {
            	SparseArray<Group> groups = createData(cyear,cmonth, events); 
            	/*Ahora el groups está aqui para que sea accesible y modificable por la AsyncTask*/
                             
            	//hago que el listview aparezca en la pantalla
        		ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        		listView.setVisibility(View.VISIBLE);
        		MyExpandableListAdapter adapter = new MyExpandableListAdapter(cact,groups);
        		listView.setAdapter(adapter);
        		if (cact.getIntent().getExtras().get("date") != null)
        			if (type.equals("teacher"))
        				filterEvents();
        		//hago el spinner y el texto invisibles y que no consuman recursos
        		hideLoading();
            }


            @Override
            /*Esto se ejecuta si ha habido alguna excepción*/
            protected void onDbAccessException(DbAccessException dbAccessException) {
                Log.e("Calendar/MainActivity", "DbAccessException in background", dbAccessException);
                hideLoading();
                Toast.makeText(getApplicationContext(),	getString(R.string.Error), Toast.LENGTH_LONG).show();
            }
        };
        getUsersTask.execute();

	}
		
	
	public SparseArray<Group> createData(int year, int month, List<EventVO> events) {
		/*CreateData ha tenido que ser reformulado... :(*/
		SparseArray<Group> groups = new SparseArray<Group>(); //Esto será lo que devuelva CreateData
		int firstDayWeek = 1; //Primer día de la semana. Inicializado a 1
		int lastDayWeek = 7 -DateUtils.toWeekDay(DateUtils.generateDateString(year, month, 1));
		/*Con toWeekDay calculo a que día de la semana cae el 1 (0 - Lunes, 1 - Martes...). Si
		 * le restas ese número a 7, tienes en que día cae el domingo.*/
		int lastDayMonth = DateUtils.lastDayOfMonth(year,month); //Último día del mes
		boolean stop = false; //Para terminar el bucle de semanas
		int j = 0; //Número de semana
		int eventDay;
		int numEventsWeek = 0; //contador del numero de eventos de la semana actual
		int numEventsMonth = 0;
		boolean sal = false;
		String semana = null; //String que se muestra en cada semana
		EventVO etemp = null;//Una variable temporal para los eventos, inicializada en el primero
		
		while (firstDayWeek <= lastDayMonth) {
			if (lastDayWeek != firstDayWeek) //En vez de mostrar (1-1) en días que tiene la semana, muestra solo (1)
				semana = getString(R.string.week) + (j+1) +  "  (" + firstDayWeek + " - " + lastDayWeek +")";
			else 
				semana = getString(R.string.week) + (j+1) +  "  (" + firstDayWeek + ")";
			Group group = new Group(semana); //Creo el Group de esta semana
			numEventsWeek = 0;
			while (numEventsMonth < events.size() && !sal) {//Bucle de eventos de cada semana
				etemp = events.get(numEventsMonth);
				eventDay = DateUtils.dayOfDate(etemp.getDate());
				Log.d("que", "mirame " + etemp.getDate());
				if (eventDay > lastDayWeek)
					break; //Si el día del evento es mayor al último día de esta semana, el evento es de la siguiente
				numEventsWeek++;
				//notShowasdfasdfasdf es el separador entre lo que se muesta primero y lo que se oculta
				String cadena =numEventsMonth + "\n "+"notShowasdfasdfasdf" + getString(R.string.day) + eventDay + "\n"+ etemp.getDescription() +
				"notShowasdfasdfasdf" +  
				"\n" + getString(R.string.Creator) + etemp.getCreator() +
				"\n" + getString(R.string.Type) + etemp.getType() +
				"\n" + getString(R.string.Tags)+ etemp.getTags();
				numEventsMonth++;
				group.children.add(cadena);
				
			}
			group.string = group.string + "\n (" + numEventsWeek + " " +  getString(R.string.events) +  ")"; 
			groups.append(j, group);
			
			/*Comprobaciones/seteos para la siguiente iteración*/
			firstDayWeek=lastDayWeek+1; //Si lastDay es 8, el primer día de la siguiente es 9...
			lastDayWeek = (lastDayWeek+7 > lastDayMonth)?lastDayMonth:lastDayWeek+7;//Avanzo 7 dias (o me pongo al ultimo si me paso)
			j +=1; //El importantísimo +1
		}
	   return groups;  
	}
	
	public void previousMonth (View view){
		//cambia al mes anterior
		 Intent intent = new Intent(this, MonthViewActivity.class);
	     completeIntent(intent, date.previous_month(), user, subjects, type);
		 startActivity(intent);
	}
	
	public void nextMonth (View view){
		//cambia al mes siguiente
		 Intent intent = new Intent(this, MonthViewActivity.class);
	     completeIntent(intent, date.next_month(), user, subjects, type);
		 startActivity(intent);
	}
	
	// este método solo esta accesibles si es una tablet
	public void previousYear (View view){
		//cambia al mes anterior
		 Intent intent = new Intent(this, MonthViewActivity.class);
	     completeIntent(intent, date.previous_year(), user, subjects, type);
		 startActivity(intent);
	}
	
	// este método solo esta accesibles si es una tablet
	public void nextYear (View view){
		//cambia al mes siguiente
		 Intent intent = new Intent(this, MonthViewActivity.class);
	     completeIntent(intent, date.next_year(), user, subjects, type);
		 startActivity(intent);
	}
	
	
	public void completeIntent (Intent intent, int[] date, String user, String subjects, String type) {
		intent.putExtra("date",date);
	    intent.putExtra("user",user);
	    intent.putExtra("subjects",subjects);
	    intent.putExtra("type", type);
	    intent.putExtra("selected", subjectsOptionsSelected);
	}
	

	/*Gestión del menu*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		Bundle bundle = getIntent().getExtras();
		type = bundle.getString("type");
		if (type.equals("teacher"))
			getMenuInflater().inflate(R.menu.complete_menu, menu);
		else
			getMenuInflater().inflate(R.menu.limited_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.action_settings:
	    		createAboutFromMenu();
	    		return true;
	        case R.id.logoutMenu:
	        	createLoginFromMenu();
	        	return true;
	        case R.id.chooseSubjectsMenu:
	        	onCreateDialog(0).show();
	        	return true;
	        case R.id.AddMenu:
	        	
	        	/*
				 * 
				 * 
				 *     AÑADIR
				 * 
				 * 
				 * */
	        	onCreateDialog(1).show();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	   
	public void createAboutFromMenu() {
		Intent intent = new Intent(this, AboutMenuActivity.class);
		startActivity(intent);
	}
	
	public void createLoginFromMenu() {
		Intent intent = new Intent(this, LoginActivity.class);
	    intent.putExtra("logout",true);
		startActivity(intent);
	}
	

	/*Historia 2 (detalles de evento)*/
	public void eventDetails (View view){
		//cambia al mes siguiente
		 Intent intent = new Intent(this, MonthViewActivity.class);
	     intent.putExtra("date",date.next_month());
		 startActivity(intent);
	}
	
	/*Hace que se salga de la aplicación al pulsar atrás*/
	public void onBackPressed() {
		   Intent intent = new Intent(Intent.ACTION_MAIN);
		   intent.addCategory(Intent.CATEGORY_HOME);
		   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   startActivity(intent);
		 }
	
	public void hideLoading() {
		ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar1);
		spinner.setVisibility(View.GONE);
		TextView loading = (TextView) findViewById(R.id.loading);
		loading.setVisibility(View.GONE);
	}
	
	
	
	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener
	{
		public void onClick( DialogInterface dialog, int clicked, boolean selected )
		{	
			Log.d( "ME", "sel " + subjectsOptions[ clicked ] + " selected: " + selected );
		}
	}
	

	public class DialogButtonClickHandler implements DialogInterface.OnClickListener
	{
		public void onClick( DialogInterface dialog, int clicked )
		{
			switch( clicked )
			{
				case DialogInterface.BUTTON_POSITIVE:
					filterEvents();
					break;
			}
		}
	}
	
	protected void filterEvents(){
		List<EventVO> filteredEvents = new ArrayList<EventVO>();
		boolean[] eventChecked = new boolean[events.size()];
		boolean result = false;
		if (events != null) {
			
			for (EventVO e:events) {
				result = false;
				for (int i = 0; i < subjectsOptions.length; i++) {
					if (subjectsOptionsSelected[i])
						if (e.getTags().contains(subjectsOptions[i])) {
							result = true;
							//break;
						}
				}
				if (result)
					filteredEvents.add(e);
					
			}
			refreshList(filteredEvents);
		}
	}
	
	public List<String> validateTags(String chain){

		List <String> tagsFinal = new ArrayList<String>();
		String []  tags = chain.split(",");

		boolean valido = false;
		for(int i = 0; i< tags.length ; i++){
			String tag = tags[i].trim();
			if (!tag.equals(",") && !tag.equals("")){
				 tagsFinal.add("#" + tag);	
			}
		}
		for(String tag : tagsFinal){
			if (subjectsList.contains(tag)){
				valido = true;
			}
		}
		if (valido == false){
			tagsFinal = null;
		}
		return tagsFinal;
	}
	
	public void onclicke(int count , String allText) {
		//count es el numero del evento
        Toast.makeText(getApplicationContext(),	 allText, Toast.LENGTH_LONG).show();
   	}
	
	public void onLongclicke(int count) {
		//count es el numero del evento
		if (type.equals("teacher"))
			showDialogOptions(count);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {	
		case 0:
			return 
					new AlertDialog.Builder( this )
			        	.setTitle(getString(R.string.chooseSubjectsMenu))
			        	.setMultiChoiceItems( subjectsOptions, subjectsOptionsSelected, new DialogSelectionClickHandler() )
			        	.setPositiveButton( "OK", new DialogButtonClickHandler() )
			        	.create();
		case 1:
			mod = false;
			return new DatePickerDialog(this, datePickerListener, year, month, 1);
		case 2:
			mod = true;
			int day = Integer.parseInt(events.get(eventClicked).getDate().split("-")[2]);
			return new DatePickerDialog(this, datePickerListener,year, month, day);
		}
		return null;
	}
	
	
	//crea el datepicker y si se le da a siguiente, crea el sigueinte dialogo pasandole los parametros correspondientes
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

		//a este método se le llama cuando se clica en el boton done del datepicker
		public void onDateSet(DatePicker view, int selectedYear,int selectedMonth, int selectedDay) {
			String op = getString(R.string.Add);
			if (mod)
				op = getString(R.string.Modify);
			String date = DateUtils.generateDateString(selectedYear, selectedMonth, selectedDay);
			DatePicker dpResult = new DatePicker(getApplicationContext());
			dpResult.init(selectedYear, selectedMonth, selectedDay, null);         
            createDialogAddDescription(date,op);
            
		}
	};
	
	
	//crea el dialogo de descripcion y si se le da a siguiente, crea el sigueinte dialogo pasandole los parametros correspondientes
	public void createDialogAddDescription(final String date, final String op){
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(op);
        alert.setMessage(getString(R.string.Description));
        final EditText eTDescription = new EditText(this);
        alert.setView(eTDescription);
	
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			String value = eTDescription.getText().toString();
			if (value == null)
				value = "Evento";
			createDialogAddTags(date, value, op);
		  }
		});

		alert.show();
		
	}

	//crea el ultimo dialogo. se le pasan todos los parámetros anteriores, y hace lo que sea con ellos
	public void createDialogAddTags(final String date,final String desc, final String op){
		//ismodifying te dice si se llama desde el crear evento o modificarlo
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(op);
		alert.setMessage(getString(R.string.Tags_window));
        final EditText etTags = new EditText(this);
        alert.setView(etTags);
	
		alert.setPositiveButton(getString(R.string.Create), new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			Editable value = etTags.getText();
			List <String> tags = validateTags(value.toString());
			
			if (tags == null) {
	            Toast.makeText(getApplicationContext(),	getText(R.string.invalidTags), Toast.LENGTH_LONG).show();
	            createDialogAddTags(date, desc, op);
			}
			else {
				EventVO etemp;
				if (op == getString(R.string.Modify)) {
					etemp = events.get(eventClicked);
					Log.d("que", "estoesunamierdaaa " + etemp.getDescription());
				}
				else
					etemp = new EventVO();
				etemp.setCreator(user);
				etemp.setDate(date);
				etemp.setDescription(desc);
				etemp.setTags(tags);
				final EventVO e = etemp;
				EktorpAsyncTask getUsersTask = new EktorpAsyncTask() {
	            @Override
	            protected void doInBackground() {  
	            	EventCouchDAO ecDAO = new EventCouchDAO(); 
					if (op == getString(R.string.Modify)) {
						ecDAO.update(e);
					}
					else
						ecDAO.add(e);
	            }

	            @Override
	            /*Si no ha habido excepciones en el doInBackground, se ejecuta esto*/
	            protected void onSuccess(){
	            	if (!mod) addInPosition(e, events);
	            	refreshList(events);
	            	Toast.makeText(getApplicationContext(),	getString(R.string.succesOnOperation), Toast.LENGTH_LONG).show();
	            }

	            @Override
	            /*Esto se ejecuta si ha habido alguna excepción*/
	            protected void onDbAccessException(DbAccessException dbAccessException) {
	                hideLoading();
	                Toast.makeText(getApplicationContext(),	getString(R.string.Error), Toast.LENGTH_LONG).show();
	            }
	        };
	        getUsersTask.execute();	
			}
		}
		});
		alert.show();
	}
	
	
	private void refreshList(List<EventVO> list) {
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
		MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,createData(year, month, list));
		listView.setAdapter(adapter);
	}
	
	private void addInPosition (EventVO e, List<EventVO> l) {
		
		if (l.size() == 0)
			l.add(e); //mirar indices
		else {
			EventVO t;
			boolean added = false;;
	    	for (int i = 0; i < events.size(); i++) {
	    		Log.d("que", "machadaaaa "+l.toString());
	    		t = events.get(i);
	    		if (DateUtils.compareData(t.getDate(), e.getDate()) >=0) {
	    			events.add(i, e);
	    			added = true;
	    			break;
	    		}
	    	}
	    	if (!added)
	    		l.add(e);
		}
		
	}
	
	
	private void showDialogOptions(final int count){
		android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this); 		
		builder.setItems(commandArray, new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int which) { 
				switch (which) {
					case 0 :		
						onCreateDialog(2).show();
						break;
					case 1 :
						showDialogSure(count);
						break;
				}		
				dialog.dismiss(); 
			} 
		}); 
		
		AlertDialog alert = builder.create(); 
		alert.show(); 
	}
	

	private void showDialogSure(final int eventClicked){
		android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		builder.setTitle(getString(R.string.Sure));
		builder.setItems(sureArray, new DialogInterface.OnClickListener() {
	    
			public void onClick(DialogInterface dialog, int which) { 
				switch (which) {
					case 0 :
						eraseEvent(eventClicked);
						break;
				}		
				dialog.dismiss(); 
			} 
		}); 
		
		AlertDialog alert = builder.create(); 
		alert.show(); 
	}
	
	public void eraseEvent(final int eventClicked){
				EktorpAsyncTask getUsersTask = new EktorpAsyncTask() {
	            @Override
	            protected void doInBackground() {  
	            	EventVO e = events.get(eventClicked);
	            	EventCouchDAO ecDAO = new EventCouchDAO(); 
					ecDAO.remove(e);
	            }

	            @Override
	            /*Si no ha habido excepciones en el doInBackground, se ejecuta esto*/
	            protected void onSuccess(){
	            	events.remove(eventClicked);
	            	refreshList(events);
	            	Toast.makeText(getApplicationContext(),	getString(R.string.succesOnOperation), Toast.LENGTH_LONG).show();
	            }

	            @Override
	            /*Esto se ejecuta si ha habido alguna excepción*/
	            protected void onDbAccessException(DbAccessException dbAccessException) {
	                hideLoading();
	                Toast.makeText(getApplicationContext(),	getString(R.string.Error), Toast.LENGTH_LONG).show();
	            }
	        };
	        getUsersTask.execute();	
	}
	
	
	private void showDialogSuccesOrError(int dif){
			android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this); 
			if (dif == 1){
				//succes
				builder.setTitle("ERROR");
			}
			else {
				//error	
				builder.setTitle("EXITO");
			}
			String [] ok = {"ok"};
			builder.setItems(ok, new DialogInterface.OnClickListener() {
		    
				public void onClick(DialogInterface dialog, int which) { 
					switch (which) {
						case 1 :
							break;
					}		
					dialog.dismiss(); 
				} 
			}); 
			
			AlertDialog alert = builder.create(); 
			alert.show(); 
		}
	
	// este método solo esta accesibles si es una tablet
	public void changeToTarget(View w){
		int month = 0;
		switch (w.getId()){
			case R.id.m1 :
				month = 0;
				break;
			case R.id.m2 :
				month = 1;
				break;
			case R.id.m3 :
				month = 2;
				break;
			case R.id.m4 :
				month = 3;
				break;
			case R.id.m5 :
				month = 4;
				break;
			case R.id.m6 :
				month = 5;
				break;
			case R.id.m7 :
				month = 6;
				break;
			case R.id.m8 :
				month = 7;
				break;
			case R.id.m9 :
				month = 8;
				break;
			case R.id.m10 :
				month = 9;
				break;
			case R.id.m11 :
				month = 10;
				break;
			case R.id.m12 :
				month = 11;	
				break;
		}
		
		Intent intent = new Intent(this, MonthViewActivity.class);
	    completeIntent(intent, date.target_month(month), user, subjects, type);
		startActivity(intent);
	}
	
	protected void notification (Context c) {
		if (getIntent().getExtras().get("date") != null)
			return;
		Calendar ca = Calendar.getInstance();
		int neventos = 0;
		String today = DateUtils.generateDateString(ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH));
		String limit = DateUtils.travelInTime(today, faltaPoco);
		Log.d("que", " a "+ limit);
		for (EventVO e:events) {
			if (!(DateUtils.compareData(today, e.getDate()) >= 0)) //Si el evento NO ha pasado
				if (DateUtils.compareData(limit, e.getDate())>=0)
					neventos+=1;
				else break;
		}
		Notification noti = new Notification.Builder(c)
         .setContentTitle("Calendario")
         .setContentText("Hay " + neventos + " eventos en los próximos " + faltaPoco + " días")
         .setSmallIcon(R.drawable.ic_launcher)
         .setAutoCancel(true)
         .build();
		 noti.flags |= Notification.FLAG_AUTO_CANCEL;
		 NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
		 nm.notify(0, noti);
		 
	}
		

}
	
	
	
	
	
	
