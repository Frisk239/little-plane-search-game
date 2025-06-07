package com.frame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MainFrame extends JFrame {
    /**
     * 寻机头游戏的主界面
     * 包含游戏模式选择、排行榜、设置等功能入口
     */
    public MainFrame() {
        // 设置窗口基本属性
        setTitle("游戏主界面");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(null);
        Color Background=new Color(204, 229, 255);//定义背景颜色
        Color Button=new Color(189,211,237);//定义模式选择按钮颜色
        getContentPane().setBackground(Background);
        setIconImage(
                Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("logo.jpg"))
        );
        // 左侧功能按钮面板
        JPanel leftPanel = new JPanel();
        leftPanel.setSize(150,550);
        leftPanel.setLocation(0,50);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS)); // 垂直布局
        leftPanel.setBackground(Background);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); // 内边距

        // 添加左侧功能按钮
        addButton(leftPanel, "排行榜", e -> new RankingBoardFrame().setVisible(true));
        addButton(leftPanel, "帮助", e -> new HelpFrame().setVisible(true));
        addButton(leftPanel, "设置", e -> new SettingsFrame().setVisible(true));
        add(leftPanel, BorderLayout.WEST); // 左侧区域

        // 右侧快捷操作面板
        JPanel rightPanel = new JPanel();
        rightPanel.setSize(200,600);
        rightPanel.setLocation(600,0);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS)); // 垂直布局
        rightPanel.setBackground(Background);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); // 内边距

        // 添加右侧快捷按钮
        addButton(rightPanel, "签到", e -> JOptionPane.showMessageDialog(null, "签到成功！感谢您的参与。"));
        addButton(rightPanel, "公告", e -> JOptionPane.showMessageDialog(null, "当前暂无新公告，敬请期待。"));
        addButton(rightPanel, "意见箱", e -> JOptionPane.showMessageDialog(null, "感谢您的反馈，我们会认真处理。"));
        addButton(rightPanel, "交流区", e -> JOptionPane.showMessageDialog(null, "欢迎来到交流区，快来和大家互动吧！"));
        add(rightPanel, BorderLayout.EAST); // 右侧区域

        // 添加游戏标题
        JLabel titleLabel = new JLabel("寻机头游戏");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 72)); // 72号粗体
        titleLabel.setForeground(new Color(35, 105, 188)); // 深蓝色
        titleLabel.setSize(400,200);
        titleLabel.setLocation(200,0);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 水平居中
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // 上下边距
        add(titleLabel);

        // 添加游戏模式选择按钮
        JButton SingleButton=new JButton("单人模式");
        SingleButton.setSize(200,50);
        SingleButton.setLocation(300,225);
        SingleButton.setBackground(Button);
        SingleButton.setFont(new Font("楷体", Font.BOLD, 28));
        SingleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SingleMode().initialize();
                MainFrame.this.dispose();

            }
        });
        add(SingleButton);

        JButton DoubleButton=new JButton("双人模式");
        DoubleButton.setSize(200,50);
        DoubleButton.setLocation(300,325);
        DoubleButton.setBackground(Button);
        DoubleButton.setFont(new Font("楷体", Font.BOLD, 28));
        DoubleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new DoubleMode().initialize();
                MainFrame.this.dispose();

            }
        });
        add(DoubleButton);
        JButton DuelButton=new JButton("对抗模式");
        DuelButton.setSize(200,50);
        DuelButton.setLocation(300,425);
        DuelButton.setBackground(Button);
        DuelButton.setFont(new Font("楷体", Font.BOLD, 28));
        DuelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new DuelMode().initializeGame();
                MainFrame.this.dispose();

            }
        });
        add(DuelButton);

        setLocationRelativeTo(null);
        setVisible(true); // 显示窗口
    }

    //添加点击按钮监听事件
    /**
     * 辅助方法：创建并添加按钮到指定面板
     *
     * @param panel    要添加按钮的面板
     * @param text     按钮显示文本
     * @param listener 按钮点击事件监听器
     * @return
     */
    private void addButton(JPanel panel, String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setBackground(new Color(202, 222, 250, 255)); // 浅灰色背景
        button.setFont(new Font("宋体", Font.BOLD, 16)); // 16号字体

        // 针对垂直布局的面板做特殊设置
        if (panel.getLayout() instanceof BoxLayout) {
            button.setMaximumSize(new Dimension(100, 40)); // 按钮最大尺寸
            button.setAlignmentX(Component.CENTER_ALIGNMENT); // 水平居中
            panel.add(Box.createVerticalStrut(70)); // 添加70像素垂直间距
        }

        panel.add(button);
    }
}
/**
 * 游戏规则窗口类
 * 显示不同游戏模式的规则说明
 */
