package Beerculator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * @author Tomáš Sekanina
 * @author Patricio Sanchez
 * @author Maud Leray
 * @author Alvaro Gonzalez
 */

/**
 * Class for Drink
 * implements the same atributes as in sql
 */
public class Drink {

    private int id = 0;
    private String name;
    private int volume;
    private double alcohol; // needs to be in percentage

    /**
     * Constructor for drink
     * @param name name of the drink
     * @param volume volume in ml
     * @param alcohol alcohol level in %
     */
    public Drink(String name, int volume, double alcohol) {
        this.name = name;
        this.volume = volume;
        this.alcohol = alcohol;
    }

    /**
     * Constructor for drink*
     * @param id id of the instance
     * @param name name of the drink
     * @param volume volume in ml
     * @param alcohol alcohol level in %
     */
    public Drink(int id, String name, int volume, double alcohol) {
        this.id = id;
        this.name = name;
        this.volume = volume;
        this.alcohol = alcohol;
    }

    /**
     * Constructor for drink, loads it from DB by its id
     * @param id id of the drink
     * @param conn db connection
     * */

    public Drink(int id, Connection conn) throws SQLException {
        this.id = id;
        String cmd = "SELECT name, volume, alcohol FROM drinks WHERE id=" + id + ";";
//        System.out.println(cmd);
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(cmd);
        if (!result.next()) {
            System.err.println("Drink " + this.name + "is not in the db.");
            return;
        }

        this.name = result.getString("name").trim();
        this.volume = result.getInt("volume");
        this.alcohol = result.getDouble("alcohol");
    }


    /**
     * Calculates the "A" value for the formula
     * @return a for this drink
     */
    public double A() {
        return (this.volume * (this.alcohol/100) * 0.8)/ 10;
    }

    /**
     * Loads all drinks drom DB and returns them as hashmap
     * @param conn db connection
     * @return hashmap with dirnks in where keys are ids and values Drink instances
     * @throws SQLException
     */
    public static HashMap<Integer, Drink> getDrinkList(Connection conn) throws SQLException {
        HashMap<Integer, Drink> list = new HashMap<>();
        String cmd = "SELECT id, name, volume, alcohol FROM drinks;";
//        System.out.println(cmd);
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(cmd);
        while(result.next()) {
            list.put(result.getInt("id"), new Drink(result.getInt("id"), result.getString("name"), result.getInt("volume"), result.getDouble("alcohol")));
        }
        return list;
    }

    /**
     * Returns atributes of the user for sql queries
     * @return list of values as a string
     */
    public String toStringValues() {
        return "('" + name +
                "', '" + volume +
                "', '" + alcohol +
                "')";
    }

    /**
     * Returns list of atributes of the user with their names
     * @return list of values and their names as a string
     */
    @Override
    public String toString() {
        return "name='" + name +
                "', volume=" + volume +
                ", alcohol=" + alcohol;
    }

    /**
     * inserts row into db for this drink and sets its id
     * @param conn db connection
     * @return result of the operations
     * @throws SQLException
     */
    public int addToDb(Connection conn) throws SQLException {
        String cmd = "INSERT INTO drinks (name, volume, alcohol) VALUES ";
        Statement stmt = conn.createStatement();
//        System.out.println(cmd + this.toString() + ";");
        if (this.id != 0) {
            System.err.println("Drink " + this.name + " has id, thus it should be just updated, not inserted. Check what you are doing");
            return -1;
        }
        try {
            stmt.execute(cmd + this.toStringValues() + ";");
        } catch (SQLException e) {
            System.err.println("Drink " + this.name + " could not be added");
            return -2;
        }
//        this.id = this.getIdByName(conn);
        return 0;
    }

    /**
     * updates row in the for this drink
     * @param conn db connection
     * @return result of the operations
     * @throws SQLException
     */
    public int updateDb(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        if (this.id == 0) {
            System.err.printf("Drink " + this.name + "does not have id, thus it should be just inserted, not updated. Check what you are doing");
            return -1;
        }
        String cmd = "UPDATE drinks SET " + this.toString() + " WHERE id =" + this.id + ";";
        System.out.println(cmd);
        stmt.execute(cmd);
        return 0;
    }

    /**
     * saves drink to db, depending on whether it already exists insersts new row or just updates current one.
     * @param conn db connection
     * @throws SQLException
     */
    public void saveToDb(Connection conn) throws SQLException {
        if (this.id == 0) {
            this.addToDb(conn);
        } else {
            this.updateDb(conn);
        }
    }

    /* Getters */

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getVolume() {
        return volume;
    }

    public double getAlcohol() {
        return alcohol;
    }
    /* End of getters */
    /* Setters */

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setAlcohol(double alcohol) {
        this.alcohol = alcohol;
    }

    /* End of setters */
}

