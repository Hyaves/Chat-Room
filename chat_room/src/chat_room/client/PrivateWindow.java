package chat_room.client;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PrivateWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private Client client;
	
	private JTextArea textArea;
	private JScrollPane scroll;
	private JTextField message;
	private JPanel panel;
	private JButton button;
	private Font font;
	
	public PrivateWindow(String name, Client client){
		
		this.name = name;
		this.client = client;
		
		setSize(300, 300);
		setTitle(name);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLocationRelativeTo(null);
		
		panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		gridbag.columnWidths = new int[]{240, 55};
		gridbag.rowHeights = new int[]{275, 25};
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
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(5, 5, 5, 5);
		
		panel.add(scroll, c);
		
		message = new JTextField();
		message.setFont(font);
		message.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					privateMessage();
				}
			}
		});
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(0, 5, 5, 5);
		
		panel.add(message, c);
		
		button = new JButton("Send");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				privateMessage();
			}			
		});
		
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0;
		c.insets = new Insets(0, 0, 5, 5);
		
		panel.add(button, c);	
		
		setVisible(true);
		message.requestFocusInWindow();	
	}
	
	public String getName(){
		return name;
	}
	
	private void privateMessage(){
		client.send("/p/" + name + " " + client.getName() + ": " + message.getText());
		message.setText("");
	}
	
	public void receiveMessage(String msg){
		textArea.append(msg + '\n');
	}

}
