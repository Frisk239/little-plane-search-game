package com.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
	/**
	 * 寻机头游戏的登录界面
	 * 负责用户账号密码验证和跳转主界面功能
	 */
	//账号和密码输入框组件
	private JTextField accountField;
	private JPasswordField passwordField;

	//界面统一字体设置
	private static final Font MAIN_FONT = new Font("微软雅黑", Font.PLAIN, 16);
	// 界面主题色 - 浅蓝背景
	private static final Color BG_COLOR = new Color(230, 242, 255);

	//构造函数：初始化登录界面
	public LoginFrame() {
		setTitle("寻机头游戏登录");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600); // 窗口尺寸：宽1000像素，高600像素
		setLayout(null); // 边界布局，组件间距20(水平)和25(垂直)
		getContentPane().setBackground(BG_COLOR); // 设置背景色为浅蓝
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(LoginFrame.class.getResource("logo.jpg"))
		);


		// 1. 顶部面板 - 显示游戏标题
		JPanel topPanel = new JPanel();
		topPanel.setBackground(BG_COLOR);
		JLabel logoLabel = new JLabel("寻机头游戏");
		topPanel.setSize(400,100);
		topPanel.setLocation(200,100);
		logoLabel.setFont(new Font("黑体", Font.BOLD, 40)); // 36号粗体黑体
		logoLabel.setForeground(new Color(20, 80, 180)); // 深蓝色文字
		topPanel.add(logoLabel);
		add(topPanel);

		// 2. 中间表单面板 - 包含账号和密码输入区域
		//建立fromPanel面板，用于存放输入框
		JPanel formPanel = new JPanel();
		formPanel.setBackground(BG_COLOR);
		formPanel.setSize(400,100);
		formPanel.setLocation(175,225);
		GridBagConstraints gbc = new GridBagConstraints();

		// 设置并添加账号输入行
		gbc.gridx = 0; // 第0列
		gbc.gridy = 0; // 第0行
		JLabel accountLabel = new JLabel("账 号:");
		accountLabel.setFont(MAIN_FONT);
		formPanel.add(accountLabel, gbc);

		gbc.gridx = 1; // 第1列
		accountField = new JTextField(20); // 输入框宽度20列
		accountField.setFont(MAIN_FONT);
		formPanel.add(accountField, gbc);

		//设置并添加密码输入行同理
		gbc.gridx = 0; // 第0列
		gbc.gridy = 1; // 第1行
		JLabel passwordLabel = new JLabel("密 码:");
		passwordLabel.setFont(MAIN_FONT);
		formPanel.add(passwordLabel, gbc);

		gbc.gridx = 1;
		passwordField = new JPasswordField(20); // 密码输入框
		passwordField.setFont(MAIN_FONT);
		formPanel.add(passwordField, gbc);

		add(formPanel);

		// 3. 底部按钮面板 - 包含登录按钮
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(BG_COLOR);

		JButton loginButton = new JButton("登   录");
		buttonPanel.setSize(600,50);
		buttonPanel.setLocation(100,350);
		loginButton.setFont(new Font("微软雅黑", Font.BOLD, 20)); // 20号粗体
		loginButton.setBackground(new Color(50, 120, 210)); // 蓝色按钮背景
		loginButton.setForeground(Color.WHITE); // 白色文字
		loginButton.setSize(300,50);
		loginButton.setLocation(0,0);
		loginButton.setBorderPainted(false); // 无边框

		// 登录按钮点击事件处理
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 获取输入的账号和密码
				String account = accountField.getText();
				String password = new String(passwordField.getPassword());

				// 实际项目中应添加账号密码验证逻辑
				System.out.println("用户尝试登录 - 账号: " + account + ", 密码: " + password);

				// 关闭当前登录界面并打开主界面
				new MainFrame(); // 创建并显示主界面
				dispose(); // 释放当前登录界面资源
			}
		});

		buttonPanel.add(loginButton);
		add(buttonPanel);

		// 设置窗口居中显示并可见
		setLocationRelativeTo(null); // 居中于屏幕
		setVisible(true); // 显示窗口
	}

	public static void main(String[] args) {
		new LoginFrame();
	}
}
