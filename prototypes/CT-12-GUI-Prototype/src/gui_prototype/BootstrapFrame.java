package gui_prototype;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class BootstrapFrame extends JFrame{
	private JTextField ipAdress;
	public BootstrapFrame() {
		setResizable(false);
		setTitle("join the radio :D");
		getContentPane().setLayout(null);
		setSize(500, 500);
		
		JPanel bootstrapPanel = new JPanel();
		bootstrapPanel.setBackground(new Color(102, 255, 255));
		bootstrapPanel.setBounds(0, 0, 500, 500);
		getContentPane().add(bootstrapPanel);
		bootstrapPanel.setLayout(null);
		
		ipAdress = new JTextField();
		ipAdress.setToolTipText("");
		ipAdress.setBounds(152, 146, 179, 27);
		bootstrapPanel.add(ipAdress);
		ipAdress.setColumns(10);
		
		JLabel lblEnterAIp = new JLabel("enter a IP:");
		lblEnterAIp.setBounds(78, 146, 62, 27);
		bootstrapPanel.add(lblEnterAIp);
		
		JButton btnBootsrap = new JButton("bootsrap");
		btnBootsrap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String ip = ipAdress.getText();
				//here you have to call the bootstrapping call
				MainFrame mf = new MainFrame();
				dispose();
			}
		});
		btnBootsrap.setBounds(200, 233, 97, 25);
		bootstrapPanel.add(btnBootsrap);
		
		setVisible(true);
	}

}
