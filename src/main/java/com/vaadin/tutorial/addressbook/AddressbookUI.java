package com.vaadin.tutorial.addressbook;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Title("Addressbook")
@Theme("valo")
public class AddressbookUI extends UI {

    private Table contactList = new Table();
    private TextField searchField = new TextField();
    private Button addNewContactButton = new Button("New");
    private Button removeContactButton = new Button("Remove this contact");
    private FormLayout editorLayout = new FormLayout();
    private FieldGroup editorFields = new FieldGroup();

    private static final String FNAME = "First Name";
    private static final String LNAME = "Last Name";
    private static final String COMPANY = "Company";
    private static final String[] fieldNames = new String[]{FNAME, LNAME,
        COMPANY, "Mobile Phone", "Work Phone", "Home Phone", "Work Email",
        "Home Email", "Street", "City", "Zip", "State", "Country"};

    IndexedContainer contactContainer = createDummyDatasource();

    protected void init(VaadinRequest request) {
        initLayout();
        initContactList();
        initEditor();
        initSearch();
        initAddRemoveButtons();
    }

    // https://vaadin.com/book/-/page/layout.tabsheet.html
    private void initLayout() {
        TabSheet tabsheet = new TabSheet();
        tabsheet.setHeight("100%");
        setContent(tabsheet);

        VerticalLayout tab1 = new VerticalLayout();
        tab1.setHeight("100%");
        VerticalLayout tab2 = new VerticalLayout();
        tab2.setHeight("100%");

        tabsheet.addTab(tab1, "Coches");
        tabsheet.addTab(tab2, "Consumibles");

        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        tab1.addComponent(splitPanel);

        VerticalLayout leftLayout = new VerticalLayout();
        splitPanel.addComponent(leftLayout);
        splitPanel.addComponent(editorLayout);
        leftLayout.addComponent(contactList);
        HorizontalLayout bottomLeftLayout = new HorizontalLayout();
        leftLayout.addComponent(bottomLeftLayout);
        bottomLeftLayout.addComponent(searchField);
        bottomLeftLayout.addComponent(addNewContactButton);

        leftLayout.setSizeFull();

        leftLayout.setExpandRatio(contactList, 1);
        contactList.setSizeFull();

        bottomLeftLayout.setWidth("100%");
        searchField.setWidth("100%");
        bottomLeftLayout.setExpandRatio(searchField, 1);

        editorLayout.setMargin(true);
        editorLayout.setVisible(false);
    }

    private void initEditor() {
        editorLayout.addComponent(removeContactButton);
        for (String fieldName : fieldNames) {
            TextField field = new TextField(fieldName);
            editorLayout.addComponent(field);
            field.setWidth("100%");
            editorFields.bind(field, fieldName);
        }
        editorFields.setBuffered(false);
    }

    private void initSearch() {
        searchField.setInputPrompt("Search contacts");
        searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
        searchField.addTextChangeListener(new TextChangeListener() {
            @Override
            public void textChange(final TextChangeEvent event) {
                contactContainer.removeAllContainerFilters();
                contactContainer.addContainerFilter(new ContactFilter(event.getText()));
            }
        });
    }

    private class ContactFilter implements Filter {

        private String needle;

        public ContactFilter(String needle) {
            this.needle = needle.toLowerCase();
        }

        public boolean passesFilter(Object itemId, Item item) {
            String haystack = ("" + item.getItemProperty(FNAME).getValue()
                    + item.getItemProperty(LNAME).getValue() + item
                    .getItemProperty(COMPANY).getValue()).toLowerCase();
            return haystack.contains(needle);
        }

        public boolean appliesToProperty(Object id) {
            return true;
        }
    }

    private void initAddRemoveButtons() {
        addNewContactButton.addClickListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {

                /*
                 * Rows in the Container data model are called Item. Here we add
                 * a new row in the beginning of the list.
                 */
                contactContainer.removeAllContainerFilters();
                Object contactId = contactContainer.addItemAt(0);

                /*
                 * Each Item has a set of Properties that hold values. Here we
                 * set a couple of those.
                 */
                contactList.getContainerProperty(contactId, FNAME).setValue(
                        "New");
                contactList.getContainerProperty(contactId, LNAME).setValue(
                        "Contact");

                /* Lets choose the newly created contact to edit it. */
                contactList.select(contactId);
            }
        });

        removeContactButton.addClickListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
                Object contactId = contactList.getValue();
                contactList.removeItem(contactId);
            }
        });
    }

    private void initContactList() {
        contactList.setContainerDataSource(contactContainer);
        contactList.setVisibleColumns(new String[]{FNAME, LNAME, COMPANY});
        contactList.setSelectable(true);
        contactList.setImmediate(true);

        contactList.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                Object contactId = contactList.getValue();

                /*
                 * When a contact is selected from the list, we want to show
                 * that in our editor on the right. This is nicely done by the
                 * FieldGroup that binds all the fields to the corresponding
                 * Properties in our contact at once.
                 */
                if (contactId != null) {
                    editorFields.setItemDataSource(contactList
                            .getItem(contactId));
                }

                editorLayout.setVisible(contactId != null);
            }
        });
    }

    /*
     * Generate some in-memory example data to play with. In a real application
     * we could be using SQLContainer, JPAContainer or some other to persist the
     * data.
     */
    private static IndexedContainer createDummyDatasource() {
        IndexedContainer ic = new IndexedContainer();

        for (String p : fieldNames) {
            ic.addContainerProperty(p, String.class, "");
        }

        /* Create dummy data by randomly combining first and last names */
        String[] fnames = {"Peter", "Alice", "Joshua", "Mike", "Olivia",
            "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene",
            "Lisa", "Marge"};
        String[] lnames = {"Smith", "Gordon", "Simpson", "Brown", "Clavel",
            "Simons", "Verne", "Scott", "Allison", "Gates", "Rowling",
            "Barks", "Ross", "Schneider", "Tate"};
        for (int i = 0; i < 1000; i++) {
            Object id = ic.addItem();
            ic.getContainerProperty(id, FNAME).setValue(
                    fnames[(int) (fnames.length * Math.random())]);
            ic.getContainerProperty(id, LNAME).setValue(
                    lnames[(int) (lnames.length * Math.random())]);
        }

        return ic;
    }

}
