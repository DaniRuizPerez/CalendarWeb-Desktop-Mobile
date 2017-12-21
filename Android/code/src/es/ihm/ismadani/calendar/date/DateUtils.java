package es.ihm.ismadani.calendar.date;

import android.util.Log;

/*Clase que incorpora algunas funciones útiles para trabajar con las fechas*/


public class DateUtils {
  /**
   *
   * Nota: el algoritmo de Zeller (que se usa en el toWeekDay) tiene una cosa "rara", que es que considera
   * el sábado el primer día de la semana. La idea inicial de toWeekDay era devolver un String con el nombre
   * del día de la semana, pero me resultó más cómodo que simplemente devolviera un entero simbolizando que día
   * de la semana es. Como no fui capaz de sacar una expresión de "mod algo", hice la siguiente gitanada:
   * 
   *  - toWeekDay me devuelve un número de 0 a 6, tal que:
   *  	0 -> Sabado
   *  	1 -> Domingo
   *  	.
   *  	.
   *  	6 -> Viernes
   *  - Tengo el array MODARRAY, tal que:
   *  	Posicion 0: 5
   *  	Posicion 1: 6
   *  	.
   *  	.
   *  	Posicion 6: 4
   *  
   *  - De este modulo, si indexo el cutremodulo en funcion de toWeekDay, ya me sale el equivalemnte
   *  a 0 Lunes, 1 Martes...
   */
  /*final static String[] DAYS_OF_WEEK = {
          "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
          "Friday"
      };*/


  public static int toWeekDay(String date) {
	  /*Convierte una fecha yyyy-mm-dd en su correspondiente día de la semana mediante el algoritmo de Zeller.
	   * Nota: da el equivalente al xxxx-03-01 si le pasas el xxxx-02-29 de un no bisiesto*/
	  int[] MODARRAY = {5,6,0,1,2,3,4,};
      String[] atoms = date.split("-");
      int q = Integer.parseInt(atoms[2]);
      int m = Integer.parseInt(atoms[1]);
      int y = Integer.parseInt(atoms[0]);

      if (m < 3) {
          m += 12;
          y -= 1;
      }

      int k = y % 100;
      int j = y / 100;

      int day = ((q + (((m + 1) * 26) / 10) + k + (k / 4) + (j / 4)) +
          (5 * j)) % 7;

      return MODARRAY[day];
  }
  
  public static boolean leapYear(int year) {
	  /*Comprobar si un año es bisiesto*/
	  return  ((( year % 4 == 0 ) && ( year % 100 != 0 )) || ( year % 400 == 0 ));
  }
  
  public static int lastDayOfMonth(int year, int month) { 
	  /*Devuelve el último día del mes month. Si el año year es bisiesto, se tiene en cuenta para devolver
	   * 29 en febrero*/
	  int[] last={31, 28, 31,30,31,30,31,31,30,31,30,31};
	  if (month == 1 && DateUtils.leapYear(year))
			  return 29;
	  else return last[month];
  }
  
  public static int dayOfDate(String date) {
	  /*Dada una fecha en formato String yyyy-mm-dd, devuelve el dd en formato int*/
	  String[] atoms = date.split("-");
      return Integer.parseInt(atoms[2]);
      
  }
  
  public static int monthOfDate(String date) {
	  /*Dada una fecha en formato String yyyy-mm-dd, devuelve el mm en formato int*/
	  String[] atoms = date.split("-");
      return Integer.parseInt(atoms[1]);
      
  }
  
  public static int yearOfDate(String date) {
	  /*Dada una fecha en formato String yyyy-mm-dd, devuelve el yyy en formato int*/
	  String[] atoms = date.split("-");
      return Integer.parseInt(atoms[0]);
      
  }
  
  public static String generateDateString (int year, int month, int day) {
	  String syear = Integer.toString(year);
      String smonth = (month+1<10)?"0":"";
      smonth +=Integer.toString(month+1);
      String sday = (day<10)?"0":"";
      sday +=Integer.toString(day);
      return syear + "-" + smonth + "-" + sday;
	  
  }
  
  public static int compareData (String date1, String date2) {
	  Log.d("que", date1 + " " + date2);
	  int day1 = dayOfDate(date1);
	  int day2 = dayOfDate(date2);
	  int month1 = monthOfDate(date1);
	  int month2 = monthOfDate(date2);
	  int year1 = yearOfDate(date1);
	  int year2 = yearOfDate(date2);
	  int result;
	  if (year1 > year2)
		  result = 1;
	  else if (year1 < year2)
		  result = -1;
	  else if (month1 > month2)
		  result = 1;
	  else if (month1 < month2)
		  result = -1;
	  else if (day1>day2)
		  result = 1;
	  else if (day1<day2)
		  result = -1;
	  else result = 0;
	  Log.d ("que", date1 + " " + date2 + " " + Integer.toString(result));
	  return result;
		  
  }
  
  public static String travelInTime (String date, int delta) {
	  int day = dayOfDate(date);
	  int month = monthOfDate(date);
	  int year = yearOfDate(date); 
	  int ldom = lastDayOfMonth(year, month-1);
	  day+=delta;
	  if (day> ldom) {
		  month +=day/ldom;
		  day = day%ldom;
		  if (month > 12) {
			  year +=month/12;
		  	  year +=month%12;
		  }		  
	  }
	  Log.d ("que", "mirame "+ year + " " + month + " "+ day);
	  return generateDateString(year, month-1, day);
  }

}