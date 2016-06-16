package org.wso2.carbon.ml.core.spark.transformations;

import java.io.*;

/**
 * Created by pekasa on 16.06.16.
 */
public class SerializeParameter implements java.io.Serializable{

    /** Write object to Base64 string. */
    public static String toString( Object o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = java.util.Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

}
