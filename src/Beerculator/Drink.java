package Beerculator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
@ManagedBean(name="drink")
@RequestScoped

public class Drink {

  private  int id = 0;
  private String name;
  private   int volume; // must be in ml
  private double alcohol; // must be in percentage

    public Drink(String name, int volume, double alcohol) {
        this.name = name;
        this.volume = volume;
        this.alcohol = alcohol;
    }

    public Drink(int id, String name, int volume, double alcohol) {
        this.id = id;
        this.name = name;
        this.volume = volume;
        this.alcohol = alcohol;
    }

    public double A() {
        /**
         * Calculates the "A" value for the formula
         */
        return ((this.volume * this.alcohol * 0.8) / 100);
    }

    public Drink(int id, Connection conn) throws SQLException {
        /**
         * Constructor for drink, loads it from DB by its id
         * */
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

    public static HashMap<Integer, Drink> getDrinkList(Connection conn) throws SQLException {
        /**
         * Loads all drinks drom DB and returns them as hashmap
         * */
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

    public String toStringValues() {
        return "('" + name +
                "', '" + volume +
                "', '" + alcohol +
                "')";
    }

    @Override
    public String toString() {
        return "name='" + name +
                "', volume=" + volume +
                ", alcohol=" + alcohol;
    }

    public int addToDb(Connection conn) throws SQLException {
        /**
         * inserts row into db for this drink and sets its id
         */
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

    public int getIdByName(Connection conn) throws SQLException {
        /**
         * returns id of drink by its name
         */
        String cmd = "SELECT id FROM drinks WHERE name='" + this.name + "';";
//        System.out.println(cmd);
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(cmd);
        if (!result.next()) {
            System.err.println("Drink " + this.name + "is not in the db.");
            return -1;
        }
        return result.getInt(1);
    }

    public int updateDb(Connection conn) throws SQLException {
        /**
         * updates row in the for this drink
         */
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

    public void saveToDb(Connection conn) throws SQLException {
        /**
         * saves drink to db, depending on whether it already exists insersts new row or just updates current one.
         */
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

}

