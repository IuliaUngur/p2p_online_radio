package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import javax.swing.JFileChooser;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

import P2P.P2PShare;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;


import utils.FileUtils;
import utils.log.LogHandler;


public class SharedFilesDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private String lastSelectedFolder; // represents starting folder for file chooser dialog.
	// download path
	private JTextField txtDownloadPath;
	private JButton btnSelectDownloadPath;
	// shared folders
	private JTree sharedFoldersTree;
	private JScrollPane sharedFoldersTreeView;
	private DefaultTreeModel sharedFoldersTreeModel;
	private JButton btnAddFolder;
	private JButton btnRemoveFolder;
	
	private boolean result;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SharedFilesDialog dialog = new SharedFilesDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public SharedFilesDialog(P2PShare share) {
		this();
		initContentFromShare(share);
	}


	/**
	 * Create the dialog.
	 * @param p2pShare 
	 */
	public SharedFilesDialog() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(SharedFilesDialog.class.getResource("/javax/swing/plaf/metal/icons/ocean/hardDrive.gif")));
		setTitle("Shared Folders and Files");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 800, 600);
		setMinimumSize(new Dimension(600, 400));
		getContentPane().setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		
		lastSelectedFolder = null;
		
		JLabel lblDownloadFolder = new JLabel("download folder");		
		initDownloadPath();
		
		JLabel lblSharedFolders = new JLabel("shared folders");
		initSharedFolders();
		
		// layout stuff
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addComponent(sharedFoldersTreeView, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
						.addComponent(lblDownloadFolder, Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(txtDownloadPath, GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSelectDownloadPath))
						.addComponent(lblSharedFolders, Alignment.LEADING)
						.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
							.addComponent(btnAddFolder)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnRemoveFolder)))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblDownloadFolder)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtDownloadPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnSelectDownloadPath))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblSharedFolders)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sharedFoldersTreeView, GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnAddFolder)
						.addComponent(btnRemoveFolder))
					.addContainerGap())
		);
		panel.setLayout(gl_panel);
		
		initButtonPane();
	}


	private void initButtonPane() {
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JLabel lblOkWillTrigger = new JLabel("OK will trigger application to publish all files listed above.");
		buttonPane.add(lblOkWillTrigger);
		{
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					hideAndClose(true);
				}
			});
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);
		}
		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					hideAndClose(false);
				}
			});
			buttonPane.add(cancelButton);
		}
		
	}

	protected void hideAndClose(boolean result) {
		this.result = result;
		setVisible(false);
		dispose();
	}


	private void initSharedFolders() {
		btnAddFolder = new JButton("+ Add Folder");
		btnAddFolder.setToolTipText("Add a new folder to the tree.");
		btnAddFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedFolder = getFolderFromFileChoserDialog();
				if(selectedFolder != null) {
					addFolderToTree(selectedFolder);
				}
			}
		});
		
		btnRemoveFolder = new JButton("- Remove");
		btnRemoveFolder.setToolTipText("Remove currently selected folder OR file");
		btnRemoveFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeCurrentSelectedItemFromTree();
			}
		});
		
		sharedFoldersTree = new JTree();
		sharedFoldersTreeView = new JScrollPane(sharedFoldersTree);
		sharedFoldersTreeModel = new DefaultTreeModel(
				new DefaultMutableTreeNode("Shared Folders and Files") {
					private static final long serialVersionUID = 1L;
				}
			);
		sharedFoldersTree.setModel(sharedFoldersTreeModel);
		
	}

	private void initDownloadPath() {
		txtDownloadPath = new JTextField();
		txtDownloadPath.setEditable(false);
		txtDownloadPath.setColumns(10);
		
		btnSelectDownloadPath = new JButton("...");
		btnSelectDownloadPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedFolder = getFolderFromFileChoserDialog();
				if(selectedFolder != null) {
					txtDownloadPath.setText(selectedFolder.getAbsolutePath());
					addFolderToTree(selectedFolder);
				}
			}

		});
		
	}
	
	
	private void initContentFromShare(P2PShare share) {
		if(share == null) return;
		this.txtDownloadPath.setText(share.getDownloadPath() != null &&  share.getDownloadPath().exists() ? share.getDownloadPath().getAbsolutePath() : "");
		this.addFolderToTree(share.getDownloadPath());
		for(File folder : share.getSharedFolders()) {
			if(share.getDownloadPath().equals(folder)) continue;
			//add folder as node
			DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(folder);
			((DefaultMutableTreeNode)sharedFoldersTreeModel.getRoot()).add(tmpNode);
			// scan for all subfolders and files in folders. check for file extension .mp3 and add music files in folder as subnodes.
			ArrayList<String> subFiles = FileUtils.listDir(folder);
			String root = folder.getAbsolutePath();
			int count = 0;
			for(String f : subFiles) {
				if(f.toLowerCase().endsWith(".mp3")) {
					tmpNode.add(new DefaultMutableTreeNode(f.replace(root, "."))); // we inherit prefix from parent, gives shorter names.
					count++;
				}
			}
			LogHandler.debug(this, "Total files added to tree: " + count + ". Folder: " + folder.getAbsolutePath());
			updateTreeLater();
		}
	}

	private void addFolderToTree(final File selectedFolder) {
		if(selectedFolder == null) return;
		// start new thread because scanning folders may take a while, but we want UI to be responsive -> async processing
		new Thread(new Runnable() {
		  public void run() {
				//add folder as node
				DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(selectedFolder);
				((DefaultMutableTreeNode)sharedFoldersTreeModel.getRoot()).add(tmpNode);
				// scan for all subfolders and files in folders. check for file extension .mp3 and add music files in folder as subnodes.
				ArrayList<String> subFiles = FileUtils.listDir(selectedFolder);
				String root = selectedFolder.getAbsolutePath();
				int count = 0;
				for(String f : subFiles) {
					if(f.toLowerCase().endsWith(".mp3")) {
						tmpNode.add(new DefaultMutableTreeNode(f.replace(root, "."))); // we inherit prefix from parent, gives shorter names.
						count++;
					}
				}
				LogHandler.debug(this, "Total files added to tree: " + count + ". Folder: " + selectedFolder.getAbsolutePath());
				updateTreeLater();
		  }
		}).start();
	}
	

	private void removeCurrentSelectedItemFromTree() {
		DefaultMutableTreeNode currentNode = null;
		currentNode = (DefaultMutableTreeNode)sharedFoldersTree.getLastSelectedPathComponent();
		if (currentNode == null) return; // Nothing is selected.
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)(currentNode.getParent());
		if(parent == null) return; // no parent -> root node		
		sharedFoldersTreeModel.removeNodeFromParent(currentNode);
		updateTreeLater();
	}
	
	private File getFolderFromFileChoserDialog() {
		String startDir;
		try {
			startDir = lastSelectedFolder != null && !lastSelectedFolder.isEmpty() ? lastSelectedFolder : new File(".").getCanonicalPath();
		} catch (IOException e1) {
			startDir = "";
		}
		JFileChooser fileChooser = new JFileChooser(startDir);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int status = fileChooser.showOpenDialog(getContentPane());
		if(status == JFileChooser.APPROVE_OPTION) {
			File selectedFolder = fileChooser.getSelectedFile();
			lastSelectedFolder = selectedFolder.getAbsolutePath();
			return selectedFolder;
		} else {
			return null;
		}
	}
	
	private void updateTreeLater() {
		EventQueue.invokeLater( new Runnable() {
			public void run() { 
				sharedFoldersTree.updateUI(); 
			}
		});
	}
	
	public String getDownloadPath() {
		return txtDownloadPath.getText();
	}
	
	public File[] getSharedFolders() {
		Set<File> sharedFolders = new HashSet<File>();
		DefaultMutableTreeNode root = ((DefaultMutableTreeNode)sharedFoldersTreeModel.getRoot());
		DefaultMutableTreeNode child;
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = root.children();
		while(children.hasMoreElements()) {
			child = children.nextElement();
			if(child.getUserObject() != null && child.getUserObject() instanceof File) {
				File f = (File)child.getUserObject();
				sharedFolders.add(f);
			}
		}
		return sharedFolders.toArray(new File[0]);
	}
	
	public File[] getSharedFiles() {
		Set<File> sharedFiles = new HashSet<File>();
		DefaultMutableTreeNode root = ((DefaultMutableTreeNode)sharedFoldersTreeModel.getRoot());
		DefaultMutableTreeNode folderNode;
		DefaultMutableTreeNode fileNode;
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> folders = root.children();
		while(folders.hasMoreElements()) {
			folderNode = folders.nextElement();
			if(folderNode.getUserObject() != null && folderNode.getUserObject() instanceof File) {
				File sharedFolder = (File)folderNode.getUserObject();
				@SuppressWarnings("unchecked")
				Enumeration<DefaultMutableTreeNode> files = folderNode.children();
				while(files.hasMoreElements()) {
					fileNode = files.nextElement();
					if(fileNode.getUserObject() != null && fileNode.getUserObject() instanceof String) {
						String relativePath = ((String)fileNode.getUserObject()).replaceFirst(".", "");
						sharedFiles.add(new File(sharedFolder, relativePath));
					}
				}
			}
		}
		return sharedFiles.toArray(new File[0]);
	}


	public boolean getResult() {
		return result;
	}
}
