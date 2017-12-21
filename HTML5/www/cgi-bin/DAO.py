# --*-- coding: utf-8 --*--

"""
Autores: Ismael Barveito VÃƒÂ¡zquez (i.barbeito)
		 Daniel Ruiz PÃƒÂ©rez (d.ruiz.perez)

Historias: 	2.1 (Consultar eventos del curso)
			2.2 ( Consultar un evento)
  			2.3 (Internacionalizacion)
  			2.4 (Consulta personalizada)
  			2.5 (Consulta por asignatura)
			2.6 (Ver como alumno)
			2.7 (Gestionar eventos de las asignaturas)
			2.8 (Recibir notificaciones de escritorio)


Modulo: DAO

"""

import couchdb

#Parte de traducciones (historia 3 en adelante)
import locale
import gettext
import os

#Clase auxiliar: excepciones (CalendarException())
class CalendarException(Exception):
	"""Clase de excepciones para el DAO"""
	def __init__(self, valor):
		self.valor = valor
		self.exception_list = [("No se ha podido acceder a la base de datos"), ("Problema interno en BD"), ("Usuario no existe en BD")]
		self.value = self.exception_list[self.valor]
	def __str_(self):
		return "Error: " + self.exception_list[self.valor]

#Clase principal del mÃ³dulo: PyCouchDAO()
class PyCouchDAO ():	
	"""PyCouchDAO ofrece un API para conectarse a una base de datos couch y obtener de ella la informacion necesaria"""

	def __init__(self, dbname = None):	
			self.db_name = "calendario" #Este es nuestro nombre por defecto para la BD
			if dbname != None:
				self.db_name = dbname
			try:
				self.couch = couchdb.Server()
				self.db = self.couch[self.db_name]
			except:
				raise CalendarException(1) #Si la bd no existe en el servidor, subo el error

	def __create_filter_if(self, qfilter): 
		'''Crea la parte de la query que hace de filtro. 
		qfilter es un diccionar de listas de strings. BÃ¡sicamente, establece una serie de condiciones para obtener el campo.
		Es reseÃ±able que entre entradas para una misma clave se aplica una lÃ³gica "OR", y entre claves una lÃ³gica "AND"
		Ejemplo: para el diccionario {'creador': ['yo', 'tu'], 'fecha': ['2012-01-02'] obtendrÃ­amos esta expresiÃ³n:
		&&((doc.creador == 'yo' || doc.creador == 'tu') && doc.fecha == '2012-01-02')}
		El && inicial se incluye debido a que el filtro es algo opcional a la query'''
		filter_words = "&&(" 
		for key in qfilter:
			for key_token in qfilter[key]:
				filter_words = filter_words + '''(doc.'''+key+'''.indexOf("'''+key_token+'''")!=-1) ||'''
			filter_words = filter_words[0:-2]
			filter_words = filter_words+ ")&&("
		return filter_words[0:-3]
	
	def __create_matcher(self,year, month = None, day = None):
		'''Crea un matcher de fechas. Por matcher entendemos una expresiÃ³n regular que permite encontrar fechas del estilo
		yyyy-mm-dd, pudiendo especificar aÃ±o, aÃ±o y mes o aÃ±o, mes y dia 
		'''
		date = str(year) + "-"
		if month != None:
			if month < 10:
				date = date + "0"
			date = date + str(month) + "-"
	
			if day != None:
				if day < 10:
					date = date + "0"
				date = date + str(day)
			else:
				date = date + "[0-9]{1,2}"
		else:
			date = date + "[0-9]{1,2}-[0-9]{1,2}" 
		return date

				
	def __create_date_query(self, matcher, qfilter = None):
		'''Crea un string que contiene la query para la bÃºsqueda por fechas.
		Tal y como estÃ¡ planteado, devuelve una lista en la cual cada elemento tiene tres valores:
				id: el id del documento
				key: la fecha del evento
				value: los campos date, creator, description, tags y type del evento
		Solo devuelve los eventos que matcheen con el matcher dado
		'''
		filter_if = ""
		if qfilter != None:
			filter_if = self.__create_filter_if(qfilter)
		return '''function(doc) {
		if (doc["date"].match("'''+matcher+'''")'''+filter_if+''')
				emit (doc.date,{"date":doc.date,
								"creator":doc.creator,
								"description":doc.description,
								"tags": doc.tags,
								"type": doc.type})
	}'''

	def __create_user_query(self, matcher):
		'''Crea un string que contiene la query para la bÃºsqueda por usuarios.
		Tal y como estÃ¡ planteado, devuelve una lista en la cual cada elemento tiene tres valores:
				id: el id del documento
				key: teacher o student
				value: los campos subjects, description y subtype
		Solo devuelve los eventos que matcheen con el matcher dado
		'''
		return '''function(doc) {
			if ((doc.subtype=="teacher" || doc.subtype == "student") && doc.description =="'''+matcher+'''")
		  emit(doc.subtype, {"subjects":doc.subjects, "description":doc.description, "subtype" : doc.subtype});
		}'''


	def __create_range_query(self, first, last, qfilter):
		return '''function(doc) {
		if (doc.date >="'''+first+'''" && doc.date <="'''+last+'''"'''+self.__create_filter_if(qfilter)+''')
				emit (doc.date,{"date":doc.date,
								"creator":doc.creator,
								"description":doc.description,
								"tags": doc.tags,
								"type": doc.type})
		}'''
	def list_events_by_date(self,year, month = None, day = None, qfilter = None):
		'''Devuelve una lista de los eventos de la fecha especificada. Se le pueden aplicar filtros especiales, en forma
		de diccionario'''
		matcher = self.__create_matcher(year,month,day)
		query = self.__create_date_query(matcher,qfilter)
		try:
			result = self.db.query(query)
			return self.__view_to_list(result)    
		except:
			raise CalendarException(0)
			
	

	def user_exists(self, login):
		'''Devuelve una excepciÃ³n si el usuario no existe, o los datos del mismo si estÃ¡ registrado en la BD'''
		query = self.__create_user_query(login)
		try:
			result = self.db.query(query)
			user = list(result)
		except:
			raise CalendarException(0)
		else:
			if user != []:
				return self.__view_to_list(user)
			else:
				raise CalendarException(2)	
	
	def add_event(self, values):
		#Recibe un diccionario clave/valor 
		allok = self.db.save(values)
		if not allok:
			raise CalendarException(1)
	
	def del_event (self, id):
		doc = self.db[id]
		try:
			self.db.delete(doc)
		except couchdb.http.ResourceNotFound:
			pass
		else:
			if id in self.db == True:
				raise CalendarException(1)
	
	def mod_event(self, id, evento):
		try:		
			doc = self.db[id]
			keys = evento.keys()
			i = 0
			for element in evento:
				doc[keys[i]] = evento[element]
				i = i+1
			self.db.save(doc)
		except:
			raise CalendarException(1)	
	
	def list_events_on_range(self, first, last, qfilter):
		query = self.__create_range_query(first, last, qfilter)
		try:
			result = self.db.query(query)
			return self.__view_to_list(result)    
		except:
			raise CalendarException(0)



	def __view_to_list(self,view_result):
		docs = []
		for row in view_result:
			docs.append({'id': row['id'], 'key': row['key'], 'value': row['value']})  
		return docs
