package com.friendly.aqa.utils;

import com.friendly.aqa.gui.Controller;
import com.friendly.aqa.test.BaseTestCase;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.friendly.aqa.pageobject.BasePage.getProtocolPrefix;
import static com.friendly.aqa.pageobject.BasePage.getSerial;

public class XmlWriter {
    private static final Logger LOGGER = Logger.getLogger(XmlWriter.class);

    public static void createTestngConfig(Set<Controller.TabTask> testSuite) {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">\n" +
                "<suite name=\"CPE Admin Automation Tests\">\n";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("resources/testng.xml"))) {
            writer.write(header);
            for (Controller.TabTask task : testSuite) {
                String tabName = task.getTabName();
                writer.write("\t<test verbose=\"2\" name=\"" + tabName + "\">\n\t\t<classes>\n");
                Map<String, Set<String>> classMap = task.getClassMap();
                Set<String> classnameSet = classMap.keySet();
                for (String className : classnameSet) {
                    writer.write("\t\t\t<class name=\"com.friendly.aqa.test." + className + "\">\n");
                    writer.write("\t\t\t\t<methods>\n");
                    Set<String> testSet = classMap.get(className);
                    for (String testName : testSet) {
                        writer.write("\t\t\t\t\t<include name=\"" + testName + "\"/>\n");
                    }
                    writer.write("\t\t\t\t</methods>\n");
                    writer.write("\t\t\t</class>\n");
                }
                writer.write("\t\t</classes>\n");
                writer.write("\t</test>\n");
            }
            writer.write("</suite>");
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createImportCpeFile() {
        String pathName = "import/" + getProtocolPrefix() + "_import_cpe.xml";
        String header = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<serials>\n\t<serial Manufacturer=\"";
        String serial = getSerial();
        String[] deviceArr = DataBaseConnector.getDevice(serial);
        String device = deviceArr[0] + "\" Model=\"" + deviceArr[1] + "\">" + serial;
        String footer = "</serial>\n</serials>";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathName))) {
            writer.write(header);
            writer.write(device);
            writer.write(footer);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
    }

//    public static void createFakeFile() {
//        String pathName = "import/fake_file.xml";
//        String header = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathName))) {
//            writer.write(header);
//            for (int i = 0; i < 390; i++) {
//                writer.write(String.format("%s%03d%s%03d%s","<span><img width=\"3%\" src=\"passed.png\"/><a href=\"#\" class=\"method navigator-link\" panel-name=\"suite-CPE_Admin_Automation_Tests\" title=\"com.friendly.aqa.test.DeviceProfileTR069Tests\" hash-for-method=\"tr069_dp_",i, "\">tr069_dp_", i, "</a><!-- method navigator-link --></span>\n<br/>\n"));
//            }
//        } catch (IOException e) {
//            logger.warn(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) {
//        createFakeFile();
//    }

    public static void createImportGroupFile() {
        String pathName = "import/" + getProtocolPrefix() + "_import_group.xml";
        String[] deviceArr = DataBaseConnector.getDevice(getSerial());
        String protocol = getProtocolPrefix().toUpperCase();
        if (protocol.equals("TR181")) {
            protocol = "TR069";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n").append("<Update>\n").append("\t<Name>imported_group</Name>\n")
                .append("\t<Childs>\n").append("\t\t<ProductGroupValue>\n").append("\t\t\t<ProductGroup>\n").append("\t\t\t\t<Manufacturer>")
                .append(deviceArr[0]).append("</Manufacturer>\n\t\t\t\t<ModelName>").append(deviceArr[1])
                .append("</ModelName>\n\t\t\t\t<ProtocolType>").append(protocol).append("</ProtocolType>\n")
                .append("\t\t\t</ProductGroup>\n").append("\t\t</ProductGroupValue>\n").append("\t\t<Tasks>\n").append("\t\t\t<TaskAction>\n")
                .append("\t\t\t\t<Action>\n").append("\t\t\t\t\t<Name>CPEReprovision</Name>\n").append("\t\t\t\t</Action>\n")
                .append("\t\t\t</TaskAction>\n").append("\t\t</Tasks>\n").append("\t</Childs>").append("\n\t<ScheduledDate />\n\t<ActivateDate>")
                .append(CalendarUtil.getImportGroupDate()).append("</ActivateDate>\n\t<Period1> - /0/0</Period1>\n")
                .append("\t<Period2> - /0/0</Period2>\n").append("\t<Online>false</Online>\n").append("\t<AskToConnect>true</AskToConnect>\n")
                .append("\t<Location>0</Location>\n").append("\t<Threshold>0</Threshold>\n").append("\t<StopFail>false</StopFail>\n")
                .append("\t<Reactivation>\n").append("\t\t<Start />\n").append("\t\t<Time />\n").append("\t\t<Finish />\n")
                .append("\t\t<RepeatCount>0</RepeatCount>\n").append("\t\t<FailOnly>false</FailOnly>\n")
                .append("\t\t<RepeatEvery>0</RepeatEvery>\n").append("\t\t<Expression />\n")
                .append("\t\t<RandomCount>0</RandomCount>\n").append("\t</Reactivation>\n").append("</Update>");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathName))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createImportMonitorFile() {
        String pathName = "import/" + getProtocolPrefix() + "_import_monitor.xml";
        String[] deviceArr = DataBaseConnector.getDevice(getSerial());
        String protocol = getProtocolPrefix().toUpperCase();
        String parameter;
        switch (protocol) {
            case "TR069":
                parameter = "InternetGatewayDevice.DeviceInfo.UpTime";
                break;
            case "LWM2M":
                parameter = "Root.Device.0.Timezone";
                break;
            default:
                parameter = "Device.DeviceInfo.Manufacturer";
                break;
        }
        if (protocol.equals("TR181")) {
            protocol = "TR069";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n").append("<MonitorGroup>\n").append("\t<Name>")
                .append(BaseTestCase.getTestName()).append("</Name>\n").append("\t<Description>imported_monitor</Description>\n")
                .append("\t<DateFrom>01/01/1</DateFrom>\n").append("\t<DateTo>null</DateTo>\n").append("\t<PerformEvery>60</PerformEvery>\n")
                .append("\t<Location>0</Location>\n").append("\t<ChildMonirorings>\n").append("\t\t<MonitorParam>\n")
                .append("\t\t\t<ProductGroup>\n").append("\t\t\t\t<Manufacturer>").append(deviceArr[0])
                .append("</Manufacturer>\n\t\t\t\t<ModelName>").append(deviceArr[1]).append("</ModelName>\n\t\t\t\t<ProtocolType>")
                .append(protocol).append("</ProtocolType>\n").append("\t\t\t</ProductGroup>\n").append("\t\t\t<applyForNew>true</applyForNew>\n")
                .append("\t\t\t<Params>\n").append("\t\t\t\t<Param>\n").append("\t\t\t\t\t<FullName>").append(parameter).append("</FullName>\n")
                .append("\t\t\t\t\t<isTemporary>false</isTemporary>").append("\t\t\t\t</Param>\n").append("\t\t\t</Params>\n")
                .append("\t\t</MonitorParam>\n").append("\t</ChildMonirorings>\n").append("</MonitorGroup>\n");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathName))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
    }
}
