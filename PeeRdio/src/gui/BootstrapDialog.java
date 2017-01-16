package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.google.common.net.InetAddresses;

public class BootstrapDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField txtNodeIpAddress;
	private JTextField txtMyNodeId;

	private Component lblMyNodeId;
	private Component lblNodeIpAddress;

	private JLabel lblTitle;

	private boolean result;
	private JComboBox<String> cboMyIpAddresses;

	private JLabel lblMyIp;

	private JCheckBox ckbxIsFirstNode;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			BootstrapDialog dialog = new BootstrapDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public BootstrapDialog() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(BootstrapDialog.class.getResource("/javax/swing/plaf/metal/icons/ocean/computer.gif")));
		setBounds(100, 100, 450, 223);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		result = false;
		lblTitle = new JLabel("connect to node");
		lblTitle.setFont(lblTitle.getFont().deriveFont(lblTitle.getFont().getStyle() | Font.BOLD, lblTitle.getFont().getSize() + 3f));
		
		initInputForm();
		initLayout();
		initButtons();
		Random generator = new Random();
		txtMyNodeId.setText(String.valueOf(generator.nextInt(Integer.MAX_VALUE)));
		InetAddress[] hosts;
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			// Just in case this host has multiple IP addresses....
			hosts = InetAddress.getAllByName(localhost.getCanonicalHostName());
			Vector<String> hostsModel = new Vector<String>(hosts.length);
			for(InetAddress h : hosts) {
				hostsModel.add(h.getHostAddress());
			}
			cboMyIpAddresses.setModel(new DefaultComboBoxModel<String>(hostsModel));
		} catch (UnknownHostException e) {
			// nothing to do if it does not work properly
		}
		
	}

	private void initButtons() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		{
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(checkFormConstraints())
						clearAndHide(true);
					else 
						showConstraintsErrorBox();
				}
			});
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);
		}
		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearAndHide(false);
				}
			});
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
		}
	}

	private void initInputForm() {
		lblNodeIpAddress = new JLabel("IP of trusted node:");
		lblMyNodeId = new JLabel("My node ID:");
		txtNodeIpAddress = new JTextField();
		txtNodeIpAddress.setColumns(10);
		txtMyNodeId = new JTextField();
		txtMyNodeId.setColumns(10);
		lblMyIp = new JLabel("My IP:");
		cboMyIpAddresses = new JComboBox<String>();
		cboMyIpAddresses.setEditable(true);
		
		ckbxIsFirstNode = new JCheckBox("this node is the first node");
		ckbxIsFirstNode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					txtNodeIpAddress.setEnabled(false);
				} else {
					txtNodeIpAddress.setEnabled(true);
				}
			}
		});
	}

	private void initLayout() {
		JSeparator separator = new JSeparator();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(separator, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
						.addComponent(lblTitle)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblMyNodeId)
								.addComponent(lblNodeIpAddress)
								.addComponent(lblMyIp))
							.addGap(11)
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(txtNodeIpAddress, GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
								.addComponent(cboMyIpAddresses, GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
								.addComponent(txtMyNodeId, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
								.addComponent(ckbxIsFirstNode))))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addComponent(lblTitle)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtMyNodeId, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblMyNodeId))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(cboMyIpAddresses, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblMyIp))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtNodeIpAddress, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNodeIpAddress))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(ckbxIsFirstNode)
					.addContainerGap(19, Short.MAX_VALUE))
		);
		contentPanel.setLayout(gl_contentPanel);
	}

	private void clearAndHide(boolean result) {
		this.result = result;
		setVisible(false);
	    dispose();
	}
	
	public boolean result() {
		return result;
	}
	
	private boolean checkFormConstraints() {
		return ckbxIsFirstNode.isSelected() || InetAddresses.isInetAddress(txtNodeIpAddress.getText().trim());
	}
	
	private void showConstraintsErrorBox() {
		JOptionPane.showMessageDialog(this,
			    "The IP address you entered is not a correct address.",
			    "Input Error",
			    JOptionPane.ERROR_MESSAGE);
	}
	
	public String getNodeId() {
		return this.txtMyNodeId.getText();
	}
	
	public String getTrustedNodeIp() {
		return this.txtNodeIpAddress.getText();
	}
	
	public boolean getIsFirstNode() {
		return !(ckbxIsFirstNode != null && !ckbxIsFirstNode.isSelected());
	}

	public String getMyIp() {
		return cboMyIpAddresses.getSelectedItem() != null ? cboMyIpAddresses.getSelectedItem().toString() : "";
	}
}
