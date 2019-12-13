package edu.baylor.ecs.prophet.bounded.context;

import com.google.gson.Gson;
import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.SystemContext;

import java.io.*;

public class FileManager {

    public static SystemContext readSystemContextFromFile(String fileName) throws FileNotFoundException {
        Gson gson = new Gson();
        Reader in = new FileReader(fileName);
        return gson.fromJson(in, SystemContext.class);
    }
}
