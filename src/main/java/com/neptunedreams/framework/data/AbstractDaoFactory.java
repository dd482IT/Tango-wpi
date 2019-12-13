package com.neptunedreams.framework.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.initialization.qual.UnderInitialization;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/12/17
 * <p>Time: 11:50 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public class AbstractDaoFactory {
  private final Map<Class<?>, Dao<?, ?, ?>> daoMap = new HashMap<>();

  @SuppressWarnings("JavaDoc")
  protected final <T, PK, F extends DBField> void addDao(@UnderInitialization AbstractDaoFactory this, Class<T> tClass, Dao<T, PK, F> tDao) {
    daoMap.put(tClass, tDao);
  }

  @SuppressWarnings("JavaDoc")
  public <T, PK, F extends DBField> Dao<T, PK, F> getDao(Class<T> tClass) {
    //noinspection unchecked
    return Objects.requireNonNull((Dao<T, PK, F>) daoMap.get(tClass));
  } 
}
