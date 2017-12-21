package es.ihm.ismadani.calendar.model;

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

public class UserDAO extends CouchDbRepositorySupport<UserVO> {

        public UserDAO() {
        		super(UserVO.class, CouchDBAndroidHelper.getInstance().getDbConnector());
                initStandardDesignDocument();
        }


        /**
         * CouchDbRepositorySupport is able to generate some views automatically.
         * Simple finder methods can be annotated with the @GenererateView annotation
         */
        @GenerateView @Override
        public List<UserVO> getAll() {
                ViewQuery q = createQuery("all")
                                .includeDocs(true);
                return db.queryView(q, UserVO.class);
        }
        
}