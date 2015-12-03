package Beerculator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
@ManagedBean(name="user")
@RequestScoped

public class User {
    int id;
    String session_id;
    String name;
    int weight; // must be in kilogramms
    String gender; 
    HashMap<Integer, DrinkRecord> drink_records;

    public User(String name, int weight, String gender) {
        /**
         * Constructor for user that is not in the db yet
         */
        this.name = name;
        String session_id = "" + Math.abs(Objects.toString(System.currentTimeMillis()).hashCode());
        this.session_id = "" + session_id.substring(session_id.length()-9, session_id.length()-1);
        this.weight = weight;
        this.gender = gender;
        this.drink_records = new HashMap<>();

    }

    public User(String session_id, Connection conn) throws SQLException {
        /**
         * Constructor for user using the session_id
         */
        this.session_id = session_id;
        this.drink_records = new HashMap<>();
        this.getFromDb(conn);
        this.loadDrinkRecords(conn);
    }

    public User(String name, Boolean is_name, Connection conn) throws SQLException {
        /**
         * Constructor for user using its name
         * purpose of arg is_name is only to overload constructor using session_id
         */
        this.name = name;
        this.drink_records = new HashMap<>();
        this.getFromDb(conn);
        this.loadDrinkRecords(conn);
    }

    public int getIdByName(Connection conn) throws SQLException {
        /**
         * returns id of user by its name
         */
        String cmd = "SELECT id FROM users WHERE name='" + this.name + "';";
//        System.out.println(cmd);
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(cmd);
        if (!result.next()) {
            System.err.println("User " + this.name + "is not in the db.");
            return -1;
        }
        return result.getInt(1);
    }


    public int loadDrinkRecords(Connection conn) throws SQLException {
        /**
         * Loads drinkRecords for user from DB
         */
        String cmd = "SELECT id, drink, quantity FROM drink_records WHERE user_id=" + this.id + ";";
//        System.out.println(cmd);
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(cmd);
        while (result.next()) {
            this.drink_records.put(result.getInt("drink"), new DrinkRecord(result.getInt("id"),
                                                                        result.getInt("quantity"),
                                                                        result.getInt("drink"),
                                                                        this, conn));
        }
        return 0;
    }

    public double getAmountOfAlcohol() {
        /**
         * Gets the amount of alcohol for the user from drink_records
         */

        double amount = 0;
        int q;
        Drink d;
        Iterator<Integer> drinkIterator = drink_records.keySet().iterator();
        while(drinkIterator.hasNext()){
            Integer key = drinkIterator.next();
            DrinkRecord dr = drink_records.get(key);
            q = dr.getQuantity();
            d = dr.getDrink();
            amount += q * d.A();
        }
        return amount;
    }

    public double formula(double a) {
        /**
         * Widmark formula with user info and amount of alcohol
         */
        double ratio;
        if (this.gender == "male") {
            ratio = 0.7;
        } else if (this.gender == "female") {
            ratio = 0.55;
        } else {
            System.out.println("This gender does not exist");
            ratio = -1;
        }

        return (a /(this.weight * ratio));
    }

    public double calculateBAC() {
        /**
         * Calculates the BAC value according to user data and drinks
         */
        double alc = this.getAmountOfAlcohol();
        return formula(alc);
    }
    
    public double hoursUntilSober(double bac) {
    	/**
    	 * Gives the nb of hours before user is sober again
    	 */
    	double hours = 0;
    	if (bac < 0) { 
    		System.out.println("the BAC value is negative, there must be a mistake");
    	} else if (0 < bac && bac <= 0.016) {
    		hours = 1;
    	} else if (0.016 < bac && bac <= 0.05) {
    		hours = 3.75;
    	} else if (0.05 < bac && bac <= 0.08) {
    		hours = 5;
    	} else if (0.08 < bac && bac <= 0.1) {
    		hours = 6.25;
    	} else if (0.1 < bac && bac <= 0.16) {
    		hours = 10;
    	} else if (0.16 < bac && bac <= 0.2) {
    		hours = 12.5;
    	} else { hours = 15; }
    	return hours;
    }

    public int getFromDb(Connection conn) throws SQLException {
        /**
         * loads user data from DB if it has session_id
         */
        String cmd;
        if(this.session_id != null) {
            cmd = "SELECT id, name, weight, gender FROM users WHERE session_id='" + this.session_id + "';";
        }else {
            cmd = "SELECT id, name, weight, gender FROM users WHERE name='" + this.name + "';";
        }
//        System.out.println(cmd);
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(cmd);
        if (!result.next()) {
            System.err.println("User with session id: " + this.session_id + "is not in the db.");
            return -1;
        }

        this.id = result.getInt("id");
        this.name = result.getString("name").trim();
        this.weight = result.getInt("weight");
        this.gender = result.getString("gender");

        return 1;
    }

    public void saveToDb(Connection conn) throws SQLException {
        /**
         * saves user and relevant drinkRecords to db, depending on whether it already exists inserts new row or just updates current one.
         */
        if (this.id == 0) {
            this.addToDb(conn);
        } else {
            this.updateDb(conn);
        }
        for(Map.Entry<Integer, DrinkRecord> drinkRecord : this.drink_records.entrySet()){
            drinkRecord.getValue().saveToDb(conn);
        }

    }

    public int updateDb(Connection conn) throws SQLException {
        /**
         * updates row in the for this drink
        */
        Statement stmt = conn.createStatement();

        if (this.id == 0) {
            System.err.printf("User " + this.name + "does not have id, thus it should be just inserted, not updated. Check what you are doing");
            return -1;
        }
        String cmd = "UPDATE users SET " + this.toString() + " WHERE id=" + this.id + ";";
        System.out.println(cmd);
        stmt.execute(cmd);
        return 0;
    }

    public int addToDb(Connection conn) throws SQLException {
        /**
         * inserts row into db for this drink and sets its id
         */
        String cmd = "INSERT INTO users (session_id, name, weight, gender) VALUES ";
        Statement stmt = conn.createStatement();
        System.out.println(cmd + this.toStringValues() + ";");
        if (this.id != 0) {
            System.err.println("User " + this.name + " has id, thus it should be just updated, not inserted. Check what you are doing");
            return -1;
        }
        try {
            stmt.execute(cmd + this.toStringValues() + ";");
        } catch (SQLException e) {
            System.err.println("User " + this.name + " could not be added");
            return -2;
        }
        this.id = this.getIdByName(conn);
        return 0;
    }

    public String toStringValues() {
        return "('" + session_id +
                "', '" + name +
                "', " + weight +
                ", " + gender +
                ")";
    }

    @Override
    public String toString() {
        return "session_id='" + session_id +
                "', name='" + name +
                "', weight=" + weight +
                ", gender=" + gender;
    }

    /* Setters for user */

    public void setId(int id) {
        this.id = id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDrinkQuantity(Drink drink, int quantity){
        /**
         * Sets quantity of drink record of user by id of the drink
         * If the drink record does not exist yet it is created
         **/
        if(this.drink_records.containsKey(drink.getId())) {
            this.drink_records.get(drink.getId()).setQuantity(quantity);
        }else{
            this.drink_records.put(drink.id, new DrinkRecord(this, drink, quantity));
        }

    }

    /* End of setters */

    /* User getters */
    public int getId() {
        return id;
    }

    public String getSession_id() {
        return session_id;
    }

    public int getWeight() {
        return weight;
    }

    public String getGender() {
        return gender;
    }
    /* End of getters */

}
