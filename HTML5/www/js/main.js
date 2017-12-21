$(document).ready(function(){


/*****************GLOBAL VARIABLES ********************/

	var date = new Date();
	var month = date.getMonth();
	var year = date.getFullYear();
	var day = date.getDate();
	var nameMonth = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];
	var subjects = [];
	var login;
	var type;
	var events = [];
	var subjectsToShow = [];


/***************** END GLOBAL VARIABLES ********************/

/***************** INITIAL SETTINGS  ********************/
	setCalendar(year,month);


/***************** END INITIAL SETTINGS  ********************/



/***************** ONCLICK HANDLERS  ********************/


	$(".clickable-tab-index").keyup(function(event){
	    if(event.keyCode == 13){
	        $(this).click();
	    }
	});


	$(function() {
		$( "#datepicker").datepicker();
	});

	$(function() {
		$( "#datepicker-m").datepicker();
	});


	$("#login").click(function () {
		document.getElementById("login").style.visibility="hidden";
		document.getElementById("enter-login").style.visibility="visible";
		document.getElementById("login-values").style.visibility="visible";
	});


	$("#login-values").click(function () {
		login = document.getElementById('username').value;
		document.getElementById("welcome").style.visibility="visible";
		$("#welcome").html("Cargando...");
		get_users(login);
		setCalendar(year,month);
	});


	$("#logout").click(function () {
		subjects = [];
		login = "";
		type = "";
		setCalendar(year,month);
		subjectsToShow = [];
		document.getElementById("login").style.visibility="visible";
		document.getElementById("logout").style.visibility="hidden";
		document.getElementById("welcome").style.visibility="hidden";
		$("#subjects").html('');
	});


	$("#add").click(function () {
		$("#add-aside").show();
		$("#overlay").show();

	});

	$("#aceptar").click(function () {

		var dataNewEvent = document.getElementById("datepicker").value;				
		var tagsNewEvent = document.getElementById("tags-entry").value;			
		var descriptionNewEvent = document.getElementById("description-entry").value;
		dataNewEvent = dataNewEvent.split("/");
			dataNewEvent = dataNewEvent[2] + "-" +dataNewEvent[0] + "-" + dataNewEvent[1];
		tagsNewEvent = validateTags(tagsNewEvent);
		console.log(tagsNewEvent);
		if (tagsNewEvent.length == 0){
			alert("DEBES INTRODUCIR UNOS TAGS VALIDOS");
		}
		else {	
			$("#add-aside").hide();
			$("#overlay").hide();
			ad_event(dataNewEvent, tagsNewEvent, descriptionNewEvent, login);
		}

	});

	$("#cancelar").click(function () {
		$("#add-aside").hide();
		$("#overlay").hide();

	});

	$("#aceptar-m").click(function () {
		var dataNewEvent = document.getElementById("datepicker-m").value;		
		var tagsNewEvent = document.getElementById("tags-entry-m").value;
		var descriptionNewEvent = document.getElementById("description-entry-m").value;
		var eventName = document.getElementById("name-event-m").value;
		dataNewEvent = dataNewEvent.split("/");
			dataNewEvent = dataNewEvent[2] + "-" +dataNewEvent[0] + "-" + dataNewEvent[1];
		tagsNewEvent = validateTags(tagsNewEvent);
		console.log(tagsNewEvent);
		if (tagsNewEvent.length == 0){
			alert("DEBES INTRODUCIR UNOS TAGS VALIDOS");
		}
		else {	
			$("#add-aside").hide();
			$("#overlay").hide();
			var id = $("#"+eventName).attr('class').split(" ")[1];
			var sentenceMod = {"date": "2013/11/11", "tags": tagsNewEvent, "type": "Event", "description": descriptionNewEvent, "creator": login };
			console.log(id,sentenceMod);
			mod_event(id,dataNewEvent, tagsNewEvent, descriptionNewEvent, login);
		}

		$("#mod-aside").hide();
		$("#overlay").hide();

	});

	$("#cancelar-m").click(function () {
		$("#mod-aside").hide();
		$("#overlay").hide();

	});

	$(".title-event").click(function () {
		console.log("clicked");
		$("#add-aside").show();
		$("#overlay").show();
	});

	$("#delete").click(function () {
		$("#delete-aside").show();
		$("#overlay").show();
	});


	$("#modify").click(function () {
		$("#mod-aside").show();
		$("#overlay").show();
	});

	$("#yes").click(function () {
		var eventName = document.getElementById("delete-entry").value;
		var ok = true;
		try{
			var id = $("#"+eventName).attr('class').split(" ")[1];
		}
		catch (exception) {
			ok = false;
		}

		if (ok == true){
			del_event(id);
			$("#delete-aside").hide();
			$("#overlay").hide();
		}
		else{
			alert("EL NOMBRE DEL EVENTO NO EXISTE");
		}
		setCalendar(year,month);
		$("#search").click();
	});

	$("#no").click(function () {
		$("#delete-aside").hide();
		$("#overlay").hide();

	});
	
	$("#siguiente").click(function() {
		nextMonth();
	});	

	$("#anterior").click(function () {
		prevMonth();
	});

	$(".next").click(function() {
		nextMonth();
	});	

	$(".prev").click(function () {
		prevMonth();
	});

	$("#check").click(function () {
		subjectsToShow = [];
		for(var i = 0 ; i < subjects.length ; i++){
			if (document.getElementById(subjects[i].substr(1,10)).checked){
				subjectsToShow.push(subjects[i]);
			}
		}
		setCalendar(year,month);
	});

	$("#search").click(function () {
		var wordsInput = document.getElementById("search-input").value;
		var words = wordsInput.split(" ");
		var text = "";
		for (var j = 0; j< words.length; j++){
			for (var i = 0; i<events.length; i++){
				if(events[i].value.description.indexOf(words[j]) >= 0)
					text += "<div id= 'Evento"+i+"' class='event "+ events[i].id  + "'><h3 class='title-event'>Evento" + i + "</h3><p>Fecha:"+events[i].value.date+"</p><p>Titulo:"+events[i].value.description+"</p><p>Autor:"+events[i].value.creator+"</p><p>Tags:" +events[i].value.tags+ "</p> <p>Tipo:"+events[i].value.type+"</p></div>";
			}
			$("#aside").find("#events-list").html('');
			$("#aside").find("#events-list").append(text)
		}
	});

	
	document.onkeyup = KeyCheck;       
	function KeyCheck(){
	   var KeyID = event.keyCode;
	   switch(KeyID){
	      case 37:
	      	prevMonth();
	      	break;
	      case 38:
	      	year++;
	      	setCalendar(year,month)
	      	break;
	      case 39:
	      	nextMonth();
		    break;
	      case 40:
	      	year--;
	    	setCalendar(year,month)
	      	break;
	   }
	}

/***************** END ONCLICK HANDLERS  ********************/



/***************** AUX FUNCTIONS ********************/


function validateTags(tagsToValidate){
	var tagsFinal = [];
	var tags = tagsToValidate.split(",");
	var valido = true;

	for (var i = 0; i < tags.length; i++){
		var tag = tags[i].trim();
		if (tag != "," && tag != ""){
			tagsFinal.push("#"+tag);
		}
	}
	for (var j = 0; j<tagsFinal.length; j++){
		var tag = tagsFinal[j];
		if (subjects.indexOf(tag) >= 0 ){
			valido = true;
		}
	}
	var final = ""
	for (var j = 0; j<tagsFinal.length; j++){
		var tag = tagsFinal[j];
		final += "'"+tag.replace("#", "^")+"'";
	}
	console.log(final);
	if (valido == false)
		return ""
	else 
		return final;
}



	function failLogin(){
		$("#welcome").html('');
		alert("USUARIO O CONTRASEÑA INCORRECTOS");
	}

	function loginOk(){
		$("#welcome").html(login);
		document.getElementById("enter-login").style.visibility="hidden";
		document.getElementById("login-values").style.visibility="hidden";
		document.getElementById("logout").style.visibility="visible";
		document.getElementById("welcome").style.visibility="visible";
		document.getElementById("add").style.visibility="visible";
		document.getElementById("delete").style.visibility="visible";
		document.getElementById("modify").style.visibility="visible";
		document.getElementById("check").style.visibility="visible";

		if (type == "teacher"){
			var checkBox = "";
			for (var i = 0; i < subjects.length ;i++)	
				checkBox += subjects[i] + "<input class='checkbox' id="+subjects[i].substr(1,10)+" type=checkbox name=subject checked=true value="+subjects[i].substr(1,10)+">          ";
			$("#subjects").append(checkBox);	
			subjectsToShow = subjects;
		}
		else{
			var showSubjects = "";
			for (var i = 0; i < subjects.length ;i++)	
				showSubjects += subjects[i] + "        ";
			$("#subjects").append(showSubjects);	
		}
	}

	function searchEvents(events, id){
		var contador = 0;
		var encontrados = [];
		for (var i = 0; i< events.length; i++){
			date = events[i].value.date;
			date = date[8]+date[9];
			if (date <10)
				date = date[1];
			if (date== id){
				encontrados[contador] = events[i].value;	
				contador++;
			}
		}
		return encontrados;
	}



/***************** END AUX FUNCTIONS ********************/



/***************** SETTING THE CALENDAR ********************/

	function setCalendar(year,month) {
		var data = get_events(year,month+1);		

		var lastDay = lastDayOfMonth(month,year);
		var firstDay = firstDayOfMonth(month,year)
		var weeks = numberOfWeeks(firstDay,lastDay);
		$('.has-events').html('');
 
		$('#tbody').html('');
		$("#mes").html('');
		$("#ano").html('');
		$("#mes").append(nameMonth[month]);
		$("#ano").append(year);

		var firstRow =  "<tr>";
		for(var i = 1; i < firstDay;i++) 
			firstRow += "<td class='gray prev'></td>";
		
		for (var j = firstDay; j<=7;j++)
			firstRow += "<td id='"+(j-i+1)+"'>"+(j-i+1)+"</td>";	
		firstRow +="</tr>";
		$(".calendar-weeks").find("tbody").append(firstRow);

		var firstDayNextWeek = j-i+1;

		var newRow = "<tr>";
		for(var i = 1; i<weeks; i++){
			for (var j = 1; j<=7; j++){
				if ((firstDayNextWeek+j-1) > lastDay){
					newRow += "<td class='gray next'></td>";	
				}
				else
				newRow += "<td id='"+(firstDayNextWeek+j-1)+"'>"+(firstDayNextWeek+j-1)+"</td>";		
			}
			newRow += "</tr>";
			firstDayNextWeek += 7;	 
		}
		$(".calendar-weeks").find("tbody").append(newRow);


		$('.prev').click(prevMonth);
		$('.next').click(nextMonth);
		$('.has-events').click(displayEvents);
		return null;

	}

	function nextMonth(){
		month++;
		if (month >= 12){
			month = 0;
			year++;
		}
		setCalendar(year,month);
	}

	function prevMonth(){
		month--;
		if (month <= 0){
			month = 11;
			year--;
		}
		setCalendar(year,month);
	}


	function markDaysWithEvents(events){
 	$("td.has-events").removeClass("has-events");
	if (type == "student"){
		subjectsToShow = subjects;
	}
    for (var i = 0; i < events.length; i++) {
		var day = events[i].value.date;
		day = day[8] + day[9];
		if (day[0]== "0")
		day = day[1];

		var subjectsEvent = events[i].value.tags;
		var canShow = false;

		for (var k = 0 ; k < subjectsToShow.length; k++){
			var subjectAux = subjectsToShow[k];
			for (var j = 0; j < subjectsEvent.length; j++){
				if (subjectsToShow[k] == subjectsEvent[j]){
					canShow=true;		
				}
			}
		}
		if (subjectsToShow.length == 0 && type != "teacher")
			canShow = true;
		if(canShow){
			$(".calendar-weeks").find("#tbody").find("#"+day).addClass("has-events");
			$(".calendar-weeks").find("#tbody").find("#"+day).addClass("clickable-tab-index");
			$(".calendar-weeks").find("#tbody").find("#"+day).attr('tabindex', '0');		
		}
				var canShow = false;

    }

    $(".has-events").click(function () {
		displayEvents($(this).attr('id'));
	});

  } 


	function displayEvents(id){
		var eventsDay = searchEvents(events,id);
		$("#aside").find("#events-list").html('');
		$("#aside").find("#events-title-div").html('');

		var textDay = "<h2> Día " + id+"</h2>";
		$("#aside").find("#events-title-div").append(textDay);

		var text = "";
		for(var i = 0; i < eventsDay.length;i++) 
			text += "<div id= 'evento"+i+"' class='event'><h3 class='title-event'>"+eventsDay[i].description+"</h3><p>Autor:"+eventsDay[i].creator+"</p><p>Tags:" +eventsDay[i].tags+ "</p> <p>Tipo:"+eventsDay[i].type+"</p></div>";
		$("#aside").find("#events-list").append(text)

	}


/***************** END SETTING THE CALENDAR ********************/


/***************** AJAX ********************/

	function get_events(year,month) {
		events = [];
	   var xmlhttp = new XMLHttpRequest();
	   
	   xmlhttp.onreadystatechange=function() {
	      if (xmlhttp.readyState==4) {

	        switch (xmlhttp.status) {
	            case 200: // OK!
					console.log(xmlhttp.responseText);
	                response = JSON.parse(xmlhttp.responseText);
	                rows = response.rows;
	                for(var i = 0; i < rows.length; i++) {
	                  events[i] = rows[i];
	                }             
	                  markDaysWithEvents(events);

	            break;
	            case 404: // Error: 404 - Resource not found!
	                alert("Resource not found!");
	            break;
	            default:  // Error: Unknown!
	        }
	       }
	   }

	  xmlhttp.open("GET","http://localhost:8080/cgi-bin/events.py?year=" + year + "&month=" + month + "&type=events", true);
	  xmlhttp.send();
	}



	function get_users(login) {
	   var xmlhttp = new XMLHttpRequest();
	   
	   xmlhttp.onreadystatechange=function() {
	      if (xmlhttp.readyState==4) {

	        switch (xmlhttp.status) {
	            case 200: // OK!
	                response = JSON.parse(xmlhttp.responseText);
	                rows = response.rows;
	                try{
	                	rows[0].id;
	                	login = rows[0].value.description;
	                	subjects = rows[0].value.subjects;
	                	type = rows[0].value.subtype;
						loginOk();
	                }
	                catch (exception) {
						failLogin();                
	                }
	            break;
	            case 404: // Error: 404 - Resource not found!
	                alert("Resource not found!");
	            break;
	            default:  // Error: Unknown!
	        }
	       }
	   }

	  xmlhttp.open("GET","http://localhost:8080/cgi-bin/events.py?type=users&login=" + login,true);
	  xmlhttp.send();
	}



	function ad_event(date, tags, desc, creator) {
	   var xmlhttp = new XMLHttpRequest();
	   
	   xmlhttp.onreadystatechange=function() {
	      if (xmlhttp.readyState==4) {
			console.log(xmlhttp.responseText);
	        switch (xmlhttp.status) {
	            case 200: // OK!
	                response = JSON.parse(xmlhttp.responseText);
	                rows = response.rows;
	                console.log(rows);
	            break;
	            case 404: // Error: 404 - Resource not found!
	                alert("Resource not found!");
	            break;
	            default:  // Error: Unknown!
	        }
	       }
	   }

	  xmlhttp.open("GET","http://localhost:8080/cgi-bin/events.py?type=add&date=" + date+"&creator="+creator+"&desc="+desc+"&tags="+tags,true);
	  xmlhttp.send();
	}



	function del_event(id) {
	   var xmlhttp = new XMLHttpRequest();
	   
	   xmlhttp.onreadystatechange=function() {
	      if (xmlhttp.readyState==4) {

	        switch (xmlhttp.status) {
	            case 200: // OK!
	                response = JSON.parse(xmlhttp.responseText);
	                rows = response.rows;
	                alert(rows);
	            break;
	            case 404: // Error: 404 - Resource not found!
	                alert("Resource not found!");
	            break;
	            default:  // Error: Unknown!
	        }
	       }
	   }

	  xmlhttp.open("GET","http://localhost:8080/cgi-bin/events.py?type=del&idE=" + id,true);
	  xmlhttp.send();
	}



	function mod_event(id,date, tags, desc, creator) {
	   var xmlhttp = new XMLHttpRequest();
	   
	   xmlhttp.onreadystatechange=function() {
	      if (xmlhttp.readyState==4) {

	        switch (xmlhttp.status) {
	            case 200: // OK!
	                response = JSON.parse(xmlhttp.responseText);
	                rows = response.rows;
	                alert(rows);
	                console.log(rows);
	            break;
	            case 404: // Error: 404 - Resource not found!
	                alert("Resource not found!");
	            break;
	            default:  // Error: Unknown!
	        }
	       }
	   }

	  xmlhttp.open("GET","http://localhost:8080/cgi-bin/events.py?type=mod&idE=" + id + "&date=" + date+"&creator="+creator+"&desc="+desc+"&tags="+tags,true);
	  xmlhttp.send();
	}





/***************** END AJAX ********************/

});
