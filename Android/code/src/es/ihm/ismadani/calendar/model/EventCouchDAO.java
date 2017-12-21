package es.ihm.ismadani.calendar.model;

import java.util.ArrayList;
import java.util.List;

import org.ektorp.*;
import org.ektorp.support.*;

/**
 * The Repository Support in Ektorp is aimed to reduce the amount of repetitive 
 * code in repositories and to facilitate the management of the design documents
 * that define the views for the documents in CouchDB.
 *
 * Ektorp provides a repository base class org.ektorp.support.CouchDbRepositorySupport 
 * that has a number of features:
 * 
 * - Out of the box CRUD (add/remove/update/get/getAll/contains functions)
 * - Automatic view generation (@GenerateView)
 * - View management (in-line view definitions)
 */

public class EventCouchDAO extends CouchDbRepositorySupport<EventVO> {

        public EventCouchDAO() {
        		super(EventVO.class, CouchDBAndroidHelper.getInstance().getDbConnector());        	
                initStandardDesignDocument();
        }

        /**
         * CouchDbRepositorySupport is able to generate some views automatically.
         * Simple finder methods can be annotated with the @GenererateView annotation
         */
        @GenerateView @Override
        public List<EventVO> getAll() {
                ViewQuery q = createQuery("all")
                                .includeDocs(true);
                return db.queryView(q, EventVO.class);
        }
      
        @View( name = "listEventsByDate", map = "function(doc){"+
                " if ((doc.type == 'Event')) " +
                " { emit(doc._id, doc.date)}}")
        //Obtiene todos los eventos entre dos fechas
        public List<EventVO> listEventsByDate(String initDate, String endDate) {
                    ViewQuery q = createQuery("by_date")
                                    .startKey(initDate)
                                    .endKey(endDate)
                                    .includeDocs(true);
                    return db.queryView(q, EventVO.class);
        }
        
        @GenerateView
        public List<EventVO> findByDate(String date) {
            List<EventVO> users = queryView("by_date", date);
            return users;        
        }
        
        //Obtiene todos los eventos entre dos fechas para una lista de asignaturas
        public List<EventVO> listEventsByDate(String initDate, String endDate, List <String> subjects) {
            ViewQuery q = createQuery("by_date")
                            .startKey(initDate)
                            .endKey(endDate)
                            .includeDocs(true);
            List <EventVO> todos = db.queryView(q, EventVO.class);
            List <EventVO> eventos = new ArrayList();
            for (EventVO e:todos)
            	for (String s:e.getTags())
            		if (subjects.contains(s)) {
            			eventos.add(e);
            			break;
            		}
            			
            return eventos;
            
          
            	
}
        
        
}
