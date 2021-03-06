package Repositories.Specifications.CharacterSpecifications;

import Repositories.Specifications.SqlSpecification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CharacterByEnjoyerIdSpecification implements SqlSpecification {
    long id;
    long charId;

    public CharacterByEnjoyerIdSpecification(long id, long charId){
        this.charId = charId;
        this.id = id;
    }

    @Override
    public PreparedStatement toSqlStatement(Connection connection) throws SQLException {
        PreparedStatement statement;
        try{
            statement = connection.prepareStatement("SELECT 1 FROM Character WHERE EnjoyerId == ? AND CharacterId == ?;");
            statement.setLong(1, id);
            statement.setLong(2, charId);
        }
        catch(Exception e){
            throw e;
        }

        return statement;
    }
}
