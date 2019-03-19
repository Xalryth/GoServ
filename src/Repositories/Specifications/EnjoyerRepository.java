package Repositories.Specifications;

import Helpers.Enjoyer;
import Repositories.Repository;

import java.util.Collection;

public class EnjoyerRepository implements Repository<Enjoyer, SqlSpecification> {
    @Override
    public void insert(Enjoyer entity) {

    }

    @Override
    public void delete(Enjoyer entity) {

    }

    @Override
    public Collection<Enjoyer> getAll() {
        return null;
    }

    @Override
    public Collection<Enjoyer> query(SqlSpecification specification) {
        return null;
    }

    @Override
    public Enjoyer getById(long id) {
        return null;
    }
}
