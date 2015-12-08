package Beerculator;

import javafx.scene.control.TableView;

import java.sql.*;
import java.util.Properties;

/**
 * @author Tomáš Sekanina
 * @author Patricio Sanchez
 * @author Maud Leray
 * @author Alvaro Gonzalez
 */

/**
 * Class for drink records
 * Implements the same attributes as are in the sql
 */
public class DrinkRecord {

    private int id;
    private int quantity;
    private Drink drink;
    private User user;

    /**
     * Constructor
     * @param user User instance
     * @param drink Drink instance
     */
    public DrinkRecord(User user, Drink drink) {
        this.user = user;
        this.drink = drink;
    }

    /**
     * Constructor of DrinkRecords using its Drink object
     * @param user User instance
     * @param drink Drink instance
     * @param quantity Quantity
     */
    public DrinkRecord(User user, Drink drink, int quantity) {
        this.user = user;
        this.drink = drink;
        this.quantity = quantity;
    }

    /**
     * Constructor of DrinkRecord using drink_id and loads the drink from DB
     * @param id Id
     * @param quantity Quantity
     * @param drink drink id
     * @param user User instance
     * @param conn db connection
     * @throws SQLException
     */
    public DrinkRecord(int id, int quantity, int drink, User user, Connection conn) throws SQLException {

        this.user = user;
        this.id = id;
        this.quantity = quantity;
        this.drink = new Drink(drink, conn);
    }

    /**
     * saves drinkRecord to db, depending on whether it already exists inserts new row or just updates current one.
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

    /**
     * Adds record to DB
     * @param conn db connection
     * @return result of the operations
     * @throws SQLException
     */
    public int addToDb(Connection conn) throws SQLException {
        String cmd = "INSERT INTO drink_records (quantity, drink, user_id) VALUES ";
        Statement stmt = conn.createStatement();
//        System.out.println(cmd + this.toStringValues() + ";");
        if (this.id != 0) {
            System.err.println("DrinkRecord " + this.id + " has id, thus it should be just updated, not inserted. Check what you are doing");
            return -1;
        }
        try {
            ResultSet rs = stmt.executeQuery(cmd + this.toStringValues() + " RETURNING id;");
            rs.next();
            this.id = rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("DrinkRecord " + this.id + " could not be added");
            return -2;
        }
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
            System.err.printf("DrinkRecord " + this.id + "does not have id, thus it should be just inserted, not updated. Check what you are doing");
            return -1;
        }
        String cmd = "UPDATE drink_records SET " + this.toString() + " WHERE id=" + this.id + ";";
//        System.out.println(cmd);
        stmt.execute(cmd);
        return 0;
    }

    /**
     * method called from the jsf
     * incerements quantity
     * @throws SQLException
     */
    public void increment() throws SQLException {
        ++this.quantity;
        if(this.user.getId()==0) {
            this.user.saveToDb();
        }else {
            this.saveToDb();
        }
    }

    /**
     * method called from the jsf
     * decrements quantity
     * @throws SQLException
     */
    public void decrement() throws SQLException {
        if (this.quantity > 0) {
            --this.quantity;
        }
        if(this.user.getId()==0) {
            this.user.saveToDb();
        }else {
            this.saveToDb();
        }
    }


    /**
     * Saves the instance to db
     * @throws SQLException
     */
    public void saveToDb() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/beerculator";
        Properties props = new Properties();
        props.setProperty("user", "beerculator_admin");
        props.setProperty("password", "beer");
        Connection conn = DriverManager.getConnection(url, props);
        this.saveToDb(conn);
    }

    /* DrinkRecord setters */


    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /* End of setters */

    /**
     * Returns list of atributes of the user with their names
     * @return list of values and their names as a string
     */
    @Override
    public String toString() {
        return "id=" + id +
                ", quantity=" + quantity +
                ", drink=" + drink.getId() +
                ", user_id=" + user.getId();
    }

    /**
     * Returns atributes of the user for sql queries
     * @return list of values as a string
     */
    public String toStringValues() {
        return "(" +
                quantity + ", " +
                drink.getId() + ", " +
                user.getId() +
                ")";
    }


    /* DrinkRecord getters */
    public int getId() {
        return id;
    }
    public int getQuantity() {
        return this.quantity;
    }

    public Drink getDrink() {
        return this.drink;
    }

    public User getUser() {
        return user;
    }


    public int getVolume() {
        return this.drink.getVolume();
    }

    public Double getAlcohol() {
        return this.drink.getAlcohol();
    }
}
