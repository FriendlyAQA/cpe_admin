package com.friendly.aqa.pageobject;

import com.friendly.aqa.entities.*;
import com.friendly.aqa.entities.Event;
import com.friendly.aqa.test.BaseTestCase;
import com.friendly.aqa.utils.*;
import com.friendly.aqa.utils.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import java.io.*;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

import static com.friendly.aqa.entities.BottomButtons.REFRESH;
import static com.friendly.aqa.pageobject.BasePage.FrameSwitch.*;

public abstract class BasePage {

    protected static final String BROWSER;
    private Date csvFileTime;
    static WebDriver driver;
    protected static Map<String, Event> eventMap;
    private static FrameSwitch frame;
    public static final long IMPLICITLY_WAIT;
    private static final Logger LOGGER;
    private static String mainWindow;
    protected static Set<String> parameterSet;
    private static Map<String, String> parameterMap;
    private static FrameSwitch previousFrame;
    protected static ParametersMonitor parametersMonitor;
    static Properties props;
    private Table savedTable;
    protected String selectedName;
    protected static Task task;
    private Date xmlFileTime;

    static {
        initProperties();
        LOGGER = Logger.getLogger(BasePage.class);
        BROWSER = props.getProperty("browser");
        IMPLICITLY_WAIT = Long.parseLong(props.getProperty("driver_implicitly_wait"));
        frame = ROOT;
    }

    BasePage() {
        PageFactory.initElements(driver, this);
        selectedName = "";
    }

    public static void closeDriver() {
        driver.quit();
    }

    public static void closeNewWindow() {
        if (!driver.getWindowHandle().equalsIgnoreCase(mainWindow)) {
            driver.close();
            driver.switchTo().window(mainWindow);
        }
    }

    public static WebElement findElement(String byId) {
        return driver.findElement(By.id(byId));
    }

