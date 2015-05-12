package it.vp.vaadinuibuilder;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Tree;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.Tree.TreeTargetDetails;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.reflections.Reflections;

/**
 *
 * @author cuta
 */
public class Main extends CustomComponent implements Button.ClickListener, Property.ValueChangeListener, ObjectNameListener {

    private ListSelect list;
    private Tree mainTree;
    private Table table;
    private Reflections reflections;
    private static final String CLASSES = "com.vaadin.ui";
    private final HorizontalLayout hl = new HorizontalLayout();
    private Button addBut, removeBut, expBut, previewBut, removeAllBut, updateComponentBut;
    
    public Main() throws IOException, ClassNotFoundException {
        hl.setMargin(true);
        hl.setSpacing(true);
        hl.setSizeFull();
        buildUI();
    }

    private void buildUI() throws IOException, ClassNotFoundException {
        
        //Delete all components
        hl.removeAllComponents();
        
        //Panel 1
        Panel panel1 = new Panel();
        panel1.setSizeFull();
       
        //Tree 
        mainTree = new Tree();
        mainTree.addItem("UI");
        mainTree.setImmediate(true);
        mainTree.addValueChangeListener(this);
        mainTree.setDragMode(TreeDragMode.NODE);
        mainTree.setDropHandler(new TreeSortDropHandler(mainTree, (HierarchicalContainer) mainTree.getContainerDataSource()));
        panel1.setContent(mainTree);
        
        //Panel 1
        Panel panel2 = new Panel();
        panel2.setSizeFull();
        
        VerticalLayout vl = new VerticalLayout();        
        vl.setMargin(true);
        vl.setSpacing(true);
        vl.setSizeFull();
        panel2.setContent(vl);
       
        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setSpacing(true);
        hl2.setSizeFull();
        
        //List
        list = new ListSelect();
        list.setSizeFull();
        list.setNullSelectionAllowed(false);
        
        list.addContainerProperty("list", String.class, null);
        
        reflections = new Reflections("com.vaadin.ui");
        for(Class<? extends AbstractComponent> e : reflections.getSubTypesOf(AbstractComponent.class)) {
            GenericItem item = new GenericItem(e.getName().replace("com.vaadin.ui.", ""), e);
            if(!item.getClassName().startsWith("components"))
                list.addItem(item).getItemProperty("list").setValue(item.getClassName());
        }
        IndexedContainer containerDataSource = (IndexedContainer) list.getContainerDataSource();
        containerDataSource.sort(new Object[]{"list"}, new boolean[] { true });
        
        //Buttons
        VerticalLayout vlBut = new VerticalLayout();
        vl.setSizeFull();
        vlBut.setSpacing(true);
        addBut = new Button("ADD", this);
        removeBut = new Button("REMOVE", this);
        removeAllBut = new Button("REMOVE ALL", this);
        expBut = new Button("EXPORT CODE", this);
        previewBut = new Button("PREVIEW", this);
        updateComponentBut = new Button("UPDATE COMPONENT", this);
        addBut.setWidth("100%");
        removeBut.setWidth("100%");
        removeAllBut.setWidth("100%");
        expBut.setWidth("100%");
        previewBut.setWidth("100%");
        updateComponentBut.setWidth("100%");
        updateComponentBut.setEnabled(false);
        vlBut.addComponents(addBut, removeBut, removeAllBut, expBut, previewBut, updateComponentBut);  
        
        //TABLE
        table = new Table();
        table.setImmediate(true);
        table.setSelectable(false);
        table.setSizeFull();
        
        table.addContainerProperty("METHOD", String.class, null);
        table.addContainerProperty("VALUE", Component.class, null);
        table.setVisible(false);
        
        hl2.addComponents(list, vlBut);
        vl.addComponents(hl2, table);
        
        hl.addComponents(panel1, panel2);
        setCompositionRoot(hl);
        
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        Button b = event.getButton();
        if(b == addBut && b != null && list.getValue() != null) {
            if(list != null && list.size() > 0) {
                Object value = mainTree.getValue();
                GenericItem item = null;
                try {
                    item = (GenericItem) ((GenericItem) list.getValue()).clone();
                    mainTree.addItem(item);
                    if(value != null) {
                        mainTree.setParent(item, value);
                        mainTree.expandItem(value);
                    } else {
                        mainTree.setParent(item, "UI");
                        mainTree.expandItem("UI");
                    }
                    openWindow(item);
                } catch (CloneNotSupportedException ex) {
                    //
                }
            }
        }
        if(b == removeBut && b != null) {
            if(mainTree.getValue() != null && !mainTree.getValue().equals("UI")) {
                GenericItem item = (GenericItem) mainTree.getValue();
                if(item != null) {
                    HierarchicalContainer h = (HierarchicalContainer) mainTree.getContainerDataSource();
                    h.removeItemRecursively(item);
                    table.removeAllItems();
                    table.setVisible(false);
                }            
            }
        }
        if(b == removeAllBut && b != null) {
            try { buildUI(); } catch (IOException ex) {  } catch (ClassNotFoundException ex) { }
        }        
        if(b == expBut && b != null) {
            String code = "";
            Collection<?> childrenUI = mainTree.getChildren("UI");
            if(childrenUI != null) {
                Iterator<?> iteratorUI = childrenUI.iterator();
                try {
                    while(iteratorUI.hasNext()) {
                        GenericItem item = (GenericItem) iteratorUI.next();
                        code += generateCode(item);
                    }
                    if(code.length() > 0) {
                        Window w2 = new Window("Code:");
                        w2.center();
                        w2.setWidth("80%");
                        w2.setHeight("80%");
                        TextArea ta = new TextArea(null, code);
                        ta.setSizeFull();
                        VerticalLayout vl2 = new VerticalLayout();
                        vl2.setSizeFull();
                        vl2.setMargin(true);
                        vl2.addComponent(ta);
                        w2.setContent(vl2);
                        getUI().addWindow(w2);
                    }
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            } else {
                Notification note = new Notification("The code is empty!", Notification.Type.WARNING_MESSAGE);
                note.setPosition(Position.TOP_CENTER);
                note.show(getUI().getPage());
            }
        }
        if(b == previewBut && b != null) {
            
            Collection<?> childrenUI = mainTree.getChildren("UI");
            if(childrenUI == null) {
                Notification note = new Notification("The tree is empty!", Notification.Type.WARNING_MESSAGE);
                note.setPosition(Position.TOP_CENTER);
                note.show(getUI().getPage());
            } else {
                VerticalLayout vl = new VerticalLayout();
                vl.setSizeFull();
                buildPreview(vl);

                Window w = new Window("Preview");
                w.center();
                w.setWidth("80%");
                w.setHeight("80%");
                w.setContent(vl);
                getUI().addWindow(w);
            }
            
        }
        if(b == updateComponentBut && b != null) {
            GenericItem value = (GenericItem) mainTree.getValue();
            if(value != null) {
                HashMap<String, Component> values = value.getProperties();
                Object itemids[] = table.getItemIds().toArray();
                if(itemids != null) {
                    for(Object itemid : itemids) {
                        if(itemid != null) {
                            Item item = table.getItem(itemid);
                            if(item != null) {
                                String method = (String) item.getItemProperty("METHOD").getValue(); 
                                Component val = (Component) item.getItemProperty("VALUE").getValue();
                                values.put(method, val); 
                            }
                        }
                    }
                }
                Notification note = new Notification(value.getObjName() + " updated", Notification.Type.HUMANIZED_MESSAGE);
                note.setPosition(Position.TOP_CENTER);
                note.show(getUI().getPage());
            }
        }   
        
    }
    
    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        if(event.getProperty().getValue() != null && !event.getProperty().getValue().equals("UI")) {
            table.removeAllItems();
            table.setVisible(true);
            updateComponentBut.setEnabled(true);
            GenericItem item = (GenericItem) event.getProperty().getValue();
            generateTableRows(item);
            table.sort(new Object[] { "METHOD", "VALUES" }, new boolean[] { true, false });
        } else {
            table.removeAllItems();
            table.setVisible(false);
            updateComponentBut.setEnabled(false);
        }
    }
    
