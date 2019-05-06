package sample;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchematController extends Main implements Initializable {

    @FXML
    Tab tabConnectionDatabase;
    @FXML
    Tab tabFileImport;
    @FXML
    Tab tabTableViews;
    @FXML
    TabPane tabPaneMain;
    @FXML
    Button buttonImport;
    @FXML
    TextField textfieldImportFilePath;
    @FXML
    Hyperlink hyperlinkFileChoose;
    @FXML
    TextField textFieldPathToDb;
    @FXML
    TableView<Customer> tableViewCustomers;
    @FXML
    TableColumn<Customer, String> tableColumnCustomerID;
    @FXML
    TableColumn<Customer, String> tableColumnCustomerName;
    @FXML
    TableColumn<Customer, String> tableColumnCustomerSurname;
    @FXML
    TableColumn<Customer, String> tableColumnCustomerAge;
    @FXML
    TableColumn<Customer, String> tableColumnCustomerCity;

    @FXML
    TableView<Contact> tableViewContacts;
    @FXML
    TableColumn<Contact, String> tableColumnContactID;
    @FXML
    TableColumn<Contact, String> tableColumnContactType;
    @FXML
    TableColumn<Contact, String> tableColumnContact;

    @FXML
    TextField textFieldLogin;
    @FXML
    PasswordField passwordField;
    @FXML
    Button buttonZaloguj;


    File selectedFile;
    ObservableList<Customer> customerData;
    ObservableList<Contact> contactData;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textFieldPathToDb.setText(url);
        tabPaneMain.getSelectionModel().select(0);
        buttonImport.setDisable(true);
        textfieldImportFilePath.setEditable(false);
        tabTableViews.setDisable(true);
        tabFileImport.setDisable(true);
        Listenery();
    }

    private void Listenery() {
        tabPaneMain.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getText().equalsIgnoreCase("Podgląd bazy")) {
                refreshTableViews();
            }
        });
        tableViewCustomers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            showContactsOfCustomer(newValue.idProperty().get());
        });
        hyperlinkFileChoose.setOnAction((event) -> {
            selectedFile = openFileChooser();
            if (selectedFile != null) {
                buttonImport.setDisable(false);
                textfieldImportFilePath.setText(selectedFile.getAbsolutePath());
            }
        });

        buttonImport.setOnAction((event -> {
            if (selectedFile.getAbsolutePath().endsWith(".txt") || selectedFile.getAbsolutePath().endsWith(".csv")) {
                new Thread(() -> {
                    importFromCSVFile(selectedFile);
                }).run();
            } else if (selectedFile.getAbsolutePath().endsWith(".xml")) {
                new Thread(() -> {
                    importFromXMLFile(selectedFile);
                }).run();
            }
        }));

        buttonZaloguj.setOnAction((event) -> {
            username = textFieldLogin.getText();
            password = passwordField.getText();
            url = textFieldPathToDb.getText();
            try {
                ConnectToDatabase();
                tabTableViews.setDisable(false);
                tabFileImport.setDisable(false);
                showAlertDialog("Poprawnie zalogowano.", "Informacja");
                tabPaneMain.getTabs().remove(0);
            } catch (Exception e) {
                e.printStackTrace();
                showAlertDialog(e.getMessage(), "Wystąpił błąd!");
                textFieldLogin.clear();
                passwordField.clear();
            }
        });


    }

    private void showContactsOfCustomer(String id_customer) {
        contactData = null;
        tableViewContacts.setItems(null);
        contactData = FXCollections.observableArrayList();
        PreparedStatement pst = null;
        ResultSet resultSet = null;
        try {
            pst = connection.prepareStatement("select * from contacts where id_customer=?");
            pst.setString(1, id_customer);
            resultSet = pst.executeQuery();
            while (resultSet.next()) {
                contactData.add(new Contact(resultSet.getString(1), resultSet.getString(3), resultSet.getString(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableColumnContactID.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        tableColumnContactType.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        tableColumnContact.setCellValueFactory(cellData -> cellData.getValue().contactProperty());
        tableViewContacts.setItems(contactData);
    }

    private void refreshTableViews() {
        customerData = null;
        tableViewCustomers.setItems(null);
        customerData = FXCollections.observableArrayList();
        ResultSet resultSet = null;
        try {
            resultSet = connection.createStatement().executeQuery("select * from customers");
            while (resultSet.next()) {
                customerData.add(new Customer(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableColumnCustomerID.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        tableColumnCustomerName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        tableColumnCustomerSurname.setCellValueFactory(cellData -> cellData.getValue().surnameProperty());
        tableColumnCustomerAge.setCellValueFactory(cellData -> cellData.getValue().ageProperty());
        tableColumnCustomerCity.setCellValueFactory(cellData -> cellData.getValue().cityProperty());
        tableViewCustomers.setItems(customerData);
    }

    private File openFileChooser() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv", "*.txt"),
                new FileChooser.ExtensionFilter("XML", "*.xml")
        );
        File file = fileChooser.showOpenDialog(hyperlinkFileChoose.getScene().getWindow());
        if (file == null) {
            return null;
        }
        return file;
    }


    private void importFromXMLFile(File xmlFile) {
        PreparedStatement pst;
        String query;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            NodeList customersList = doc.getElementsByTagName("person");


            for (int indexOfCurrentNode = 0; indexOfCurrentNode < customersList.getLength(); indexOfCurrentNode++) {

                Node customer = customersList.item(indexOfCurrentNode);

                if (customer.getNodeType() == Node.ELEMENT_NODE) {
                    Element customerElement = (Element) customer;

                    BigInteger currentTime = BigInteger.valueOf(System.currentTimeMillis());
                    query = "insert into customers(ID,NAME,SURNAME,AGE,CITY) values (?,?,?,?,?)";
                    pst = connection.prepareStatement(query);
                    pst.setObject(1, currentTime);
                    pst.setString(2, customerElement.getElementsByTagName("name").item(0).getTextContent());
                    pst.setString(3, customerElement.getElementsByTagName("surname").item(0).getTextContent());
                    if (customerElement.getElementsByTagName("age").getLength() > 0) {
                        pst.setString(4, customerElement.getElementsByTagName("age").item(0).getTextContent());
                    } else {
                        pst.setString(4, null);
                    }
                    pst.setString(5, customerElement.getElementsByTagName("city").item(0).getTextContent());
                    if (pst.executeUpdate() == 0) {
                        throw new Exception("Nie dodałem osoby: " + customerElement.getElementsByTagName("name").item(0).getTextContent() + " " + customerElement.getElementsByTagName("surname").item(0).getTextContent());
                    }

                    NodeList contactsList = doc.getElementsByTagName("contacts");
                    Node contact = contactsList.item(indexOfCurrentNode);
                    if (contact.getNodeType() == Node.ELEMENT_NODE) {
                        Element contactElement = (Element) contact;
                        query = "insert into contacts(ID,ID_CUSTOMER,TYPE,CONTACT) values (?,?,?,?)";
                        pst = connection.prepareStatement(query);
                        for (int i = 0; i < contactElement.getElementsByTagName("phone").getLength(); i++) {
                            pst.setObject(1, BigInteger.valueOf(System.currentTimeMillis()));
                            pst.setObject(2, currentTime);
                            pst.setInt(3, 2);
                            pst.setString(4, contactElement.getElementsByTagName("phone").item(i).getTextContent());
                            if (pst.executeUpdate() == 0) {
                                throw new Exception("Błąd zapisu telefonu: " + contactElement.getElementsByTagName("phone").item(i).getTextContent());
                            }
                        }
                        for (int i = 0; i < contactElement.getElementsByTagName("email").getLength(); i++) {
                            pst.setObject(1, BigInteger.valueOf(System.currentTimeMillis()));
                            pst.setObject(2, currentTime);
                            pst.setInt(3, 1);
                            pst.setString(4, contactElement.getElementsByTagName("email").item(i).getTextContent());
                            if (pst.executeUpdate() == 0) {
                                throw new Exception("Błąd zapisu e-mail: " + contactElement.getElementsByTagName("email").item(i).getTextContent());
                            }
                        }
                        for (int i = 0; i < contactElement.getElementsByTagName("jabber").getLength(); i++) {
                            pst.setObject(1, BigInteger.valueOf(System.currentTimeMillis()));
                            pst.setObject(2, currentTime);
                            pst.setInt(3, 3);
                            pst.setString(4, contactElement.getElementsByTagName("jabber").item(i).getTextContent());
                            if (pst.executeUpdate() == 0) {
                                throw new Exception("Błąd zapisu jabber: " + contactElement.getElementsByTagName("jabber").item(i).getTextContent());
                            }
                        }
                        for (int i = 0; i < contactElement.getElementsByTagName("icq").getLength(); i++) {
                            pst.setObject(1, BigInteger.valueOf(System.currentTimeMillis()));
                            pst.setObject(2, currentTime);
                            pst.setInt(3, 0);
                            pst.setString(4, contactElement.getElementsByTagName("icq").item(i).getTextContent());
                            if (pst.executeUpdate() == 0) {
                                throw new Exception("Błąd zapisu icq: " + contactElement.getElementsByTagName("icq").item(i).getTextContent());
                            }
                        }
                    }
                }
            }

            showAlertDialog("Popranie zapisano do bazy danych.", "Inforamcja");
            tabPaneMain.getSelectionModel().select(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void importFromCSVFile(File csvFile) {
        PreparedStatement pst;
        String query;

        int idx_customer_name = 0;
        int idx_customer_surname = 1;
        int idx_customer_age = 2;
        int idx_customer_city = 3;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(csvFile));

            String line;
           
            while ((line = br.readLine()) != null) {
                BigInteger currentTime = BigInteger.valueOf(System.currentTimeMillis());
                String[] fields = line.split(",");
                query = "insert into customers(ID,NAME,SURNAME,AGE,CITY) values (?,?,?,?,?)";
                pst = connection.prepareStatement(query);
                pst.setObject(1, currentTime);
                pst.setString(2, fields[idx_customer_name]);
                pst.setString(3, fields[idx_customer_surname]);
                pst.setString(4, fields[idx_customer_age].isEmpty() ? null : fields[idx_customer_age]);
                pst.setString(5, fields[idx_customer_city]);
                if (pst.executeUpdate() == 0) {
                    throw new Exception("Nie dodałem osoby: " + fields[idx_customer_name] + " " + fields[idx_customer_surname]);
                }
                for (int i = 4; i < fields.length; i++) {
                    query = "insert into contacts(ID,ID_CUSTOMER,TYPE,CONTACT) values (?,?,?,?)";
                    pst = connection.prepareStatement(query);
                    pst.setObject(1, BigInteger.valueOf(System.currentTimeMillis()));
                    pst.setObject(2, currentTime);
                    pst.setInt(3, getTypeOfContact(fields[i]));
                    pst.setString(4, fields[i]);
                    if (pst.executeUpdate() == 0) {
                        throw new Exception("Nie dodałem kontaktu: " + fields[i]);
                    }
                }
            }

            showAlertDialog("Popranie zapisano do bazy danych.", "Inforamcja");
            tabPaneMain.getSelectionModel().select(1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getTypeOfContact(String contact) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._]*@[a-zA-Z0-9]+([.][a-zA-Z]+)+");
        Matcher matcher = pattern.matcher(contact);
        boolean matchFound = matcher.matches();
        if (matchFound) {
            return 1;
        }

        String mobilePhone = contact;
        mobilePhone = mobilePhone.replaceAll(" ", "");
        pattern = Pattern.compile("[\\d]{9}");
        matcher = pattern.matcher(mobilePhone);
        matchFound = matcher.matches();
        if (matchFound) {
            return 2;
        }

        pattern = Pattern.compile("jbr");
        matcher = pattern.matcher(contact);
        matchFound = matcher.matches();
        if (matchFound) {
            return 3;
        }
        return 0;
    }

    private void showAlertDialog(String info, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getButtonTypes().forEach((t) -> {
            ((Button) alert.getDialogPane().lookupButton(t)).setDefaultButton(false);
        });
        alert.setTitle(title);
        alert.setHeaderText(info);
        alert.setGraphic(null);
        alert.initOwner(null);
        alert.initModality(Modality.APPLICATION_MODAL);
        EventHandler<KeyEvent> fireOnEnter = event -> {
            if (KeyCode.ENTER.equals(event.getCode())
                    && event.getTarget() instanceof Button) {
                ((Button) event.getTarget()).fire();
            }
        };

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getButtonTypes().stream()
                .map(dialogPane::lookupButton)
                .forEach(button
                                -> button.addEventHandler(
                        KeyEvent.KEY_PRESSED,
                        fireOnEnter
                        )
                );

        alert.showAndWait();
    }


    public void ConnectToDatabase() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(url, username, password);
    }

    public class Customer {
        private final StringProperty id;
        private final StringProperty name;
        private final StringProperty surname;
        private final StringProperty city;
        private final StringProperty age;

        Customer(String id, String name, String surname, String age, String city) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.surname = new SimpleStringProperty(surname);
            this.city = new SimpleStringProperty(city);
            this.age = new SimpleStringProperty(age);
        }

        public StringProperty idProperty() {
            return id;
        }


        public StringProperty nameProperty() {
            return name;
        }


        public StringProperty surnameProperty() {
            return surname;
        }


        public StringProperty cityProperty() {
            return city;
        }

        public StringProperty ageProperty() {
            return age;
        }
    }

    public class Contact {
        private final StringProperty id;
        private final StringProperty type;
        private final StringProperty contact;

        Contact(String id, String type, String contact) {
            this.id = new SimpleStringProperty(id);
            this.type = new SimpleStringProperty(type);
            this.contact = new SimpleStringProperty(contact);
        }

        public StringProperty idProperty() {
            return id;
        }

        public StringProperty typeProperty() {
            return type;
        }

        public StringProperty contactProperty() {
            return contact;
        }
    }
}
