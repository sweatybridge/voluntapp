package utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentHashSet<T> implements Iterable<T> {
  
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
  
  public void remove(Object o) {
    writeLock.lock();
    set.remove(o);
    writeLock.unlock();
  }
  
  public Iterator<T> iterator() {
    readLock.lock();
    Iterator<T> iter = set.iterator();
    readLock.unlock();
    return iter;
  }
  
}
