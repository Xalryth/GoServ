package Repositories;

import Repositories.Specifications.Specification;

import java.util.Collection;

public interface Repository<T, S extends Specification> {
    boolean insert(T entity);
    boolean delete(T entity);
    Collection<T> getAll();
    Collection<T> query(S specification);
    T getById(long id);
}
