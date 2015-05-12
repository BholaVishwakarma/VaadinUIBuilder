package it.vp.vaadinuibuilder;

import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *
 * @author cuta
 */
public class ObjectNameComponent extends CustomComponent implements Button.ClickListener {

    private final VerticalLayout vl;
    private TextField name;
    private Button submit;
    private GenericItem item;
    private Window win;
    private ObjectNameListener listener;
    
    public ObjectNameComponent() {
        vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSpacing(true);
        setCompositionRoot(vl);
        buildUI();
    }

    private void buildUI() {
        name = new TextField();
        name.focus();
        submit = new Button("Submit", this);
        name.setWidth("100%");
        submit.setWidth("100%");
        submit.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        vl.addComponents(name, submit);
    }

    public void setItem(GenericItem item) {
        this.item = item;
    }

    public void setWin(Window win) {
        this.win = win;
    }
    
    public void setListener(ObjectNameListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void buttonClick(Button.ClickEvent event) {
        if(name.getValue().length() > 0) {
            if(item != null && win != null) {
                item.setObjName(name.getValue());
                if(listener != null) {
                    listener.onSubmit(item);
                }
                win.close();
            }
        } else {
            Notification note = new Notification("Error", "field empty!", Notification.Type.WARNING_MESSAGE);
            note.setPosition(Position.TOP_CENTER);
            note.show(getUI().getPage());
        }
    }
    
    

}
