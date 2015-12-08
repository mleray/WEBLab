/**
 *  Main file of project beerculator serving for comutation of BAC.
 */

package Beerculator;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * @author Tomáš Sekanina
 * @author Patricio Sanchez
 * @author Maud Leray
 * @author Alvaro Gonzalez
 */

/**
 * Bean representing the user. Works as a session scope
 */
@ManagedBean(name = "userBean")
@SessionScoped
public class User {

    private int id;
    private String session_id;
    private int weight;
    private Boolean gender = true; // change to String
    private HashMap<Integer, DrinkRecord> drink_records;
    private double BAC = -1;
    private Date start;

    /**
     * returns list of keys of drink_records
     * @return The returnd ArrayList 
     */
    public ArrayList<Integer> getDrinkRecordsKeys() {
        ArrayList<Integer> dRKeys = new ArrayList<>();
        dRKeys.addAll(this.drink_records.keySet());
        return dRKeys;
    }

    /**
     * method which loads the user from db
     * @param session_id session id by which the user is loaded from db
     * @throws SQLException
     */
    public void initialize(String session_id) throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/beerculator";

        Properties props = new Properties();
        props.setProperty("user", "beerculator_admin");
        props.setProperty("password", "beer");
        Connection conn = DriverManager.getConnection(url, props);

        if (!session_id.equals("") || this.session_id != null) {
            if (!session_id.equals("")) {
                this.session_id = session_id;
            }
            this.getFromDb(conn);
        } else {
            this.setNewSessionID();
        }
        conn = DriverManager.getConnection(url, props);
        this.loadDrinkRecords(conn);
    }

    /**
     * Constructor for user which is called on start of the session
     */
    public User() {
        if (this.session_id == null) {
            this.drink_records = new HashMap<>();
            this.start = new Date();
        }
    }

    /**
     * Constructor for user used for testing
     */
    public User(int weight, Boolean gender) {
        this.setNewSessionID();
        this.weight = weight;
        this.gender = gender;
        this.drink_records = new HashMap<>();

    }

    /**
     * Method called when reset button pressed. Serves for creating a new session
     * @return redirect string
     */
    public String reset() {
        this.id = 0;
        this.weight = 0;
        this.gender = null;
        this.drink_records = new HashMap<>();
        this.BAC = -1;
        return "viewId?faces-redirect=true";
    }

    public String getNewSessionID() {
        String session_id = "" + Math.abs(Objects.toString(System.currentTimeMillis()).hashCode());
        session_id = "" + session_id.substring(session_id.length() - 9, session_id.length() - 1);
        return session_id;
    }

    public void setNewSessionID() {
        this.session_id = getNewSessionID();
    }
//    public User(String session_id, Connection conn) throws SQLException {
//        /**
//         * Constructor for user using the session_id
//         */
//        this.session_id = session_id;
//        this.drink_records = new HashMap<>();
//        this.getFromDb(conn);
//        this.loadDrinkRecords(conn);
//    }

