package Repositories.Specifications;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SqlSpecification extends Specification {
    PreparedStatement toSqlStatement(Connection connection) throws SQLException;
}
