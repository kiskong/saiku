package org.saiku.database;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by bugg on 01/05/14.
 */
public class Database {

  public ServletContext getServletContext() {
    return servletContext;
  }

  public void setServletContext( ServletContext servletContext ) {
    this.servletContext = servletContext;
  }

  @Autowired
  ServletContext servletContext;

  public Database() {

  }

  public void init() throws SQLException {
    initDB();
    loadUsers();
  }
  private JdbcDataSource ds;

  private void initDB(){
    String url = servletContext.getInitParameter( "db.url" );
    String user = servletContext.getInitParameter("db.user");
    String pword = servletContext.getInitParameter("db.password");
    ds = new JdbcDataSource();
    ds.setURL(url);
    ds.setUser( user );
    ds.setPassword( pword );
  }

  private void  loadUsers() throws SQLException {

    Connection c = ds.getConnection();

    Statement statement = c.createStatement();
    statement.execute("CREATE TABLE IF NOT EXISTS LOG(time TIMESTAMP AS CURRENT_TIMESTAMP NOT NULL, log CLOB);");

    statement.execute("CREATE TABLE IF NOT EXISTS USERS(user_id INT(11) NOT NULL AUTO_INCREMENT, username VARCHAR(45) NOT NULL, password VARCHAR(45) NOT NULL, enabled TINYINT NOT NULL DEFAULT 1, PRIMARY KEY(user_id));");

    statement.execute( "CREATE TABLE IF NOT EXISTS USER_ROLES (\n"
      + "  user_role_id INT(11) NOT NULL AUTO_INCREMENT,\n"
      + "  username VARCHAR(45) NOT NULL REFERENCES USERS(username),\n"
      + "  ROLE VARCHAR(45) NOT NULL,\n"
      + "  PRIMARY KEY (user_role_id),\n"
      + "  UNIQUE KEY uni_username_role (ROLE,username));" );

    ResultSet result = statement.executeQuery( "select count(*) as c from LOG where log = 'insert users'" );
    result.next();
    if(result.getInt("c")==0) {

      statement.execute( "INSERT INTO users(username,password,enabled)\n"
        + "VALUES ('admin','admin', TRUE);" +
        "INSERT INTO users(username,password,enabled)\n"
        + "VALUES ('alex','123456', TRUE);");
      statement.execute(
        "INSERT INTO user_roles (username, ROLE)\n"
        + "VALUES ('admin', 'ROLE_USER');" +
        "INSERT INTO user_roles (username, ROLE)\n"
        + "VALUES ('admin', 'ROLE_ADMIN');" +
        "INSERT INTO user_roles (username, ROLE)\n"
        + "VALUES ('alex', 'ROLE_USER');" );

      statement.execute( "INSERT INTO LOG(log) VALUES('insert users');" );
    }
  }
}