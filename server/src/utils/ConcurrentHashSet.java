package utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentHashSet<T> implements Iterable<T>, Set<T> {
  
  private final Set<T> set;
  private final ReadWriteLock lock;
  private final Lock readLock;
  private final Lock writeLock;
  
  public ConcurrentHashSet() {
    set = new HashSet<T>();
    lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();
  }
  
  public boolean add(T elem) {
    writeLock.lock();
    boolean result = set.add(elem);
    writeLock.unlock();
    return result;
  }
  
  public boolean isEmpty() {
    readLock.lock();
    boolean result = set.isEmpty();
    readLock.unlock();
    return result;
  }
  
  public boolean contains(Object o) {
    readLock.lock();
    boolean result = set.contains(o);
    readLock.unlock();
    return result;
  }
  
  public Object[] toArray() {
    readLock.lock();
    Object[] result = set.toArray();
    readLock.unlock();
    return result;
  }
  
  public void clear() {
    writeLock.lock();
    set.clear();
    writeLock.unlock();
  }
  
  public boolean remove(Object o) {
    writeLock.lock();
    boolean result = set.remove(o);
    writeLock.unlock();
    return result;
  }
  
  public Iterator<T> iterator() {
    readLock.lock();
    Iterator<T> iter = set.iterator();
    readLock.unlock();
    return iter;
  }

  @Override
  public int size() {
    readLock.lock();
    int size = set.size();
    readLock.unlock();
    return size;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    // ???
    return null;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    readLock.lock();
    boolean result = set.containsAll(c);
    readLock.unlock();
    return result;
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    writeLock.lock();
    boolean result = set.addAll(c);
    writeLock.unlock();
    return result;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    writeLock.lock();
    boolean result = set.retainAll(c);
    writeLock.unlock();
    return result;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    writeLock.lock();
    boolean result = set.removeAll(c);
    writeLock.unlock();
    return result;
  }
  
}
