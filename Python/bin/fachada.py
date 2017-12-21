# --*-- coding: utf-8 --*--

"""
Autores: Ismael Barveito Vázquez (i.barbeito)
		 Daniel Ruiz Pérez (d.ruiz.perez)

Historias: 	2.1 (Consultar eventos del curso)
			2.2 ( Consultar un evento)
  			2.3 (Internacionalizacion)
  			2.4 (Consulta personalizada)
   			2.5 (Consulta por asignatura)
			2.6 (Ver como alumno)
			2.7 (Gestionar eventos de las asignaturas)
			2.8 (Recibir notificaciones de escritorio)

 
Módulo: Fachada

"""
import controlador
import threading	

#Clase auxiliar: lanzamiento de Threads(ThreadLauncher())
class ThreadLauncher(threading.Thread): #Permite lanzar una función de 0 o 1 argumentos en un thread
	def __init__(self,function,arg = None):
		super(ThreadLauncher, self).__init__()
		self.function = function
		self.arg = arg
	def run(self):
		if self.arg != None:
			self.function(self.arg)
		else:
			self.function()

#Clase principal del módulo: Fachada()
class Fachada ():
	'''La clase fachada es la pieza que interactua entre el controlador y la vista. Básicamente, permite lanzar
	los métodos que calendar necesita en threads'''
	def __init__(self,builder,fecha):
		self.controlador = controlador.Controller(builder,fecha)

	def month_changed(self,fecha): #Cualquier evento que provoque un cambio de mes en el calendario
		self.__thread_launch(self.controlador.month_changed,fecha)

	def day_selected(self,fecha): #Click en un día en el calendario
		self.__thread_launch(self.controlador.day_selected,fecha)
	
	def login_clicked(self,login): #Botón "Autentificarse"
		self.__thread_launch(self.controlador.login_clicked,login)
	
	def logout_clicked(self): #Botón de "Cerrar sesión"
		self.__thread_launch(self.controlador.logout_clicked)

	def choose_activate(self): #Menú "Ver/Ver eventos de una determinada asignatura"
		self.__thread_launch(self.controlador.choose_activate)

	def treeview2_row_activated(self,row): #Selección de "ver materia de X asignatura"
		self.__thread_launch(self.controlador.treeview2_row_activated,row)
	
	def on_teacher_menu_view(self): #Volver a la vista de profesor
		self.__thread_launch(self.controlador.teacher_view)
	
	def on_student_menu_view(self): #Ver como alumno (profesor)
		self.__thread_launch(self.controlador.student_view)

	def day_selected_double_click(self,fecha):	
		self.__thread_launch(self.controlador.day_selected_double_click,fecha)

	def treeview1_row_activated(self,values):
		self.__thread_launch(self.controlador.treeview1_row_activated,values)

	def editar_evento_clicked(self):
		self.__thread_launch(self.controlador.editar_evento_clicked)

	def borrar_evento_clicked(self):
		self.__thread_launch(self.controlador.borrar_evento_clicked)

	def yes_button_clicked(self):
		self.__thread_launch(self.controlador.yes_button_clicked)

	def listo_editar_button_clicked(self):
		self.__thread_launch(self.controlador.listo_editar_button_clicked)

	def listo_crear_button_clicked(self):
		self.__thread_launch(self.controlador.listo_crear_button_clicked)

	def __thread_launch(self, f,arg = None): #Método para simplificar el lanzamiento de threads
		thread = ThreadLauncher(f,arg)
		thread.start()
