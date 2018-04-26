package se.medituner.app;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Queue;
import se.medituner.app.MojoScreen;

/**
 * A utility class for persistence.
 *
 * @author Julia Danek, Sasa Lekic, Grigory Glukhov
 */
public class Persistence {

    private final Context context;

    public Persistence(Context context) {
        this.context = context;
    }

    /**
     * Save an object with a given filename, privately for given persistence context.
     *
     * @param object The object to save.
     * @param filename The filename of the object to save to.
     * @throws IOException Something terrible happened to one of the output streams.
     * @author Julia Danek, Sasa Lekic, Grigory Glukhov
     */
    public void saveObject(Object object, String filename) throws IOException {
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
    }

    /** Load an object of given filename, privately for given persistence context.
     *
     * @param filename Filename of the object to load.
     * @return The loaded object
     * @throws IOException              Something terrible happened to one of the input streams.
     * @throws ClassNotFoundException   Couldn't find corresponding object class.
     * @author Julia Danek, Sasa Lekic, Grigory Glukhov
     */
    public Object loadObject(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object retObj = ois.readObject();
        ois.close();
        return retObj;
    }
}
