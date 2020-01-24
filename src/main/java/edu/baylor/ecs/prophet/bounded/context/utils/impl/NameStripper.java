package edu.baylor.ecs.prophet.bounded.context.utils.impl;

import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.*;
import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.Module;

public class NameStripper {

    public static String getBasicName(String name){
        String [] parts = name.split("[^a-zA-Z0-9]+");
        return parts[parts.length - 1];
    }

    public static void sanitizeSystemContext(SystemContext toSanitize){
        for(Module m : toSanitize.getModules()){
            m.setName(new Name(getBasicName(m.getName().getName())));
            for(Entity e : m.getEntities()){
                // make the full name just the simple name
                e.getEntityName().setFullName(e.getEntityName().getName());
                for(Field f : e.getFields()){
                    f.getName().setFullName(getBasicName(f.getName().getName()));
                }
            }
        }
    }
}
