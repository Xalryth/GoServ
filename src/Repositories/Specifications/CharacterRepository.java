package Repositories.Specifications;

import Helpers.Character;
import Helpers.CharacterClass;
import Logging.Logger;
import Repositories.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class CharacterRepository implements Repository<Character, SqlSpecification> {
    Logger logger;
    Connection connection;

    public CharacterRepository(Logger logger) throws SQLException {
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
    public boolean insert(Character entity) {
        try{
            PreparedStatement statement = connection.prepareStatement(
                    "IF NOT EXISTS(" +
                            "SELECT * FROM Character WHERE " +
                            "CharacterId == ? OR " +
                            "Name == ?)" +
                                "BEGIN " +
                                "INSERT INTO Character VALUES(?, ?, ?, DEFAULT, DEFAULT) " +
                                "END;");

            statement.setLong(1, entity.getId());
            statement.setString(2, entity.getName());
            statement.setString(3, entity.getName());
            statement.setLong(4, entity.getEnjoyerId());
            statement.setByte(5, (byte)entity.getCharacterClass().ordinal());

            return statement.execute();
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return false;
    }

    @Override
    public boolean delete(Character entity) {
        try{
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM Character WHERE CharacterId == ? OR Name == ?;");

            statement.setLong(1, entity.getId());
            statement.setString(2, entity.getName());

            return statement.execute();
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return false;
    }

    @Override
    public Collection<Character> getAll() {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Character;");

            ResultSet result = statement.executeQuery();

            return getCharactersFromResult(result);
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return null;
    }

    @Override
    public Collection<Character> query(SqlSpecification specification) {
        try{
            ResultSet result = specification.toSqlStatement(connection).executeQuery();
            return getCharactersFromResult(result);
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return null;
    }

    @Override
    public Character getById(long id) {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM Character WHERE CharacterId == ?;");
            statement.setLong(1, id);

            ResultSet result = statement.executeQuery();

            Character character = getCharactersFromResult(result).get(0);
            return character;
        }
        catch(SQLException e){
            logger.logError(e);
        }

        return null;
    }

    private ArrayList<Character> getCharactersFromResult(ResultSet result) throws SQLException {
        ArrayList<Character> fetchedCharacters = new ArrayList<>();
        while(result.next()){
            Character newCharacter =
                    new Character(
                            result.getLong(1),
                            result.getLong(6),
                            result.getString(2),
                            result.getShort(5),
                            CharacterClass.values()[result.getByte(4)]);

            fetchedCharacters.add(newCharacter);
        }

        if(!(fetchedCharacters.size() == 0)){
            return fetchedCharacters;
        }

        return null;
    }
}
