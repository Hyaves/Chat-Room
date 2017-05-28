package chat_room.client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel panel;
	private JLabel nameLabel, addressLabel, portLabel, warningLabel, passwordLabel;
	private JTextField nameField, addressField, portField;
	private JPasswordField passwordField;
	private JButton button;
	private JCheckBox checkBox;
	
	private String name, password, address;
	private int port;
	
	public Login(){
		initUI();
	}
	
	private void initUI(){
		
		setSize(250, 360);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setTitle("Login");
		setResizable(false);
		
		panel = new JPanel();
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		
		gridbag.columnWidths = new int[]{50, 150, 50};
		gridbag.rowHeights = new int[]{30, 25, 25, 25, 25, 25, 25, 25, 25, 25, 40, 25, 40};
		
		panel.setLayout(gridbag);
		add(panel);
		
		nameLabel = new JLabel("Name: ", SwingConstants.CENTER);
		c.gridx = 1;
		c.gridy = 1;
		panel.add(nameLabel, c);
		
		nameField = new JTextField();
		c.gridy = 2;
		panel.add(nameField, c);
		
		passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
		c.gridy = 3;
		panel.add(passwordLabel, c);
		
		passwordField = new JPasswordField();
		passwordField.setEchoChar('*');
		c.gridy = 4;
		panel.add(passwordField, c);
		
		checkBox = new JCheckBox("Show password");
		checkBox.setFont(new Font("Verdana", Font.ITALIC, 10));
		checkBox.setSelected(false);
		checkBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkBox.isSelected()){
					passwordField.setEchoChar('\u0000');
				} else {
					passwordField.setEchoChar('*');
				}
			}			
		});
		c.gridy = 5;
		panel.add(checkBox, c);
		
		addressLabel = new JLabel("Address: ", SwingConstants.CENTER);
		c.gridy = 6;
		panel.add(addressLabel, c);
		
		addressField = new JTextField();
		addressField.setText("localhost");
		c.gridy = 7;
		panel.add(addressField, c);
		
		portLabel = new JLabel("Port: ", SwingConstants.CENTER);
		c.gridy = 8;
		panel.add(portLabel, c);
		
		portField = new JTextField();
		portField.setText("3231");
		c.gridy = 9;
		panel.add(portField, c);
		
		warningLabel = new JLabel("", SwingConstants.CENTER);
		warningLabel.setForeground(Color.RED);
		c.gridy = 10;
		panel.add(warningLabel, c);
		
		button = new JButton("Login");
		c.fill = 0;
		c.gridy = 11;
		panel.add(button, c);
		
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {				
				if(inputCheck()){
					login(name, password, address, port);
				};	
			}		
		});		
		
		setVisible(true);
	}
	
	private boolean inputCheck(){
		
		if(warningLabel.getText() != ""){
			warningLabel.setText("");
		}
		
		if(!nameField.getText().equals("")){
			this.name = nameField.getText();
		} else {
			warningLabel.setText("Name required!");
			return false;
		}
		
		if(!passwordField.equals("")){
			this.password = String.valueOf(passwordField.getPassword());
		} else {
			warningLabel.setText("Password required!");
			return false;
		}

		if(!addressField.getText().equals("")){
			this.address = addressField.getText();
		} else {
			warningLabel.setText("Address required!");
			return false;
		}
		
		if(!portField.getText().equals("")){
			if(portField.getText().matches("[0-9]*")){
				port = Integer.parseInt(portField.getText());
			} else {
				warningLabel.setText("Port must be number!");
				return false;
			}
		} else {
			warningLabel.setText("Port required!");
			return false;
		}
		return true;
	}
	
	public void setWarnignLabel(String warning){
		warningLabel.setText(warning);
	}
	
	private void login(String name, String password, String address, int port){
		new ClientWindow(name, password, address, port);
		dispose();
	}
	
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			public void run(){	
				new Login();
			}
		});	
	}

}
