package gui;


import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import utils.Constants;
import utils.log.LogHandler;
import P2P.P2PController;
import P2P.PlaylistBuilder;
import P2P.SearchDomain;
import P2P.interfaces.IP2PKeywordElement;
import core.MusicFile;
import core.P2PRadio;
import core.PlayList;
import core.exceptions.BootstrappingFailedException;
import core.exceptions.NotYetConnectedException;
import core.exceptions.SearchFailedException;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JSeparator;

import utils.Constants;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;


public class MainFrame extends JFrame implements BasicPlayerListener{
	
	private static final long serialVersionUID = 1L;

	private final static int PROGRESS_MAX = 10000;
	private P2PController controller;

	// panels
	private JPanel mainPanel;
	private JPanel contentPane;
	
	private JLabel lblLogConsole;
	private LogPane logPane;
	
	private JLabel lblPlaylist;
	private PlaylistUI playlistUI;
	
	// player
	private JMenuBar menuBar;
	private JButton btnPlay;
	private JButton btnSkipback;
	private JButton btnPause;
	private JButton btnStop;
	private JButton btnSkip;
	private JProgressBar progressBar;
	
	// search
	private JLabel lblSearch;
	private JTextField txtSearch;
	private JButton btnSearchArtist;
	private JButton btnSearchSong;
	private JButton btnSearchAlbum;
	private JButton btnSearchGenre;

	// rating
	private JLabel lblRating;
	private JButton btnRatingOne;
	private JButton btnRatingTwo;
	private JButton btnRatingThree;
	private JButton btnRatingFour;
	private JButton btnRatingFive;

	

	


	
	