    private String generateCode(GenericItem item) throws Exception {
        
        /** BASE STEP **/
        String objClass = item.getClassName();
        String objName = item.getObjName();
        String retval = objClass + " " + objName + " = new " + objClass + "(); \n";

        String setter = "";
        HashMap<String, Component> properties = item.getProperties();
        Set<String> keys = properties.keySet();
        Iterator<String> keyiter = keys.iterator();
        while (keyiter.hasNext()) {
            String key = keyiter.next();
            Component component = (Component) properties.get(key);
            if(component instanceof CheckBox) {
                    CheckBox cb = (CheckBox) component;
                    if(cb.getValue() != false) {
                        setter += objName+"."+key+"("+cb.getValue()+");\n";
                    }
            } else if(component instanceof TextField) {
                TextField tf = (TextField) component;
                if(tf.getValue().length() > 0) {
                    setter += objName+"."+key+"("+   "\""   +tf.getValue()+   "\""   +");\n";
                }
            }
            
        }
        retval += setter;

        /** FINAL STEP **/
        Collection<GenericItem> children = (Collection<GenericItem>) mainTree.getChildren(item);
        if ( children == null || children.size() == 0 ) {
            return retval;
        }

        if (item.hasMethod("addComponents")) {
            String childrennames = "";
            /** RECURSIVE STEP **/
            String childrenretval = "";
            Iterator<GenericItem> iterator = children.iterator();
            while (iterator.hasNext()) {
                GenericItem childitem = iterator.next();
                childrennames += (childrennames.length() > 0 ? "," : "") + childitem.getObjName();
                childrenretval += generateCode(childitem);
            }
            String rettotal = retval;
            rettotal += childrenretval;
            rettotal += objName + ".addComponents("+childrennames+");\n";
            return rettotal;
        } else {
            throw new Exception("You have added childrens to a node that not implementing interface ComponentContainer");
        }
        
    }

