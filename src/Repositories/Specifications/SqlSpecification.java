package Repositories.Specifications;

import java.sql.Connection;
import java.sql.PreparedStatement;

public interface SqlSpecification extends Specification {
    PreparedStatement toSqlStatement(Connection connection);
}