	/**
	 * Launch main frame (only)
	 */
	public static void main(String[] args) {
		try {
			MainFrame frame = new MainFrame();
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public MainFrame() {
		
		initFrame();		
		initMenu();
		initMainPanel();
		initPlaylist();
		initLogView();
		initLayout();
		
		this.addWindowListener(new WindowAdapter()
	      {
	         public void windowClosing(WindowEvent e)
	         {
	           quit();
	         }
	      });
		
		setVisible(true);
		
		controller = P2PController.getInstance();
	}
	
	private void initFrame() {
		setBounds(new Rectangle(0, 0, 900, 600));
		setTitle("PEERdio");
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);		
	}

	private void initMenu() {
		// menu
		menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmSharedFiles = new JMenuItem("Shared Files");
		mntmSharedFiles.setIcon(new ImageIcon(MainFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif")));
		mntmSharedFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SharedFilesDialog dialog = new SharedFilesDialog(P2PController.getInstance().getShare());
				dialog.setVisible(true);
				if(dialog.getResult()) {
					P2PController.getInstance().getShare().setDownloadPath(new File(dialog.getDownloadPath() != null ? dialog.getDownloadPath() : Constants.DOWNLOADED_FILES_FOLDER));
					try {
						P2PController.getInstance().publishSharedFiles(dialog.getSharedFolders(), dialog.getSharedFiles());
					} catch (NotYetConnectedException e1) {
						showErrorMessage("Operation Failed", e1.getMessage());
					}
				}
	
			}
		});
		
		JMenuItem mntmConnect = new JMenuItem("Connect");
		mntmConnect.setIcon(new ImageIcon(MainFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/computer.gif")));
		mnFile.add(mntmConnect);
		mnFile.add(mntmSharedFiles);
		mntmConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BootstrapDialog dialog = new BootstrapDialog();
				LogHandler.debug(this, "About to open Connect Dialog.");
				dialog.setVisible(true);
				if(dialog.result() == true) {
					try {
						P2PController.getInstance().bootstrap(dialog.getTrustedNodeIp(), dialog.getNodeId(), dialog.getIsFirstNode(), dialog.getMyIp());
					} catch (BootstrappingFailedException ex) {
						final BootstrappingFailedException exception = ex;
						showErrorMessage("Bootstrapping Failed", exception.getMessage());						
						System.out.println(ex.getMessage());
					
					}
				} else {
					// nothing to do since user canceled action
				}
				LogHandler.debug(this, String.format("Connect Dialog closed (%1$2s).", dialog.result()));
			}
		});
		
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.setIcon(new ImageIcon(MainFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/close.gif")));
		mnFile.add(mntmQuit);
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
	}

	protected void quit() {
		LogHandler.info(this, "Quit P2P Application due to user action.");
		dispose();
		P2PController.getInstance().shutdown();
		System.exit(0);
	}

	private void initMainPanel() {
		mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		initPlayer();
		initSearch();
		initRating();
	}

	private void initPlayer() {
			btnPlay = new JButton("play");
			btnPause = new JButton("pause");
			btnStop = new JButton("stop");
			btnSkipback = new JButton("<<<");
			btnSkip = new JButton(">>>");
			progressBar = new JProgressBar(0, PROGRESS_MAX);
			
			btnStop.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						P2PRadio.getInstance().getMp3Player().stop();
						progressBar.setValue(0);
					} catch (BasicPlayerException e1) {
						LogHandler.warning(this, "Could not play song. " + e1.getMessage());
					}
				}
			});
			
		
			
			
			//Action Events
			btnPlay.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (P2PRadio.getInstance().getMp3Player().isPaused()){
							P2PRadio.getInstance().getMp3Player().play();
						}
						else{
							P2PRadio.getInstance().getMp3Player().playSong(playlistUI.getCurrentIndex());
						}
					} catch (BasicPlayerException e1) {
						LogHandler.warning(this, "Could not play song. " + e1.getMessage());
					}
				}
			});
			
			btnPause.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						P2PRadio.getInstance().getMp3Player().pause();
					} catch (BasicPlayerException e1) {
						LogHandler.warning(this, "Could not play song. " + e1.getMessage());
					}
				}
			});
			
			btnSkip.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						P2PRadio.getInstance().getMp3Player().playNextSong();
					} catch (BasicPlayerException e1) {
						LogHandler.warning(this, "Could not play song. " + e1.getMessage());
					}
				}
			});
			
			btnSkipback.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						P2PRadio.getInstance().getMp3Player().playPreviousSong();
					} catch (BasicPlayerException e1) {
						LogHandler.warning(this, "Could not play song. " + e1.getMessage());
					}
				}
			});
			
		}

	private void initSearch() {
		txtSearch = new JTextField();
		txtSearch.setText("artist, song name, album or genre");
		txtSearch.setColumns(10);
		
		txtSearch.addFocusListener(new FocusListener() {
			
			@Override
			public void focusGained(FocusEvent e) {
				try {
					txtSearch.setText("");
				} catch (Exception ex) {
					showErrorMessage("Operation Failed", ex.getMessage());
				}
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		
		lblSearch = new JLabel("search");
		lblSearch.setFont(lblSearch.getFont().deriveFont(lblSearch.getFont().getStyle() | Font.BOLD, lblSearch.getFont().getSize() + 2f));
	
		btnSearchArtist = new JButton("Artist");
		btnSearchSong = new JButton("Song");
		btnSearchAlbum = new JButton("Album");
		btnSearchGenre = new JButton("Genre");
		

		
		btnSearchSong.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					buildPlaylist(SearchDomain.SONG, txtSearch.getText().trim().toLowerCase());
				} catch (Exception ex) {
					showErrorMessage("Operation Failed", ex.getMessage());
				}
			}
		});
		
		btnSearchArtist.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					buildPlaylist(SearchDomain.ARTIST, txtSearch.getText().trim().toLowerCase());
				} catch (Exception ex) {
					showErrorMessage("Operation Failed", ex.getMessage());
				}
			}
		});
		
		btnSearchAlbum.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					buildPlaylist(SearchDomain.ALBUM, txtSearch.getText().trim().toLowerCase());
				} catch (Exception ex) {
					showErrorMessage("Operation Failed", ex.getMessage());
				}
				
			}
		});
		
		btnSearchGenre.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					buildPlaylist(SearchDomain.GENRE, txtSearch.getText().trim().toLowerCase());
				} catch (Exception ex) {
					showErrorMessage("Operation Failed", ex.getMessage());
				}
			}
		});
	}

	protected void buildPlaylist(SearchDomain searchDomain, String keyword) throws BasicPlayerException, SearchFailedException, NotYetConnectedException, IOException {
		P2PRadio.getInstance().getMp3Player().stop();
		P2PRadio.getInstance().getPlaylist().clearPlaylist();
		PlaylistBuilder builder = new PlaylistBuilder();
		builder.buildPlaylist(searchDomain, keyword);
	}

	private void initRating() {
		lblRating = new JLabel("rating");
		lblRating.setFont(lblRating.getFont().deriveFont(lblRating.getFont().getStyle() | Font.BOLD, lblRating.getFont().getSize() + 2f));
		btnRatingOne = new JButton("1");
		btnRatingTwo = new JButton("2");
		btnRatingThree = new JButton("3");
		btnRatingFour = new JButton("4");
		btnRatingFive = new JButton("5");
		
		btnRatingOne.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				P2PRadio.getInstance().getPlaylist().rateCurrentSelectedSong(Constants.RATING_ONE);
			}
		});
		
		
		btnRatingTwo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				P2PRadio.getInstance().getPlaylist().rateCurrentSelectedSong(Constants.RATING_TWO);
			}
		});
		
		btnRatingThree.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				P2PRadio.getInstance().getPlaylist().rateCurrentSelectedSong(Constants.RATING_THREE);
			}
		});
		
		btnRatingFour.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				P2PRadio.getInstance().getPlaylist().rateCurrentSelectedSong(Constants.RATING_FOUR);
			}
		});
		
		btnRatingFive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				P2PRadio.getInstance().getPlaylist().rateCurrentSelectedSong(Constants.RATING_FIVE);
			}
		});
		
	}

	private void initPlaylist() {
		lblPlaylist = new JLabel("playlist");
		playlistUI = new PlaylistUI();
		lblPlaylist.setFont(lblPlaylist.getFont().deriveFont(lblPlaylist.getFont().getStyle() | Font.BOLD, lblPlaylist.getFont().getSize() + 2f));
	}

	private void initLogView() {
		lblLogConsole = new JLabel("log");
		lblLogConsole.setFont(lblLogConsole.getFont().deriveFont(lblLogConsole.getFont().getStyle() | Font.BOLD, lblLogConsole.getFont().getSize() + 2f));
		
		logPane = new LogPane();
		logPane.getTpMessageBoard().setBounds(0, 0, 854, 258);
		logPane.setVisible(true);
		
		LogHandler.addLogEventListener(logPane);
	}

	private void initLayout() {
		GroupLayout gl_mainPanel = new GroupLayout(mainPanel);
		gl_mainPanel.setHorizontalGroup(
			gl_mainPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(logPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
						.addComponent(playlistUI, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
						.addComponent(lblLogConsole, Alignment.LEADING)
						.addGroup(Alignment.LEADING, gl_mainPanel.createSequentialGroup()
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_mainPanel.createSequentialGroup()
									.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING, false)
										.addGroup(gl_mainPanel.createSequentialGroup()
											.addComponent(btnSkipback)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(btnPlay)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(btnPause)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(btnStop)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(btnSkip))
										.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addGap(18)
									.addComponent(lblSearch))
								.addGroup(gl_mainPanel.createSequentialGroup()
									.addComponent(lblRating)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnRatingOne)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnRatingTwo)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnRatingThree)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnRatingFour)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnRatingFive)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(txtSearch)
								.addGroup(gl_mainPanel.createSequentialGroup()
									.addComponent(btnSearchArtist)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnSearchAlbum)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnSearchSong)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnSearchGenre))))
						.addComponent(lblPlaylist, Alignment.LEADING))
					.addContainerGap())
		);
		gl_mainPanel.setVerticalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnSkipback)
						.addComponent(btnPlay)
						.addComponent(btnPause)
						.addComponent(btnStop)
						.addComponent(btnSkip)
						.addComponent(txtSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblSearch))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnSearchArtist)
							.addComponent(btnSearchAlbum)
							.addComponent(btnSearchSong)
							.addComponent(btnSearchGenre))
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblRating)
								.addComponent(btnRatingOne)
								.addComponent(btnRatingTwo)
								.addComponent(btnRatingThree)
								.addComponent(btnRatingFour)
								.addComponent(btnRatingFive))))
					.addGap(18)
					.addComponent(lblPlaylist)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(playlistUI, GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblLogConsole)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(logPane, GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
					.addContainerGap())
		);
		mainPanel.setLayout(gl_mainPanel);
	}

	public void setPlaylist(PlayList playlist) {
		playlistUI.setTableModel(playlist);	
	}

	public static ImageIcon getScaledImage(ImageIcon srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg.getImage(), 0, 0, w, h, null);
        g2.dispose();
        return new ImageIcon(resizedImg);
    }

	public PlaylistUI getPlaylistUI() {
		return playlistUI;
	}
	
	
	private void showErrorMessage(final String title, final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JOptionPane.showMessageDialog(mainPanel,
						message,
						title,
					    JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	
	// mp3 player listener implementation
	@Override
	public void opened(Object arg0, Map arg1) {
		
	}

	@Override
	public void progress(int arg0, long arg1, byte[] arg2, Map arg3) {
		long position = (long) arg3.get("mp3.position.byte");
		long total = P2PRadio.getInstance().getMp3Player().getCurrentSong().getFile().length();
		int progress = (int)(position * PROGRESS_MAX / total);
		if(progress < 0) progress = 0;
		else if(progress > PROGRESS_MAX) progress = PROGRESS_MAX;
		
		progressBar.setValue(progress);
	}

	@Override
	public void setController(BasicController arg0) {
		// not used
	}

	@Override
	public void stateUpdated(BasicPlayerEvent arg0) {
		// not used
	}
	// -- mp3 player listener implementation
}
