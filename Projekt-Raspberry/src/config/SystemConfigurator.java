package config;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import spssystem.IOSystem;

public class SystemConfigurator extends JFrame {
	
	private static final long serialVersionUID = -3642226737598812803L;
	
	protected JButton buttonSetAdress;
	protected JButton buttonAbbort;
	protected JTextField fieldAdress;
	protected JTextField fieldMasterAdress;
	protected JLabel labelModuleInfo;
	
	protected int setAdress = 0;
	protected int masterAdress = 0;
	protected int timeOutTimer;
	
	protected IOSystem ioInstance;
	
	private boolean isRunning;
	
	@SuppressWarnings("deprecation")
	public void start() {
		
		System.out.println("Start SystemConfigurator!");
		
		JPanel panel = new JPanel();
		panel.setLayout(null);
		
		buttonSetAdress = new JButton("Adresse setzen für modul ...");
		buttonSetAdress.setBounds(10, 10, 200, 30);
		buttonSetAdress.addActionListener((e) -> onButtonPressed(e));
		panel.add(buttonSetAdress);
		
		buttonAbbort = new JButton("Abbruch");
		buttonAbbort.setBounds(220, 10, 100, 30);
		buttonAbbort.addActionListener((e) -> onButtonPressed(e));
		panel.add(buttonAbbort);
		
		JLabel label1 = new JLabel("Neue Adresse | Modul-Master-Adresse");
		label1.setBounds(10, 50, 300, 20);
		panel.add(label1);
		
		fieldAdress = new JTextField("1");
		fieldAdress.setBounds(10, 70, 50, 20);
		fieldAdress.addKeyListener(new FieldAdressChecker());
		panel.add(fieldAdress);
		
		fieldMasterAdress = new JTextField("0");
		fieldMasterAdress.setBounds(100, 70, 50, 20);
		fieldMasterAdress.addKeyListener(new FieldAdressChecker());
		fieldMasterAdress.addActionListener((e) -> onButtonPressed(e));
		panel.add(fieldMasterAdress);

		JLabel label2 = new JLabel("============================================");
		label2.setBounds(10, 90, 320, 20);
		panel.add(label2);
		
		labelModuleInfo = new JLabel("<html>SET-Button an Zielmodul drücken.<br/>Meldung verschwinded wenn adresse gesetzt!</html>");
		labelModuleInfo.setBounds(10, 100, 320, 55);
		labelModuleInfo.setForeground(Color.RED);
		labelModuleInfo.setVisible(false);
		panel.add(labelModuleInfo);
		
		this.add(panel);
		this.setSize(345, 200);
		this.setResizable(false);
		this.setTitle("SPS-SystemConfigurator");
		
		this.show();
		
		ioInstance = new IOSystem();
		
		isRunning = true;
		while (isRunning) {
			
			if (!this.isVisible()) stop();
			
			tick();
			
			try {
				// Clock Speed
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		System.out.println("SystemConfigurator closed, shutdown ...");
				
	}
	
	public void stop() {
		this.isRunning = false;
	}
	
	public void onButtonPressed(ActionEvent e) {
		
		if (e.getSource() == buttonSetAdress && !isSettingAdress()) {
			
			int adress = getAdressFromField(fieldAdress);
			int masterAdr = getAdressFromField(fieldMasterAdress);
			
			if (adress > 0 && masterAdr >= 0 && adress != masterAdr) {
				
				System.out.println("Set Adress " + adress + " for ...!");
				
				setAdress(adress, masterAdr);
				
			}
			
		} else if (e.getSource() == buttonAbbort) {
			
			timeOutTimer = 0;
			
		}
		
	}
	
	protected int getAdressFromField(JTextField field) {
		try {
			int b = Integer.valueOf(field.getText());
			if (b > 255) return -1;
			return b;
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	public void setAdress(int adress, int masterAdress) {
		if (ioInstance != null) {
			this.setAdress = adress;
			this.masterAdress = masterAdress;
			timeOutTimer = 500;
		}
	}
	
	public boolean isSettingAdress() {
		return timeOutTimer > 0;
	}
	
	public void tick() {
		
		this.labelModuleInfo.setVisible(isSettingAdress());
		this.labelModuleInfo.setText("<html>SET-Button an Zielmodul drücken.<br/>Meldung verschwinded wenn adresse gesetzt!<br/>Timeout in: " + this.timeOutTimer + "</html>");
		if (this.timeOutTimer > 0) this.timeOutTimer--;
		
		if (isSettingAdress()) {
			
			byte[] response = ioInstance.writeAdressConfig((byte) setAdress, (byte) masterAdress);

			System.out.println("DDDDD ");
			
			for (byte b : response) System.out.print(b);
			
			if (response.length == 2) {
				
				byte masterRecived = response[0];
				byte adressSet = response[1];
				
				if (masterRecived == this.masterAdress && adressSet == this.setAdress) {
					
					timeOutTimer = 0;
					
					System.out.println("Adress Set!");
					
				}
				
			}
			
		}
		
	}
	
	
	
	
	
	
	protected class FieldAdressChecker implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
			
			int adr = getAdressFromField((JTextField) e.getComponent());
			e.getComponent().setForeground(adr >= 0 ? Color.BLACK : Color.RED);
			
		}

		@Override
		public void keyPressed(KeyEvent e) {

			int adr = getAdressFromField((JTextField) e.getComponent());
			e.getComponent().setForeground(adr >= 0 ? Color.BLACK : Color.RED);
			
		}

		@Override
		public void keyReleased(KeyEvent e) {

			int adr = getAdressFromField((JTextField) e.getComponent());
			e.getComponent().setForeground(adr >= 0 ? Color.BLACK : Color.RED);
			
		}
		
	}
	
}
