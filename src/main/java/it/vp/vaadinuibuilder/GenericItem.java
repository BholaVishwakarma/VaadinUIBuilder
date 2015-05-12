package it.vp.vaadinuibuilder;

import com.vaadin.ui.Component;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 *
 * @author cuta
 */
public class GenericItem {
    
    private String className;
    private String objName;
    private Class iClass;
    private HashMap<String, Component> properties;

    public GenericItem(String name, Class iClass) {
        this.className = name;
        this.iClass = iClass;
        properties = new HashMap<String, Component>();
    }

    public Class getIclass() {
        return iClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String name) {
        this.className = name;
    }

    @Override
    public String toString() {
        if(objName != null && objName.length() > 0) {
            return className + " ("+objName+")";    
        } else {
            return className; 
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new GenericItem(this.className, this.iClass);
    }

    public void setProperties(HashMap<String, Component> values) {
        properties.clear();
        if(values != null && values.size() > 0) {
            properties.putAll(values);
        }
    }

    public HashMap<String, Component> getProperties() {
        return properties;
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public boolean hasMethod(String methodname) {
        Method[] methods = iClass.getMethods();
        for (Method m: methods) {
            if (m.getName().compareTo(methodname) == 0) {
                return true;
            }
        }
        return false;
    }
    
    public Method getMethod(String methodname) {
        Method[] methods = iClass.getMethods();
        for (Method m: methods) {
            if (m.getName().compareTo(methodname) == 0) {
                return m;
            }
        }
        return null;
    }
    
}