class HelpFrame extends JFrame {
	public HelpFrame() {
	    setTitle("帮助");
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    setSize(600, 400);
	    setLocationRelativeTo(null);
	    setLayout(new BorderLayout());
	    setIconImage(
	            Toolkit.getDefaultToolkit().getImage(HelpFrame.class.getResource("logo.jpg"))
	    );

	    // 顶部模式切换按钮面板
	    JPanel northPanel = new JPanel();
	    northPanel.setLayout(new FlowLayout());
	    JButton singleModeButton = new JButton("单人模式");
	    JButton doubleModeButton = new JButton("双人模式");
	    JButton DuelButton = new JButton("对抗模式");

	    // 设置按钮样式
	    singleModeButton.setBackground(new Color(220, 220, 220));
	    doubleModeButton.setBackground(new Color(220, 220, 220));
	    DuelButton.setBackground(new Color(220, 220, 220));

	    singleModeButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
	    doubleModeButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
	    DuelButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        //创建Rules类的对象，以便调用
        Rules r=new Rules();
	    singleModeButton.addActionListener(e -> r.showSRules());
        doubleModeButton.addActionListener(e ->r.showDRule());
        DuelButton.addActionListener(e -> r.showDuRules());
	    northPanel.add(singleModeButton);
	    northPanel.add(doubleModeButton);
	    northPanel.add(DuelButton);
	    add(northPanel, BorderLayout.NORTH);

	    // 中间规则显示区域
	    JPanel bodyPanel = new JPanel(new BorderLayout());
	    bodyPanel.setBackground(new Color(204, 229, 255));

        try {
            // 使用 ImageIO 的 read 方法读取图片资源，通过 getClass().getResourceAsStream 方法
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/com/frame/PlaneType.png"));
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);  // 居中对齐
            // 创建一个 JScrollPane 滚动面板，并将包含图片的 JLabel 添加到滚动面板中
            JScrollPane scrollPane = new JScrollPane(imageLabel);
            // 为滚动面板设置边框，这里使用 BorderFactory.createEmptyBorder() 创建一个空边框
            // 即去掉滚动面板默认的边框，让界面看起来更简洁
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            bodyPanel.add(scrollPane, BorderLayout.CENTER);
        } catch (IOException e) {
            // 如果在读取图片过程中发生输入输出异常（如文件不存在、文件损坏等）
            // 打印异常的堆栈信息，方便调试时定位问题
            e.printStackTrace();
            // 创建一个新的 JLabel 用于显示错误信息
            JLabel errorLabel = new JLabel("无法加载图片: " + e.getMessage(), JLabel.CENTER);//居中
            bodyPanel.add(errorLabel);
        }
	    add(bodyPanel, BorderLayout.CENTER);
	    setVisible(true);
	}
}

