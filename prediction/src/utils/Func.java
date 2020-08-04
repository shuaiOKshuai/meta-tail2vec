package utils;

public interface Func<T,V>  {     
    public T call(V data) throws Exception;     
}
