package Repositories.Specifications;

import Helpers.Enjoyer;
import Logging.Logger;
import Repositories.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class EnjoyerRepository implements Repository<Enjoyer, SqlSpecification> {
    Logger logger;
    Connection connection;

    public EnjoyerRepository(Logger logger) throws SQLException {
        this.logger = logger;

        try{
            connection = DriverManager.getConnection("","","");
            connection.setAutoCommit(true);
        }
        catch(Exception e){
            throw e;
        }
    }

    @Override
    public boolean insert(Enjoyer entity) {
        try{
            PreparedStatement statement = connection.prepareStatement(
                    "IF NOT EXISTS(" +
                            "SELECT * FROM Enjoyer WHERE " +
                            "EnjoyerId == ? OR " +
                            "Email == ?)" +
                            "BEGIN " +
                                "INSERT INTO Enjoyer VALUES(DEFAULT, ?, ?, ?, NULL) " +
                            "END;");

            statement.setLong(1, entity.getId());
            statement.setString(2, entity.getEmail());

            //generate password 'hash'
            byte[] salt = new byte[128];
            new Random().nextBytes(salt);
            byte[] password = entity.getPasswordHash().getBytes();

            for (int i = 0; i < password.length; i++){
                password[i] = (byte)~(password[i] | (salt[i]));
            }

            //fill in remaining data
            statement.setString(3, entity.getEmail());
            statement.setBytes(4, password);
            statement.setBytes(5, salt);


            return statement.execute();
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return false;
    }

    @Override
    public boolean delete(Enjoyer entity) {
        try{
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM Enjoyer WHERE EnjoyerId == ? OR Email == ?;");

            statement.setLong(1, entity.getId());
            statement.setString(2, entity.getEmail());

            return statement.execute();
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return false;
    }

    @Override
    public Collection<Enjoyer> getAll() {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Enjoyer;");

            ResultSet result = statement.executeQuery();

            return getEnjoyersFromResult(result);
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return null;
    }

    @Override
    public Collection<Enjoyer> query(SqlSpecification specification) {
        if(specification != null){
            try{
                ResultSet result = specification.toSqlStatement(connection).executeQuery();
                return getEnjoyersFromResult(result);
            }
            catch(SQLException e){
                logger.logError(e);
            }
        }

        return null;
    }

    @Override
    public Enjoyer getById(long id) {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM Enjoyer WHERE EnjoyerId == ?;");
            statement.setLong(1, id);

            ResultSet result = statement.executeQuery();

            Enjoyer enjoyer = getEnjoyersFromResult(result).get(0);
            return enjoyer;
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return null;
    }

    private ArrayList<Enjoyer> getEnjoyersFromResult(ResultSet result) throws SQLException {
        ArrayList<Enjoyer> fetchedEnjoyers = new ArrayList<>();
        while(result.next()){
            Enjoyer enjoyer = new Enjoyer(
                                    result.getLong(0),
                                    result.getString(1),
                                    new String(result.getBytes(2)),
                                    result.getBytes(3)
            );

            fetchedEnjoyers.add(enjoyer);
        }

        if(!(fetchedEnjoyers.size() == 0)){
            return fetchedEnjoyers;
        }

        return null;
    }
}