class Rules extends JFrame{
    public void showSRules() {
        String rulesText = ("<html><div style='width:300px;'>"
                + "<h2>单人模式游戏规则</h2>"
                + " 在蓝色方块（机身）的指引下，找出所有飞机头（红色方块），即可获得游戏胜利\n");
        JOptionPane.showMessageDialog(this, rulesText, "游戏规则", JOptionPane.INFORMATION_MESSAGE);
    }
    public void showDRule(){
        String rulesText=("<html><body style='width: 300px; padding: 10px;'>"
                + "<h2>双人模式游戏规则</h2>"
                + "<p><b>游戏目标：</b><br>"
                + "通过攻击对方区域，找到并击中所有飞机头。</p>"
                + "<p><b>游戏玩法：</b><br>"
                + "1. 每位玩家在自己的区域布置飞机<br>"
                + "2. 轮流点击对方区域进行攻击<br>"
                + "3. 击中机身会显示蓝色<br>"
                + "4. 击中机头会显示红色并揭示整架飞机<br>"
                + "5. 先找到所有飞机头的玩家获胜</p>"
                + "<p><b>统计信息：</b><br>"
                + "- 游戏时间<br>"
                + "- 玩家点击次数</p>"
                + "</body></html>" );
        JOptionPane.showMessageDialog(this, rulesText, "游戏规则", JOptionPane.INFORMATION_MESSAGE);
    }
    public void showDuRules() {
        String rulesText = "<html><body style='width: 300px; padding: 10px;'>"
                + "<h2>对抗模式游戏规则</h2>"
                + "<p><b>游戏目标：</b><br>"
                + "通过攻击对方区域，找到并击中所有飞机头。</p>"
                + "<p><b>游戏玩法：</b><br>"
                + "1. 每位玩家在自己的区域布置飞机<br>"
                + "2. 选择位置和方向后点击确认放置<br>"
                + "3. 轮流点击对方区域进行攻击<br>"
                + "4. 击中机身会显示蓝色<br>"
                + "5. 击中机头会显示红色并揭示整架飞机<br>"
                + "6. 先找到所有飞机头的玩家获胜</p>"
                + "<p><b>统计信息：</b><br>"
                + "- 游戏时间<br>"
                + "- 玩家点击次数</p>"
                + "</body></html>";

        JOptionPane.showMessageDialog(this, rulesText, "游戏规则说明",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
/**
 * 设置窗口类
 * 包含亮度、音乐、音效和难度设置选项
 */
class SettingsFrame extends JFrame {
    public SettingsFrame() {
        setTitle("设置");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭时仅释放窗口资源
        setSize(400, 350);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new GridLayout(5, 1, 10, 20)); // 5行1列网格布局
        setBackground(new Color(204, 229, 255));
        setIconImage(
                Toolkit.getDefaultToolkit().getImage(SettingsFrame.class.getResource("logo.jpg"))
        );

        // 1. 亮度调节面板
        JPanel brightnessPanel = new JPanel();
        brightnessPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

        JLabel brightnessLabel = new JLabel("亮度");
        brightnessLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JSlider brightnessSlider = new JSlider(0, 100, 50); // 范围0-100，初始值50
        brightnessSlider.setPreferredSize(new Dimension(200, 40));
        brightnessSlider.setMajorTickSpacing(20); // 主刻度间隔20
        brightnessSlider.setPaintTicks(true); // 显示刻度
        brightnessSlider.setPaintLabels(true); // 显示标签

        brightnessPanel.add(brightnessLabel);
        brightnessPanel.add(brightnessSlider);
        add(brightnessPanel);

        // 2. 音乐设置面板
        JPanel musicPanel = new JPanel();
        musicPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

        JLabel musicLabel = new JLabel("音乐");
        musicLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        ButtonGroup musicGroup = new ButtonGroup();
        JRadioButton musicOn = new JRadioButton("开");
        JRadioButton musicOff = new JRadioButton("关");

        musicOn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        musicOff.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        musicGroup.add(musicOn);
        musicGroup.add(musicOff);
        musicOn.setSelected(true); // 默认开启

        musicPanel.add(musicLabel);
        musicPanel.add(musicOn);
        musicPanel.add(musicOff);
        add(musicPanel);

        // 3. 音效设置面板
        JPanel soundEffectPanel = new JPanel();
        soundEffectPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

        JLabel soundEffectLabel = new JLabel("音效");
        soundEffectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        ButtonGroup soundEffectGroup = new ButtonGroup();
        JRadioButton soundEffectOn = new JRadioButton("开");
        JRadioButton soundEffectOff = new JRadioButton("关");

        soundEffectOn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        soundEffectOff.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        soundEffectGroup.add(soundEffectOn);
        soundEffectGroup.add(soundEffectOff);
        soundEffectOn.setSelected(true); // 默认开启

        soundEffectPanel.add(soundEffectLabel);
        soundEffectPanel.add(soundEffectOn);
        soundEffectPanel.add(soundEffectOff);
        add(soundEffectPanel);

        // 4. 难度系数设置面板
        JPanel difficultyPanel = new JPanel();
        difficultyPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

        JLabel difficultyLabel = new JLabel("难度系数");
        difficultyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        ButtonGroup difficultyGroup = new ButtonGroup();
        JRadioButton easy = new JRadioButton("易");
        JRadioButton medium = new JRadioButton("中");
        JRadioButton hard = new JRadioButton("难");

        easy.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        medium.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        hard.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        difficultyGroup.add(easy);
        difficultyGroup.add(medium);
        difficultyGroup.add(hard);
        easy.setSelected(true); // 默认简单难度

        difficultyPanel.add(difficultyLabel);
        difficultyPanel.add(easy);
        difficultyPanel.add(medium);
        difficultyPanel.add(hard);
        add(difficultyPanel);

        // 5. 确认按钮面板
        JPanel confirmPanel = new JPanel();
        confirmPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
//        confirmPanel.setBackground(new Color(204, 229, 255));

        JButton confirmButton = new JButton("确认");
        confirmButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "设置已保存！");
                dispose(); // 关闭设置窗口
            }
        });

        confirmPanel.add(confirmButton);
        add(confirmPanel);
    }
}
