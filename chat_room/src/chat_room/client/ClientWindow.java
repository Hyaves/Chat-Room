package chat_room.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class ClientWindow extends JFrame implements Runnable{
	
	private Client client;
	private Thread mainThread, receiveThread;
	private boolean running = false;
	
	private JPanel panel;
	private JTextArea textArea;
	private JTextField message;
	private JScrollPane scroll, scrollFriends, scrollUsers;
	private JButton sendButton, addButton, removeButton;
	private JList<String> onlineUsers;
	private JList<ListEntry> friends;
	private List<PrivateWindow> pw = new ArrayList<>();
	private DefaultListModel<ListEntry> dlm;
	
	private Font font;

	private static final long serialVersionUID = 1L;

	public ClientWindow(String name, String password, String address, int port){
		try {		
			client = new Client(name, password, address, port);
		} catch (IOException e) {			
			new Login().setWarnignLabel("Unable to connect to server");			
			return;
		}	

		initUI();
		mainThread = new Thread(this, "main");
		mainThread.start();
	}
	
	private void initUI(){
		
		setSize(820, 500);
		setTitle("Chat Client || " + client.getName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		panel = new JPanel();	
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		gridbag.columnWidths = new int[]{570, 80, 85, 85};
		gridbag.rowHeights = new int[]{220, 225, 35};
		c.fill = GridBagConstraints.BOTH;	
		font = new Font("Verdana", Font.PLAIN, 12);

		panel.setLayout(gridbag);		
		add(panel);
		
		textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setFont(font);
		scroll = new JScrollPane(textArea);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 2;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		
		panel.add(scroll, c);
		
		message = new JTextField();
		message.setFont(font);
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(0, 10, 10, 10);
		
		message.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					sendToAll();
				}
			}
		});
	
		panel.add(message, c);	
		
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				sendToAll();
			}			
		});
		
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.weightx = 0;
		c.insets = new Insets(0, 0, 10, 10);
		
		panel.add(sendButton, c);	
		
		onlineUsers = new JList<String>();
		onlineUsers.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Users online"));
		onlineUsers.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e){
				if(e.getClickCount() == 1){
					friends.clearSelection();
				}
				if(e.getClickCount() == 2){
					String value = onlineUsers.getSelectedValue();
					PrivateWindow window = getWindowByName(value);
					if(window == null){
						createWindow(value);		
					} else if(!window.isVisible()){
						window.setVisible(true);
					} else {
						window.requestFocus();
					}		
				}
			}
		});
		
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(10, 0, 10, 10);
		
		scrollUsers = new JScrollPane(onlineUsers);
		panel.add(scrollUsers, c);
		
		dlm = new DefaultListModel<>();
		friends = new JList<>(dlm);
		friends.setCellRenderer(new ListEntryCellRenderer());
		friends.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Friends"));
		friends.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e){
				if(e.getClickCount() == 1){
					onlineUsers.clearSelection();
				}
				if(e.getClickCount() == 2){
					String value = friends.getSelectedValue().getValue();
					PrivateWindow window = getWindowByName(value);
					if(window == null){
						createWindow(value);		
					} else if(!window.isVisible()){
						window.setVisible(true);
					} else {
						window.requestFocus();
					}		
				}
			}
		});
		
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(0, 0, 10, 10);
		
		scrollFriends = new JScrollPane(friends);
		panel.add(scrollFriends, c);
		
		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String selection = onlineUsers.getSelectedValue();
				if(selection == null){
					selection = JOptionPane.showInputDialog(getThis(), "Type friend's name:", "Add a friend", JOptionPane.PLAIN_MESSAGE);	
					if(selection == null){
						return;
					}
				}
				if(selection.equals("You") || selection.equals(client.getName())){
					textArea.append("You cannot add yourself." + '\n');
					return;
				}
				for(int i = 0; i < dlm.getSize(); i++){
					if(dlm.getElementAt(i).getValue().equals(selection)){
						textArea.append("User " + selection + " is already on friend list." + '\n');
						return;
					}
				}
				client.send("/f/" + selection);
			}			
		});
		
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(0, 0, 10, 10);
		panel.add(addButton, c);

		removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String selection = friends.getSelectedValue().getValue();
				if(selection == null){
					textArea.append("First select user to remove." + '\n');
					return;
				}
				int choice = JOptionPane.showOptionDialog(getThis(), "Are u sure you want to remove " + selection + " from a friends list?", 
						"Remove friend", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Yes", "No"}, null);
				if(choice == 0){
					client.send("/r/" + selection);
				}
			}			
		});
		
		c.gridx = 3;
		c.gridy = 2;
		
		panel.add(removeButton, c);
		
		message.requestFocusInWindow();	
	}
	
	private void createWindow(String value){
		if(value.equals("You") || value.equals("")){
			return;
		}
		if(pw.isEmpty()){
			pw.add(new PrivateWindow(value, client));
			return;
		}
		if(getWindowByName(value) == null){
			pw.add(new PrivateWindow(value, client));
		}
	}
	
	private PrivateWindow getWindowByName(String name){
		for(int i = 0; i < pw.size(); i++){
			if(pw.get(i).getName().equals(name)){
				return pw.get(i);
			}
		}
		return null;
	}
	
	private void sendToAll(){
		String text = message.getText();
		if(text.equals("")) return;
		client.send("/m/" + text);
		message.setText("");
	}
	
	public void sendPrivateMessage(String text, String name){
		client.send("/p/" + name + text);
	}
	
	private void receive(){
		receiveThread = new Thread(){
			public void run(){
				while(running){
					String text = client.receive();
					process(text);					
				}
			}
		};
		receiveThread.start();
	}
	
	private void process(String text){
		String message = text.substring(3, text.length());;
		String prefix = text.substring(0, 3);
		if(prefix.equals("/c/")){
			setVisible(true);			
			textArea.append(message + '\n');
		}
		if(prefix.equals("/d/")){
			new Login().setWarnignLabel(message);
			for(int i = 0; i < pw.size(); i++){
				pw.get(i).dispose();
			}
			running = false;
			client.close();
			dispose();
		}
		if(prefix.equals("/m/")){
			if(message.startsWith(client.getName() + ":")){
				message = "You" + message.substring(client.getName().length(), message.length());
			}
			textArea.append(message + '\n');
			return;
		}
		if(prefix.equals("/p/")){
			String recipient = message.substring(1, message.indexOf(':'));	
			message = message.substring(1, message.length());
			PrivateWindow recipientWindow = getWindowByName(recipient);
	
			if(recipientWindow != null){
				if(!recipientWindow.isVisible()){
					recipientWindow.setVisible(true);
				}
				recipientWindow.receiveMessage(message);
				return;				
			}

			createWindow(recipient);
			getWindowByName(recipient).receiveMessage(message);
			return;
		}		
		if(prefix.equals("/s/")){
			String recipient = message.substring(0, message.indexOf(' '));
			message = message.substring(recipient.length() + 1, message.length());
			message = "You" + message.substring(client.getName().length(), message.length());
			getWindowByName(recipient).receiveMessage(message);
			return;
		}
		if(prefix.equals("/o/")){
			String recipient = message.substring(0, message.indexOf(' '));
			message = message.substring(recipient.length() + 1, message.length());
			getWindowByName(recipient).receiveMessage(message);
			return;
		}
		if(prefix.equals("/u/")){
			String[] users = message.split("/./");
			updateList(users);
			return;
		}
		if(prefix.equals("/f/")){
			String[] list = message.split("/./");
			dlm.clear();
			for(int i = 0; i < list.length; i++){	
				dlm.addElement(new ListEntry(list[i], new CustomIcon(checkOnline(list[i]))));
			}
			return;
		}
		if(prefix.equals("/e/")){
			textArea.append(message + '\n');
			running = false;
			client.close();
			return;
		}
	}
	
	private Color checkOnline(String friend){
		for(int i = 0; i < onlineUsers.getModel().getSize(); i++){
			if(onlineUsers.getModel().getElementAt(i).equals(friend)){
				return Color.GREEN;
			}
		}
		return Color.RED;
	}
	
	private void updateList(String[] users){
		String[] userL = new String[users.length];
		for(int i = 0, j = 0; i < users.length; i++){
			if(!users[i].equals(client.getName())){
				userL[j + 1] = users[i];
				j++;
			} else {
				userL[0] = "You";
			}
		}
		onlineUsers.setListData(userL);
		for(int i = 0; i < dlm.getSize(); i++){
			for(int j = 0; j < onlineUsers.getModel().getSize(); j++){
				if(dlm.getElementAt(i).getValue().equals(onlineUsers.getModel().getElementAt(j))){
					dlm.getElementAt(i).changeColor(Color.GREEN);
					break;
				}
				dlm.getElementAt(i).changeColor(Color.RED);
			}
		}
	}
	
	private ClientWindow getThis(){
		return this;
	}
	
	@Override
	public void run(){
		running = true;
		receive();
	}
	
	class ListEntry{
		
		private String value;
		private CustomIcon icon;
		
		public ListEntry(String value, CustomIcon icon){
			this.value = value;
			this.icon = icon;
		}
		
		public String getValue(){
			return value;
		}
		
		public CustomIcon getIcon(){
			return icon;
		}
		
		public void changeColor(Color c){
			icon.setColor(c);
		}
	}
	
	class ListEntryCellRenderer extends JLabel implements ListCellRenderer<Object>{

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			ListEntry entry = (ListEntry)value;
			
			setText(entry.getValue());
			setIcon(entry.getIcon());
			
			if(isSelected){
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			setHorizontalAlignment(SwingConstants.RIGHT);
		    setEnabled(list.isEnabled());
		    setFont(list.getFont());
		    setOpaque(true);

			return this;
		}		
	}
	
	class CustomIcon extends ImageIcon{

		private static final long serialVersionUID = 1L;
		
		private Color color;
		
		public CustomIcon(Color c){
			this.color = c;
		}
		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y){
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor(color);
			g2d.fill(new Ellipse2D.Double(3, 3, c.getHeight() - 6, c.getHeight() - 6));
		}				
		
		public void setColor(Color color){
			this.color = color;
			repaint();
		}
	}
}
