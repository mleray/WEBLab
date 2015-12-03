package Beerculator;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
@ManagedBean(name="drink_record")
@RequestScoped

public class DrinkRecord {
	private int id;
    private int quantity = 0;
    private Drink drink;
    private User user;

    public DrinkRecord(User user, Drink drink) {
        this.user = user;
        this.drink = drink;
    }

    public DrinkRecord(User user, Drink drink, int quantity) {
        /**
         * constructor of DrinkRecords using its Drink object
         */
        this.user = user;
        this.drink = drink;
        this.quantity = quantity;
    }

    public DrinkRecord(int id, int quantity, int drink, User user, Connection conn) throws SQLException {
        /**
         * constructor of DrinkRecord using drink_id -> loads the drink from DB
         */
        this.user = user;
        this.id = id;
        this.quantity = quantity;
        this.drink = new Drink(drink, conn);
    }

    public void saveToDb(Connection conn) throws SQLException {
        /**
         * saves drinkRecord to db, depending on whether it already exists inserts new row or just updates current one.
         */
        if (this.id == 0) {
            this.addToDb(conn);
        } else {
            this.updateDb(conn);
        }

    }

    public int addToDb(Connection conn) throws SQLException {
        /**
         * Adds record to DB
         */
        String cmd = "INSERT INTO drink_records (quantity, drink, user_id) VALUES ";
        Statement stmt = conn.createStatement();
        System.out.println(cmd + this.toStringValues() + ";");
        if (this.id != 0) {
            System.err.println("DrinkRecord " + this.id + " has id, thus it should be just updated, not inserted. Check what you are doing");
            return -1;
        }
        try {
            stmt.execute(cmd + this.toStringValues() + ";");
        } catch (SQLException e) {
            System.err.println("DrinkRecord " + this.id + " could not be added");
            return -2;
        }
//        this.id = this.getIdByName(conn);
        return 0;
    }

    public int updateDb(Connection conn) throws SQLException {
        /**
         * updates row in the for this drink
         */
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

    /* DrinkRecord setters */

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /* End of setters */
    @Override
    public String toString() {
        return  "id=" + id +
                ", quantity=" + quantity +
                ", drink=" + drink.getId() +
                ", user_id=" + user.getId();
    }

    /* DrinkRecord getters */

    public int getQuantity() { return this.quantity; }

    public Drink getDrink() { return this.drink; }

    public String toStringValues() {
        return  "(" +
                quantity + ", " +
                drink.getId() + ", " +
                user.getId() +
                ")";
    }
}
