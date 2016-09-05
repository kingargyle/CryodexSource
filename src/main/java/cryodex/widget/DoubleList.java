package cryodex.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DoubleList<T extends Comparable<T>> extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String filterHint = "Filter Player List";

    private JList<T> list1;
    private JList<T> list2;
    private DefaultListModel<T> listModel1;
    private DefaultListModel<T> listModel2;

    private JButton to2;
    private JButton to1;
    private JButton allTo2;
    private JButton allTo1;

    private JLabel list1Label;
    private JLabel list2Label;

    private JPanel buttonPanel;

    private String list1LabelText = "";
    private String list2LabelText = "";

    private JTextField filterList1;
    private List<T> list1Prefiltered = new ArrayList<>();
    private List<T> list1Filtered = new ArrayList<>();
    private boolean filtering = false;

    public DoubleList() {
        this(null, null, "", "");
    }

    public DoubleList(List<T> values1, List<T> values2, String label1, String label2) {

        super(new BorderLayout());

        list1LabelText = label1;
        list2LabelText = label2;

        if (values1 != null) {
            Collections.sort(values1);
            for (T element : values1) {
                getModel1().addElement(element);
            }
        }
        if (values2 != null) {
            Collections.sort(values2);
            for (T element : values2) {
                getModel2().addElement(element);
            }
        }

        updateLabels();

        buildWidget();
    }

    public void setValues(List<T> values1, List<T> values2) {
        getModel1().removeAllElements();
        getModel2().removeAllElements();
        if (values1 != null) {
            for (T element : values1) {
                getModel1().addElement(element);
            }
        }
        if (values2 != null) {
            for (T element : values2) {
                getModel2().addElement(element);
            }
        }

        updateLabels();
    }

    private void buildWidget() {

        JScrollPane listScroller1 = new JScrollPane(getList1());
        listScroller1.setPreferredSize(new Dimension(150, 250));

        JScrollPane listScroller2 = new JScrollPane(getList2());
        listScroller2.setPreferredSize(new Dimension(150, 250));

        this.add(ComponentUtils.addToVerticalBorderLayout(getList1Label(), listScroller1, null), BorderLayout.WEST);
        this.add(ComponentUtils.addToVerticalBorderLayout(null, new JLabel(), getButtonPanel()), BorderLayout.CENTER);
        this.add(ComponentUtils.addToVerticalBorderLayout(getList2Label(), listScroller2, null), BorderLayout.EAST);
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new SpringLayout());

            buttonPanel.add(ComponentUtils.addToFlowLayout(getList1FilterInput(), FlowLayout.CENTER));
            buttonPanel.add(ComponentUtils.addToFlowLayout(getTo2(), FlowLayout.CENTER));
            buttonPanel.add(ComponentUtils.addToFlowLayout(getAllTo2(), FlowLayout.CENTER));
            buttonPanel.add(ComponentUtils.addToFlowLayout(getAllTo1(), FlowLayout.CENTER));
            buttonPanel.add(ComponentUtils.addToFlowLayout(getTo1(), FlowLayout.CENTER));

            SpringUtilities.makeCompactGrid(buttonPanel, buttonPanel.getComponentCount(), 1, 1, 1, 1, 20);
        }
        return buttonPanel;
    }

    public JLabel getList1Label() {
        if (list1Label == null) {
            list1Label = new JLabel(list1LabelText);
        }
        return list1Label;
    }

    public JLabel getList2Label() {
        if (list2Label == null) {
            list2Label = new JLabel(list2LabelText);
        }
        return list2Label;
    }

    public void setList1LabelText(String labelText) {
        list1LabelText = labelText;
    }

    public void setList2LabelText(String labelText) {
        list2LabelText = labelText;
    }

    public void updateLabels() {
        getList1Label().setText(list1LabelText + "(" + getModel1().getSize() + ")");
        getList2Label().setText(list2LabelText + "(" + getModel2().getSize() + ")");
    }

    private JButton getTo2() {
        if (to2 == null) {
            to2 = new JButton("Selected >");
            to2.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    List<T> list1 = getList1Values();
                    List<T> list2 = getList2Values();

                    List<T> selected = getList1().getSelectedValuesList();
                    getList1().clearSelection();

                    list1.removeAll(selected);
                    list2.addAll(selected);

                    getModel1().removeAllElements();
                    getModel2().removeAllElements();

                    Collections.sort(list1);
                    Collections.sort(list2);

                    for (T element : list1) {
                        getModel1().addElement(element);
                    }
                    for (T element : list2) {
                        getModel2().addElement(element);
                    }

                    updateLabels();
                    list1Prefiltered = getList1Values();
                }
            });
        }
        return to2;
    }

    private JButton getTo1() {
        if (to1 == null) {
            to1 = new JButton("< Selected");
            to1.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    List<T> list1 = getList1Values();
                    List<T> list2 = getList2Values();

                    List<T> selected = getList2().getSelectedValuesList();
                    getList2().clearSelection();

                    list2.removeAll(selected);
                    list1.addAll(selected);

                    getModel1().removeAllElements();
                    getModel2().removeAllElements();

                    Collections.sort(list1);
                    Collections.sort(list2);

                    for (T element : list1) {
                        getModel1().addElement(element);
                    }
                    for (T element : list2) {
                        getModel2().addElement(element);
                    }

                    updateLabels();
                    list1Prefiltered = getList1Values();
                }
            });
        }
        return to1;
    }

    private JButton getAllTo2() {
        if (allTo2 == null) {
            allTo2 = new JButton("All >>");
            allTo2.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    List<T> list1 = getList1Values();
                    List<T> list2 = getList2Values();

                    list2.addAll(list1);

                    getModel1().removeAllElements();
                    getModel2().removeAllElements();

                    Collections.sort(list2);

                    for (T element : list2) {
                        getModel2().addElement(element);
                    }

                    updateLabels();
                    list1Prefiltered = getList1Values();
                }
            });
        }
        return allTo2;
    }

    private JButton getAllTo1() {
        if (allTo1 == null) {
            allTo1 = new JButton("<< All");
            allTo1.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    List<T> list1 = getList1Values();
                    List<T> list2 = getList2Values();

                    list1.addAll(list2);

                    getModel1().removeAllElements();
                    getModel2().removeAllElements();

                    Collections.sort(list1);

                    for (T element : list1) {
                        getModel1().addElement(element);
                    }

                    updateLabels();
                    list1Prefiltered = getList1Values();
                }
            });
        }
        return allTo1;
    }

    private DefaultListModel<T> getModel1() {
        if (listModel1 == null) {
            listModel1 = new DefaultListModel<T>();
        }

        return listModel1;
    }

    private DefaultListModel<T> getModel2() {
        if (listModel2 == null) {
            listModel2 = new DefaultListModel<T>();
        }

        return listModel2;
    }

    private JList<T> getList1() {
        if (list1 == null) {
            list1 = new JList<T>(getModel1());
            list1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list1.setLayoutOrientation(JList.VERTICAL);
            list1.setVisibleRowCount(-1);
        }

        return list1;
    }

    private JList<T> getList2() {
        if (list2 == null) {
            list2 = new JList<T>(getModel2());
            list2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list2.setLayoutOrientation(JList.VERTICAL);
            list2.setVisibleRowCount(-1);
        }

        return list2;
    }

    public List<T> getList1Values() {
        return Collections.list(getModel1().elements());
    }

    public List<T> getList2Values() {
        return Collections.list(getModel2().elements());
    }

    public JTextField getList1FilterInput() {
        if (filterList1 == null) {
            list1Prefiltered = getList1Values();
            filterList1 = new JTextField(filterHint, 10);
            filterList1.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (filterList1.getText().equals(filterHint)) {
                        filterList1.setText("");
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (filterList1.getText().trim().equals("")) {
                        filterList1.setText(filterHint);
                    }
                }
            });

            filterList1.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterList1(filterList1.getText().toLowerCase().trim());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterList1(filterList1.getText().toLowerCase().trim());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterList1(filterList1.getText().toLowerCase().trim());
                }
            });
        }
        return filterList1;
    }

    /**
     * Filter list to only include elements whose toString function contains the filterText. Elements that have a word
     * that starts with the filter text are given priority.
     */
    private void filterList1(String filterText) {

        // Prevent the function from multiple simultaneous executions
        if (!filtering) {
            filtering = true;

            getModel1().removeAllElements();

            // Reset list for blank filter
            if (filterText.equals("") || filterText.equalsIgnoreCase(filterHint)) {
                for (T element : list1Prefiltered) {
                    getModel1().addElement(element);
                }
                updateLabels();
            } else {

                list1Filtered.clear();

                // Check the beginning of each word
                for (T element : list1Prefiltered) {
                    String[] name = element.toString().toLowerCase().split(" ");
                    for (String s : name) {
                        if (s.startsWith(filterText)) {
                            list1Filtered.add(element);
                            break;
                        }
                    }
                }

                // Check if the element contains the text
                for (T element : list1Prefiltered) {
                    if (element.toString().contains(filterText) && !list1Filtered.contains(element)) {
                        list1Filtered.add(element);
                    }
                }

                // Add all matching elements to the list
                for (T element : list1Filtered) {
                    getModel1().addElement(element);
                }
                updateLabels();
            }
            filtering = false;
        }
    }
}
