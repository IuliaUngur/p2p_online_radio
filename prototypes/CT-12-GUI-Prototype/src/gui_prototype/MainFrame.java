package gui_prototype;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import java.awt.Button;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

@SuppressWarnings("serial")
public class MainFrame extends JFrame{
	private JTextField searchText;
	private int w, h;
	public MainFrame() {
		setTitle("TomP2P Radio Prototype");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		w = d.width;
		h = d.height;
		setSize(w, h);
		setResizable(false);
		getContentPane().setLayout(null);
		
		final JPanel mainPanel = new JPanel();
		mainPanel.setBackground(new Color(153, 255, 255));
		mainPanel.setBounds(0, 0, w, h);
		getContentPane().add(mainPanel);
		mainPanel.setLayout(null);
		
		searchText = new JTextField();
		searchText.setText("   artist, song name, album or genre");
		searchText.setBounds(w-w/4, h/8, w/4-w/16, h/32);
		mainPanel.add(searchText);
		searchText.setColumns(10);
		
		JLabel searchLabel = new JLabel("enter here the \"search\" text :");
		searchLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		searchLabel.setBounds(w-w/4 - 229, h/8, 229, h/32);
		mainPanel.add(searchLabel);
		
		final int buttonW = w/8;
		final int buttonH = w/16;
		final int distanceB = w/32;
		
		JButton btnSearchArtist = new JButton("search Artist");
		btnSearchArtist.setFont(new Font("Arial", Font.BOLD, 16));
		btnSearchArtist.setBounds(w-w/4-2*distanceB, h/4, buttonW, buttonH);
		mainPanel.add(btnSearchArtist);
		
		JButton btnSearchSong = new JButton("search Song");
		btnSearchSong.setFont(new Font("Arial", Font.BOLD, 16));
		btnSearchSong.setBounds(w-w/4-2*distanceB + buttonW + distanceB, h/4, buttonW, buttonH);
		mainPanel.add(btnSearchSong);
		
		JButton btnSearchAlbum = new JButton("search Album");
		btnSearchAlbum.setFont(new Font("Arial", Font.BOLD, 16));
		btnSearchAlbum.setBounds(w-w/4-2*distanceB, h/4 + buttonH + distanceB, buttonW, buttonH);
		mainPanel.add(btnSearchAlbum);
		
		JButton btnSearchGenre = new JButton("search Genre");
		btnSearchGenre.setFont(new Font("Arial", Font.BOLD, 16));
		btnSearchGenre.setBounds(w-w/4-2*distanceB + buttonW + distanceB, h/4 + buttonH + distanceB, buttonW, buttonH);
		mainPanel.add(btnSearchGenre);
		
		JButton play = new JButton();
		play.setBounds(distanceB, h/2, buttonH + 15, buttonH + 15);
		ImageIcon playIcon = getScaledImage(new ImageIcon("images/playButton.png"), buttonH +15, buttonH+15);
		ImageIcon playIconPressed = getScaledImage(new ImageIcon("images/playButtonPressed.png"), buttonH+15, buttonH+15);
		play.setIcon(playIcon);
		play.setPressedIcon(playIconPressed);
		play.setBorder(null);
		play.setContentAreaFilled(false);
		mainPanel.add(play);
		
		JButton pause = new JButton();
		pause.setBounds(2 * distanceB + buttonH, h/2, buttonH, buttonH);
		ImageIcon pauseIcon = getScaledImage(new ImageIcon("images/pauseButton.png"), buttonH, buttonH);
		ImageIcon pauseIconPressed = getScaledImage(new ImageIcon("images/pauseButtonPressed.png"), buttonH, buttonH);
		pause.setIcon(pauseIcon);
		pause.setPressedIcon(pauseIconPressed);
		pause.setBorder(null);
		pause.setContentAreaFilled(false);
		mainPanel.add(pause);
		
		JButton stop = new JButton("");
		stop.setBounds(3 * distanceB + 2*buttonH, h/2, buttonH, buttonH);
		ImageIcon stopIcon = getScaledImage(new ImageIcon("images/stopButton.png"), buttonH, buttonH);
		ImageIcon stopIconPressed = getScaledImage(new ImageIcon("images/stopButtonPressed.png"), buttonH, buttonH);
		stop.setIcon(stopIconPressed);
		stop.setPressedIcon(stopIcon);
		stop.setBorder(null);
		stop.setContentAreaFilled(false);
		mainPanel.add(stop);
		
		JButton skipback = new JButton("");
		skipback.setBounds(2* distanceB, h/2 + distanceB + buttonH, buttonH, buttonH);
		ImageIcon skipbackIcon = getScaledImage(new ImageIcon("images/backSkipButton.png"), buttonH, buttonH);
		skipback.setIcon(skipbackIcon);
		skipback.setBorder(null);
		skipback.setContentAreaFilled(false);
		mainPanel.add(skipback);
		
		JButton skip = new JButton("");
		skip.setBounds(3* distanceB + buttonH, h/2 + distanceB + buttonH, buttonH, buttonH);
		ImageIcon skipIcon = getScaledImage(new ImageIcon("images/skipButton.png"), buttonH, buttonH);
		skip.setIcon(skipIcon);
		skip.setBorder(null);
		skip.setContentAreaFilled(false);
		mainPanel.add(skip);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, w, h/40);
		mainPanel.add(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem uploadFile = new JMenuItem("Upload file...");
		uploadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filename = File.separator+"tmp";
				JFileChooser fc = new JFileChooser(new File(filename));

				// Show open dialog; this method does not return until the dialog is closed
				fc.showOpenDialog(mainPanel);
				File selFile = fc.getSelectedFile();

				//upload sel file into TomP2P

			}
		});
		mnFile.add(uploadFile);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(distanceB, h/2 - buttonH - distanceB, 3 * buttonH + 2 * distanceB, 14);
		mainPanel.add(progressBar);
		
		JLabel playLabel = new JLabel("");
		playLabel.setBounds(distanceB - 30, h/2 - buttonH - distanceB - 3, 20, 20);
		ImageIcon playLabelIconPressed = getScaledImage(new ImageIcon("images/playButton.png"), 20, 20);
		playLabel.setIcon(playLabelIconPressed);
		mainPanel.add(playLabel);
		
		String[] listData = { "Song 1", "Song 2", "Song 3",
                "Song 4", "Song 5", "Song 6" };
		
		final JLabel songLabel = new JLabel("");
		songLabel.setFont(new Font("Arial", Font.BOLD, 16));
		songLabel.setBounds(distanceB, h/2 - buttonH - 2*distanceB, buttonW, 16);
		mainPanel.add(songLabel);
		
		final JList<String> list = new JList<String>();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					String selectedValue = list.getSelectedValue().toString();
					songLabel.setText(selectedValue);
				}
			}
		});
		list.setBounds(w/2 - w/8, h/2 - h/6, w/4, h/3);
		list.setListData(listData);
		mainPanel.add(list);
		
		
		
		setVisible(true);
	}
	
	private static ImageIcon getScaledImage(ImageIcon srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg.getImage(), 0, 0, w, h, null);
        g2.dispose();
        return new ImageIcon(resizedImg);
    }
}
