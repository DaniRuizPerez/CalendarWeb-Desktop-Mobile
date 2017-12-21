# --*-- coding: utf-8 --*--

"""
Autores: Ismael Barveito VÃ¡zquez (i.barbeito)
		 Daniel Ruiz PÃ©rez (d.ruiz.perez)

Historias: 	2.1 (Consultar eventos del curso)
			2.2 ( Consultar un evento)
  			2.3 (Internacionalizacion)
  			2.4 (Consulta personalizada)
  			2.5 (Consulta por asignatura)
			2.6 (Ver como alumno)	
			2.7 (Gestionar eventos de las asignaturas)
			2.8 (Recibir notificaciones de escritorio)
			
 
MÃ³dulo: Controlador

"""
import DAO
from gi.repository import Gtk
from gi.repository import GObject
from gi.repository import Notify
import datetime

#Parte de traducciones (historia 3 en adelante)
import locale
import gettext
import os
import time 
import string
locale.setlocale(locale.LC_ALL,'')
LOCALE_DIR = os.path.join(os.path.dirname(__file__),"locale")
locale.bindtextdomain('app',LOCALE_DIR)
gettext.bindtextdomain('app',LOCALE_DIR)
gettext.textdomain('app')
_ = gettext.gettext
N_ = gettext.ngettext

