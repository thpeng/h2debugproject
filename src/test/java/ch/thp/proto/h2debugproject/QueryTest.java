/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.thp.proto.h2debugproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Thierry
 */
public class QueryTest {

    private static String DATABASE_NAME = "test";

    @BeforeClass
    public static void beforeClass() {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() throws Exception {
        Connection con = null;
        con = DriverManager.getConnection("jdbc:h2:mem:"
                + DATABASE_NAME + ";DB_CLOSE_DELAY=-1");

        prepareAndExecute(con, "create table APPLICATION_GROUP (uuid VARCHAR(36) not null, description varchar(255), name varchar(255), primary key (uuid))");
        prepareAndExecute(con, "create table APPLICATION_USER (uuId VARCHAR(36) not null, password varchar(255), username varchar(255), primary key (uuId))");
        prepareAndExecute(con, "create table USER_GROUPS (APPLICATION_USER_uuId VARCHAR(36) not null, groups_uuid VARCHAR(36) not null, primary key (APPLICATION_USER_uuId, groups_uuid))");
        prepareAndExecute(con, "alter table USER_GROUPS add constraint FK_16s9sixawfrh5xmdsx2jcs9hg foreign key (groups_uuid) references APPLICATION_GROUP");
        prepareAndExecute(con, "alter table USER_GROUPS add constraint FK_qtaldgcouaelm449cfotlii84 foreign key (APPLICATION_USER_uuId) references APPLICATION_USER");
        con.close();

    }

    @After
    public void tearDown() throws Exception {
        //clean up, prepare for next test
        Connection con = null;
        con = DriverManager.getConnection("jdbc:h2:mem:"
                + DATABASE_NAME + ";DB_CLOSE_DELAY=-1");
        prepareAndExecute(con, "drop table APPLICATION_GROUP if exists");
        prepareAndExecute(con, "drop table APPLICATION_USER if exists");
        prepareAndExecute(con, " drop table USER_GROUPS if exists");
        con.close();
    }

    /**
     * with uuid, it does not work
     */
    @Test
    public void testGroupQueryWithUuid() throws Exception {
        String queryString = "select distinct name from APPLICATION_GROUP g inner join user_groups ug on g.uuid = ug.groups_uuid inner join APPLICATION_USER u on ug.APPLICATION_USER_uuid = u.uuid where u.username = 'a.user'";
        Connection con = null;
        con = DriverManager.getConnection("jdbc:h2:mem:" + DATABASE_NAME
                + ";IFEXISTS=TRUE");

        //setup testdata
        prepareAndExecute(con, "insert into APPLICATION_GROUP (description, name, uuid) values ('bla', 'standard', '111e1111-e29b-41d4-a716-446655440000')");
        prepareAndExecute(con, "insert into APPLICATION_USER (password, username, uuId) values ('dontmatter', 'a.user', '222e2222-e29b-41d4-a716-446655440000')");
        prepareAndExecute(con, "insert into USER_GROUPS (APPLICATION_USER_uuId, groups_uuid) values ('222e2222-e29b-41d4-a716-446655440000', '111e1111-e29b-41d4-a716-446655440000')");
        //end testdata

        PreparedStatement stmt1 = con.prepareStatement(queryString);
        ResultSet blah = stmt1.executeQuery();
        int size = 0;
        while (blah.next()) {
            Assert.assertEquals("standard", blah.getString(1));
            size++;

        }
        Assert.assertEquals("size must be 1, was {}", 1, size);
        blah.close();
        stmt1.close();
        con.close();

    }

    /**
     * castable to integers, works fine 
     */
    @Test
    public void testGroupQueryWithStringCastableToInteger() throws Exception {
        String queryString = "select distinct name from APPLICATION_GROUP g inner join user_groups ug on g.uuid = ug.groups_uuid inner join APPLICATION_USER u on ug.APPLICATION_USER_uuid = u.uuid where u.username = 'b.user'";
        Connection con = null;
        con = DriverManager.getConnection("jdbc:h2:mem:" + DATABASE_NAME
                + ";IFEXISTS=TRUE");

        //setup testdata
        prepareAndExecute(con, "insert into APPLICATION_GROUP (description, name, uuid) values ('bla', 'standard', '111')");
        prepareAndExecute(con, "insert into APPLICATION_USER (password, username, uuId) values ('dontmatter', 'b.user', '222')");
        prepareAndExecute(con, "insert into USER_GROUPS (APPLICATION_USER_uuId, groups_uuid) values ('222', '111')");
        //end testdata

        PreparedStatement stmt1 = con.prepareStatement(queryString);
        ResultSet blah = stmt1.executeQuery();
        int size = 0;
        while (blah.next()) {
            Assert.assertEquals("standard", blah.getString(1));
            size++;

        }
        Assert.assertEquals("size must be 1, was {}", 1, size);
        blah.close();
        stmt1.close();
        con.close();

    }

    private void prepareAndExecute(Connection con, String query) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.executeUpdate();
    }
}
