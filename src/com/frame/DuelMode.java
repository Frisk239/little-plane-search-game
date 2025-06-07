package com.frame;

import javax.imageio.ImageIO;
import javax.swing.*;

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DuelMode extends JFrame {
	private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 50;
    private static final int PANEL_WIDTH = GRID_SIZE * CELL_SIZE;
    private static final int PANEL_HEIGHT = GRID_SIZE * CELL_SIZE;

    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel controlPanel;
    private JMenuBar menuBar;
    private JButton confirmButton;
    private JComboBox<String> directionComboBox;

    private int airplaneCount = 1;
    private Color airplaneHeadColor = new Color(255, 0, 0, 199);
    private Color airplaneBodyColor = new Color(0, 105, 180, 255);
    private String airplaneType = "机型1";

    // 游戏状态，运用GameState来管理游戏状态
    private enum GameState {
        PLAYER1_SETUP, // 玩家1布置飞机
        PLAYER2_SETUP, // 玩家2布置飞机
        PLAYER1_ATTACK, // 玩家1攻击
        PLAYER2_ATTACK, // 玩家2攻击
        GAME_OVER // 游戏结束
    }

    private GameState gameState = GameState.PLAYER1_SETUP;

    // 游戏数据
    private boolean[][] player1Airplanes;
    private boolean[][] player2Airplanes;
    private boolean[][] player1Hits;
    private boolean[][] player2Hits;
    private boolean[][] player1Revealed;
    private boolean[][] player2Revealed;

    private int player1Found = 0;
    private int player2Found = 0;
    private int player1Clicks = 0;
    private int player2Clicks = 0;

    // 计时器相关
    private Timer gameTimer;
    private long startTime;
    private JLabel timeLabel;
    private JLabel clicksLabel;

    private List<AirplaneInfo> player1AirplaneInfos;
    private List<AirplaneInfo> player2AirplaneInfos;

    // 临时存储飞机位置和方向
    private Point tempPosition;
    private int tempDirection;
    private List<Point> tempBodyParts;

    private class AirplaneInfo {
        int x, y;
        int direction;
        List<Point> bodyParts;
        Point head;

        AirplaneInfo(int x, int y, int direction, List<Point> bodyParts, Point head) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.bodyParts = bodyParts;
            this.head = head;
        }
    }

    public DuelMode() {
        setTitle("对抗模式");
        setSize(PANEL_WIDTH * 2 + 100, PANEL_HEIGHT + 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setIconImage(
        		Toolkit.getDefaultToolkit().getImage(getClass().getResource("logo.jpg"))
        );

        createMenuBar();

        leftPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        rightPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        controlPanel = new JPanel(new FlowLayout());

        // 方向选择下拉框
        directionComboBox = new JComboBox<>(new String[]{"向上", "向右", "向下", "向左"});
        directionComboBox.setSelectedIndex(0);
        // 添加方向改变监听器
        directionComboBox.addActionListener(e -> {
            if (tempPosition != null) {
                // 重新预览飞机
                boolean isPlayer1 = gameState == GameState.PLAYER1_SETUP;
                JPanel currentPanel = isPlayer1 ? leftPanel : rightPanel;
                previewAirplane(currentPanel, isPlayer1, tempPosition.x, tempPosition.y);
            }
        });

        // 确认按钮
        confirmButton = new JButton("确认放置");
        confirmButton.addActionListener(e -> confirmPlacement());
        confirmButton.setEnabled(false);

        initializeGame();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        // 添加控制面板组件
        JPanel setupPanel = new JPanel();
        setupPanel.add(new JLabel("方向:"));
        setupPanel.add(directionComboBox);
        setupPanel.add(confirmButton);

        controlPanel.add(setupPanel);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
    //确认飞机的放置
    /*1.检查数据
     * 2.确定玩家（区分玩家1和玩家2的数据存储)
     * 3.存储飞机
     * 4.记录信息
     * 5.清理数据
     * 6.检查完成
     * 7.刷新游戏状态显示
     * */
    private void confirmPlacement() {
        if (tempPosition == null || tempBodyParts == null) return;

        boolean isPlayer1 = gameState == GameState.PLAYER1_SETUP;
        boolean[][] grid = isPlayer1 ? player1Airplanes : player2Airplanes;
        List<AirplaneInfo> airplaneInfos = isPlayer1 ? player1AirplaneInfos : player2AirplaneInfos;

        // 放置飞机
        for (Point p : tempBodyParts) {
            grid[p.x][p.y] = true;
        }

        Point headPos = null;
        int[][] offsets = getPlaneOffsets();
        int[] headOffset = offsets[0];
        for (Point p : tempBodyParts) {
            if (p.x == tempPosition.x && p.y == tempPosition.y) {
                headPos = p;
                break;
            }
        }

        airplaneInfos.add(new AirplaneInfo(tempPosition.x, tempPosition.y, tempDirection, tempBodyParts, headPos));

        // 清除临时数据
        tempPosition = null;
        tempBodyParts = null;
        confirmButton.setEnabled(false);

        // 检查是否所有飞机都已放置
        if (airplaneInfos.size() >= airplaneCount) {
            if (isPlayer1) {
                gameState = GameState.PLAYER2_SETUP;
                hidePlayerSetup(leftPanel);
                JOptionPane.showMessageDialog(this,
                        "玩家1已完成布置，请玩家2布置飞机", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                createSetupPanel(rightPanel, false);
            } else {
                gameState = GameState.PLAYER1_ATTACK;
                hidePlayerSetup(rightPanel);
                JOptionPane.showMessageDialog(this, "玩家2已完成布置，游戏开始！",
                        "提示", JOptionPane.INFORMATION_MESSAGE);
                startGame();
            }
        }

        updateStatus();
    }

    private void initTimer() {
        startTime = System.currentTimeMillis();
        gameTimer = new Timer(1000, e -> updateTimer());
        gameTimer.start();
    }

    private void updateTimer() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        timeLabel.setText(String.format(" 时间: %02d:%02d", minutes, seconds));
    }

    private int[][] getPlaneOffsets() {
        switch (airplaneType) {
            case "机型1": return new int[][]{{0,0},{3,0},{1,0},{2,0},{1,2},{1,1},{1,-1},{1,-2},{3,1},{3,-1}};
            case "机型2": return new int[][]{{0,0},{4,0},{1,-1},{1,0},{1,1},{2,-2},{2,0},{2,2},{3,-3},{3,0},{3,3},{4,-1},{4,1}};
            case "机型3": return new int[][]{{0,0},{4,0},{1,0},{2,-2},{2,-1},{2,0},{2,1},{2,2},{3,0},{4,-1},{4,1}};
            case "机型4": return new int[][]{{0,0},{3,0},{1,-1},{1,0},{1,1},{2,-2},{2,0},{2,2},{3,-1},{3,1}};
            case "机型5": return new int[][]{{0,0},{3,0},{1,-2},{1,-1},{1,0},{1,1},{1,2},{2,0},{3,-1},{3,1}};
            case "机型6": return new int[][]{{0,0},{3,0},{1,-1},{1,0},{1,1},{2,-2},{2,0},{2,2},{3,-1},{3,1}};
            default: return new int[][]{{2,0},{1,0}};
        }
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGameItem = new JMenuItem("新游戏");
        newGameItem.addActionListener(e -> initializeGame());

        JMenuItem exitItem = new JMenuItem("主界面");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        DuelMode.this,
                        "确定返回主界面游戏吗",
                        "退出确认",
                        YES_NO_OPTION
                );

                if (choice == YES_OPTION) {
                   new MainFrame().setVisible(true);
                   DuelMode.this.dispose();
                }
            }
        });
        
        JMenuItem button1=new JMenuItem("单人模式");
        button1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		SingleMode t=new SingleMode();
        		t.initialize();
        		DuelMode.this.dispose();	
        	}
        });
        
        JMenuItem button2=new JMenuItem("双人模式");
        button2.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		DoubleMode d=new DoubleMode();
        		d.setVisible(true);
        		DuelMode.this.dispose();
        	}
        });

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        gameMenu.add(button1);
        gameMenu.add(button2);
        JMenu settingsMenu = new JMenu("设置");

        JMenu airplaneCountMenu = new JMenu("飞机数量");
        ButtonGroup countGroup = new ButtonGroup();
        JRadioButtonMenuItem count1 = new JRadioButtonMenuItem("1架", airplaneCount == 1);
        count1.addActionListener(e -> { airplaneCount = 1; initializeGame(); });
        JRadioButtonMenuItem count2 = new JRadioButtonMenuItem("2架", airplaneCount == 2);
        count2.addActionListener(e -> { airplaneCount = 2; initializeGame(); });
        countGroup.add(count1);
        countGroup.add(count2);
        airplaneCountMenu.add(count1);
        airplaneCountMenu.add(count2);

        JMenu airplaneTypeMenu = new JMenu("飞机类型");
        ButtonGroup typeGroup = new ButtonGroup();
        String[] types = {"机型1", "机型2", "机型3", "机型4", "机型5", "机型6"};
        for (String type : types) {
            JRadioButtonMenuItem typeItem = new JRadioButtonMenuItem(type, airplaneType.equals(type));
            typeItem.addActionListener(e -> { airplaneType = type; initializeGame(); });
            typeGroup.add(typeItem);
            airplaneTypeMenu.add(typeItem);
        }

        JMenu colorMenu = new JMenu("颜色设置");
        JMenuItem headColorItem = new JMenuItem("机头颜色");
        headColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "选择机头颜色", airplaneHeadColor);
            if (newColor != null) {
                airplaneHeadColor = newColor;
                updateBoardColors();
            }
        });
        JMenuItem bodyColorItem = new JMenuItem("机身颜色");
        bodyColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "选择机身颜色", airplaneBodyColor);
            if (newColor != null) {
                airplaneBodyColor = newColor;
                updateBoardColors();
            }
        });
        colorMenu.add(headColorItem);
        colorMenu.add(bodyColorItem);

        settingsMenu.add(airplaneCountMenu);
        settingsMenu.add(airplaneTypeMenu);
        settingsMenu.add(colorMenu);

        JMenu helpMenu=new JMenu("帮助");

        JMenuItem rulesItem = new JMenuItem("游戏规则");
        Rules r=new Rules();
        rulesItem.addActionListener(e -> r.showDuRules());
        //这个也是后续添加的
        JMenuItem planeType = new JMenuItem("飞机样式");
        planeType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //创建显示图片的窗口
                JFrame imageFrame=new JFrame("飞机样式");
                imageFrame.setSize(600,400);
                imageFrame.setLocationRelativeTo(null);

                try {
                    // 添加图片
                    BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/com/frame/PlaneType.png"));
                    JLabel imageLabel = new JLabel(new ImageIcon(image));
                    imageLabel.setHorizontalAlignment(JLabel.CENTER);

                    // 添加滚动功能，因为图片很大
                    JScrollPane scrollPane = new JScrollPane(imageLabel);
                    imageFrame.add(scrollPane);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    imageFrame.add(new JLabel("无法加载图片: " + ex.getMessage(), JLabel.CENTER));
                }

                imageFrame.setVisible(true);
            }
        });

        helpMenu.add(rulesItem);
        helpMenu.add(planeType);

        menuBar.add(gameMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
        setLocationRelativeTo(null);
    }

    void initializeGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }

        gameState = GameState.PLAYER1_SETUP;
        player1Found = 0;
        player2Found = 0;
        player1Clicks = 0;
        player2Clicks = 0;

        player1Airplanes = new boolean[GRID_SIZE][GRID_SIZE];
        player2Airplanes = new boolean[GRID_SIZE][GRID_SIZE];
        player1Hits = new boolean[GRID_SIZE][GRID_SIZE];
        player2Hits = new boolean[GRID_SIZE][GRID_SIZE];
        player1Revealed = new boolean[GRID_SIZE][GRID_SIZE];
        player2Revealed = new boolean[GRID_SIZE][GRID_SIZE];

        player1AirplaneInfos = new ArrayList<>();
        player2AirplaneInfos = new ArrayList<>();

        tempPosition = null;
        tempBodyParts = null;
        confirmButton.setEnabled(false);

        leftPanel.removeAll();
        rightPanel.removeAll();
        controlPanel.removeAll();

        // 初始化布置面板
        createSetupPanel(leftPanel, true);
        createSetupPanel(rightPanel, false);

        // 重新添加控制组件
        JPanel setupPanel = new JPanel();
        setupPanel.add(new JLabel("方向:"));
        setupPanel.add(directionComboBox);
        setupPanel.add(confirmButton);

        controlPanel.add(setupPanel);
        updateStatus();
        validate();
        repaint();
    }

    private void createSetupPanel(JPanel panel, boolean isPlayer1) {
        panel.removeAll();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                button.setBackground(Color.WHITE);

                final int row = i;
                final int col = j;

                button.addActionListener(e -> {
                    if ((isPlayer1 && gameState == GameState.PLAYER1_SETUP) ||
                            (!isPlayer1 && gameState == GameState.PLAYER2_SETUP)) {

                        // 尝试预览飞机
                        previewAirplane(panel, isPlayer1, row, col);
                    }
                });

                panel.add(button);
            }
        }
    }

    private void previewAirplane(JPanel panel, boolean isPlayer1, int x, int y) {
        boolean[][] grid = isPlayer1 ? player1Airplanes : player2Airplanes;
        List<AirplaneInfo> airplaneInfos = isPlayer1 ? player1AirplaneInfos : player2AirplaneInfos;

        // 如果已经放置了足够数量的飞机，则不允许再放置
        if (airplaneInfos.size() >= airplaneCount) {
            return;
        }

        int direction = directionComboBox.getSelectedIndex();
        int[][] offsets = getPlaneOffsets();
        List<Point> positions = new ArrayList<>();
        boolean canPlace = true;

        for (int[] offset : offsets) {
            int dx = offset[0];
            int dy = offset[1];
            int newX = x, newY = y;

            switch (direction) {
                case 0: newX += dx; newY += dy; break;
                case 1: newX += dy; newY -= dx; break;
                case 2: newX -= dx; newY -= dy; break;
                case 3: newX -= dy; newY += dx; break;
            }

            if (newX < 0 || newX >= GRID_SIZE || newY < 0 || newY >= GRID_SIZE || grid[newX][newY]) {
                canPlace = false;
                break;
            }

            positions.add(new Point(newX, newY));
        }

        if (canPlace) {
            tempPosition = new Point(x, y);
            tempDirection = direction;
            tempBodyParts = positions;
            confirmButton.setEnabled(true);

            // 更新预览
            updatePreview(panel, positions);
        } else {
            JOptionPane.showMessageDialog(this,
                    "无法在此位置放置飞机，请尝试其他位置或方向", "提示",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updatePreview(JPanel panel, List<Point> positions) {
        Component[] components = panel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JButton) {
                JButton button = (JButton) components[i];
                int row = i / GRID_SIZE;
                int col = i % GRID_SIZE;

                boolean isPart = false;
                boolean isHead = false;

                for (Point p : positions) {
                    if (p.x == row && p.y == col) {
                        isPart = true;
                        if (p.x == tempPosition.x && p.y == tempPosition.y) {
                            isHead = true;
                        }
                        break;
                    }
                }

                if (isPart) {
                    button.setBackground(isHead ? airplaneHeadColor : airplaneBodyColor);
                } else {
                    button.setBackground(Color.WHITE);
                }
            }
        }
    }

    private void hidePlayerSetup(JPanel panel) {
        panel.removeAll();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                button.setBackground(Color.WHITE);
                panel.add(button);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void startGame() {
        // 初始化攻击面板
        createAttackPanel(leftPanel, player2Airplanes, player2Hits, player2AirplaneInfos, true);
        createAttackPanel(rightPanel, player1Airplanes, player1Hits, player1AirplaneInfos, false);

        // 开始计时
        initTimer();
    }

    private void createAttackPanel(JPanel panel, boolean[][] airplanes, boolean[][] hits,
                                   List<AirplaneInfo> airplaneInfos, boolean isPlayer1Attacking) {
        panel.removeAll();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));

                if (hits[i][j] || (isPlayer1Attacking ? player2Revealed[i][j] : player1Revealed[i][j])) {
                    if (airplanes[i][j]) {
                        boolean isHead = false;
                        for (AirplaneInfo info : airplaneInfos) {
                            if (info.head.x == i && info.head.y == j) {
                                isHead = true;
                                break;
                            }
                        }
                        button.setBackground(isHead ? airplaneHeadColor : airplaneBodyColor);
                    } else {
                        button.setBackground(Color.GRAY);
                    }
                } else {
                    button.setBackground(Color.WHITE);
                }

                final int row = i;
                final int col = j;

                button.addActionListener(e -> {
                    if (gameState == GameState.PLAYER1_ATTACK && isPlayer1Attacking ||
                            gameState == GameState.PLAYER2_ATTACK && !isPlayer1Attacking) {

                        if (!hits[row][col]) {
                            // 增加点击计数
                            if (isPlayer1Attacking) {
                                player1Clicks++;
                            } else {
                                player2Clicks++;
                            }

                            hits[row][col] = true;

                            if (airplanes[row][col]) {
                                boolean isHead = false;
                                AirplaneInfo hitAirplane = null;
                                for (AirplaneInfo info : airplaneInfos) {
                                    if (info.head.x == row && info.head.y == col) {
                                        isHead = true;
                                        hitAirplane = info;
                                        break;
                                    }
                                }

                                if (isHead) {
                                    button.setBackground(airplaneHeadColor);
                                    if (isPlayer1Attacking) {
                                        player1Found++;
                                        for (Point p : hitAirplane.bodyParts) {
                                            player2Revealed[p.x][p.y] = true;
                                        }
                                    } else {
                                        player2Found++;
                                        for (Point p : hitAirplane.bodyParts) {
                                            player1Revealed[p.x][p.y] = true;
                                        }
                                    }

                                    // 重新创建面板以显示所有被击中的部分
                                    if (isPlayer1Attacking) {
                                        createAttackPanel(leftPanel, player2Airplanes, player2Hits, player2AirplaneInfos, true);
                                    } else {
                                        createAttackPanel(rightPanel, player1Airplanes, player1Hits, player1AirplaneInfos, false);
                                    }

                                    // 检查游戏是否结束
                                    if (player1Found >= airplaneCount || player2Found >= airplaneCount) {
                                        gameState = GameState.GAME_OVER;
                                        revealAllAirplanes();
                                        checkGameOver();
                                    }
                                } else {
                                    button.setBackground(airplaneBodyColor);
                                }
                            } else {
                                button.setBackground(Color.GRAY);
                            }

                            // 切换玩家回合
                            if (gameState != GameState.GAME_OVER) {
                                gameState = isPlayer1Attacking ? GameState.PLAYER2_ATTACK : GameState.PLAYER1_ATTACK;
                                updateStatus();
                            }
                        }
                    }
                });

                panel.add(button);
            }
        }
    }

    private void revealAllAirplanes() {
    	//显示最终的时间
   	    updateTimer();
        gameTimer.stop();
        for (AirplaneInfo info : player1AirplaneInfos) {
            for (Point p : info.bodyParts) {
                player1Revealed[p.x][p.y] = true;
            }
        }
        for (AirplaneInfo info : player2AirplaneInfos) {
            for (Point p : info.bodyParts) {
                player2Revealed[p.x][p.y] = true;
            }
        }

        createAttackPanel(leftPanel, player2Airplanes, player2Hits, player2AirplaneInfos, true);
        createAttackPanel(rightPanel, player1Airplanes, player1Hits, player1AirplaneInfos, false);
    }

    private void checkGameOver() {
        String winner = player1Found >= airplaneCount ? "玩家1" : "玩家2";
        String message =
                String.format("<html><b>%s获胜!</b><br>游戏时间: %s<br>点击次数: 玩家1(%d) 玩家2(%d)</html>",
                        winner,
                        timeLabel.getText().substring(3),
                        player1Clicks,
                        player2Clicks);
        JOptionPane.showMessageDialog(this, message, "游戏结束",
                JOptionPane.INFORMATION_MESSAGE);
        updateStatus();
    }

    private void updateStatus() {
        // 清除旧的状态组件
        for (Component comp : controlPanel.getComponents()) {
            if (comp instanceof JLabel || comp instanceof JButton) {
                if (comp != confirmButton && comp != directionComboBox) {
                    controlPanel.remove(comp);
                }
            }
        }

        String statusText = "";
        switch (gameState) {
            case PLAYER1_SETUP:
                statusText = "玩家1正在布置飞机 (" + player1AirplaneInfos.size() + "/" + airplaneCount + ")";
                break;
            case PLAYER2_SETUP:
                statusText = "玩家2正在布置飞机 (" + player2AirplaneInfos.size() + "/" + airplaneCount + ")";
                break;
            case PLAYER1_ATTACK:
                statusText = "玩家1的回合 - 攻击玩家2的区域";
                break;
            case PLAYER2_ATTACK:
                statusText = "玩家2的回合 - 攻击玩家1的区域";
                break;
            case GAME_OVER:
                statusText = "游戏结束 - " + (player1Found >= airplaneCount ? "玩家1获胜!" : "玩家2获胜!");
                break;
        }

        JLabel statusLabel = new JLabel(statusText);
        controlPanel.add(statusLabel);

        JLabel scoreLabel = new JLabel(" 找到: 玩家1(" + player1Found + "/" + airplaneCount + ") 玩家2(" + player2Found + "/" + airplaneCount + ")");
        controlPanel.add(scoreLabel);

        clicksLabel = new JLabel(" 点击: 玩家1(" + player1Clicks + ") 玩家2(" + player2Clicks + ")");
        controlPanel.add(clicksLabel);

        timeLabel = new JLabel(" 时间: 00:00");
        controlPanel.add(timeLabel);

        JButton newGameButton = new JButton("新游戏");
        newGameButton.addActionListener(e -> initializeGame());
        controlPanel.add(newGameButton);

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void updateBoardColors() {
        Component[] leftComponents = leftPanel.getComponents();
        for (int i = 0; i < leftComponents.length; i++) {
            if (leftComponents[i] instanceof JButton) {
                JButton button = (JButton) leftComponents[i];
                if (button.getBackground() == airplaneHeadColor ||
                        button.getBackground() == airplaneBodyColor) {

                    int row = i / GRID_SIZE;
                    int col = i % GRID_SIZE;

                    boolean isHead = false;
                    for (AirplaneInfo info : player2AirplaneInfos) {
                        if (info.head.x == row && info.head.y == col) {
                            isHead = true;
                            break;
                        }
                    }

                    button.setBackground(isHead ? airplaneHeadColor : airplaneBodyColor);
                }
            }
        }

        Component[] rightComponents = rightPanel.getComponents();
        for (int i = 0; i < rightComponents.length; i++) {
            if (rightComponents[i] instanceof JButton) {
                JButton button = (JButton) rightComponents[i];
                if (button.getBackground() == airplaneHeadColor ||
                        button.getBackground() == airplaneBodyColor) {

                    int row = i / GRID_SIZE;
                    int col = i % GRID_SIZE;

                    boolean isHead = false;
                    for (AirplaneInfo info : player1AirplaneInfos) {
                        if (info.head.x == row && info.head.y == col) {
                            isHead = true;
                            break;
                        }
                    }

                    button.setBackground(isHead ? airplaneHeadColor : airplaneBodyColor);
                }
            }
        }
    }
}
