package DataBase;

import Collection.*;

import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

public class DataBaseManagerTickets {

    public TicketsList getTickList() {
        return tickList;
    }

    public BaseOwners getOwners() {
        return owners;
    }

    private TicketsList tickList;
    private BaseOwners owners = null;
    ReentrantLock rLock = new ReentrantLock();

    private String user = "programiifromskolkovo";
    private String password = "programiifromskolkovo";
    private String url = "jdbc:postgresql://localhost:5432/Tickets";
    public Connection connection;

    public DataBaseManagerTickets(Connection c) {
        connection = c;
    }

    public DataBaseManagerTickets(String u, String p, String ur) {
        user = u;
        password = p;
        url = ur;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int sendTown(Location town, boolean insert) throws SQLException {
        if (town == null) {
            return 0;
        }
        String sql;
        if (insert) {
            sql = "INSERT INTO locations (id, x, y, z, name) VALUES (DEFAULT, (?), (?), (?), (?)) RETURNING id;";
        } else {
            sql = "UPDATE locations SET " +
                    "x = (?), y = (?), z = (?), name = (?) WHERE id = (?) RETURNING id;";
        }
        PreparedStatement st = connection.prepareStatement(sql);

        st.setDouble(1, town.getX());
        st.setFloat(2, town.getY());
        st.setLong(3, town.getZ());
        st.setString(4, town.getName());

        if (!insert) {
            st.setInt(5, town.getId());
        }

        ResultSet resultSet = st.executeQuery();
        if (resultSet.next()) {
            int i = resultSet.getInt("id");
            town.setId(i);
            return i;
        }
        throw new SQLException();
    }

    private int sendAddress(Address addr, boolean insert) throws SQLException {
        if (addr == null) {
            return 0;
        }
        String sql;
        if (insert) {
            sql = "INSERT INTO address (id, zipcode, town) VALUES (DEFAULT, (?), (?)) RETURNING id;";
        } else {
            sql = "UPDATE address SET " +
                    "zipcode = (?), town = (?) WHERE id = (?) RETURNING id;";
        }
        PreparedStatement st = connection.prepareStatement(sql);
        st.setString(1, addr.getZipCode());
        int i = sendTown(addr.getTown(), insert);
        st.setInt(2, i);
        if (!insert) {
            st.setInt(3, addr.getId());
        }
        ResultSet resultSet = st.executeQuery();
        if (resultSet.next()) {
            i = resultSet.getInt("id");
            addr.setId(i);
            return i;
        }
        throw new SQLException();
    }

    private int sendVenue(Venue ven, boolean insert) throws SQLException {
        String sql;
        if (insert) {
            sql = "INSERT INTO venues (id, name, capacity, type, address) VALUES (DEFAULT, (?), (?), (?), (?)) RETURNING id;";
        } else {
            sql = "UPDATE venues SET " +
                    "name = (?), capacity = (?), type = (?), address = (?) WHERE id = (?) RETURNING id;";
        }
        PreparedStatement st = connection.prepareStatement(sql);
        st.setString(1, ven.getName());
        st.setInt(2, ven.getCapacity());
        st.setString(3, ven.getType().toString());
        int i = sendAddress(ven.getAddress(), insert);

        st.setInt(4, i);

        if (!insert) {
            st.setInt(5, (int) ven.getId());
        }

        ResultSet resultSet = st.executeQuery();
        if (resultSet.next()) {
            i = resultSet.getInt("id");
            ven.setId(i);
            return i;
        }
        throw new SQLException();
    }

    private int sendCoordinates(Coordinates coord, boolean insert) throws SQLException {
        String sql;
        if (insert) {
            sql = "INSERT INTO coordinates (id, x, y) VALUES (DEFAULT, (?), (?)) RETURNING id;";
        } else {
            sql = "UPDATE coordinates SET " +
                    "x = (?), y = (?) WHERE id = (?) RETURNING id;";
        }
        PreparedStatement st = connection.prepareStatement(sql);


        st.setLong(1, coord.getX());
        st.setDouble(2, coord.getY());

        if (!insert) {
            st.setInt(3, coord.getId());
        }

        ResultSet resultSet = st.executeQuery();
        if (resultSet.next()) {
            int i = resultSet.getInt("id");
            coord.setId(i);
            return i;
        }
        throw new SQLException();
    }

    public int sendTicket(Ticket tick, String key, boolean insert) {
        Savepoint save;
        try {
            connection.setAutoCommit(false);
            save = connection.setSavepoint();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        String sql;
        if (insert) {
            sql = "INSERT INTO tickets (id, " +
                    "name, " +
                    "coordinates, " +
                    "creationdate, " +
                    "price, " +
                    "comment, " +
                    "refundable, " +
                    "type, " +
                    "venue, " +
                    "userofticket, " +
                    "key) VALUES (DEFAULT, " +
                    "(?), (?), (?), (?), (?), (?), (?), (?), (?), (?)) RETURNING id;";
        } else {
            sql = "UPDATE tickets SET " +
                    "name = (?), " +
                    "coordinates = (?), " +
                    "creationdate = (?), " +
                    "price = (?), " +
                    "comment = (?), " +
                    "refundable = (?), " +
                    "type = (?), " +
                    "venue = (?), " +
                    "userofticket = (?), " +
                    "key = (?) WHERE id = (?) RETURNING id;";
        }
        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setString(1, tick.getName());
            int i = sendCoordinates(tick.getCoordinates(), insert);

            st.setInt(2, i);
            st.setString(3, tick.getCreationDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss ZZ")));
            st.setLong(4, tick.getPrice());
            st.setString(5, tick.getComment());
            st.setBoolean(6, tick.isRefundable());
            st.setString(7, tick.getType().toString());
            i = sendVenue(tick.getVenue(), insert);
            st.setInt(8, i);
            st.setInt(9, tick.getTowner().getId());
            st.setString(10, key);

            if (!insert) {
                st.setInt(11, (int) tick.getId());
            }

            ResultSet resultSet = st.executeQuery();
            if (resultSet.next()) {
                i = resultSet.getInt("id");
                tick.setId(i);
                connection.commit();
                connection.releaseSavepoint(save);
                resultSet.close();
                return i;
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.rollback();
            connection.releaseSavepoint(save);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int sendTOwner(TicketOwner towner, boolean insert) {
        String sql;
        if (insert) {
            sql = "INSERT INTO users (id, name, password, mail) VALUES (DEFAULT, (?), (?), (?)) RETURNING id;";
        } else {
            sql = "UPDATE users SET name = (?), password = (?), mail = (?) WHERE id = (?) RETURNING id;";
        }
        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setString(1, towner.getName());
            st.setBytes(2, towner.getPassword());
            st.setString(3, towner.getMail());

            if (!insert) {
                st.setInt(4, towner.getId());
            }

            ResultSet resultSet = st.executeQuery();
            if (resultSet.next()) {
                int i = resultSet.getInt("id");
                towner.setId(i);
                return i;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private Location receiveTown(int id) {
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM locations WHERE id = (?);")) {
            st.setInt(1, id);

            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {
                Double x = resultSet.getDouble("x");
                Float y = resultSet.getFloat("y");
                Long z = resultSet.getLong("z");
                String name = resultSet.getString("name");
                return new Location(id, x, y, z, name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Address receiveAddress(int id) {
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM address WHERE id = (?);")) {
            st.setInt(1, id);

            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {
                String zipcode = resultSet.getString("zipcode");
                int loc = resultSet.getInt("town");
                Location town;
                if (loc == 0) {
                    town = null;
                } else {
                    town = receiveTown(loc);
                }
                return new Address(id, zipcode, town);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Venue receiveVenue(int id) {
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM venues WHERE id = (?);")) {
            st.setInt(1, id);

            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {
                id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Integer capacity = resultSet.getInt("capacity");
                VenueType type = VenueType.valueOf(resultSet.getString("type"));
                int addr = resultSet.getInt("address");
                Address address;
                if (addr == 0) {
                    address = null;
                } else {
                    address = receiveAddress(addr);
                }
                return new Venue(id, name, capacity, type, address);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Coordinates receiveCoordinates(int id) {
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM coordinates WHERE id = (?);")) {
            st.setInt(1, id);

            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {

                long x = resultSet.getLong("x");
                double y = resultSet.getDouble("y");

                return new Coordinates(id, x, y);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Ticket receiveTicket(int id) {
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM tickets WHERE id = (?);")) {
            st.setInt(1, id);

            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {
                id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Coordinates coordinates = receiveCoordinates(resultSet.getInt("coordinates"));
                ZonedDateTime creationDate = ZonedDateTime.parse(resultSet.getString("creationdate"), DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss ZZ"));
                long price = resultSet.getLong("price");
                String comment = resultSet.getString("comment");
                boolean refundable = resultSet.getBoolean("refundable");
                TicketType type = TicketType.valueOf(resultSet.getString("type"));

                Venue venue = receiveVenue(resultSet.getInt("venue"));

                TicketOwner ticketOwner = receiveTOwner(resultSet.getInt("userofticket"));
                String key = resultSet.getString("key");

                if (!owners.containsKey(ticketOwner.getId())) {
                    owners.put(ticketOwner.getId(), ticketOwner);
                }
                Ticket t = new Ticket(id, name, coordinates, creationDate, price, comment, refundable, type, venue, owners.get(ticketOwner.getId()));
                t.setKey(key);
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TicketOwner receiveTOwner(int id) {
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM users WHERE id = (?);")) {
            st.setInt(1, id);

            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {

                String name = resultSet.getString("name");
                byte[] password = resultSet.getBytes("password");
                String mail = resultSet.getString("mail");

                return new TicketOwner(id, name, password, mail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Doesn't get TOwner");
        return null;
    }

    private boolean deleteTown(Location town) throws SQLException {
        if (town == null) {
            return true;
        }
        PreparedStatement st = connection.prepareStatement("DELETE FROM locations WHERE id = (?) RETURNING id;");

        st.setInt(1, (int) town.getId());

        ResultSet resultSet = st.executeQuery();

        if (resultSet.next()) {
            return (town.getId() == resultSet.getInt("id"));
        }

        throw new SQLException();
    }

    private boolean deleteAddress(Address addr) throws SQLException {
        if (addr == null) {
            return true;
        }
        boolean work = false;
        PreparedStatement st = connection.prepareStatement("DELETE FROM address WHERE id = (?) RETURNING id;");

        st.setInt(1, (int) addr.getId());

        ResultSet resultSet = st.executeQuery();

        if (resultSet.next()) {
            work = (addr.getId() == resultSet.getInt("id"));
            work = work & deleteTown(addr.getTown());
            return work;
        }
        throw new SQLException();
    }

    private boolean deleteVenue(Venue ven) throws SQLException {
        if (ven == null) {
            return false;
        }
        boolean work = false;
        PreparedStatement st = connection.prepareStatement("DELETE FROM venues WHERE id = (?) RETURNING id;");
        st.setInt(1, (int) ven.getId());
        ResultSet resultSet = st.executeQuery();
        if (resultSet.next()) {
            work = (ven.getId() == resultSet.getInt("id"));
            work = work & deleteAddress(ven.getAddress());
            return work;
        }
        throw new SQLException();
    }

    private boolean deleteCoordinates(Coordinates coord) throws SQLException {
        if (coord == null) {
            return false;
        }
        PreparedStatement st = connection.prepareStatement("DELETE FROM coordinates WHERE id = (?) RETURNING id;");
        st.setInt(1, (int) coord.getId());
        ResultSet resultSet = st.executeQuery();
        if (resultSet.next()) {
            return (coord.getId() == resultSet.getInt("id"));
        }
        throw new SQLException();
    }

    public boolean deleteTicket(Ticket tick) {
        if (tick == null) {
            System.out.println("Can't delete null");
            return false;
        }
        Savepoint save;
        try {
            connection.setAutoCommit(false);
            save = connection.setSavepoint();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        boolean work = false;
        try (PreparedStatement st = connection.prepareStatement("DELETE FROM tickets WHERE id = (?) RETURNING id;")) {
            st.setInt(1, (int) tick.getId());
            ResultSet resultSet = st.executeQuery();
            if (resultSet.next()) {
                work = (tick.getId() == resultSet.getInt("id"));
                work = work & deleteCoordinates(tick.getCoordinates()) && deleteVenue(tick.getVenue());
                if(work){
                    connection.commit();
                }else {
                    connection.rollback();
                }
                connection.releaseSavepoint(save);
                resultSet.close();
                return work;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.rollback();
            connection.releaseSavepoint(save);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTOwner(TicketOwner towner) {
        try (PreparedStatement st = connection.prepareStatement("DELETE FROM users WHERE id = (?) RETURNING id;")) {
            st.setInt(1, towner.getId());

            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {
                return (towner.getId() == resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean fillTable(TicketsList ticketsList, BaseOwners baseOwners) {
        tickList = ticketsList;
        if (fillOwnersTable(baseOwners)) {
            try (PreparedStatement st = connection.prepareStatement("SELECT * FROM tickets;")) {
                ResultSet resultSet = st.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    Coordinates coordinates = receiveCoordinates(resultSet.getInt("coordinates"));
                    ZonedDateTime creationDate = ZonedDateTime.parse(resultSet.getString("creationdate"), DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss ZZ"));
                    long price = resultSet.getLong("price");
                    String comment = resultSet.getString("comment");
                    boolean refundable = resultSet.getBoolean("refundable");
                    TicketType type = TicketType.valueOf(resultSet.getString("type"));
                    Venue venue = receiveVenue(resultSet.getInt("venue"));
                    String key = resultSet.getString("key");
                    TicketOwner ticketOwner = receiveTOwner(resultSet.getInt("userofticket"));
                    Ticket t = new Ticket(id, name, coordinates, creationDate, price, comment, refundable, type, venue, owners.get(ticketOwner.getId()));
                    t.setKey(key);
                    ticketsList.put(key, t);
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean fillOwnersTable(BaseOwners baseOwners) {
        owners = baseOwners;
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM users;")) {
            ResultSet resultSet = st.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                if (!baseOwners.containsKey(id)) {
                    String name = resultSet.getString("name");
                    byte[] password = resultSet.getBytes("password");
                    String mail = resultSet.getString("mail");
                    baseOwners.put(id, new TicketOwner(id, name, password, mail));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}