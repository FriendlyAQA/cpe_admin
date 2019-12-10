package com.friendly.aqa.gui;

import com.friendly.aqa.test.BaseTestCase;
import com.friendly.aqa.test.GroupUpdateTests;
import com.friendly.aqa.utils.XmlWriter;
import org.testng.TestNG;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

public class Controller implements WindowListener {

    View view;
    private JRadioButton[] runSpecifiedButtons, excludeSpecificButtons;
    private JTextField[] runSpecifiedFields, excludeSpecificFields;
    private JCheckBox[] enableTabCheckboxes;
    private Set<String>[] tabTestAmount;
    private JCheckBox reRunFailedCheckbox;
    private JButton runButton;
    private Set<String>[] writtenTestSet;
    private int[] lastTestNumber;
    private int testSum;
    private final List<Character> allowedChars = new ArrayList<>(Arrays.asList(new Character[]{44, 45, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57}));

    @SuppressWarnings("unchecked")
    public Controller() {
        tabTestAmount = new Set[8];
        writtenTestSet = new Set[8];
        for (int i = 0; i < 8; i++) {
            tabTestAmount[i] = new TreeSet<>();
            writtenTestSet[i] = new TreeSet<>();
        }
        handleWrittenTests();
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(() -> (view = new View(this)).setVisible(true));
    }

    private void handleWrittenTests() {
        lastTestNumber = new int[8];
        for (Method method : GroupUpdateTests.class.getDeclaredMethods()) {
            String name = method.getName();
            writtenTestSet[2].add(name);
        }
        tabTestAmount[2] = writtenTestSet[2];
        lastTestNumber[2] = Integer.parseInt(((TreeSet<String>) writtenTestSet[2]).last().split("\\D+")[1]);
        testSum = tabTestAmount[2].size();
    }

    private void calculateTestSum() {
        testSum = 0;
        for (int i = 0; i < tabTestAmount.length; i++) {
            if (enableTabCheckboxes[i].isSelected()) {
                testSum += tabTestAmount[i].size();
            }
        }
    }

    public void runPressed(boolean start) {
        if (start) {
//            XmlWriter.createXml();
            TestNG testng = new TestNG();
            List<String> suites = new ArrayList<>();
            suites.add("testng.xml");
            testng.setTestSuites(suites);
            testng.run();
        } else {
            BaseTestCase.interruptTestRunning(true);
        }
    }

    private int getTabNumber(JTextField field) {
        for (int i = 0; i < 8; i++) {
            if (field == runSpecifiedFields[i]) {
                return i;
            }
        }
        for (int i = 0; i < 8; i++) {
            if (field == excludeSpecificFields[i]) {
                return i;
            }
        }
        return -1;
    }

    private void createExecutableTestSet(JTextField field) {
        int tabNum = getTabNumber(field);
        Set<String> out = new TreeSet<>(writtenTestSet[tabNum]);
        Pattern p = Pattern.compile("-?\\d+\\S*");
        String input = field.getText();
        Matcher m = p.matcher(input);
        if (m.find()) {
            if (runSpecifiedButtons[tabNum].isSelected()) {
                out.retainAll(getTestSet(input, tabNum));
            } else {
                out.removeAll(getTestSet(input, tabNum));
            }
        }
        tabTestAmount[tabNum] = out;
    }

