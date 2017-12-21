#!/usr/bin/python
# --*-- coding: utf-8 --*--

"""
Autores: Ismael Barveito Vázquez (i.barbeito)
		 Daniel Ruiz Pérez (d.ruiz.perez)

Historias: 	2.1 (Consultar eventos del curso)
			2.2 (Consultar un evento)
  			2.3 (Internacionalizacion)
  			2.4 (Consulta personalizada)
  			2.5 (Consulta por asignatura)
			2.6 (Ver como alumno)
			2.7 (Gestionar eventos de las asignaturas)
			2.8 (Recibir notificaciones de escritorio)

Módulo: calendar

"""

from gi.repository import Gtk
from gi.repository import GdkPixbuf
import fachada 
from gi.repository import GObject

#Parte de traducciones (historia 3 en adelante)
import locale
import gettext
import os

locale.setlocale(locale.LC_ALL,'')
LOCALE_DIR = os.path.join(os.path.dirname(__file__),"locale")
locale.bindtextdomain('app',LOCALE_DIR)
gettext.bindtextdomain('app',LOCALE_DIR)
gettext.textdomain('app')
_ = gettext.gettext
N_ = gettext.ngettext

#Clase principal del módulo: App()

class App():
	def __init__(self):
		self.builder = Gtk.Builder()
		self.builder.set_translation_domain("app")
		self.builder.add_from_file("calendar.glade")
		self.builder.connect_signals(self)
		
		#Los siguientes atributos se cargan por necesidad de enviar algún parámetro a la fachada
		self.calendar = self.builder.get_object("calendar1")
		self.entry1 = self.builder.get_object("entry1")
		self.liststore2 = self.builder.get_object("liststore2")
		self.treeview2 = self.builder.get_object("treeview2")

		self.liststore1 = self.builder.get_object("liststore1")
		self.treeview1 = self.builder.get_object("treeview1")


		w = self.builder.get_object("window1")
		w.show_all()	

		fecha = Gtk.Calendar.get_date(self.calendar)	
		self.fachada = fachada.Fachada(self.builder,fecha)
		
	def on_acercade(self,w): #Creamos aquí el "acerca de" para facilitar cambios en el mismo 
		about = Gtk.AboutDialog()
		about.set_program_name("Dani & Isma CalendarIO")
		about.set_version("3.5")
		about.set_copyright("the modrefockers")
		about.set_comments("Practica 1, IPM")
		about.set_logo(GdkPixbuf.Pixbuf.new_from_file("imagen.jpg"))
		about.run()
		about.destroy() 

	def on_close_a(self,w): #Close para "File/quit"
		Gtk.main_quit()

	def on_close(self,w,e): #Close para el botón superior derecho de cerrar
		Gtk.main_quit()

	def on_calendar1_month_changed(self,c): #Cualquier evento que provoque un cambio de mes en el calendario
		fecha = Gtk.Calendar.get_date(c)
		self.fachada.month_changed(fecha)

	def on_calendar1_day_selected(self,c): #Click en un día en el calendario
		fecha = Gtk.Calendar.get_date(c)
		self.fachada.day_selected(fecha)

	def on_login_clicked(self,w): #Botón "Autentificarse"
		login = self.entry1.get_text()
		self.fachada.login_clicked(login)

	def on_logout_clicked(self,w): #Botón de "Cerrar sesión"
		self.fachada.logout_clicked()

	def on_choose_activate(self,w): #Menú "Ver/Ver eventos de una determinada asignatura"
		self.fachada.choose_activate()

	def on_treeview2_row_activated(self,w,row,b): #Selección de "ver materia de X asignatura"
		self.fachada.treeview2_row_activated([self.liststore2[row.get_indices()[0]][0]])
	
	def on_teacher_menu_view(self,w): #Menú "ver como profesor"
		self.fachada.on_teacher_menu_view()
	
	def on_student_menu_view(self,w): #Menú "ver como alumno"
		self.fachada.on_student_menu_view()

	def on_calendar1_day_selected_double_click(self,c):
		fecha = Gtk.Calendar.get_date(c)
		self.fachada.day_selected_double_click(fecha)

	def on_treeview1_row_activated(self,w,row,b):
		#pasarlo como una tupla (id,descirpcion,creador,etc)
		values = (self.liststore1[row.get_indices()[0]][0],self.liststore1[row.get_indices()[0]][1],self.liststore1[row.get_indices()[0]][2],self.liststore1[row.get_indices()[0]][3],self.liststore1[row.get_indices()[0]][4])
		self.fachada.treeview1_row_activated(values)

	def on_editar_evento_clicked(self,w):
		self.fachada.editar_evento_clicked()

	def on_borrar_evento_clicked(self,w):
		self.fachada.borrar_evento_clicked()

	def on_yes_button_clicked(self,w):
		self.fachada.yes_button_clicked()

	def on_listo_editar_button_clicked(self,w):
		self.fachada.listo_editar_button_clicked()

	def on_listo_crear_button_clicked(self,w):
		self.fachada.listo_crear_button_clicked()


App()
GObject.threads_init()
Gtk.main()
