package cryodex.modules.xwing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import cryodex.widget.FilteredTree;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class XWingRules extends JPanel implements TreeSelectionListener,
		HyperlinkListener {

	private static final long serialVersionUID = 1L;

	private JEditorPane htmlPane;
	private JEditorPane linkPane;
	private FilteredTree tree;

	private DefaultMutableTreeNode root;

	private HashMap<Integer, DefaultMutableTreeNode> idToArticle = new HashMap<Integer, DefaultMutableTreeNode>();

	// Optionally play with line styles. Possible values are
	// "Angled" (the default), "Horizontal", and "None".
	private static boolean playWithLineStyle = true;
	private static String lineStyle = "None";

	// Optionally set the look and feel.
	private static boolean useSystemLookAndFeel = false;

	public XWingRules() {
		super(new GridLayout(1, 0));

		loadRules();

		// Create a tree that allows one selection at a time.
		tree = new FilteredTree(root) {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean matches(DefaultMutableTreeNode node,
					String textToMatch) {
				Object nodeInfo = node.getUserObject();

				textToMatch = textToMatch.toUpperCase();

				if (nodeInfo instanceof ArticleNode) {
					ArticleNode article = (ArticleNode) nodeInfo;
					if (article.getName().toUpperCase().contains(textToMatch)) {
						return true;
					}
					if (article.getData().toUpperCase().contains(textToMatch)) {
						return true;
					}
				}

				return false;
			}
		};

		// Listen for when the selection changes.
		tree.getTree().addTreeSelectionListener(this);

		if (playWithLineStyle) {
			tree.putClientProperty("JTree.lineStyle", lineStyle);
		}

		// Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);

		// Create the HTML viewing pane.
		htmlPane = new JEditorPane("text/html", "");
		htmlPane.setEditable(false);
		htmlPane.addHyperlinkListener(this);

		linkPane = new JEditorPane("text/html", "");
		linkPane.setEditable(false);
		linkPane.addHyperlinkListener(this);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(htmlPane, BorderLayout.CENTER);
		contentPane.add(linkPane, BorderLayout.SOUTH);

		JScrollPane htmlView = new JScrollPane(contentPane);

		// Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setTopComponent(treeView);
		splitPane.setBottomComponent(htmlView);

		Dimension minimumSize = new Dimension(200, 50);
		htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(200);
		splitPane.setPreferredSize(new Dimension(700, 400));

		// Add the split pane to this panel.
		add(splitPane);
	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e) {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath()
				.getLastPathComponent();

		if (node == null) {
			return;
		}

		Object nodeInfo = node.getUserObject();

		if (nodeInfo instanceof ArticleNode) {
			ArticleNode article = (ArticleNode) nodeInfo;
			displayData(article);
		} else {
			displayData(null);
		}

	}

	

	private void displayData(ArticleNode node) {
		if (node == null) {
			htmlPane.setText("");
			linkPane.setText("");
		} else {
			htmlPane.setText(node.getData());
			linkPane.setText(node.getLink());
		}
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
			return;
		}

		DefaultMutableTreeNode node = idToArticle.get(Integer.valueOf(e
				.getDescription()));
		if (node != null) {
			ArticleNode articleNode = (ArticleNode) node.getUserObject();
			displayData(articleNode);
		}

		TreePath path = new TreePath(node.getPath());

		tree.getTree().setSelectionPath(path);
		tree.getTree().scrollPathToVisible(path);
	}


	private void loadRules() {
		try {

			InputStream stream = XWingRules.class
					.getResourceAsStream("XWING.rules");
			if (stream == null) {
				return;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream, "UTF-8"));

			Element mainElement = XMLUtils.getItem(reader);

			if (mainElement == null) {
				return;
			}

			List<Element> articles = mainElement.getChildren();

			List<ArticleNode> articleNodes = new ArrayList<ArticleNode>();

			for (Element e : articles) {
				ArticleNode node = new ArticleNode(e);
				articleNodes.add(node);
				idToArticle.put(node.getId(), new DefaultMutableTreeNode(node));
			}

			// ///////////
			// Build Tree
			// ///////////
			root = new DefaultMutableTreeNode("X-Wing");

			for (DefaultMutableTreeNode node : idToArticle.values()) {
				ArticleNode an = (ArticleNode) node.getUserObject();
				if (an.getParent() == 0) {
					root.add(node);
				} else {
					idToArticle.get(an.getParent()).add(node);
				}
			}

			// //////////
			// Fix links
			// //////////

			for (DefaultMutableTreeNode node : idToArticle.values()) {
				ArticleNode an = (ArticleNode) node.getUserObject();

				String data = an.getData();

				while (data.contains("#")) {

					int index1 = data.indexOf('#');
					int index2 = index1;

					while (index2 < data.length()) {
						index2++;

						if (data.length() == index2
								|| data.charAt(index2) < '0'
								|| data.charAt(index2) > '9') {
							break;
						}
					}

					String linkValue = data.substring(index1 + 1, index2);

					int linkInt = Integer.valueOf(linkValue);

					ArticleNode toNode = (ArticleNode) idToArticle.get(linkInt)
							.getUserObject();

					String linkString = "<a href=\"" + toNode.getId() + "\">"
							+ toNode.getName() + "</a>";

					data = data.substring(0, index1) + linkString
							+ data.substring(index2, data.length());
				}

				an.setData(data);

				String link = an.getLink();

				while (link.contains("#")) {

					int index1 = link.indexOf('#');
					int index2 = index1;

					while (index2 < link.length()) {
						index2++;

						if (link.length() == index2
								|| link.charAt(index2) < '0'
								|| link.charAt(index2) > '9') {
							break;
						}
					}

					String linkValue = link.substring(index1 + 1, index2);

					int linkInt = Integer.valueOf(linkValue);

					ArticleNode toNode = (ArticleNode) idToArticle.get(linkInt)
							.getUserObject();

					String linkString = "<a href=\"" + toNode.getId() + "\">"
							+ toNode.getName() + "</a>";

					link = link.substring(0, index1) + linkString
							+ link.substring(index2, link.length());
				}

				an.setLink(link);
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class ArticleNode {
		public int id;
		public String name;
		public String data;
		public int parent;
		public String link;

		public ArticleNode(Element e) {
			name = e.getChild("name").getData();
			id = Integer.valueOf(e.getChild("id").getData());
			data = "<html>" + e.getChild("data").getData() + "</html>";
			parent = Integer.valueOf(e.getChild("parent").getData());
			link = "<html>" + e.getChild("related").getData() + "</html>";
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		@Override
		public String toString() {
			return getName();
		}

		public int getParent() {
			return parent;
		}

		public String getLink() {
			return link;
		}

		public void setLink(String link) {
			this.link = link;
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		if (useSystemLookAndFeel) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				System.err.println("Couldn't use system look and feel.");
			}
		}

		// Create and set up the window.
		JFrame frame = new JFrame("X-Wing Rules");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add content to the window.
		frame.add(new XWingRules());

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}