//    public User(String name, Boolean is_name, Connection conn) throws SQLException {
//        /**
//         * Constructor for user using its name
//         * purpose of arg is_name is only to overload constructor using session_id
//         */
//        this.name = name;
//        this.drink_records = new HashMap<>();
//        this.getFromDb(conn);
//        this.loadDrinkRecords(conn);
//    }


    public int loadDrinkRecords(Connection conn) throws SQLException {
        /**
         * Loads drinkRecords for user from DB including drinks that the user hasn't drank yet
         */
        int d;
        HashMap<Integer, Drink> menu = Drink.getDrinkList(conn);
        Iterator<Integer> drinkIterator = menu.keySet().iterator();
        while (drinkIterator.hasNext()) {
            Integer key = drinkIterator.next();
            Drink dr = menu.get(key);
            this.drink_records.put(key, new DrinkRecord(this, dr));
        }

        String cmd = "SELECT id, drink, quantity FROM drink_records WHERE user_id=" + this.id + ";";
//        System.out.println(cmd);
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(cmd);
        while (result.next()) {
            d = result.getInt("drink");
            this.drink_records.get(d).setQuantity(result.getInt("quantity"));
            this.drink_records.get(d).setId(result.getInt("id"));
            this.drink_records.get(d).setUser(this);
        }
        return 0;
    }

    public double hoursUntilSober() {
        /**
         * Gives the nb of hours before user is sober again
         */
        double bac = this.BAC/100;
        double hours = 0;
        if (bac < 0) {
            System.err.println("the BAC value is negative, there must be a mistake");
        } else if (bac == 0){
            hours = 0;
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
        } else {
            hours = 15;
        }
        return hours;
    }

    public double getAmountOfAlcohol() {
        /**
         * Gets the amount of alcohol for the user from drink_records
         */

        double amount = 0;
        int q;
        Drink d;
        Iterator<Integer> drinkIterator = drink_records.keySet().iterator();
        if(!drinkIterator.hasNext()){
            return 0;
        }
        while (drinkIterator.hasNext()) {
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
        if (this.gender) {
            ratio = 0.7;
        } else {
            ratio = 0.55;
        }
        if(a==0) return 0;
        return ((a / (this.weight * ratio)) - (0.015 * this.getHoursDrinking()));
    }


    public void calculateBAC() {
        /**
         * Fully calculates the BAC value
         */
        double alc = this.getAmountOfAlcohol();
        this.BAC = Math.abs(formula(alc));
    }

    public int getFromDb(Connection conn) throws SQLException {
        /**
         * loads user data from DB if it has session_id
         */
        String cmd;
        cmd = "SELECT id, weight, gender FROM users WHERE session_id='" + this.session_id + "';";
//        System.out.println(cmd);
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(cmd);
        if (!result.next()) {
            this.reset();

//            System.err.println("User with session id: " + this.session_id + "is not in the db.");
//            return -1;
        } else {

            this.id = result.getInt("id");
            this.weight = result.getInt("weight");
            this.gender = result.getBoolean("gender");
        }
        return 1;
    }

    public void saveToDb() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/beerculator";

        Properties props = new Properties();
        props.setProperty("user", "beerculator_admin");
        props.setProperty("password", "beer");
        Connection conn = DriverManager.getConnection(url, props);
        this.saveToDb(conn);
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
        for (Map.Entry<Integer, DrinkRecord> drinkRecord : this.drink_records.entrySet()) {
            if (drinkRecord.getValue().getQuantity() > 0) {
                drinkRecord.getValue().saveToDb(conn);
            }
        }

    }

    public int updateDb(Connection conn) throws SQLException {
        /**
         * updates row in the for this drink
         */
        Statement stmt = conn.createStatement();

        if (this.id == 0) {
            System.err.printf("User does not have id, thus it should be just inserted, not updated. Check what you are doing");
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
        String cmd = "INSERT INTO users (session_id, weight, gender) VALUES ";
        Statement stmt = conn.createStatement();
        System.out.println(cmd + this.toStringValues() + ";");
        if (this.id != 0) {
            System.err.println("User has id, thus it should be just updated, not inserted. Check what you are doing");
            return -1;
        }
        try {
            ResultSet rs = stmt.executeQuery(cmd + this.toStringValues() + " RETURNING id;");
            rs.next();
            this.id = rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("User could not be added");
            return -2;
        }
        return 0;
    }

    public String toStringValues() {
        return "('" + session_id +
                "', " + weight +
                ", " + gender +
                ")";
    }

    @Override
    public String toString() {
        return "session_id='" + session_id +
                "', weight=" + weight +
                ", gender=" + gender;
    }

    /* Setters for user */


    /**
     * setter of session id
     * @param session_id id to be set
     */
    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public void setDrinkQuantity(Drink drink, int quantity) {
        /**
         * Sets quantity of drink record of user by id of the drink
         * If the drink record does not exist yet it is created
         **/
        if (this.drink_records.containsKey(drink.getId())) {
            this.drink_records.get(drink.getId()).setQuantity(quantity);
        } else {
            this.drink_records.put(drink.getId(), new DrinkRecord(this, drink, quantity));
        }

    }

    public void setStart(Date start) {
//        this.start = start;
    }

    /* End of setters */

    /* User getters */

    public HashMap<Integer, DrinkRecord> getDrink_records() {
        return drink_records;
    }


    public int getId() {
        return id;
    }

    public String getSession_id() {
        return session_id;
    }

    public int getWeight() {
        return weight;
    }

    public Boolean getGender() {
        return gender;
    }

    public double getBAC() {
        if(BAC == -1) return 0;
        return BAC-BAC%0.01;
    }

    public Date getStart() {
        return start;
    }

    public static double hoursDiff(Date start, Date end) {
        double diff;
        diff = end.getTime() - start.getTime();
        diff /= (3600 * 1000);
        if(diff<1){
            return 1;
        }
        return diff;
    }

    public double getHoursDrinking() {
        Date now = new Date();
        return hoursDiff(this.start, now);
    }

    /* End of getters */

}

