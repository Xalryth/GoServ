package Repositories.Specifications;

import Helpers.Character;
import Repositories.Repository;

import java.util.Collection;

public class CharacterRepository implements Repository<Character, SqlSpecification> {
    @Override
    public void insert(Character entity) {

    }

    @Override
    public void delete(Character entity) {

    }

    @Override
    public Collection<Character> getAll() {
        return null;
    }

    @Override
    public Collection<Character> query(SqlSpecification specification) {
        return null;
    }

    @Override
    public Character getById(long id) {
        return null;
    }
}
