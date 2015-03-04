package io.mkr.archivefs.archive;

/**
 * A {@link Creator} that contains logic to decorate the object created by another {@link Creator}.
 * @param <S> the class created by the decorated creator
 * @param <T> the class created by this creator
 */
public abstract class DecoratingCreator<S, T> implements Creator<T> {

  private Creator<S> innerCreator;

  public abstract T decorate(S o);

  public DecoratingCreator(Creator<S> innerCreator) {
    this.innerCreator = innerCreator;
  }

  @Override
  public T create() {
    return decorate(innerCreator.create());
  }

}