    private Set<String> getTestSet(String input, int tabNum) {
        Set<Integer> integerSet = new HashSet<>();
        List<String> rangeList = new ArrayList<>(Arrays.asList(input.split(",")));
        for (int i = 0; i < rangeList.size(); i++) {
            String range = rangeList.get(i);
            String[] limits = range.split("-");
            if (limits.length == 0) {
                continue;
            }
            if (limits.length == 1 && !limits[0].isEmpty()) {
                int a = Integer.parseInt(limits[0]);
                if (input.endsWith("-") && i == rangeList.size() - 1) {
                    for (; a <= lastTestNumber[tabNum]; a++) {
                        integerSet.add(a);
                    }
                } else {
                    integerSet.add(a);
                }
            } else if (limits.length == 2) {
                int a = limits[0].isEmpty() ? 1 : Integer.parseInt(limits[0]);
                int b = Math.min(Integer.parseInt(limits[1]), lastTestNumber[tabNum]);
                for (int j = Math.min(a, b); j <= Math.max(a, b); j++) {
                    integerSet.add(j);
                }
            }
        }
        Set<String> stringSet = new TreeSet<>();
        for (int i : integerSet) {
            stringSet.add(String.format("%s%03d", "test_", i));
        }
        return stringSet;
    }

    public void textChanged(JTextField field) {
        char[] chars = field.getText().toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (allowedChars.contains(c)) {
                sb.append(c);
            }
        }
        int position = field.getCaretPosition();
        String out = sb.toString()
                .replaceAll("-+,?", "-")
                .replaceAll("^-,", "-")
                .replaceAll(",-", ",")
                .replaceAll(",+", ",");
        out = out.replaceAll("(.+)?-\\d*-", out.length() > 0 ? out.substring(0, out.length() - 1) : "");
        field.setText(out);
        if (position < sb.toString().length()) {
            field.setCaretPosition(position);
        }
        createExecutableTestSet(field);
        calculateTestSum();
        view.setToExecValue(testSum);
    }

    public void tabStateChanged(JCheckBox checkBox) {
        for (int i = 0; i < enableTabCheckboxes.length; i++) {
            if (enableTabCheckboxes[i] == checkBox) {
                view.setEnabled(checkBox.isSelected(), runSpecifiedButtons[i], excludeSpecificButtons[i]);
                testSelected(i);
            }
        }
        checkRunButton();
    }

    public void testSelected(int tabNum) {
        runSpecifiedFields[tabNum].setEnabled(runSpecifiedButtons[tabNum].isSelected() && runSpecifiedButtons[tabNum].isEnabled());
        excludeSpecificFields[tabNum].setEnabled(excludeSpecificButtons[tabNum].isSelected() && excludeSpecificButtons[tabNum].isEnabled());
        tabTestAmount[tabNum] = writtenTestSet[tabNum];//writtenTestSet[tabNum]
        textChanged(runSpecifiedButtons[tabNum].isSelected() ? runSpecifiedFields[tabNum] : excludeSpecificFields[tabNum]);
    }

    public void enableAllTabs(boolean enable) {
        for (JCheckBox enableTabCheckbox : enableTabCheckboxes) {
            enableTabCheckbox.setSelected(enable);
            tabStateChanged(enableTabCheckbox);
        }
        runButton.setEnabled(enable);
    }

    public void testsuiteStarted() {
        runButton.setText("STOP");
        checkRunButton();
    }

    public void testsuiteStopped() {
        runButton.setText("RUN");
        checkRunButton();
    }

    private void checkRunButton() {
        boolean enable = false;
        for (JCheckBox box : enableTabCheckboxes) {
            if (box.isSelected()) {
                enable = true;
                break;
            }
        }
        runButton.setEnabled(enable);
        reRunFailedCheckbox.setEnabled(enable && !view.getFailedFieldText().isEmpty());
    }

    @Override
    public void windowOpened(WindowEvent e) {
        view.setToExecValue(testSum);
        runSpecifiedButtons = view.getRunSpecifiedRadioButtonArray();
        excludeSpecificButtons = view.getExcludeSpecificRadioButtonArray();
        runSpecifiedFields = view.getRunSpecifiedFieldArray();
        excludeSpecificFields = view.getExcludeSpecificFieldArray();
        enableTabCheckboxes = view.getEnableTabCheckBoxArray();
        runButton = view.getRunButton();
        reRunFailedCheckbox = view.getReRunCheckBox();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println("closing");
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    public static void main(String[] args) {
        new Controller();
    }
}
