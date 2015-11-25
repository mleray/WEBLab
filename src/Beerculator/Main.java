package Beerculator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
@ManagedBean(name="maintest")
@RequestScoped

public class Main {
	public String  check(){
		return "It works";
	}
	
    public static HashMap<Integer, Drink> menu = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        //your DB settings
        String url = "jdbc:postgresql://localhost:5432/beerculator";
        Properties props = new Properties();
        props.setProperty("user","beerculator_admin");
        props.setProperty("password","beer");

        Connection conn = DriverManager.getConnection(url, props);
        //load drinks from db
        menu = Drink.getDrinkList(conn);

        //create new user
//        User us = new User("Peter Man", 75, true);
        //load user by session_id
        User us = new User("0233279866", conn);
        conn.close();

        //update number of drinks
        us.setDrinkQuantity(menu.get(39), 3);
        us.setDrinkQuantity(menu.get(38), 2);
        us.setDrinkQuantity(menu.get(37), 1);


        conn = DriverManager.getConnection(url, props);
        //save user back to database
        us.saveToDb(conn);
    }
}
