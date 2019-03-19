package Repositories.Specifications.CharacterSpecifications;

import Repositories.Specifications.SqlSpecification;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CharacterByEnjoyerIdSpecification implements SqlSpecification {
    long id;

    public CharacterByEnjoyerIdSpecification(long id){
        this.id = id;
    }

    @Override
    public PreparedStatement toSqlStatement(Connection connection) {
        PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM Character WHERE "); //TODO:
    }
}