    public static void flushCollections() {
        parameterSet = null;
        parameterMap = null;
        eventMap = null;
        parametersMonitor = null;
        task = null;
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static Properties getProps() {
        return props;
    }

    public static String getProtocolPrefix() {
        return BaseTestCase.getTestName().split("_")[0];
    }

    public static String getSerial() {
        return props.getProperty(getProtocolPrefix() + "_cpe_serial");
    }

    public static void initDriver() {
        String browser;
        browser = props.getProperty("browser");
        switch (browser) {
            case "chrome":
                System.setProperty("webdriver.chrome.driver", props.getProperty("chrome_driver_path"));
                driver = new ChromeDriver();
                break;
            case "ie":
                System.setProperty("webdriver.ie.driver", props.getProperty("ie_driver_path"));
                driver = new InternetExplorerDriver();
                break;
            case "edge":
                System.setProperty("webdriver.edge.driver", props.getProperty("edge_driver_path"));
                driver = new EdgeDriver();
                break;
            default:
                browser = "firefox";
                System.setProperty("webdriver.gecko.driver", props.getProperty("firefox_driver_path"));
                driver = new FirefoxDriver();
        }
        LOGGER.info(browser + " driver is running");
        driver.manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        driver.get(props.getProperty("ui_url"));
        mainWindow = driver.getWindowHandle();
    }

    private static void initProperties() {
        props = new Properties();
        try (InputStream input = new FileInputStream("resources/config.properties")) {
            props.load(input);
        } catch (IOException ex) {
            System.out.println("File 'config.properties' is not found!");
            System.exit(1);
        }
    }

    public static void setDefaultImplicitlyWait() {
        setImplicitlyWait(IMPLICITLY_WAIT);
    }

    public static void setImplicitlyWait(long seconds) {
        driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
    }

    public static void switchToFrame(FrameSwitch frame) {
        BasePage.previousFrame = BasePage.frame;
        if (BasePage.frame == frame) {
            return;
        }
        driver.switchTo().defaultContent();
        BasePage.frame = frame;
        if (frame == ROOT) {
            return;
        }
        if (frame == SUB_FRAME) {
            driver.switchTo().frame(driver.findElement(By.id(DESKTOP.frameId)));
        }
        WebElement frameEl = driver.findElement(By.id(frame.frameId));
        driver.switchTo().frame(frameEl);
    }

    public static void switchToPreviousFrame() {
        switchToFrame(previousFrame);
    }

    public static WebElement scrollToElement(WebElement element) {
        ((JavascriptExecutor) BasePage.getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
        return element;
    }

    public static void takeScreenshot(String pathname) throws IOException {
        TakesScreenshot screenshot = (TakesScreenshot) driver;
        File src = screenshot.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(src, new File(pathname));
//        System.out.println("Successfully captured a screenshot");
    }

    @FindBy(id = "btnAddFilter_btn")
    private WebElement addFilterButton;

    @FindBy(id = "btnAddSubFilter_btn")
    private WebElement addSubFilterButton;

    @FindBy(id = "btnAddTask_btn")
    protected WebElement addTaskButton;

    @FindBy(id = "spnAlert")
    private WebElement alertWindow;

    @FindBy(id = "rdAnd")
    private WebElement andRadioButton;

    @FindBy(id = "btnBottom_btn")
    private WebElement bottomButton;

    @FindBy(id = "btnCancel_btn")
    protected WebElement cancelButton;

    @FindBy(id = "IsDefaultViewForUser")
    protected WebElement defaultViewCheckbox;

    @FindBy(id = "btnDelFilter_btn")
    private WebElement deleteFilterButton;

    @FindBy(id = "UcFirmware1_ddlDeliveryMethod")
    protected WebElement deliveryMethodComboBox;

    @FindBy(id = "UcFirmware1_ddlDeliveryProtocol")
    protected WebElement deliveryProtocolCombobox;

    @FindBy(id = "ddlDiagnostics")
    private WebElement diagnosticTypeComboBox;

    @FindBy(id = "btnDown_btn")
    private WebElement downButton;

    @FindBy(id = "btnEditView_btn")
    protected WebElement editButton;

    @FindBy(id = "UcFirmware1_ddlFileName")
    protected WebElement fileNameComboBox;

    @FindBy(id = "ddlManuf")
    protected WebElement filterManufacturerComboBox;

    @FindBy(id = "ddlModel")
    protected WebElement filterModelNameComboBox;

    @FindBy(id = "ddlView")
    protected WebElement filterViewComboBox;

    @FindBy(id = "UcFirmware1_rdTarget")
    protected WebElement fromListRadioButton;

    @FindBy(id = "lrbImmediately")
    private WebElement immediatelyRadioButton;

    @FindBy(id = "frmImportFromFile")
    private WebElement importFrame;

    @FindBy(id = "fuSerials")
    private WebElement importDeviceField;

    @FindBy(id = "fuImport")
    protected WebElement importField;

    @FindBy(id = "txtDnsServer")
    private WebElement inputDnsField;

    @FindBy(id = "txtHost")
    private WebElement inputHostField;

    @FindBy(id = "txtText")
    protected WebElement inputTextField;

    @FindBy(id = "ddlPageSizes")
    private WebElement itemsOnPageComboBox;

    @FindBy(id = "imgLogout")
    private WebElement logOutButton;

    @FindBy(id = "UcFirmware1_rdUrl")
    protected WebElement manuallyDownloadRadioButton;

    @FindBy(id = "rdUrlUpload")
    private WebElement manuallyUploadRadioButton;

    @FindBy(id = "ddlManufacturer")
    protected WebElement manufacturerComboBox;

    @FindBy(id = "ddlModelName")
    protected WebElement modelComboBox;

    @FindBy(id = "txtName")
    protected WebElement nameField;

    @FindBy(id = "tbName")
    protected WebElement nameTextField;

    @FindBy(id = "btnNewView_btn")
    protected WebElement newViewButton;

    @FindBy(id = "txtNumberRepetitions")
    private WebElement numOfRepetitionsField;

    @FindBy(id = "btnAlertOk_btn")
    protected WebElement okButtonAlertPopUp;

    @FindBy(id = "btnOk_btn")
    private WebElement okButtonPopUp;

    @FindBy(id = "pager2_lblPagerTotal")
    protected WebElement pager;

    @FindBy(id = "UcFirmware1_tbPa_ss")
    protected WebElement passwordField;

    @FindBy(id = "tbPa_ss")
    protected WebElement passwordUploadField;

    @FindBy(id = "btnDefaultView_btn")
    protected WebElement resetViewButton;

    @FindBy(id = "tbTimeHour")
    protected WebElement scheduledHours;

    @FindBy(id = "tbTimeMinute")
    protected WebElement scheduledMinutes;

    @FindBy(id = "lrbWaitScheduled")
    protected WebElement scheduledToRadioButton;

    @FindBy(id = "btnSearch_btn")
    protected WebElement searchButton;

    @FindBy(id = "btnSelectDevices_btn")
    protected WebElement selectButton;

    @FindBy(id = "ddlColumns")
    private WebElement selectColumnFilter;

    @FindBy(id = "ddlCondition")
    private WebElement selectCompare;

    @FindBy(id = "UcFirmware1_ddlFileType")
    protected WebElement selectDownloadFileTypeComboBox;

    @FindBy(id = "ddlMethods")
    private WebElement selectMethodComboBox;

    @FindBy(id = "ddlTasks")
    protected WebElement selectTask;

    @FindBy(id = "ddlFileType")
    protected WebElement selectUploadFileTypeComboBox;

    @FindBy(id = "ddlSend")
    protected WebElement sendToComboBox;

    @FindBy(id = "menuCircularG")
    protected WebElement spinner;

    @FindBy(id = "btnTop_btn")
    private WebElement topButton;

    @FindBy(id = "btnUp_btn")
    private WebElement upButton;

    @FindBy(id = "ddlUpdateStatus")
    protected WebElement updateStatusCombobox;

    @FindBy(id = "tbUrl")
    private WebElement uploadUrlField;

    @FindBy(id = "UcFirmware1_tbUrl")
    protected WebElement urlField;

    @FindBy(id = "UcFirmware1_tbLo_gin")
    protected WebElement userNameField;

    @FindBy(id = "tbLo_gin")
    protected WebElement userNameUploadField;


    public BasePage addDeviceWithoutTemplate() {
        String[] device = props.getProperty("device_without_template").split(":");
        selectManufacturer(device[0]);
        selectModel(device[1]);
        return this;
    }

    public BasePage addFilter() {
        return clickButton(addFilterButton);
    }

    public BasePage addSubFilter() {
        return clickButton(addSubFilterButton);
    }

    public BasePage addTask(String task) {
        switchToFrame(POPUP);
        waitUntilElementIsDisplayed(addTaskButton);
        selectComboBox(selectTask, task);
        getTask(task);
        clickButton(addTaskButton);
        return this;
    }

    public BasePage andRadioButton() {
        andRadioButton.click();
        return this;
    }

    public void assertAbsenceOfOptions(String comboBoxId, String... options) {
        WebElement comboBox = findElement(comboBoxId);
        comboBox.click();
        Arrays.asList(options).forEach(opt -> {
            List<String> actualOptList = getOptionList(comboBox);
            if (actualOptList.contains(opt)) {
                throw new AssertionError("Unexpected option (" + opt + ") found inside dropdown #" + comboBoxId);
            }
        });
    }

    public BasePage assertButtonsAreEnabled(boolean enabled, IBottomButtons... buttons) {
        waitForUpdate();
        assertButtonsArePresent(buttons);
        switchToFrame(BOTTOM_MENU);
        setDefaultImplicitlyWait();
        for (IBottomButtons button : buttons) {
            WebElement btn = findElement(button.getId());
            if (btn.getAttribute("class").equals("button_disabled") == enabled) {
                switchToPreviousFrame();
                throw new AssertionError("Button " + button + " has unexpected state (" + (enabled ? "disabled)" : "enabled)"));
            }
        }
        switchToPreviousFrame();
        return this;
    }

    protected BasePage assertButtonsArePresent(IBottomButtons... buttons) {
        switchToFrame(BOTTOM_MENU);
        setDefaultImplicitlyWait();
        for (IBottomButtons button : buttons) {
            List<WebElement> list = findElements(button.getId());
            if (list.size() != 1 || !list.get(0).isDisplayed()) {
                switchToPreviousFrame();
                throw new AssertionError("Button " + button + " not found");
            }
        }
        switchToPreviousFrame();
        return this;
    }

    public BasePage assertButtonIsEnabled(boolean expectedActive, String id) {
        if (buttonIsActive(id) == expectedActive) {
            return this;
        }
        throw new AssertionError("Button id='" + id + "' has unexpected state (" +
                (expectedActive ? "disabled" : "enabled") + ")");
    }

    public void assertCellEndsWith(String tabId, int row, int column, String expectedText) {
        getTable(tabId).assertEndsWith(row, column, expectedText);
    }

    public BasePage assertCheckboxesAreSelected(String tableId, boolean expectedState, int column, int... rows) {//negative row = from |row| till end of table
        Table table = new Table(tableId);
        for (int row : rows) {
            if (row < 0) {
                for (int i = Math.abs(row); i < table.getTableSize()[0]; i++) {
                    assertTrue(table.getInput(i, column).isSelected() == expectedState,
                            "The checkbox in table cell " + i + ":" + column + " has unexpected state!");
                }
                break;
            }
            assertTrue(table.getInput(row, column).isSelected() == expectedState,
                    "The checkbox in table cell " + row + ":" + column + " has unexpected state!");
        }
        return this;
    }

    public BasePage assertColumnContainsValue(String column, String value) {
        waitForUpdate();
        String[] col = getMainTable().getColumn(column);
        if (!Arrays.asList(col).contains(value)) {
            throw new AssertionError("Column '" + column + "' doesn't contain the value '" + value + "'!");
        }
        return this;
    }

    public void assertColumnHasSeveralValues(String column) {
        waitForUpdate();
        String[] col = getMainTable().getColumn(column);
        Set<String> set = new HashSet<>(Arrays.asList(col));
        if (set.size() > 1) {
            return;
        }
        throw new AssertionError("Column '" + column + "' has 0 or single value!");
    }

    public BasePage assertColumnHasSingleValue(String column, String value) {
        waitForUpdate();
        String[] col = getMainTable().getColumn(column);
        Set<String> set = new HashSet<>(Arrays.asList(col));
        if (set.size() > 1) {
            throw new AssertionError("Column '" + column + "' has more than one value!");
        }
        if (!set.contains(value)) {
            throw new AssertionError("Column '" + column + "' doesn't contain '" + value + "'!");
        }
        return this;
    }

    public void assertDuplicateNameErrorIsDisplayed() {
        setImplicitlyWait(0);
        List<WebElement> list = driver.findElements(By.id("lblNameInvalid"));
        setDefaultImplicitlyWait();
        if (list.size() == 1) {
            return;
        }
        String warn = "Error message 'This name is already in use' not found on current page!";
        LOGGER.warn(warn);
        throw new AssertionError(warn);
    }

    public BasePage assertElementsAreAbsent(String... elementsId) {
        waitForUpdate();
        setImplicitlyWait(0);
        for (String id : elementsId) {
            List<WebElement> list = findElements(id);
            if (list.size() != 0 && list.get(0).isDisplayed()) {
                throw new AssertionError("Element with id='" + id + "' is found on current page");
            }
        }
        return this;
    }

    public BasePage assertElementsAreEnabled(WebElement... elements) {
        for (WebElement element : elements) {
            if (!element.isDisplayed() || !element.isEnabled()) {
                throw new AssertionError("Element #" + element.getAttribute("id") + " is not found / not active!");
            }
        }
        return this;
    }

    public BasePage assertElementsArePresent(boolean switchToDesktopFrame, String... elementsId) {
        if (switchToDesktopFrame) {
            switchToFrame(DESKTOP);
        }
        setDefaultImplicitlyWait();
        for (String el : elementsId) {
            List<WebElement> list = findElements(el);
            if (list.size() != 1 || !list.get(0).isDisplayed()) {
                switchToPreviousFrame();
                throw new AssertionError("Element #" + el + " not found");
            }
        }
        if (switchToDesktopFrame) {
            switchToPreviousFrame();
        }
        return this;
    }

    public BasePage assertElementsArePresent(String... elementsId) {
        return assertElementsArePresent(true, elementsId);
    }

    public BasePage assertElementIsSelected(String id) {
        WebElement element = findElement(id);
        if (element.isSelected()) {
            return this;
        }
        String type = element.getAttribute("type");
        String el = type.equals("radio") ? "Radiobutton" : type.equals("checkbox") ? "Checkbox" : "Element";
        throw new AssertionError(el + " id='" + id + "' is not selected!");
    }

    public BasePage assertEquals(String actual, String expected) {
        Assert.assertEquals(actual, expected);
        return this;
    }

    public void assertEquals(int actual, int expected, String message) {
        Assert.assertEquals(actual, expected, message);
    }

    public BasePage assertEquals(String actual, String expected, String message) {
        Assert.assertEquals(actual, expected, message);
        return this;
    }

    public void assertEquals(Object actual, Object expected, String message) {
        Assert.assertEquals(actual, expected, message);
    }

    public BasePage assertEqualsAlertMessage(String expectedMessage) {
        switchToFrame(ROOT);
        String out = alertWindow.getText();
        okButtonPopUp();
        switchToFrame(DESKTOP);
        Assert.assertEquals(out, expectedMessage);
        return this;
    }

    public BasePage assertFalse(boolean condition) {
        Assert.assertFalse(condition);
        return this;
    }

    public BasePage assertFalse(boolean condition, String message) {
        Assert.assertFalse(condition, message);
        return this;
    }

    public BasePage assertItemIsSelected() {
        return assertItemIsSelected(selectedName);
    }

    public BasePage assertItemIsSelected(String text) {
        Table table = getMainTable();
        WebElement cell = table.getCellWebElement(table.getFirstRowWithText(text), 0);
        List<WebElement> checkBoxList = cell.findElements(By.tagName("input"));
        if (checkBoxList.size() > 0) {
            if (!checkBoxList.get(0).isSelected()) {
                throw new AssertionError("Table item with text '" + text + "' is not selected!");
            }
            return this;
        }
        throw new AssertionError("Checkbox not found!");
    }

    public BasePage assertMainPageIsDisplayed() {
        assertElementsAreEnabled(filterViewComboBox, filterManufacturerComboBox, filterModelNameComboBox, editButton, newViewButton, resetViewButton);
        return this;
    }

    public BasePage assertPresenceOfOptions(String comboBoxId, String... options) {
        WebElement comboBox = findElement(comboBoxId);
        comboBox.click();
        List<String> actualOptionList = getOptionList(comboBox);
        Set<String> expectedOptionSet = new HashSet<>(Arrays.asList(options));
        expectedOptionSet.removeAll(actualOptionList);
        if (!expectedOptionSet.isEmpty()) {
            throw new AssertionError("Options :" + expectedOptionSet + " not found inside dropdown #" + comboBoxId + "'");
        }
        return this;
    }

    public void assertPresenceOfParameter(String value) {
        getTable("tblTasks")/*.print()*/.assertPresenceOfParameter(value);
    }

    public BasePage assertPresenceOfValue(String tableId, int column, String value) {
        getTable(tableId).assertPresenceOfValue(column, value);
        return this;
    }

    private BasePage assertSelectedOptionIs(WebElement combobox, String option) {
        String actual = getSelectedOption(combobox);
        if (actual.equalsIgnoreCase(option)) {
            return this;
        }
        throw new AssertionError("Unexpected option is selected! Expected: " + option + "; actual: " + actual);
    }

    public BasePage assertSelectedViewIs(String expectedView) {
        return assertSelectedOptionIs(filterViewComboBox, expectedView);
    }

    public void assertTableColumnAmountIs(int number, String tableId) {
        int actual = getTable(tableId).getTableSize()[1];
        if (actual != number) {
            throw new AssertionError("Wrong number of table column! Expected: " + number + ", but found :" + actual);
        }
    }

    public BasePage assertTableIsEmpty(String id) {
        Table table = getTable(id);
        if (table.getTableSize()[0] > 0) {
            throw new AssertionError("Unexpected table content (expected: empty table)");
        }
        return this;
    }

    public BasePage assertTableIsNotEmpty(String tableId) {
        Table table = getTable(tableId);
        if (table.getTableSize()[0] == 0) {
            throw new AssertionError("Unexpected table content (expected: not empty table)");
        }
        return this;
    }

    public BasePage assertTrue(boolean condition) {
        Assert.assertTrue(condition);
        return this;
    }

    public BasePage assertTrue(boolean condition, String message) {
        Assert.assertTrue(condition, message);
        return this;
    }

    public void assertWarningMessageIsDisplayed() {
        switchToFrame(ROOT);
        if (!alertWindow.isDisplayed()) {
            throw new AssertionError("Warning message is expected, but not found!");
        }
    }

    public BasePage bottomButton() {
        return clickButton(bottomButton);
    }

    public BasePage bottomMenu(IBottomButtons button) {
        clickBottomButton(button);
        return this;
    }

    public BasePage cancelButtonPopUp() {
        switchToFrame(ROOT);
        waitForUpdate();
        while (cancelButton.isDisplayed()) {
            showPointer(cancelButton).click();
            waitForUpdate();
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='0px solid red'", cancelButton);
        }
        switchToFrame(DESKTOP);
        return this;
    }

    public void checkRefreshPage() {
        assertMainPageIsDisplayed();
        String beforeSorting = null;
        String beforeSortingArrowDirection = null;
        WebElement mainTable;
        Table table;
        if (!(table = getTable(getMainTableId())).isEmpty()) {
            mainTable = findElement(getMainTableId());
            WebElement arrow = mainTable.findElement(By.tagName("img"));
            beforeSorting = arrow.findElement(By.xpath("preceding-sibling::*")).getText();
            beforeSortingArrowDirection = arrow.getAttribute("src");
        }
        List<String> before = new ArrayList<>(Arrays.asList(getSelectedOption(filterViewComboBox), getSelectedOption(filterManufacturerComboBox), getSelectedOption(filterModelNameComboBox)));
        bottomMenu(REFRESH);
        assertMainPageIsDisplayed();
        List<String> after = new ArrayList<>(Arrays.asList(getSelectedOption(filterViewComboBox), getSelectedOption(filterManufacturerComboBox), getSelectedOption(filterModelNameComboBox)));
        assertEquals(after, before, "Values of bottom dropdowns are changed!");
        if (beforeSorting != null) {
            mainTable = findElement(getMainTableId());
            WebElement arrow = mainTable.findElement(By.tagName("img"));
            String afterSorting = arrow.findElement(By.xpath("preceding-sibling::*")).getText();
            String afterSortingArrowDirection = arrow.getAttribute("src");
            assertEquals(afterSorting, beforeSorting, "Sorting arrow changed location!");
            assertEquals(afterSortingArrowDirection, beforeSortingArrowDirection, "Sorting arrow direction changed!");
            try {
                table.clickOn(0, 0);
                throw new AssertionError("Page has not been refreshed!");
            } catch (StaleElementReferenceException e) {
                System.out.println("page has been refreshed successfully");
            }
        }
    }

    void clickBottomButton(IBottomButtons button) {
        waitForUpdate();
        switchToFrame(BOTTOM_MENU);
        setDefaultImplicitlyWait();
        for (int i = 0; i < 2; i++) {
            try {
                new FluentWait<>(driver)
                        .withMessage("Button " + button + " was not active")
                        .withTimeout(Duration.ofSeconds(2))
                        .pollingEvery(Duration.ofMillis(100))
                        .until(ExpectedConditions.elementToBeClickable(findElement(button.getId())));
                pause(200);
                showPointer(findElement(button.getId())).click();
                waitForUpdate();
                return;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                LOGGER.warn("Button " + button + " click failed. Try again...(" + (i + 1) + " attempt)\n" + e.getMessage());
                pause(1000);
            } finally {
                switchToFrame(DESKTOP);
            }
        }
        throw new AssertionError("Cannot click button " + button);
    }

    public BasePage clickButton(WebElement button) {
        waitForUpdate();
        for (int i = 0; i < 2; i++) {
            try {
                new FluentWait<>(driver)
                        .withMessage("Button '#" + button.getAttribute("id") + "' is grayed out")
                        .withTimeout(Duration.ofSeconds(IMPLICITLY_WAIT))
                        .pollingEvery(Duration.ofMillis(100))
                        .until(ExpectedConditions.elementToBeClickable(button));
                showPointer(button).click();
                waitForUpdate();
                return this;
            } catch (StaleElementReferenceException e) {
                LOGGER.info("Button click failed. Retrying..." + (i + 1) + "time(s)");
            }
        }
        throw new AssertionError("cannot click button!");
    }

    public BasePage clickIfPresent(IBottomButtons button) {
        waitForUpdate();
        waitUntilBottomMenuIsDownloaded();
        switchToFrame(BOTTOM_MENU);
        List<WebElement> list = findElements(button.getId());
        if (!list.isEmpty() && list.get(0).isDisplayed()) {
            list.get(0).click();
            okButtonPopUp();
        }
        waitForUpdate();
        switchToFrame(DESKTOP);
        return this;
    }

    public BasePage clickOn(String id) {
        waitForUpdate();
        scrollToElement(findElement(id)).click();
        return this;
    }

    public BasePage clickOnTable(String id, String text) {
        getTable(id).clickOn(text);
        return this;
    }

    public BasePage clickOnTable(String id, int row, int column) {
        getTable(id).clickOn(row, column);
        return this;
    }

    public BasePage clickOnTable(String id, int row, int column, int tagNum) {
        getTable(id).clickOn(row, column, tagNum);
        return this;
    }

    public void closeMapWindow() {
        switchToFrame(ROOT);
        pause(2000);
        getTable("tblPopupTitle").clickOn(0, 1);
        switchToFrame(DESKTOP);
    }

    public void closePopup() {
        switchToFrame(ROOT);
        executeScript("PopupHide('cancel');");
    }

    public void compareTable(String tableId) {
        Table table = new Table(tableId);
        if (savedTable.equals(table)) {
            return;
        }
        System.out.println("Table 1:");
        savedTable.print();
        System.out.println("Table 2:");
        table.print();
        throw new AssertionError("Table comparing failed!");
    }

    public BasePage defaultViewForCurrentUserCheckbox() {
        defaultViewCheckbox.click();
        return this;
    }

    public BasePage deleteAll(WebElement element) {
        waitForUpdate();
        List<String> optList = getOptionList(element);
        String prefix = BaseTestCase.getTestName().substring(0, BaseTestCase.getTestName().lastIndexOf('_'));
        for (String opt : optList) {
//            if (opt.matches(".{3,5}_\\w{2}_\\d{3}(_\\d)?")) {
            if (opt.startsWith(prefix)) {
                selectComboBox(element, opt);
                waitForUpdate();
                editButton.click();
                bottomMenu(DeviceUpdatePage.BottomButtons.DELETE_VIEW);
                okButtonPopUp();
                waitForUpdate();
            }
        }
        return this;
    }

    public BasePage deleteAllCustomViews() {
        return deleteAll(filterViewComboBox);
    }

    public void deleteAllMonitoring() {
        StringBuilder request;
        Set<String> idSet = DataBaseConnector.getMonitorIdSetByModelName();
        if (idSet.size() > 0) {
            request = new StringBuilder("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ftac=\"http://ftacs.com/\">" +
                    "<soapenv:Header/><soapenv:Body><ftac:deleteMonitoring><ids>");
            for (String id : idSet) {
                request.append("<id>").append(id).append("</id>");
            }
            request.append("</ids></ftac:deleteMonitoring></soapenv:Body></soapenv:Envelope>");
            String response = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>" +
                    "<ns2:deleteMonitoringResponse xmlns:ns2=\"http://ftacs.com/\"/></soap:Body></soap:Envelope>";
            try {
                assertEquals(HttpConnector.sendSoapRequest(request.toString()), response, "Unexpected response after all monitoring delete!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BasePage deleteFilter() {
        return clickButton(deleteFilterButton);
    }

    public BasePage deselectCheckbox(String id) {
        WebElement checkbox = findElement(id);
        return setCheckboxState(false, checkbox);
    }

    public BasePage disableAllEvents() {
        Table table = new Table("tblEvents");
        for (int i = 1; i < table.getTableSize()[0]; i++) {
            setEvent(new Event(table.getCellText(i, 0), false, "", null), table);
        }
        return this;
    }

    public BasePage downButton() {
        return clickButton(downButton);
    }

    public BasePage editButton() {
        return clickButton(editButton);
    }

    public boolean elementIsAbsent(String id) {
        waitForUpdate();
        setImplicitlyWait(0);
        boolean out = findElements(id).size() == 0;
        setDefaultImplicitlyWait();
        return out;
    }

    public boolean elementIsPresent(String id) {
        return !elementIsAbsent(id);
    }

    public BasePage enterIntoItem(String itemName) {
        getMainTable().clickOn(itemName);
        waitForUpdate();
        return this;
    }

    public void executeScript(String script) {
        ((JavascriptExecutor) getDriver()).executeScript(script);
    }

    public BasePage expandEvents() {
        findElement("imgSpoilerEvents").click();
        return this;
    }

    public BasePage fillDownloadUrl() {
        fromListRadioButton.click();
        List<String> optList = getOptionList(fileNameComboBox);
        String lastOpt = optList.get(optList.size() - 1);
        String fileType = BaseTestCase.getTestName().contains("_du") ? "Download" : getSelectedOption(selectDownloadFileTypeComboBox);
        manuallyDownloadRadioButton();
        String value = props.getProperty("file_server") + lastOpt;
        getParameterMap().put(fileType, value);//delete
        getTask().setParameterName(fileType);
        getTask().setValue(value);
        urlField.sendKeys(value);
        return this;
    }

    public BasePage fillName() {
        nameField.sendKeys(BaseTestCase.getTestName());
        return this;
    }

    public BasePage fillName(String name) {
        nameField.sendKeys(name);
        return this;
    }

    public BasePage fillPassword() {
        passwordField.sendKeys(getProps().getProperty("ftp_password"));
        return this;
    }

    public BasePage fillUploadPassword() {
        passwordUploadField.sendKeys(props.getProperty("ftp_password"));
        return this;
    }

    public BasePage fillUploadUrl() {
        uploadUrlField.clear();
        String path = props.getProperty("file_server");
        uploadUrlField.sendKeys(path);
        waitForUpdate();
        userNameUploadField.click();
        String fileType = BaseTestCase.getTestName().contains("_du") ? "Upload" : getSelectedOption(selectUploadFileTypeComboBox);
        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        getParameterMap().put(fileType, path);//delete
        getTask().setParameterName(fileType);
        return this;
    }

    public BasePage fillUploadUserName() {
        userNameUploadField.sendKeys(props.getProperty("ftp_user"));
        return this;
    }

    public BasePage fillUsername() {
        userNameField.sendKeys(getProps().getProperty("ftp_user"));
        return this;
    }

    public BasePage filterRecordsCheckbox() {
        Table table = new Table("tblTree");
        for (int i = 0; i < table.getTableSize()[0]; i++) {
            table.getInput(i, 0).click();
        }
        return this;
    }

    public List<WebElement> findElements(String byId) {
        return driver.findElements(By.id(byId));
    }

    protected List<WebElement> findElementsByText(String text) {
        return driver.findElements(By.xpath("//*[text() = '" + text + "']"));
    }

    @SuppressWarnings("unused")
    public void forceFail() {
        throw new AssertionError("Test is ok, but forced down manually");
    }

    String generateValue(String parameter, String oldValue) {
        String value;
        int generatedValue = (int) (Math.random() * 1000);
        String paramType = DataBaseConnector.getValueType(parameter).toLowerCase();
        switch (paramType) {
            case "string":
                value = "value" + generatedValue;
                break;
            case "int":
            case "integer":
            case "unsignedint":
                value = "" + generatedValue;
                break;
            case "datetime":
                value = CalendarUtil.getDbShiftedDate(0);   //"2019-10-27T02:00:0";
                break;
            case "opaque":
                value = " ";
                break;
            case "time":
                value = CalendarUtil.getTimeStamp();
                break;
            case "boolean":
                switch (oldValue) {
                    case "false":
                        value = "true";
                        break;
                    case "true":
                        value = "false";
                        break;
                    case "1":
                        value = "0";
                        break;
                    default:
                        value = "1";
                        break;
                }
                break;
            default:
                throw new AssertionError("Unsupported data type:" + paramType);
        }
        return value;
    }

    protected String[] getDevice(String protocol) {
        return DataBaseConnector.getDevice(props.getProperty(protocol + "_cpe_serial"));
    }

    public String getElementText(String id) {
        return driver.findElement(By.id(id)).getText();
    }

    public String getExportLink(String groupName) {
        return props.getProperty("ui_url") + "/Update/Export.aspx?updateId=" + DataBaseConnector.getGroupUpdateId(groupName);
    }

    public Table getMainTable() {
        return getTable(getMainTableId());
    }

    public String getMainTableId() {
        return "tbl";
    }

    protected Table getMainTableWithText(String text, String sortedColumn) {
        Table table = getMainTable();
        if (table.isEmpty()) {
            clickIfPresent(REFRESH);
            waitForUpdate();
            table = getMainTable();
        }
        if (!table.contains(text)) {
            table.clickOn(sortedColumn);
            waitForUpdate();
            table = getMainTable();
            if (!table.contains(text)) {
                setSinglePage();
                table = getMainTable();
            }
        }
        return table;
    }

    public String getManufacturer() {
        return DataBaseConnector.getDevice(getSerial())[0];
    }

    public String getModelName() {
        return DataBaseConnector.getDevice(getSerial())[1];
    }

    public List<String> getOptionList(WebElement comboBox) {
        List<String> out = new ArrayList<>();
        new Select(comboBox)
                .getOptions()
                .forEach(element -> out.add(element.getText()));
        return out;
    }

    public BasePage getParameter(int row, int column) {//column = 0 - All, 1 - names, 2 - values, 3 - attributes;
        Table table = new Table("tblParamsValue");
        String hint = table.getHint(row);
        String values;
        if (column < 1) {
            values = "values,names,attributes";
            for (int i = 1; i < table.getRowLength(row); i++) {
                table.clickOn(row, i, 0);
            }
        } else {
            String[] valuesArr = {"", "names", "values", "attributes"};
            values = valuesArr[column];
            table.clickOn(row, column, 0);
        }
        getParameterMap().put(hint, values);//delete
        getTask().setParameterName(hint);
        getTask().setValue(hint);
        return this;
    }

    protected Map<String, String> getParameterMap() {
        if (parameterMap == null) {
            parameterMap = new HashMap<>();
        }
        return parameterMap;
    }

    public String getRandomStringValue(int length) {
        String sample = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(sample.charAt((int) (Math.random() * 62)));
        }
        return sb.toString();
    }

    public String getSelectedOption(String comboBoxId) {
        return getSelectedOption(findElement(comboBoxId));
    }

    public String getSelectedOption(WebElement comboBox) {
        List<WebElement> optList = comboBox.findElements(By.tagName("option"));
        for (WebElement el : optList) {
            if (el.getAttribute("selected") != null) {
                return el.getText();
            }
        }
        return null;
    }

    public Map.Entry<String, String> getSingleParameterEntry() {
        return new ArrayList<>(getParameterMap().entrySet()).get(0);
    }

    public Table getTable(String id, FrameSwitch frame) {
        waitForUpdate();
        if (frame != null) {
            switchToFrame(frame);
        }
        Table table = null;
        Timer timer = new Timer(30000);
        while (!timer.timeout()) {
            try {
                table = new Table(id);
                break;
            } catch (StaleElementReferenceException e) {
                System.out.println("StaleElementReferenceException handled. Re-parse table...");
            }
        }
        if (table == null) {
            throw new AssertionError("Web table parsing process failed! (WebElement id = '" + id + "'");
        }
        return savedTable = table;
    }

    public Table getTable(String id) {
        return getTable(id, null);
    }

    public Table getTable(String id, int expectedRowsNumber, boolean checkAsymmetry) {
        Table table = getTable(id);
        Timer timer = new Timer();
        while (!timer.timeout()) {
            if (table.getTableSize()[0] == expectedRowsNumber && (!checkAsymmetry || !table.isAsymmetric())) {
                return table;
            }
            waitForUpdate();
            table = getTable(id);
        }
        throw new AssertionError("Table rows number != " + expectedRowsNumber);
    }

    public Table getTabTable() {
        return getTable("tabsSettings_tblTabs", null);
    }

    public BasePage immediately() {
        immediatelyRadioButton.click();
        return this;
    }

    public BasePage inputDnsField(String text) {
        inputDnsField.clear();
        inputDnsField.sendKeys(text);
        return this;
    }

    public BasePage inputHost(String text) {
        inputHostField.clear();
        inputHostField.sendKeys(text);
        getTask(null).setValue(text);
        return this;
    }

    public BasePage inputNumOfRepetitions(String text) {
        numOfRepetitionsField.clear();
        numOfRepetitionsField.sendKeys(text);
        return this;
    }

    public BasePage inputText(String id, String text) {
        return inputText(findElement(id), text);
    }

    public BasePage inputText(WebElement el, String text) {
        el.clear();
        el.sendKeys(text);
        return this;
    }

    public void inputText2(WebElement el, String text) {
        el.clear();
        el.sendKeys(text + " ");
        waitForUpdate();
        el.sendKeys(Keys.BACK_SPACE);
        waitForUpdate();
    }

    public BasePage inputTextField(String text) {
        inputTextField.sendKeys(text);
        return this;
    }

    public boolean buttonIsActive(IBottomButtons button) {
        switchToFrame(BOTTOM_MENU);
        List<WebElement> list = findElements(button.getId());
        boolean out = list.size() == 1 && list.get(0).getAttribute("class").equals("button_default");
        switchToPreviousFrame();
        return out;
    }

    public boolean buttonIsActive(String id) {
        return !findElement(id).getAttribute("class").equals("button_disabled");
    }

    public BasePage itemsOnPage(String number) {
        if (elementIsPresent("ddlPageSizes")) {
            selectComboBox(itemsOnPageComboBox, number);
            waitForUpdate();
        }
        return this;
    }

    public BasePage leftMenu(ILeft item) {
        switchToFrame(ROOT);
        Timer timer = new Timer();
        try {
            Table table;
            List<Integer> list;
            do {
                table = getTable("tblLeftMenu");
                list = table.getRowsWithText(item.getValue());
            } while (list.size() == 0 && !timer.timeout());
            table.clickOn(list.get(0), 0);
//            showPointer(table.getCellWebElement(list.get(0), 0)).click();
        } catch (ElementNotInteractableException | IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            throw new AssertionError("Left menu item '" + item + "' not found on current page!");
        }
        waitForUpdate();
        switchToFrame(DESKTOP);
        return this;
    }

    public void logOut() {
        switchToFrame(ROOT);
        waitForUpdate();
        logOutButton.click();
    }

    public BasePage manuallyDownloadRadioButton() {
        waitForUpdate();
        manuallyDownloadRadioButton.click();
//        waitForUpdate();
        return this;
    }

    public BasePage manuallyUploadRadioButton() {
        waitForUpdate();
        manuallyUploadRadioButton.click();
        return this;
    }

    public BasePage newViewButton() {
        return clickButton(newViewButton);
    }

    public BasePage okButtonPopUp() {
        switchToFrame(ROOT);
        waitForUpdate();
        while (okButtonAlertPopUp.isDisplayed()) {
            showPointer(okButtonAlertPopUp).click();
            waitForUpdate();
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='0px solid red'", okButtonAlertPopUp);
        }
        while (okButtonPopUp.isDisplayed()) {
            showPointer(okButtonPopUp).click();
            waitForUpdate();
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='0px solid red'", okButtonPopUp);
        }
        switchToFrame(DESKTOP);
        return this;
    }

    public BasePage pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Map<String, Event> readEvents(String tableId) {
        Table table = new Table(tableId);
        setImplicitlyWait(0);
        Map<String, Event> map = new HashMap<>();
        for (int i = 1; i < table.getTableSize()[0]; i++) { //Very slow performance!!!
            String countOfEvents = getSelectedOption(table.getCellWebElement(i, 2).findElement(By.tagName("select")));
            if (countOfEvents.isEmpty()) {
                continue;
            }
            boolean onEachEvent = table.getInput(i, 1).isSelected();
            List<WebElement> selectList = table.getCellWebElement(i, 3).findElements(By.tagName("select"));
            String num = getSelectedOption(selectList.get(0));
            String units = getSelectedOption(selectList.get(1));
            String name = table.getCellText(i, 0);
            map.put(name, new Event(name, onEachEvent, countOfEvents, num + ":" + units));
        }
        setDefaultImplicitlyWait();
        return map;
    }

    public BasePage readTasksFromDb() {
        List<String[]> groupList;
        int count = Integer.parseInt(props.getProperty("pending_tasks_check_time"));
        for (int i = 0; i < count; i++) {
            long start = System.currentTimeMillis();
            groupList = DataBaseConnector.getTaskList();
            if (groupList.isEmpty()) {
                String warn = "There are no tasks created by '" + BaseTestCase.getTestName() + "' Group Update";
                throw new AssertionError(warn);
            }
            Set<String> StateSet = new HashSet<>();
            for (String[] line : groupList) {
                StateSet.add(line[2]);
            }
            if (!StateSet.contains("1")) {
                if (StateSet.size() != 1 || !StateSet.contains("2")) {
                    LOGGER.info("All tasks created. One or more tasks failed or rejected");
                }
                return this;
            }
            long timeout;
            if ((timeout = 1000 - System.currentTimeMillis() + start) > 0) {
                pause(timeout);
            }
        }
        LOGGER.info("All tasks created. One or more tasks remains in pending state");
        return this;
    }

    public BasePage rebootRadioButton() {
        waitForUpdate();
        Table table = getTable("tblActions");
        table.getInput("Reboot", 1).click();
        return this;
    }

    public BasePage resetView() {
        resetViewButton.click();
        waitForUpdate();
        return this;
    }

    public BasePage saveFileName() {
        switchToFrame(ROOT);
        String message = alertWindow.getText();
        Pattern datePattern = Pattern.compile("Inventory_.+_(.+)\\)\\.");
        Pattern extPattern = Pattern.compile("\\)\\.([xmlcsv]{3})'");
        Matcher m = datePattern.matcher(message);
        Matcher extM = extPattern.matcher(message);
        if (m.find()) {
            try {
                Date date = CalendarUtil.getDate(m.group(1));
                if (extM.find()) {
                    if (extM.group(1).equals("csv")) {
                        csvFileTime = date;
                    } else if (extM.group(1).equals("xml")) {
                        xmlFileTime = date;
                    } else {
                        throw new AssertionError("File extension parsing error!");
                    }
                }
            } catch (ParseException e) {
                System.out.println("Date parsing error! \n" + message);
            }
        }
        return this;
    }

    public BasePage saveTable(String tableId) {
        savedTable = new Table(tableId);
        return this;
    }

    public BasePage scheduledTo() {
        scheduledToRadioButton.click();
        return this;
    }

    public void scrollTo(WebElement element) {
        ((JavascriptExecutor) BasePage.getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public BasePage selectAction(String action) {
        return selectAction(action, null);
    }

    public BasePage selectAction(String action, String instance) {
        waitForUpdate();
        Table table = getTable("tblActions");
        table.getInput(action, 1).click();
        WebElement select = table.getSelect(action, 0);
        if (select != null) {
            if (instance == null) {
                List<String> options = getOptionList(select);
                instance = options.get(options.size() - 1);
            }
            selectComboBox(select, instance);
            action += " - instance " + instance;//!23.12.2020
        }
        if (!action.equals("Custom RPC")) {
            getParameterMap().put("Action", action);//!23.12.2020//delete
        }
        getTask(null).setParameterName(action);
        return this;
    }

    public void selectAnotherBranch() {
        String branch = getElementText("divPath");
        Table branchTable = new Table("tblTree");
        for (int i = 0; i < branchTable.getTableSize()[0]; i++) {
            if (branchTable.getCellWebElement(i, 0).isDisplayed()) {
                branchTable.clickOn(i, 0, -1);
                if (!getElementText("divPath").equals(branch)) {
                    setDefaultImplicitlyWait();
                    return;
                }
            }
            setImplicitlyWait(0);
            List<WebElement> tagList = branchTable.getCellWebElement(i, 0).findElements(By.tagName("img"));
            if (!tagList.isEmpty() && tagList.get(0).getAttribute("src").endsWith("expand.png")) {
                tagList.get(0).click();
            }
        }
    }

    public BasePage selectBranch(String branch) {
//        System.out.println(branch);
        waitForUpdate();
        List<String> nodeList = new ArrayList<>();
        Pattern p = Pattern.compile("(.+?(\\.\\d+)?)(\\.|$)");
        Matcher m = p.matcher(branch);
        while (m.find()) {
            if (m.group(1).matches(".+\\.\\d+")) {
                nodeList.add(m.group(1).split("\\.\\d+")[0]);
            }
            nodeList.add(m.group(1));
        }
        Table branchTable = new Table("tblTree");
        for (String node : nodeList) {
            int rowNum = branchTable.getFirstRowWithText(node);
            WebElement cell = branchTable.getCellWebElement(rowNum, 0);
            List<WebElement> tagList = cell.findElements(By.xpath("child::img | child::span | child::input"));
            if (tagList.get(0).getTagName().equals("img") && tagList.get(0).getAttribute("src").endsWith("expand.png")) {
//                System.out.println("click on node:" + node);
                tagList.get(0).click();
            }
            tagList.get(tagList.size() - 1).click();    //edited 03.09.2020 due to tr181_du_194 failed
        }
        waitForUpdate();
        return this;
    }

    public BasePage selectCheckbox(String id) {
        WebElement checkbox = findElement(id);
        return setCheckboxState(true, checkbox);
    }

    public BasePage selectColumnFilter(String option) {
        selectComboBox(selectColumnFilter, option);
        return this;
    }

    protected void selectComboBox(WebElement comboBox, String value) {
        waitForUpdate();
        List<WebElement> options = comboBox.findElements(By.tagName("option"));
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase(value)) {
                new Select(comboBox).selectByValue(option.getAttribute("value"));
                return;
            }
        }
        new Select(comboBox).selectByValue(value);
    }

    public BasePage selectCompare(String option) {
        selectComboBox(selectCompare, option);
        return this;
    }

    public BasePage selectDate(String date) {
        executeScript("CalendarPopup_FindCalendar('calFilterDate').SelectDate('" + date + "')");
        return this;
    }

    public BasePage selectDiagnostic(String value) {
        try {
            getTask(null).setParameterName(value);
            selectComboBox(diagnosticTypeComboBox, value);
        } catch (NoSuchElementException e) {
            String warn = value + " type is not supported by current device!";
            throw new AssertionError(warn);
        }
        return this;
    }

    public BasePage selectDownloadFileType(String type) {
        return selectFileType(type, selectDownloadFileTypeComboBox);
    }

    public BasePage selectFileName(String parameter) {
        waitForUpdate();
        List<String> optList = getOptionList(fileNameComboBox);
        String lastOpt = optList.get(optList.size() - 1);
        selectComboBox(fileNameComboBox, lastOpt);
        getParameterMap().put(parameter, props.getProperty("file_server") + lastOpt);//delete
        getTask().setParameterName(parameter);
        getTask().setValue(props.getProperty("file_server") + lastOpt);
        return this;
    }

    private BasePage selectFileType(String type, WebElement combobox) {
        selectComboBox(new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(IMPLICITLY_WAIT))
                .pollingEvery(Duration.ofMillis(100))
                .until(ExpectedConditions.elementToBeClickable(combobox)), type);
        return this;
    }

    public BasePage selectFilterManufacturer(String value) {
        selectComboBox(filterManufacturerComboBox, value);
        waitForUpdate();
        return this;
    }

    public BasePage selectFilterModelName(String value) {
        selectComboBox(filterModelNameComboBox, value);
        return this;
    }

    public BasePage selectFromListRadioButton() {
        fromListRadioButton.click();
        return this;
    }

    public BasePage selectImportDevicesFile() {
        XmlWriter.createImportCpeFile();
        switchToFrame(DESKTOP);
        driver.switchTo().frame(importFrame);
        String inputText = new File("import/" + getProtocolPrefix() + "_import_cpe.xml").getAbsolutePath();
        importDeviceField.sendKeys(inputText);
        driver.switchTo().parentFrame();
        return this;
    }

    public BasePage selectItem() {
        selectItem(BaseTestCase.getTestName());
        return this;
    }

    public BasePage selectItem(String text) {
        return selectItem(text, 1);
    }

    public BasePage selectItem(String text, int startFromRow) {
        return selectItem(getMainTable(), text, startFromRow);
    }

    public BasePage selectItem(Table table, String text, int startFromRow) {
        selectedName = "";
        List<Integer> list = table.getRowsWithText(text);
        for (int i : list) {
            if (i >= startFromRow) {
                table.clickOn(i, 0);
                if (table.contains("Name")) {
                    int colNumber = table.getColumnNumber(0, "Name");
                    if (colNumber >= 0) {
                        selectedName = table.getCellText(i, colNumber);
                    }
                }
                break;
            }
        }
        return this;
    }

    public BasePage selectMainTab(String tab) {
        getTable("tabsMain_tblTabs").clickOn(tab);
        waitForUpdate();
        return this;
    }

    public BasePage selectManufacturer(String manufacturer) {
        selectComboBox(manufacturerComboBox, manufacturer);
        waitForUpdate();
        return this;
    }

    public BasePage selectMethod(String value) {
        selectComboBox(selectMethodComboBox, value);
        getParameterMap().put("Custom RPC", value);
        getTask(null).setValue(value);
        return this;
    }

    public BasePage selectModel(String modelName) {
        List<String> list = getOptionList(modelComboBox);
        if (list.size() == 1) {
            selectComboBox(modelComboBox, list.get(0));
        } else {
            selectComboBox(modelComboBox, modelName);
        }
        waitForUpdate();
        return this;
    }

    public BasePage selectModel() {
        selectComboBox(modelComboBox, getModelName());
        return this;
    }

    public BasePage selectSendTo(String value) {
        selectComboBox(sendToComboBox, value);
        waitForUpdate();
        return this;
    }

    public BasePage selectShiftedDate(String id, int value) {
        switchToFrame(DESKTOP);
        executeScript("CalendarPopup_FindCalendar('" + id + "').SelectDate('" + CalendarUtil.getShiftedDate(value) + "')");
        return this;//CalendarPopup_FindCalendar('calDate1').SelectDate('1/25/2021')
    }

    public BasePage selectSendTo() {
        return selectSendTo("All");
    }

    public BasePage selectTab(String tab) {
        return selectTab(tab, getTabTable());
    }

    public BasePage selectTab(String tab, Table tabTable) {
        if (!tab.equals("Management") && !tab.equals("Device")) {
            String start;
            try {
                start = new Table("tblTree").getCellText(0, 0);
            } catch (StaleElementReferenceException e) {
                start = new Table("tblTree").getCellText(0, 0);
            }
            tabTable.clickOn(tab);
            Timer timer = new Timer(10000);
            while (!timer.timeout()) {
                waitForUpdate();
                try {
                    if (!new Table("tblTree").getCellText(0, 0).equals(start)) {
                        break;
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("BP:1731 - StaleElementReferenceException handled (selectTab)");
                }
            }
        }
        waitForUpdate();
        return this;
    }

    public BasePage selectTreeObject(boolean clickOnCheckbox, int objNum) {
        Table table = new Table("tblTree");
        List<Integer> rows = table.getVisibleRowsWithInput(0);
        while (rows.size() <= objNum) {
            List<WebElement> expanders = table.getExpandableRowList();
            if (expanders.size() == 0) {
                throw new AssertionError("There are not enough clickable objects to finish this test case!");
            }
            expanders.get(0).click();
            table = new Table("tblTree");
            rows = table.getVisibleRowsWithInput(0);
        }
        table.clickOn(rows.get(objNum), 0, -1);
        waitForUpdate();
        if (clickOnCheckbox) {
            table.clickOn(rows.get(objNum), 0, -2);
            waitForUpdate();
        }
        return this;
    }

    public BasePage selectUploadFileType(String type) {
        return selectFileType(type, selectUploadFileTypeComboBox);
    }

    public BasePage selectView(String value) {
        selectComboBox(filterViewComboBox, value);
        waitForUpdate();
        return this;
    }

    protected BasePage setCheckboxState(boolean setSelected, WebElement checkbox) {
        if (setSelected != checkbox.isSelected()) {
            checkbox.click();
        }
        return this;
    }

    public BasePage setEvent(Event event, Table table) {
        return setEvent(event, table, false);
    }

    public BasePage setEvent(Event event, Table table, boolean addTask) {
        if (eventMap == null) {
            eventMap = new HashMap<>();
        }
        int rowNum = table.getFirstRowWithText(0, event.getName());
        WebElement input = table.getInput(event.getName(), 1);
        if (event.isOnEachEvent() != null) {
            if (event.isOnEachEvent() != input.isSelected()) {
                input.click();
            }
        } else {
            event.setOnEachEvent(input.isSelected());
        }
        WebElement select = table.getCellWebElement(rowNum, 2).findElement(By.tagName("select"));
        if (event.getCountOfEvents() != null) {
            if (select.isEnabled()) {
                selectComboBox(select, event.getCountOfEvents());
            }
        } else {
            event.setCountOfEvents(getSelectedOption(select));
        }
        List<WebElement> selectList = table.getCellWebElement(rowNum, 3).findElements(By.tagName("select"));
        if (event.getDuration() != null) {
            if (selectList.get(0).isEnabled()) {
                selectComboBox(selectList.get(0), event.getDuration().split(":")[0]);
                selectComboBox(selectList.get(1), event.getDuration().split(":")[1]);
            }
        } else {
            String num = getSelectedOption(selectList.get(0));
            String units = getSelectedOption(selectList.get(1));
            event.setDuration(num + ":" + units);
        }
        if (Objects.requireNonNull(getSelectedOption(select)).isEmpty()) {
            eventMap.remove(event.getName());
        } else {
            eventMap.put(event.getName(), event);
        }
        if (addTask) {
            table.clickOn(rowNum, 4);
        }
        waitForUpdate();
        return this;
    }

    public BasePage setEvents(int amount, Event example, Table table) {
        String[] names = table.getColumn(0);
        if (names.length == 0) {
            String warn = "No one events was found!";
            throw new AssertionError(warn);
        }
        for (int i = 0; i < Math.min(amount, names.length); i++) {
            setEvent(new Event(names[i], example.isOnEachEvent(), example.getCountOfEvents(), example.getDuration()), table);
        }
        return this;
    }

    public BasePage setParameter(Table table, String paramName, ParameterType option, String value) {
//        Table table = new Table("tblParamsValue");
        int rowNum = table.getFirstRowWithText(paramName);
        String hint = table.getHint(paramName);
        WebElement paramCell = table.getCellWebElement(rowNum, 1);
        if (props.getProperty("browser").equals("edge")) {
            scrollToElement(paramCell);
        }
        new Select(paramCell.findElement(By.tagName("select"))).selectByValue(option != ParameterType.CUSTOM ? option.getOption() : value);
        if (value != null && option == ParameterType.VALUE) {
            waitForUpdate();
            WebElement input = paramCell.findElement(By.tagName("input"));
            input.clear();
            input.sendKeys(value);
        }
        getParameterMap().put(hint, value);//delete
        getTask(null).setParameterName(hint);
        getTask(null).setValue(value);
        if (!BROWSER.equals("edge")) {
            table.clickOn(0, 0);
        }
        return this;
    }

    public BasePage setParameterOverApi(String parameter, String value) {
        String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ftac=\"http://ftacs.com/\">" +
                "<soapenv:Header/><soapenv:Body><ftac:setCPEParams><cpeList><id>" + DataBaseConnector.getDeviceId(getSerial()) +
                "</id></cpeList><cpeParamList><CPEParam><name>" + parameter + "</name><reprovision>0</reprovision><value>" +
                value + "</value></CPEParam></cpeParamList><priority>1</priority><group>1</group><push>1</push><reset>0</reset>" +
                "<transactionId>10000</transactionId><user>" + BasePage.getProps().getProperty("ui_user") +
                "</user></ftac:setCPEParams></soapenv:Body></soapenv:Envelope>";
        try {
            HttpConnector.sendSoapRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public BasePage setParametersMonitor(Condition condition) {
        return setParametersMonitor(condition, false);
    }

    public BasePage setParametersMonitor(Condition condition, boolean addTask) {
        Table table = new Table("tblParamsMonitoring");
        String name = table.getColumn(0)[0];
        String value = generateValue(table.getHint(1), "1");
        setParametersMonitor(table, name, condition, value);
        if (addTask) {
            table.clickOn(1, 3);
        }
        return this;
    }

    protected void setParametersMonitor(Table table, String parameter, Condition condition, String value) {
        parametersMonitor = new ParametersMonitor(null, condition, value);
        String[] names = table.getWholeColumn(0);
        for (int i = 1; i < names.length; i++) {
            if (names[i].equals(parameter) || table.getHint(i).equals(parameter)) {
                selectComboBox(table.getSelect(i, 1), condition.toString());
                waitForUpdate();
                if (condition != Condition.VALUE_CHANGE) {
                    WebElement field = table.getInput(i, 2);
                    field.clear();
                    field.sendKeys(value + " ");
                    waitForUpdate();
                    field.sendKeys(Keys.BACK_SPACE);
                    waitForUpdate();
                } else {
                    parametersMonitor.setValue("");
                }
                parametersMonitor.setName(table.getHint(i));
                return;
            }
        }
        throw new AssertionError("Parameter name '" + parameter + "' not found!");
    }

    public BasePage setPolicy(Table table, int scenario) {                     //Access=AcsOnly      - scenario #1
        int shift = 1;                                                         //Notification=Off *2 - scenario #2
        if (scenario == 1) {                                                   //Notification=Active;Access=AcsOnly *ALL - scenario #4
            List<Integer> rowsList = table.getRowsWithSelectList(2);    //Notification=Off                    * \
            if (!rowsList.isEmpty()) {                                         //Notification=Passive Access=AcsOnly ** - scenario #3
                shift = rowsList.get(0);                                       //Notification=Active      Access=All * /
            } else {
                Table treeTable = new Table("tblTree");
                String branch = driver.findElement(By.id("divPath")).getText();
                for (int i = 1; i < treeTable.getTableSize()[0]; i++) {
                    treeTable.clickOn(i, 0, 0);
                    waitForUpdate();
                    String newBranch = driver.findElement(By.id("divPath")).getText();
                    if (newBranch.equals(branch)) {
                        continue;
                    }
                    table = new Table("tblPolicy");
                    branch = newBranch;
                    rowsList = table.getRowsWithSelectList(2);
                    if (!rowsList.isEmpty()) {
                        shift = rowsList.get(0);
                        break;
                    }
                }
            }
        }
        int tableSize = table.getTableSize()[0];
        int limit = scenario > 3 || scenario + 1 > tableSize ? tableSize : scenario + shift;
        String[][] all = {{"Off", "Passive", "Active"}, {"Default", "AcsOnly", "All"}};
        for (int i = shift; i < limit; i++) {
            WebElement notification = table.getSelect(i, 1);
            WebElement accessList = table.getSelect(i, 2);
            String result = "";
            waitForUpdate();
            if (scenario == 1) {
                if (accessList != null) {
                    selectComboBox(accessList, "ACS only");
                    result = "Access=AcsOnly";
                }
            } else if (scenario == 2) {
                if (notification != null) {
                    selectComboBox(notification, "Off");
                    result = "Notification=Off ";
                }
            } else if (scenario == 3) {
                if (notification != null) {
                    new Select(notification).selectByIndex(i);
                    result = "Notification=" + all[0][i - 1] + " ";
                    waitForUpdate();
                }
                if (accessList != null) {
                    new Select(accessList).selectByIndex(i - 1);
                    result += (i == 1 ? "" : "Access=" + all[1][i - 1]);
                }
            } else {
                if (notification != null) {
                    selectComboBox(notification, "Active");
                    result = "Notification=Active ";
                    waitForUpdate();
                }
                if (accessList != null) {
                    selectComboBox(accessList, "ACS only");
                    result += "Access=AcsOnly";
                }
            }
            if (result.isEmpty()) {
                String warn = "Cannot complete test on current tab for this device!";
                LOGGER.warn('(' + BaseTestCase.getTestName() + ')' + warn);
                throw new AssertionError(warn);
            }
            getParameterMap().put(table.getHint(i), result);//delete
            getTask().setParameterName(table.getHint(i));
            getTask().setValue(result);
        }
        return this;
    }

    protected void setSinglePage() {
        if (elementIsPresent("btnPager2")) {
            itemsOnPage("200");
        }
    }

    public BasePage setVisibleColumns(int startParam, int endParam) {
        Table table = getTable("tblFilter");
        int size = table.getTableSize()[0] - 1;
        endParam = Math.min(size, endParam);
        startParam = Math.min(startParam, endParam);
        parameterSet = new HashSet<>();
        for (int i = startParam; i <= endParam; i++) {
            table.clickOn(i, 1);
            parameterSet.add(table.getCellText(i, 0));
        }
        return this;
    }

    public BasePage setVisibleColumns(String paramName) {
        Table table = getTable("tblFilter");
        parameterSet = new HashSet<>();
        table.clickOn(paramName);
        parameterSet.add(paramName);
        return this;
    }

    public WebElement showPointer(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", element);
        return element;
    }

    public void switchToNewWindow() {
        Set<String> windows = driver.getWindowHandles();
        windows.forEach(window -> {
            if (!window.equalsIgnoreCase(mainWindow)) {
                driver.switchTo().window(window);
            }
        });
    }

    String[] toLowerCase(String[] array) {
        String[] output = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = array[i].toLowerCase();
        }
        return output;
    }

    public BasePage topButton() {
        return clickButton(topButton);
    }

    public BasePage topMenu(TopMenu value) {
        waitForUpdate();
        switchToFrame(ROOT);
        getTable("tblTopMenu").clickOn(value.getItem());
        waitForUpdate();
        switchToFrame(DESKTOP);
        return this;
    }

    public BasePage upButton() {
        return clickButton(upButton);
    }

    protected void validateAddedAction(Table table, String eventName, String parameter, String value) {
        int row = eventName == null ? 1 : table.getFirstRowWithText(eventName);
        table.clickOn(row, -1);
        switchToFrame(POPUP);
        validateAddedTask("tblTasks", parameter, value, 1);
    }

    protected void validateAddedTask(Table table, String name, String parameter, String value) {
        int row = name == null ? 1 : table.getFirstRowWithText(name);
        table.clickOn(row, -1);
        switchToFrame(POPUP);
        validateAddedTask(parameter, value);
        cancelButtonPopUp();
    }

    protected void validateAddedTask(Table table, String eventName, String taskName) {
        pause(1000);
        int row = eventName == null ? 1 : table.getFirstRowWithText(eventName);
        table.clickOn(row, -1);
        switchToFrame(POPUP);
        table = new Table("tblTasks");
        try {
            table.assertPresenceOfTask(taskName);
        } catch (AssertionError e) {
            pause(1000);
            table.assertPresenceOfParameter(taskName);
        }
    }

    public BasePage validateAddedTask(String parameter, String value) {
        return validateAddedTask("tblTasks", parameter, value, 0);
    }

    public BasePage validateAddedTask(String tableId, String parameter, String value, int shift) {
        waitForUpdate();
        Table table = getTable(tableId);
        Timer timer = new Timer();
        while ((table.getTableSize()[0] == 1 || table.getRowLength(1) < 2) && !timer.timeout()) {
            table = getTable(tableId);
        }
        boolean match = false;
        for (int i = 0; i < table.getTableSize()[0]; i++) {
            try {
                int length = table.getRowLength(i);
                if (table.getCellText(i, length - 2 - shift).equalsIgnoreCase(parameter)) {
                    if (table.getCellText(i, length - 1 - shift).equalsIgnoreCase(value)) {
                        match = true;
                        break;
                    }
                    if (value.equals("0") || value.equals("1")) {
                        String alternateValue = value.equals("0") ? "false" : "true";
                        if (table.getCellText(i, length - 1 - shift).equals(alternateValue)) {
                            match = true;
                            break;
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("ArrayIndexOutOfBoundsException:" + e.getMessage());
            }
        }
        if (!match) {
            String warning = "Pair '" + parameter + "' : '" + value + "' not found";
            table.print();
            scrollTo(table.getCellWebElement(table.getTableSize()[0] - 1, 0));
            throw new AssertionError(warning);
        }
        return this;
    }

    protected void validateAddedTasks(Table table, String eventName) {
        pause(1000);
        int row = eventName == null ? 1 : table.getFirstRowWithText(eventName);
        table.clickOn(row, -1);
        switchToFrame(POPUP);
        try {
            validateAddedTasks();
        } catch (AssertionError e) {
            pause(1000);
            System.out.println("Validation failed. Trying to validate again...");
            validateAddedTasks();
        }
        cancelButton.click();
    }

    public BasePage validateAddedTasks() {
        Set<Map.Entry<String, String>> entrySet = getParameterMap().entrySet();
        assertEquals(getTable("tblTasks").getColumn(0).length, entrySet.size(), "Expected number of parameters does not match the actual one!");
        for (Map.Entry<String, String> entry : entrySet) {
            validateAddedTask(entry.getKey(), entry.getValue());   // don't use trim()
        }
        return this;
    }

    public void validateDevicesAmount() {
        validateDevicesAmountIs(DataBaseConnector.getDeviceAmount(getSerial()));
    }

    public void validateDevicesAmountIs(int amount) {
        String tabId = BaseTestCase.getTestName().matches("^.{3,5}_ev_\\d{3}$") ? "tabsMain_tblTabs" : "tabsSettings_tblTabs";
        assertCellEndsWith(tabId, 1, -2, String.valueOf(amount));
    }

    public BasePage validateName() {
        assertEquals(nameField.getAttribute("value"), BaseTestCase.getTestName());
        return this;
    }

    public void validateObjectTree() {
        Table table = new Table("tblTree");
        List<Integer> plusList = new ArrayList<>();
        List<Integer> minusList = new ArrayList<>();
        setImplicitlyWait(0);
        for (int i = 0; i < table.getTableSize()[0]; i++) {
            List<WebElement> images = scrollToElement(table.getCellWebElement(i, 0)).findElements(By.tagName("img"));
            if (!images.isEmpty()) {
                if (images.get(0).getAttribute("src").endsWith("collapse.png")) {
                    minusList.add(i);
                } else {
                    plusList.add(i);
                }
            }
        }
        setDefaultImplicitlyWait();
        if (plusList.isEmpty() || minusList.size() != 1 || minusList.get(0) != 0) {
            throw new AssertionError("Object tree looks like broken...\n" +
                    "plusList.isEmpty:" + plusList.isEmpty() + "; minusList.size:" + minusList.size() +
                    "; minusList.get(0):" + minusList.get(0));
        }
    }

    public BasePage validateParametersMonitor() {
        String tabId = BaseTestCase.getTestName().contains("_ev_") ? "tblDataParams" : "tblParamsMonitoring";
        Table table = new Table(tabId);
        if (parametersMonitor.getName().equals(table.getHint(1)) && parametersMonitor.getValue().equals(table.getInputText(1, 2))
                && parametersMonitor.getCondition().toString().equals(getSelectedOption(table.getSelect(1, 1)))) {
            return this;
        }
        String warn = "Expected Parameters Monitoring not found on current page!\nExpected: " + parametersMonitor + "\n  Actual: "
                + "ParametersMonitor{" + table.getHint(1) + " | " + getSelectedOption(table.getSelect(1, 1))
                + " | " + table.getInputText(1, 2) + '}';
        throw new AssertionError(warn);
    }

    public void validateSavedExport(String... extensions) {
        if (!getProtocolPrefix().equals("reports")) {
            switchToFrame(POPUP);
        }
        Table table = getTable("tbl");
        for (String ext : extensions) {
            if (!ext.equalsIgnoreCase("csv") && !ext.equalsIgnoreCase("xml")) {
                throw new AssertionError("Unsupported file type!");
            }
            if (ext.equalsIgnoreCase("csv")) {
                table.assertPresenceOfValue(1, "Report(Inventory_Default_" + CalendarUtil.getCsvFileFormat(csvFileTime) + ").csv");
            }
            if (ext.equalsIgnoreCase("xml")) {
                table.assertPresenceOfValue(1, "Report(Inventory_Default_" + CalendarUtil.getCsvFileFormat(xmlFileTime) + ").xml");
            }
        }
    }

    public BasePage validateSelectedGroup() {
        return assertSelectedOptionIs(sendToComboBox, BaseTestCase.getTestName());
    }

    public BasePage validateSorting(String column) {
        Table table;
        int colNum = -1;
        for (int i = 0; i < 2; i++) {
            try {
                waitForUpdate();
                table = getMainTable();
                colNum = table.getColumnNumber(0, column);
                table.clickOn(0, colNum);
                break;
            } catch (StaleElementReferenceException e) {
                System.out.println("StaleElementReferenceException caught while sorting by '" + column + "' column");
            }
        }
        Timer timer = new Timer(30000);
        for (int i = 0; i < 2; i++) {
            try {
                waitForUpdate();
                table = getMainTable();
                scrollToElement(table.getCellWebElement(0, colNum));
                WebElement pointer;
                try {
                    pointer = table.getCellWebElement(0, colNum).findElement(By.tagName("img"));
                } catch (NoSuchElementException e) {
                    System.out.println("Sorting arrow not found!");
                    if (!timer.timeout()) {
                        i--;
                    }
                    table.clickOn(0, colNum);//!
                    continue;
                }
                boolean descending = pointer.getAttribute("src").endsWith("down.png");
                String[] arr = table.getColumn(colNum, true);
                arr = toLowerCase(arr);
                String[] arr2 = Arrays.copyOf(arr, arr.length);
                Arrays.sort(arr, descending ? Comparator.reverseOrder() : Comparator.naturalOrder());
                if (!Arrays.deepEquals(arr, arr2)) {
                    System.out.println("Expected:" + Arrays.toString(arr));
                    System.out.println("Found   :" + Arrays.toString(arr2));
                    String warn = "sorting by column '" + column + "' failed!";
                    warn = (descending ? "Descending " : "Ascending ") + warn;
                    throw new AssertionError(warn);
                }
                System.out.println((descending ? "Descending " : "Ascending ") + "sorting by column '" + column + "' is OK!");
                if (i < 1) {
                    table.clickOn(0, colNum);
                }
            } catch (StaleElementReferenceException e) {
                System.out.println("StaleElementReferenceException handled");
                if (!timer.timeout())
                    i--;
            }
        }
        return this;
    }

    public void validateViewColumns() {
        waitUntilElementIsDisplayed(editButton);
        List<String> columnList = new ArrayList<>(Arrays.asList(getMainTable().getRow(0)));
        columnList.removeIf(s -> s.equals(""));
        if (parameterSet.size() == columnList.size() && parameterSet.removeAll(columnList) && parameterSet.isEmpty()) {
            return;
        }
        if (!parameterSet.isEmpty()) {
            parameterSet.removeAll(columnList);
            StringBuilder sb = new StringBuilder("Below columns have not been applied to the view: ");
            parameterSet.forEach(param -> sb.append("'").append(param).append("'; "));
            LOGGER.warn('(' + BaseTestCase.getTestName() + ") " + sb.toString());
        }
        throw new AssertionError("Checking column headers failed!");
    }

    protected WebElement waitForClickableOf(WebElement element) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofMillis(100))
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    public BasePage waitForStatus(String status, String testName, int timeoutSec) {
        Timer timer = new Timer(timeoutSec * 1000L);
        while (!timer.timeout()) {
            Table table = getMainTable();
            if (table.contains(testName) && table.getCellText(testName, "State").equalsIgnoreCase(status)) {
                return this;
            }
            bottomMenu(REFRESH);
        }
        throw new AssertionError("Timed out while waiting for status " + status);
    }

    public BasePage waitForStatus(String status, int timeoutSec) {
        return waitForStatus(status, BaseTestCase.getTestName(), timeoutSec);
    }

    public BasePage waitForUpdate() {   //TODO rework with ExpectedCondition
        Timer timer = new Timer(30000);
        switchToFrame(ROOT);
        String style;
        do {
            pause(100);
            style = spinner.getAttribute("style");
            if (style.contains("display: none;")) {
                break;
            }
        } while (!timer.timeout());
        switchToPreviousFrame();
        previousFrame = frame;  //to avoid traces of switching to ROOT frame
        return this;
    }

    protected void waitUntilBottomMenuIsDownloaded() {
        waitForUpdate();
        switchToFrame(BOTTOM_MENU);
        Timer timer = new Timer();
        exit:
        while (!timer.timeout()) {
            List<WebElement> list = driver.findElements(By.tagName("input"));
            if (!list.isEmpty()) {
                for (WebElement el : list) {
                    try {
                        if (el.isDisplayed()) {
                            break exit;
                        }
                    } catch (StaleElementReferenceException e) {
                        System.out.print(".");
                    }
                }
            }
        }
        switchToPreviousFrame();
    }

    public void waitUntilButtonIsDisplayed(IBottomButtons button) {
        switchToFrame(BOTTOM_MENU);
        try {
            waitUntilElementIsDisplayed(findElement(button.getId()));
        } catch (StaleElementReferenceException e) {
            waitForUpdate();
            waitUntilElementIsDisplayed(findElement(button.getId()));
        }
        switchToFrame(DESKTOP);
    }

    public void waitUntilButtonIsEnabled(IBottomButtons button) {
        switchToFrame(BOTTOM_MENU);
        try {
            waitUntilElementIsEnabled(button.getId());
        } catch (AssertionError e) {
            String warn = "Button '" + button + "' not found/not active";
            throw new AssertionError(warn);
        }
        switchToPreviousFrame();
    }

    protected void waitUntilElementIsDisplayed(WebElement element) {
        new FluentWait<>(driver)
                .withMessage("Element '#" + element.getAttribute("id") + "' not found")
                .withTimeout(Duration.ofSeconds(IMPLICITLY_WAIT))
                .pollingEvery(Duration.ofMillis(100))
                .until(ExpectedConditions.visibilityOf(element));
    }

    public void waitUntilElementIsEnabled(String id) {
        Timer timer = new Timer();
        boolean success = false;
        while (!timer.timeout()) {
            List<WebElement> list = findElements(id);
            if (list.size() == 1 && list.get(0).isEnabled()) {
                success = true;
                break;
            }
        }
        if (!success) {
            throw new AssertionError("Element with id='" + id + "' not found/not active");
        }
    }

    public enum FrameSwitch {
        ROOT(null), DESKTOP("frmDesktop"), BOTTOM_MENU("frmButtons"),
        CONDITIONS("frmPopup2"), POPUP("frmPopup"), SUB_FRAME("frmSubFrame");

        FrameSwitch(String frameId) {
            this.frameId = frameId;
        }

        String frameId;
    }

    static class Task {
        String taskName;
        String parameterName = "";
        String value = "";

        Task(String taskName) {
            this.taskName = taskName;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private Task getTask() {
        return getTask(null);
    }

    private Task getTask(String taskName) {
        if (task == null) {
            task = new Task(taskName);
        }
        return task;
    }
}