    private void buildPreview(ComponentContainer parent) {
        Collection<?> childrenUI = mainTree.getChildren("UI");
        if(childrenUI != null && childrenUI.size() > 0) {
            Iterator<?> iteratorUI = childrenUI.iterator();
            try {
                while(iteratorUI.hasNext()) {
                    GenericItem item = (GenericItem) iteratorUI.next();
                    parent.addComponent(generateComponent(item));
                }
            } catch (Exception ex) { Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);}
        }
    }
    
    private Component generateComponent(GenericItem item) throws Exception {
        
        /** BASE STEP **/
        Object obj = item.getIclass().newInstance();
        Class iclass = item.getIclass();
        
        HashMap<String, Component> properties = item.getProperties();
        Set<String> keys = properties.keySet();
        Iterator<String> keyiter = keys.iterator();
        
        while(keyiter.hasNext()) {
            String key = keyiter.next();
            Component value = properties.get(key);
            //check textfield properties is not empty
            if(value instanceof CheckVoid) {
                CheckVoid cv = (CheckVoid) value;
                if(cv.getValue() != false) {
                    Method method = iclass.getMethod(key, new Class[] { });
                    method.invoke(obj);
                }
            } else if(value instanceof CheckBox) {
                CheckBox cb = (CheckBox) value;
                if(cb.getValue() != false) {
                    Class[] args = new Class[1];
                    args[0] = boolean.class;
                    Method method = iclass.getMethod(key, args);
                    method.invoke(obj, cb.getValue());  
                }
            }
            if(value instanceof TextField) {
                TextField tf = (TextField) value;
                Class[] args = new Class[1];
                args[0] = String.class;
                if(tf.getValue().length() > 0) {
                    Method method = iclass.getMethod(key, args);
                    method.invoke(obj, tf.getValue()); 
                }
            }
        }
        
        /** FINAL STEP **/
        Collection<GenericItem> children = (Collection<GenericItem>) mainTree.getChildren(item);
        if(children == null || children.size() == 0) {
            return (Component)obj;
        }
        
        if (item.hasMethod("addComponent")) {
            /** RECURSIVE STEP **/
            Iterator<GenericItem> iterator = children.iterator();
            while (iterator.hasNext()) {
                GenericItem childitem = iterator.next();
                Component child = generateComponent(childitem); 

                Class[] args1 = new Class[1];
                args1[0] = Component.class;
                Method m = iclass.getMethod("addComponent", args1);
                m.invoke(obj, child); 
            }
            return (Component)obj;
        } else {
            throw new Exception("You have added childrens to a node that not implementing interface ComponentContainer");
        }
    }
    
    private void openWindow(GenericItem item) {
        Window win = new Window("Name:");
        ObjectNameComponent onc = new ObjectNameComponent();
        onc.setItem(item);
        onc.setWin(win);
        onc.setListener(this);
        win.center();
        win.setWidth("20%");
        win.setModal(true);
        win.setResizable(false);
        win.setClosable(false);
        win.setContent(onc);
        getUI().addWindow(win);
    }

    @Override
    public void onSubmit(GenericItem item) {
        mainTree.markAsDirty();
    }

    private void generateTableRows(GenericItem item) {        
        
        if(item != null && item.getProperties().isEmpty()) {
            
            try {
                Class clazz = item.getIclass(); //get Component Class
                Method m[] = clazz.getMethods();
                for(int i = 0; i < m.length; i++) {
                    Type[] genericParameterTypes = m[i].getGenericParameterTypes();
                    if(!m[i].getName().contains("Listener")) {
                        if(genericParameterTypes.length == 1) {
                            for(int j = 0; j < genericParameterTypes.length; j++) {
                                if(genericParameterTypes[j].toString().endsWith("boolean")) {
                                    CheckBox obj = new CheckBox();
                                    if(m[i].getName().equals("setEnabled") || m[i].getName().equals("setVisible")) {
                                        obj.setValue(true);
                                    }
                                    table.addItem(new Object[] { m[i].getName(), obj }, i);
                                }

                                if(genericParameterTypes[j].toString().endsWith("String")) {
                                    TextField obj = new TextField();
                                    obj.setWidth("100%");
                                    table.addItem(new Object[] { m[i].getName(), obj }, i);
                                }
                            }
                        }
                    }
                }
                
                for(int i = 0; i < m.length; i++) {
                    if(m[i].toGenericString().startsWith("public void") && m[i].toGenericString().endsWith("()")) {
                        if(m[i].getParameterTypes().length == 0) {
                            CheckVoid obj = new CheckVoid();
                            table.addItem(new Object[] { m[i].getName(), obj }, i);
                        }
                    }
                }
                
            } catch (Exception e) {
               Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
            
        }
        
        if(item != null && !item.getProperties().isEmpty()) {
            HashMap<String, Component> properties = item.getProperties();
            Iterator<String> iterator = properties.keySet().iterator();
            int count = 0;
            while(iterator.hasNext()) {
                String next = iterator.next();
                Component component = properties.get(next);
                if(component instanceof CheckBox) {
                    CheckBox cb = (CheckBox) component;
                    table.addItem(new Object[] { next, cb }, count);
                }
                if(component instanceof TextField) {
                    TextField tf = (TextField) component;
                    table.addItem(new Object[] { next, tf }, count);
                }                
                count++;
            }
        }
        
    }
    
    private void addTableItem(String s) {
        if(table != null && s != null) {
            TextField tf = new TextField();
            tf.setWidth("100%");
            if(mainTree.getValue() != null) {
                GenericItem item = (GenericItem) mainTree.getValue();
                Object get = item.getProperties().get(s);
                if(get instanceof TextField) {
                    TextField tf1 = (TextField) get;
                    tf.setValue(tf1.getValue());
                }
                item.getProperties().put(s, tf);
            }
            table.addItem(new Object[] { s, tf }, s);
        }
    }
    
    private class TreeSortDropHandler implements DropHandler {
        
        private final Tree tree;

        /**
         * Tree must use {@link HierarchicalContainer}.
         * 
         * @param tree
         */
        public TreeSortDropHandler(final Tree tree,
                final HierarchicalContainer container) {
            this.tree = tree;
        }

        @Override
        public AcceptCriterion getAcceptCriterion() {
            // Alternatively, could use the following criteria to eliminate some
            // checks in drop():
            // new And(IsDataBound.get(), new DragSourceIs(tree));
            return AcceptAll.get();
        }

        @Override
        public void drop(final DragAndDropEvent dropEvent) {
            // Called whenever a drop occurs on the component

            // Make sure the drag source is the same tree
            final Transferable t = dropEvent.getTransferable();

            // see the comment in getAcceptCriterion()
            if (t.getSourceComponent() != tree
                    || !(t instanceof DataBoundTransferable)) {
                return;
            }

            final TreeTargetDetails dropData = ((TreeTargetDetails) dropEvent
                    .getTargetDetails());

            final Object sourceItemId = ((DataBoundTransferable) t).getItemId();
            // FIXME: Why "over", should be "targetItemId" or just
            // "getItemId"
            final Object targetItemId = dropData.getItemIdOver();

            // Location describes on which part of the node the drop took
            // place
            final VerticalDropLocation location = dropData.getDropLocation();

            moveNode(sourceItemId, targetItemId, location);

        }

        /**
         * Move a node within a tree onto, above or below another node depending
         * on the drop location.
         * 
         * @param sourceItemId
         *            id of the item to move
         * @param targetItemId
         *            id of the item onto which the source node should be moved
         * @param location
         *            VerticalDropLocation indicating where the source node was
         *            dropped relative to the target node
         */
        private void moveNode(final Object sourceItemId,
                final Object targetItemId, final VerticalDropLocation location) {
            final HierarchicalContainer container = (HierarchicalContainer) tree
                    .getContainerDataSource();

            // Sorting goes as
            // - If dropped ON a node, we append it as a child
            // - If dropped on the TOP part of a node, we move/add it before
            // the node
            // - If dropped on the BOTTOM part of a node, we move/add it
            // after the node

            if (location == VerticalDropLocation.MIDDLE) {
                if (container.setParent(sourceItemId, targetItemId)
                        && container.hasChildren(targetItemId)) {
                    // move first in the container
                    container.moveAfterSibling(sourceItemId, null);
                }
            } else if (location == VerticalDropLocation.TOP) {
                final Object parentId = container.getParent(targetItemId);
                if (container.setParent(sourceItemId, parentId)) {
                    // reorder only the two items, moving source above target
                    container.moveAfterSibling(sourceItemId, targetItemId);
                    container.moveAfterSibling(targetItemId, sourceItemId);
                }
            } else if (location == VerticalDropLocation.BOTTOM) {
                final Object parentId = container.getParent(targetItemId);
                if (container.setParent(sourceItemId, parentId)) {
                    container.moveAfterSibling(sourceItemId, targetItemId);
                }
            }
        }
    }

}

class CheckVoid extends CheckBox {

    public CheckVoid() {
    
    }
    
}