package geopublish.geoserverconfigurator;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MainActivity extends AppCompatActivity {

    private String configFilePath;
    private String serverPort;
    private static EditText txtExecutiondelay;
    private static EditText txtServerPort;
    private static CheckBox chkPlayAdvertising;
    private static CheckBox chkEmulatedGPS;
    private static CheckBox chkAllowMultipleStops;
    private static CheckBox chkQueryUserStops;
    private int executiondelay;
    private int  allowPlayAdvertising;
    private int allowEmulatedGPS;
    private int allowMultipleStops;
    private int queryUserStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Validar que el puerto no este vacio
        //TODO: validar que el puerto sea numerico entre 1 y 32000 y pico
        //TODO: Validar que el delay no este vacio
        //TODO: Validar que el delay no sea numerico entre 0 y 120000
        //TODO: cambiar la carpeta de configurator para el cliente a GeopublishSettings
        //TODO: cambiar nivel de ZOOM

        txtExecutiondelay=(EditText) findViewById(R.id.txtExecutiondelay);
        txtServerPort=(EditText) findViewById(R.id.txtServerPort);
        chkPlayAdvertising=(CheckBox) findViewById(R.id.chkPlayAdvertising);
        chkEmulatedGPS=(CheckBox) findViewById(R.id.chkEmulatedGPS);
        chkAllowMultipleStops=(CheckBox) findViewById(R.id.chkAllowMultipleStops);
        chkQueryUserStops=(CheckBox) findViewById(R.id.chkQueryUserStops);

        File root = Environment.getExternalStorageDirectory();

        configFilePath= root.getAbsolutePath()  + "/GeoPublishSettings/Server.xml";

        File file = new File(configFilePath);

        if(file.exists())
        {
            readXML(configFilePath);

            txtServerPort.setText(serverPort);
            txtExecutiondelay.setText(String.valueOf(executiondelay));
            chkPlayAdvertising.setChecked(allowPlayAdvertising != 0);
            chkEmulatedGPS.setChecked(allowEmulatedGPS != 0);
            chkAllowMultipleStops.setChecked(allowMultipleStops != 0);
            chkQueryUserStops.setChecked(queryUserStops != 0);
            //TODO: opcion de no centrar mapa
            //TODO: no reproducir sonido
        }

        Button btnSave=(Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File dir = new File(Environment.getExternalStorageDirectory() + "/GeoPublishSettings");
                if (dir.exists() && dir.isDirectory()) {
                    // do something here
                } else {
                    dir.mkdirs();
                }

                saveToXML(configFilePath,txtServerPort.getText().toString(),txtExecutiondelay.getText().toString()
                        , chkPlayAdvertising.isChecked()?1:0
                        ,chkEmulatedGPS.isChecked()?1:0
                        ,chkAllowMultipleStops.isChecked()?1:0
                ,chkQueryUserStops.isChecked()?1:0);

                Toast.makeText(MainActivity.this, "Datos guardados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveToXML(String xml, String serverPort, String executionDelay,int allowPlayAdvertising, int allowEmulatedGPS, int allowMultipleStops, int queryUserStops)
    {
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootEle = dom.createElement("settings");

            // create data elements and place them under root
            e = dom.createElement("ServerPort");
            e.appendChild(dom.createTextNode(String.valueOf(serverPort)));
            rootEle.appendChild(e);

            e = dom.createElement("ExecutionDelay");
            e.appendChild(dom.createTextNode(executionDelay));
            rootEle.appendChild(e);

            e = dom.createElement("AllowPlayAdvertising");
            e.appendChild(dom.createTextNode(String.valueOf(allowPlayAdvertising)));
            rootEle.appendChild(e);

            e = dom.createElement("AllowEmulatedGPS");
            e.appendChild(dom.createTextNode(String.valueOf(allowEmulatedGPS)));
            rootEle.appendChild(e);

            e = dom.createElement("AllowMultipleStops");
            e.appendChild(dom.createTextNode(String.valueOf(allowMultipleStops)));
            rootEle.appendChild(e);

            e = dom.createElement("QueryUserStops");
            e.appendChild(dom.createTextNode(String.valueOf(queryUserStops)));
            rootEle.appendChild(e);

            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(xml)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    private String getTextValue( Element doc, String tag) {
        String value=null;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }


    public void readXML(String fileName) {
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            File file = new File(fileName);
            InputStream is = new FileInputStream(file.getPath());
            //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(is));
            doc.getDocumentElement().normalize();


            // parse using the builder to get the DOM mapping of the
            // XML file
            //dom = db.parse(xml);

            Element docElement = doc.getDocumentElement();

            executiondelay = Integer.parseInt(getTextValue(docElement, "ExecutionDelay"));
            serverPort = getTextValue(docElement, "ServerPort");
            allowPlayAdvertising=Integer.parseInt(getTextValue(docElement, "AllowPlayAdvertising"));
            allowEmulatedGPS=Integer.parseInt(getTextValue(docElement, "AllowEmulatedGPS"));
            allowMultipleStops=Integer.parseInt(getTextValue(docElement, "AllowMultipleStops"));
            queryUserStops=Integer.parseInt(getTextValue(docElement, "QueryUserStops"));

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

    }
}