#Clase principal del mÃ³dulo: controller
class Controller ():
	def __init__(self,builder,fecha_init):
		self.builder = builder
		self.calendar = self.builder.get_object("calendar1")
		self.liststore = self.builder.get_object("liststore1")
		self.treeview = self.builder.get_object("treeview2")
		self.liststore2 = self.builder.get_object("liststore2") #Liststore para las asignaturas de un profesor (h4)
		self.treeview2 = self.builder.get_object("treeview1") #Treeview del liststore 2 (h4)
		self.entry1 = self.builder.get_object("entry1") #Login
		self.entry2 = self.builder.get_object("entry2") #Pass
		self.label = self.builder.get_object("label1") #Label "Bienvenido, <usuario>
		self.login = self.builder.get_object("login") #Boton de login
		self.logout = self.builder.get_object("logout") #Boton de logout
		self.label_loading = self.builder.get_object("label_loading") #Etiqueta "cargando", que aparece en la parte inferior izquierda
		self.spinner_loading = self.builder.get_object("spinner_loading") #Spinner que acompaÃ±a a label_loading
		self.message = self.builder.get_object("messagedialog1") #Mensajes de error
		self.choose_subject = self.builder.get_object("messagedialog2") #DiÃ¡logo para selecciÃ³n de filtrado por asignatura (h4)
		self.fecha = fecha_init #Esta variable tiene como propÃ³sito almacenar el aÃ±o y mes actual
		self.only_subject_menu = self.builder.get_object("Ver") #MenÃº especial para profesores (h4)
		self.month_events_list = None #Los eventos se cargan mensualmente. La consulta diaria se hace en memoria
		self.teacher_menu_view = self.builder.get_object("menuitem5")
		self.student_menu_view = self.builder.get_object("menuitem3")
		self.only_subject_token_menu = self.builder.get_object("menuitem2")
		self.editar_dialog = self.builder.get_object("editar_dialog")
		self.crear_dialog = self.builder.get_object("crear_dialog")
		self.fecha_label = self.builder.get_object("fecha_label")
		self.entry_label = self.builder.get_object("entry_label")
		self.crear_borrar_dialog = self.builder.get_object("crear_borrar_dialog")
		self.sure_dialog = self.builder.get_object("sure_dialog")
		self.yes_button = self.builder.get_object("yes_button")
		self.operacion_exito_dialog = self.builder.get_object("operacion_exito_dialog")
		self.entry_fecha = self.builder.get_object("entry_fecha")
		self.entry_description = self.builder.get_object("entry_description")
		self.entry_tag = self.builder.get_object("entry_tag")
		self.entry_description1 = self.builder.get_object("entry_description1")
		self.entry_tag1= self.builder.get_object("entry_tag1")
		self.value_event = None
		self.login_user = None
		self.values = None
		self.falta_poco = 3
	
		self.is_teacher_view = None			
		self.fecha_incorrecta = None
		GObject.idle_add(self.__hide_init) #Escondemos aquellos elementos que no necesitamos en pantalla para iniciar

		self.subjects = None #Lista de asignaturas del usuario logueado
		self.subjects_filter= None 

		try:
			self.dao = DAO.PyCouchDAO()
		except DAO.CalendarException as e:
			GObject.idle_add(self.__crear_mensaje_error_destroy,e.value)
		else:
			self.month_changed(fecha_init)#Lanzamos month_changed sobre la fecha actual para hacer el primer marcado del calendario
			

	def __hide_init(self): #Ocultamos aquellas partes de la interfaz que no deben ser visibles al inicio
		self.label_loading.hide()
		self.spinner_loading.hide()
		self.label.hide()
		self.logout.hide()
		self.only_subject_menu.hide()

			
	def __crear_mensaje_error_destroy(self,text): #Se crea un mensaje de error que, al cerrarse, serÃ¡ destruÃ­do
		self.message.set_markup(text)
		self.message.run()
		self.message.destroy()
		exit()

	def __loading_show(self): #Mostrar el spinner y label de loading
		self.label_loading.show()
		self.spinner_loading.show()
		self.spinner_loading.start()

	def __loading_hide(self): #Ocultar el spinner y label de loading
		self.label_loading.hide()
		self.spinner_loading.hide()
		self.spinner_loading.stop()

	def __crear_mensaje_error(self,text): #Se muestra un mensaje de error reusable (al cerrarse se oculta, no se destruye)
		self.message.set_markup(text)
		self.message.run()
		self.message.hide()

	def __get_day_of_date(self,date): #Para fechas formato yyyy-mm-dd, obtiene el dia como entero
		return int(date[8:10])

	def get_days_with_events(self,year,month): #Obtenemos solo los dÃ­as que tengan eventos de un mes dado
		self.month_events_list = self.dao.list_events_by_date(year,month, qfilter = self.subjects_filter)
		lista = []
		for elemento in self.month_events_list:
			lista = lista + [self.__get_day_of_date(elemento.key)]
		return lista

	def __mark_days_with_events(self,dias): #Se marcan (en negrita) los dÃ­as del mes en los que haya eventos 
		for dia in dias:
			Gtk.Calendar.mark_day(self.calendar,dia)

	def __clear_marks(self): #GTK mantiene las marcas de un mes a otro. Es pues necesario limpiarlas.
		self.__loading_show() #Mostramos que hay un proceso ejecutandose
		Gtk.Calendar.clear_marks(self.calendar)
		self.liststore.clear()

	def __mark_days(self,days):#EncapsulaciÃ³n de mark_days_with_events para idle_add
		Gtk.Calendar.clear_marks(self.calendar) #Si hay problemas de carga lenta, borramos marcas "sucias"
		self.__mark_days_with_events(days)
		self.__loading_hide()

	def month_changed(self,fecha): #Carga de un nuevo mes (actualizaciÃ³n en memoria + interfaz)
		GObject.idle_add(self.__clear_marks)
		try:
			days = self.get_days_with_events(fecha[0],fecha[1]+1) # days se llama desde aqui porque no puedo meter el dao en un idle_add
			GObject.idle_add(self.__mark_days,days)
			self.fecha = fecha
		except DAO.CalendarException as e:
			GObject.idle_add(self.__login_error,e)

	def __show_events_of_day(self,lista): #Este evento es el encargado de mostrar los dÃ­as en la interfaz
		for elemento in lista:
			self.liststore.append([elemento["value"]["date"],elemento["value"]["creator"],elemento["value"]["description"],str(elemento["value"]["tags"]),elemento.id])

	def __day_selected_aux(self,day):#Engloba el comportamiento "view-side" de day_selected
		self.liststore.clear()
		if Gtk.Calendar.get_day_is_marked(self.calendar,day):
			lista = []
			for elemento in self.month_events_list:
				if self.__get_day_of_date(elemento.key) == day:
					lista = lista + [elemento]
			self.__show_events_of_day(lista)		

	def day_selected(self,fecha): #Muestra en la lista de la zona izquierda de la interfaz los eventos del dÃ­a marcado
		day = fecha[2]
		GObject.idle_add(self.__day_selected_aux,day)

	def __loged_view(self,login): #Muestra el mensaje de bienvenida y el botÃ³n de desloguearse, ocultando las opciones de login
		self.entry1.hide()
		self.entry2.hide()
		self.login.hide()
		self.label.set_text(_("Bienvenido ") +  login +  "!")
		self.label.show()
		self.logout.show()

	def __logout_view(self): #El inverso al anterior
		self.login.show()
		self.entry1.show()
		self.entry2.show()
		self.label.hide()
		self.logout.hide()

	def __login_error(self,e): #Crea el mensaje de error al autentificarse
		self.__crear_mensaje_error(e.value)
		self.__loading_hide()
	
	def teacher_view(self): #Fachada para lanzar __change_to_teacher con idle_add
		GObject.idle_add(self.__change_to_teacher)
	
	def student_view(self): #Fachada para lanzar __change_from_teacher con idle_add
		GObject.idle_add(self.__change_from_teacher)

	def __change_to_teacher(self): #Configura la interfaz en "modo profesor"
		self.is_teacher_view = True
		self.only_subject_menu.show()
		self.teacher_menu_view.hide()
		self.student_menu_view.show()
		self.only_subject_token_menu.show()

	def __change_from_teacher(self): #Configura la interfaz en "modo alumno"
		self.is_teacher_view = False
		self.student_menu_view.hide()
		self.only_subject_token_menu.hide()
		self.teacher_menu_view.show()

	def login_clicked(self,login):#Realiza el login del usuario, verificando que este en la BD y si es alumno/profesor
		GObject.idle_add(self.__loading_show)
		self.login_user = login
		try:
			data_user = self.dao.user_exists(login)
		except DAO.CalendarException as e:
			GObject.idle_add(self.__login_error,e)
		else:
			self.subjects = data_user[0]["value"]["subjects"]
			self.subjects_filter = {"tags": self.subjects}

			if data_user[0].key == "teacher":
				GObject.idle_add(self.__change_to_teacher)

			self.month_changed(self.fecha)
			GObject.idle_add(self.__loged_view,login)
			notification = self.__check_notifications()
			if notification != []:
				GObject.idle_add(self.__notify_user,notification)

	def logout_clicked(self): #Desloguea al usuario
		GObject.idle_add(self.__loading_show)

		self.subjects = None
		self.subjects_filter = None
		self.month_changed(self.fecha)
		self.is_teacher_view = False


		GObject.idle_add(self.only_subject_menu.hide)
		GObject.idle_add(self.__logout_view)
		GObject.idle_add(self.__loading_hide)


	def __append_teacher_subjects(self): #Prepara el liststore2 para mostrar las asignaturas del profesor
		self.liststore2.clear()
		for asignatura in self.subjects:
			self.liststore2.append([asignatura])
			

	def __create_choice_subjects(self): #Crea el diÃ¡logo de filtrado por asignatura para el profesor
		self.choose_subject.set_markup(_("Haga click sobre la asignatura de la que quiere ver sus eventos"))
		self.choose_subject.run()
		self.choose_subject.hide()

	def choose_activate(self): #Muestra solo los eventos de la asignatura elegida por el profesor 
		GObject.idle_add(self.__append_teacher_subjects)
		GObject.idle_add(self.__create_choice_subjects)


	def treeview2_row_activated(self,subject): #Muestra solo los eventos de la asignatura que el profesor escoja de las que imparte
		self.subjects_filter = {"tags": subject}
		GObject.idle_add(self.choose_subject.hide)
		self.month_changed(self.fecha)

	def treeview1_row_activated(self,values):
		if self.is_teacher_view == True:
			GObject.idle_add(self.mostrar_crear_borrar_dialog)
		self.values = values

	def mostrar_crear_borrar_dialog(self):
		self.crear_borrar_dialog.run()
		self.crear_borrar_dialog.hide()

	def borrar_evento_clicked(self):
		GObject.idle_add(self.mostrar_sure_dialog_delete)

	def mostrar_sure_dialog_delete(self):
		self.sure_dialog.run()
		self.sure_dialog.hide()

	def __mostrar_operacion_exito_dialog(self):
		self.operacion_exito_dialog.run()
		self.operacion_exito_dialog.hide()

	def yes_button_clicked(self):
		try:
			self.dao.del_event(self.values[4])
		except DAO.CalendarException as e:
			GObject.idle_add(self.__crear_mensaje_error,e)		
		else :
			GObject.idle_add(self.__mostrar_operacion_exito_dialog)
		self.month_changed(self.fecha)

	def mostrar_editar_evento(self):
		self.entry_fecha.set_text(self.values[0])
		self.entry_description.set_text(self.values[2])
		tags = self.values[3]
		tags = tags.replace("[","")
		tags = tags.replace("]","")
		tags = tags.replace("#","")
		tags = tags.replace("'","")
		self.entry_tag.set_text(tags)
		self.editar_dialog.run()
		self.editar_dialog.hide()


	def editar_evento_clicked(self):
		GObject.idle_add(self.mostrar_editar_evento)


	def __obtener_campos_create(self):
		content_entry_tag = None
		content_entry_description = self.entry_description1.get_text()
		content_entry_tag = self.entry_tag1.get_text()
		content_entry_tag = self.__validar_strings_tags(content_entry_tag)
		if content_entry_tag == None:
			self.__crear_mensaje_error(_("Debe introducir las etiquetas como en el siguiente ejemplo: ipm, so, deathline... y por lo menos una tiene que coincidir con una asignatura de las que impartes"))
			self.value_event = False
		else:
			self.value_event = {"date": str(self.fecha[0])+"-"+string.zfill(str(self.fecha[1]+1),2)+"-"+string.zfill(str(self.fecha[2]),2), "tags": content_entry_tag, "type": "Event", "description": content_entry_description,  "creator": self.login_user }

	def __obtener_campos_entrys(self):
		content_entry_tag = None

		content_entry_fecha = self.entry_fecha.get_text()
		content_entry_description = self.entry_description.get_text()
		content_entry_tag = self.entry_tag.get_text()
		content_entry_tag = self.__validar_strings_tags(content_entry_tag)

		if content_entry_tag == None:
			self.__crear_mensaje_error(_("Debe introducir las etiquetas como en el siguiente ejemplo: ipm, so, deathline... y por lo menos una tiene que coincidir con una asignatura de las que impartes"))
			self.value_event = False
		else:
			self.fecha_incorrecta = False
			split = string.split(content_entry_fecha,"-")
			try:
				if len(split[0]) != 4 or len(split[1]) != 2 or len(split[2]) != 2:
					self.__crear_mensaje_error(_("Formato de fecha incorrecto"))
					self.fecha_incorrecta = True
			except:
				self.__crear_mensaje_error(_("Formato de fecha incorrecto"))
				self.fecha_incorrecta = True

			self.value_event = {"date": content_entry_fecha, "tags": content_entry_tag, "type": "Event", "description": content_entry_description,  "creator": self.login_user }

	def listo_crear_button_clicked(self):
		GObject.idle_add(self.__obtener_campos_create)

		while self.value_event == None:
			pass

		if self.value_event != False:
			try:
				self.dao.add_event(self.value_event)
			except DAO.CalendarException as e:
				GObject.idle_add(self.__crear_mensaje_error,e.value)
			else :
				GObject.idle_add(self.__mostrar_operacion_exito_dialog)
				self.month_changed(self.fecha)
		self.value_event = None

	def listo_editar_button_clicked(self):
		GObject.idle_add(self.__obtener_campos_entrys)

		while self.value_event == None:
			pass

		if self.value_event != False and self.fecha_incorrecta != True:
			try:
				self.dao.mod_event(self.values[4],self.value_event)
			except DAO.CalendarException as e:
				GObject.idle_add(self.__crear_mensaje_error,e.value)
			else :
				GObject.idle_add(self.__mostrar_operacion_exito_dialog)
				self.month_changed(self.fecha)
		self.value_event = None




	def __validar_strings_tags(self,string_tags):
		list_tags = string_tags.split(",")
		for i in range(len(list_tags)):
			list_tags[i] = list_tags[i].strip()
		temp = False
		for s in self.subjects:
			if s[1:] in list_tags:
				temp = True
				break
		if temp:
			list_tags_f = []
			for i in range(len(list_tags)):
				if list_tags[i] != "":
					list_tags_f = list_tags_f + ["#" + list_tags[i]]
			return list_tags_f
		else:
			return None




	def day_selected_double_click(self,fecha):
		#se muestra self.aÃ±adir_editar_window solo con la opcion de aÃ±adir
		self.fecha = fecha
		if self.is_teacher_view == True:
			GObject.idle_add(self.mostrar_crear_evento)
 

	def mostrar_crear_evento(self):
		self.crear_dialog.run()
		self.crear_dialog.hide()
	
	
	def __check_notifications(self):
		today = str(datetime.date.today())
		last_day = str(datetime.date.today() + datetime.timedelta(days=self.falta_poco))
		events = self.dao.list_events_on_range(today, last_day, self.subjects_filter)
		result = []
		for element in events:
			result = result + [element.value['date'] + " " + element.value['description']+ " " + str(element.value['tags'])]
		return result
		
	def __notify_user(self, notifications):
		Notify.init("Notification")
		str_notification = ""
		n_notifications = 0
		for event in notifications:
			str_notification = str_notification + event + "\n"
			n_notifications = n_notifications + 1

			uno = _("Hay ")
			dos = _(" nuevos eventos en los proximos dias:")
		n = Notify.Notification.new(uno  +str(n_notifications) + dos, str_notification, "dialog-information")
		n.show()






