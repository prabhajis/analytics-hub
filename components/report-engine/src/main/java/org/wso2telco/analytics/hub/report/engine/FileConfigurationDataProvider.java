package org.wso2telco.analytics.hub.report.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;



public class FileConfigurationDataProvider {

    private static FileConfigurationDataProvider instance;
    private static final Log log = LogFactory.getLog(FileConfigurationDataProvider.class);
    static {
        try {
            instance = new FileConfigurationDataProvider();
        } catch (JDOMException e) {
        	log.error("error in ConfigurationDataProvider",e);
        } catch (IOException e) {
        	log.error("error in ConfigurationDataProvider",e);
        }
    }

    private String CSVSeparator;
   

    private FileConfigurationDataProvider() throws JDOMException, IOException {
        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "file-config.xml";
        File xml = new File(filePath);
        SAXBuilder builder = new SAXBuilder();
        Document document = (Document) builder.build(xml);
        Element rootNode = document.getRootElement();
        setCSVSeparator(rootNode);
        
    }

    public static FileConfigurationDataProvider getInstance() {
        if (instance == null) {
            try {
                return new FileConfigurationDataProvider();
            } catch (JDOMException e) {
            	log.error("error in ConfigurationDataProvider",e);
                e.printStackTrace();
            } catch (IOException e) {
            	log.error("error in ConfigurationDataProvider",e);
                e.printStackTrace();
            }
        }
        return instance;
    }

    public String getCSVSeparator() {
        return CSVSeparator;
    }

    public void setCSVSeparator(Element rootNode) {
        List<Element> list = rootNode.getChildren("CSVSeparator");
        Element node = list.get(0);
        CSVSeparator = node.getText();

    }

   
}
