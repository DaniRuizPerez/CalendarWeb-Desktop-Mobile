#!/usr/bin/env python
# -*- coding: utf-8 -*-

#!/usr/bin/env python
# -*- coding: utf-8 -*-

import cgi
import json
import DAO
import ast

def get_database_connection():
    dao = DAO.PyCouchDAO()
    return dao

def get_params():
    # Parse get data
    form = cgi.FieldStorage()
    month=0
    year = 0
    login = ""
    tipo = ""
    sentence= ""
    idE = ""
    creator = ""
    date = ""
    desc = ""
    tags = ""

    if form.has_key("year"):
        year = form["year"].value
    if form.has_key("month"):
        month = form["month"].value
    if form.has_key("type"):
        tipo = form["type"].value
    if form.has_key("login"):
        login = form["login"].value
    if form.has_key("sentence"):
        sentence = form["sentence"].value
    if form.has_key("idE"):
        idE = form["idE"].value
    if form.has_key("creator"):
        creator = form["creator"].value
    if form.has_key("date"):
        date = form["date"].value
	if form.has_key("desc"):
		desc = form["desc"].value
	if form.has_key("tags"):
		tags = form["tags"].value  
		tags = tags.replace("^", "#");     
    return [idE,sentence,tipo,login, year, month,creator,date,desc,tags]
    



def main():
    [idE,sentence,tipo,login, year, month,creator,date,desc,tags] = get_params()
    dao = get_database_connection()
    if (tipo == "events"):
        events = dao.list_events_by_date(year,month)
        print '{ "count": ' + str(len(events)) + ', "rows" : ' + json.dumps(events) + '}'
    if (tipo == "users"):
        try:
            loginUser = dao.user_exists(login)
        except DAO.CalendarException as e:
            loginUser = []        
        print '{ "count": ' +  "1" + ', "rows" : ' + json.dumps(loginUser) + '}'
    
    if (tipo == "add"):
        try:
			sentence = "{'date': '"+date+"', 'creator':'"+creator+"', 'type': 'Event', 'description':'"+desc+"', 'tags': ["+tags+"]}"
			a = ast.literal_eval(sentence)
			dao.add_event(a)
			devolver = "Evento añadido con éxito"
        except DAO.CalendarException as e:
            devolver = e.value
        print '{ "count": ' +  "1" + ', "rows" : ' + json.dumps(devolver) + '}'
    if (tipo == "del"):
        try:
            dao.del_event(idE)
            sentence = "Evento borrado con éxito"
        except DAO.CalendarException as e:
            sentence = e.value
        print '{ "count": ' +  "1" + ', "rows" : ' + json.dumps(sentence) + '}'

    if (tipo == "mod"):
        try:
			sentence = "{'date': '"+date+"', 'creator':'"+creator+"', 'type': 'Event', 'description':'"+desc+"', 'tags': ["+tags+"]}"
			a = ast.literal_eval(sentence)
			dao.mod_event(idE,sentence)
			devolver = "evento modificado con éxito"
        except DAO.CalendarException as e:
            devolver = e.value
        print '{ "count": ' +  "1" + ', "rows" : ' + json.dumps(devolver) + '}'

try:
    print 'Content-Type: application/json\n\n'
    main()  
except:
    cgi.print_exception()
  
    

