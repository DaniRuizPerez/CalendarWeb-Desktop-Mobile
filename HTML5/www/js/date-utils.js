/*Month should always be a number on range 0-11*/

function zeller(day,month,year) {
	
	var MODARRAY = [5,6,0,1,2,3,4];
	
      if (month < 3) {
          month += 12;
          year -= 1;     
      }
      var k = year % 100;
      var j = year / 100;
      var result = ((day + Math.floor(((month + 1) * 26) / 10) + k + Math.floor(k / 4) + Math.floor(j / 4)) +
          (5 * j)) % 7;
      return MODARRAY[Math.floor(day)];
  }


function isLeapYear(year) {
	return (((year%4 == 0) && (year%100!=0))||(year%400==0));
	
}


function numberOfWeeks(firstDayOfMonth, lastDayOfMonth){
 	var used = firstDayOfMonth + lastDayOfMonth;
    return Math.ceil( used / 7);
}


function firstDayOfMonth(month,year){
	var day = new Date(year,month, 1).getDay();
	day = (day===0) ? 7 : day
	return day;
}


function lastDayOfMonth(month,year) {
	var last = [31,28,31,30,31,30,31,31,30,31,30,31]
	if (month == 1 && isLeapYear(year))
		return 29;
	else
		return last[month];

}

