package Repositories.Specifications.EnjoyerSpecifications;

import Helpers.Enjoyer;
import Repositories.Specifications.SqlSpecification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnjoyerBySigninCredentials implements SqlSpecification {
    String email;
    String password;

    public EnjoyerBySigninCredentials(String email, String password){
        this.email = email;
        this.password = password;
    }

    @Override
    public PreparedStatement toSqlStatement(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM Enjoyer WHERE Email == ?;");
        statement.setString(1, email);

        ResultSet result = statement.executeQuery();
        Enjoyer enjoyer;
        while(result.next()){
            enjoyer = new Enjoyer(
                            result.getLong(0),
                            result.getString(1),
                            new String(result.getBytes(2)),
                            result.getBytes(3));

            if(enjoyer.getId() != 0){
                byte[] passwordBytes = password.getBytes();
                byte[] passwordSalt = enjoyer.getSalt();

                boolean correctPass = true;
                byte[] actualPassword = enjoyer.getPasswordHash().getBytes();
                //TODO: replace with actual hashing and such, currently just some simple bitshifting for proof of concept
                for (int i = 0; i < passwordBytes.length; i++){
                    passwordBytes[i] = (byte)~(passwordBytes[i] | (passwordSalt[i]));

                    if(passwordBytes[i] != actualPassword[i])
                    {
                        correctPass = false;
                        break;
                    }
                }

                if(correctPass){
                    return statement;
                }

                return null;
            }
        }


        return null;
    }
}
