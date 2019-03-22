package Repositories.Specifications.CharacterSpecifications;

import Repositories.Specifications.SqlSpecification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CharactersByEnjoyerIdSpecification implements SqlSpecification {
    long id;

    public CharactersByEnjoyerIdSpecification(long id){
        this.id = id;
    }

    @Override
    public PreparedStatement toSqlStatement(Connection connection) throws SQLException {
        PreparedStatement statement;

        try
        {
            statement = connection.prepareStatement("SELECT * FROM Character WHERE EnjoyerId == ?;");
            statement.setLong(1, id);
        }
        catch(Exception e){
            throw e;
        }

        return statement;
    }
}
