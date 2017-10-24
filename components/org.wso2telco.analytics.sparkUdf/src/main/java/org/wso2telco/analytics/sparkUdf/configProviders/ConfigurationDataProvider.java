package org.wso2telco.analytics.sparkUdf.configProviders;

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



public class ConfigurationDataProvider {

    private static ConfigurationDataProvider instance;
    private static final Log log = LogFactory.getLog(ConfigurationDataProvider.class);
    static {
        try {
            instance = new ConfigurationDataProvider();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String url;
    private String password;
    private String uname;
    private String apiKey;
    private String apiSecret;

    private ConfigurationDataProvider() throws JDOMException, IOException {
        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "KillBillEndPointConfig.xml";
        File xml = new File(filePath);
        SAXBuilder builder = new SAXBuilder();
        Document document = (Document) builder.build(xml);
        Element rootNode = document.getRootElement();
        setUrl(rootNode);
        setUname(rootNode);
        setPassword(rootNode);
        setApiKey(rootNode);
        setApiSecret(rootNode);
    }

    public static ConfigurationDataProvider getInstance() {
        if (instance == null) {
            try {
                return new ConfigurationDataProvider();
            } catch (JDOMException e) {
            	log.error("error in ConfigurationDataProvider"+e);
                e.printStackTrace();
            } catch (IOException e) {
            	log.error("error in ConfigurationDataProvider"+e);
                e.printStackTrace();
            }
        }
        return instance;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(Element rootNode) {
        List<Element> list = rootNode.getChildren("url");
        Element node = list.get(0);
        url = node.getText();

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(Element rootNode) {
        List<Element> list = rootNode.getChildren("password");
        Element node = list.get(0);
        password = node.getText();

    }

    public String getUname() {
        return uname;
    }

    public void setUname(Element rootNode) {
        List<Element> list = rootNode.getChildren("uname");
        Element node = list.get(0);
        uname = node.getText();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(Element rootNode) {
        List<Element> list = rootNode.getChildren("apiKey");
        Element node = list.get(0);
        apiKey = node.getText();
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(Element rootNode) {
        List<Element> list = rootNode.getChildren("apiSecret");
        Element node = list.get(0);
        apiSecret = node.getText();
    }
}
