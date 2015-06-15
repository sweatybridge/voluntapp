package utils;

public class Pair<T extends Comparable<? super T>, U> implements Comparable<Pair<T,U>> {
  private final T key;
  private U value;
  
  public Pair(T key, U value) {
    this.key = key;
    this.value = value;
  }
  
  public T getKey() {
    return key;
  }
  
  public U getValue() {
    return value;
  }
  
  public void setValue(U value) {
    this.value = value;
  }

  @Override
  public int compareTo(Pair<T, U> other) {
    return key.compareTo(other.key);
  }
  
  @Override
  public String toString() {
    return "(" + key.toString() + ", " + value.toString() + ")";
  }
  
  @Override
  public int hashCode() {
    return key.hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Pair)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    Pair<T, U> other = (Pair<T, U>) o;
    return (other.key.equals(key));
  }
  
}
