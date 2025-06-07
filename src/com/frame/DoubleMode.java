package com.frame;

import javax.imageio.ImageIO;
import javax.swing.*;

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DoubleMode extends JFrame {
    //这里定义静态的final的变量有利于对窗口大小更好的管控
    private static final int GRID_SIZE = 10;//网格的行数和列数
    private static final int CELL_SIZE = 50;//每个单元格的宽度和高度
    private static final int PANEL_WIDTH = GRID_SIZE * CELL_SIZE;//网格面板的总宽度
    private static final int PANEL_HEIGHT = GRID_SIZE * CELL_SIZE;//网格面板的总高度

    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel controlPanel;
    private JMenuBar menuBar;

    private int airplaneCount = 1;
    private Color airplaneHeadColor = new Color(255, 0, 0, 199);
    private Color airplaneBodyColor = new Color(0, 105, 180, 255);
    private String airplaneType = "机型1";

    // 游戏数据
    private boolean[][] player1Airplanes;
    private boolean[][] player2Airplanes;
    private boolean[][] player1Hits;
    private boolean[][] player2Hits;
    private boolean[][] player1Revealed;
    private boolean[][] player2Revealed;

    private boolean player1Turn = true;
    private boolean gameOver = false;
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

    //用于存储与飞机（或类似对象）相关的信息。
    private class AirplaneInfo {
        int x, y;//表示飞机当前位置的坐标
        int direction;//表示飞机当前的方向
        List<Point> bodyParts;//是一个Point类型的列表，存储飞机的所有“身体部分的坐标
        Point head;

        //构造方法
        AirplaneInfo(int x, int y, int direction, List<Point> bodyParts, Point head) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.bodyParts = bodyParts;
            this.head = head;
        }
    }

    public DoubleMode() {
        setTitle("双人模式");
        setSize(PANEL_WIDTH * 2 + 100, PANEL_HEIGHT + 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        //设置图片
        setIconImage(
                Toolkit.getDefaultToolkit().getImage(DoubleMode.class.getResource("logo.jpg"))
        );

        createMenuBar();

        leftPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        rightPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        controlPanel = new JPanel();

        initialize();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
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

    //飞机模型
    private int[][] getPlaneOffsets() {
        switch (airplaneType) {
            case "机型1":
                return new int[][]{{0, 0}, {3, 0}, {1, 0}, {2, 0}, {1, 2}, {1, 1}, {1, -1}, {1, -2}, {3, 1}, {3, -1}};
            case "机型2":
                return new int[][]{{0, 0}, {4, 0}, {1, -1}, {1, 0}, {1, 1}, {2, -2}, {2, 0}, {2, 2}, {3, -3}, {3, 0}, {3, 3}, {4, -1}, {4, 1}};
            case "机型3":
                return new int[][]{{0, 0}, {4, 0}, {1, 0}, {2, -2}, {2, -1}, {2, 0}, {2, 1}, {2, 2}, {3, 0}, {4, -1}, {4, 1}};
            case "机型4":
                return new int[][]{{0, 0}, {3, 0}, {1, -1}, {1, 0}, {1, 1}, {2, -2}, {2, 0}, {2, 2}, {3, -1}, {3, 1}};
            case "机型5":
                return new int[][]{{0, 0}, {3, 0}, {1, -2}, {1, -1}, {1, 0}, {1, 1}, {1, 2}, {2, 0}, {3, -1}, {3, 1}};
            case "机型6":
                return new int[][]{{0, 0}, {3, 0}, {1, -1}, {1, 0}, {1, 1}, {2, -2}, {2, 0}, {2, 2}, {3, -1}, {3, 1}};
            default:
                return new int[][]{{2, 0}, {1, 0}};
        }
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGameItem = new JMenuItem("新游戏");
        newGameItem.addActionListener(e -> initialize());

        JMenuItem exitItem = new JMenuItem("主界面");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        DoubleMode.this,
                        "确定返回主界面吗",
                        "退出确认",
                        YES_NO_OPTION
                );

                if (choice == YES_OPTION) {
                    new MainFrame().setVisible(true);
                    DoubleMode.this.dispose();
                }
            }
        });
        //后面添加用来实现页面跳转的
        JMenuItem button1 = new JMenuItem("单人模式");
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SingleMode t = new SingleMode();
                t.initialize();
                DoubleMode.this.dispose();
            }
        });

        JMenuItem button2 = new JMenuItem("对抗模式");
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DuelMode d = new DuelMode();
                d.setVisible(true);
                DoubleMode.this.dispose();
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
        count1.addActionListener(e -> {
            airplaneCount = 1;
            initialize();
        });

        JRadioButtonMenuItem count2 = new JRadioButtonMenuItem("2架", airplaneCount == 2);
        count2.addActionListener(e -> {
            airplaneCount = 2;
            initialize();
        });

        countGroup.add(count1);
        countGroup.add(count2);

        airplaneCountMenu.add(count1);
        airplaneCountMenu.add(count2);

        JMenu airplaneTypeMenu = new JMenu("飞机类型");
        ButtonGroup typeGroup = new ButtonGroup();
        //创建一个包含多个飞机机型选项的单选按钮菜单
        String[] types = {"机型1", "机型2", "机型3", "机型4", "机型5", "机型6"};
        for (String type : types) {
            JRadioButtonMenuItem typeItem = new JRadioButtonMenuItem(type, airplaneType.equals(type));
            typeItem.addActionListener(e -> {
                airplaneType = type;
                initialize();
            });
            typeGroup.add(typeItem);
            airplaneTypeMenu.add(typeItem);
        }

        JMenu colorMenu = new JMenu("颜色设置");
        //机头的颜色
        JMenuItem headColorItem = new JMenuItem("机头颜色");
        headColorItem.addActionListener(e -> {
            /*JcolorChooser是java Swing原生提供的，无需额外配置的颜色选择器*/
            Color newColor = JColorChooser.showDialog(this, "选择机头颜色", airplaneHeadColor);
            if (newColor != null) {
                airplaneHeadColor = newColor;
                updateBoardColors();
            }
        });
        //机身的颜色
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

        JMenu helpMenu = new JMenu("帮助");

        JMenuItem rulesItem = new JMenuItem("游戏规则");
        Rules r = new Rules();
        rulesItem.addActionListener(e -> r.showDRule());
        //这个也是后续添加的
        JMenuItem planeType = new JMenuItem("飞机样式");
        planeType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //创建显示图片的窗口
                JFrame imageFrame = new JFrame("飞机样式");
                imageFrame.setSize(600, 400);
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

        setJMenuBar(menuBar);//将菜单栏设置到窗口
        setLocationRelativeTo(null);
    }

    void initialize() {
        //停止当前游戏的计时器
        if (gameTimer != null) {
            gameTimer.stop();
        }
        //重置游戏状态变量
        gameOver = false;//标记游戏是否结束
        player1Turn = true;//玩家1
        player1Found = 0;//击中飞机数量
        player2Found = 0;//击中飞机数量
        player1Clicks = 0;//记录点击的次数
        player2Clicks = 0;//记录点击的次数

        //初始化游戏网格数组

        //表示飞机的部件
        player1Airplanes = new boolean[GRID_SIZE][GRID_SIZE];
        player2Airplanes = new boolean[GRID_SIZE][GRID_SIZE];
        //用于显示击中效果
        player1Hits = new boolean[GRID_SIZE][GRID_SIZE];
        player2Hits = new boolean[GRID_SIZE][GRID_SIZE];
        //用于标记哪些格子被揭示
        player1Revealed = new boolean[GRID_SIZE][GRID_SIZE];
        player2Revealed = new boolean[GRID_SIZE][GRID_SIZE];

        //用于记录飞机的坐标，方向等信息
        player1AirplaneInfos = new ArrayList<>();
        player2AirplaneInfos = new ArrayList<>();

        //清理面板的内容，一处左右面板和控制面板的中所有组件，为重新布局做准备
        leftPanel.removeAll();
        rightPanel.removeAll();
        controlPanel.removeAll();

        //放置飞机
        placeAirplanes(player1Airplanes, player1AirplaneInfos);
        placeAirplanes(player2Airplanes, player2AirplaneInfos);

        //创建攻击面板
        //显示玩家1攻击玩家2的界面（参数 true 表示当前玩家是玩家1）
        createAttackPanel(leftPanel, player2Airplanes, player2Hits, player2AirplaneInfos, true);
        //显示玩家2攻击玩家1的界面（参数 false 表示当前玩家2）
        createAttackPanel(rightPanel, player1Airplanes, player1Hits, player1AirplaneInfos, false);

        //启动游戏计时器
        initTimer();
        //更新界面状态
        updateStatus();

        validate();//重新计算面板布局（因之前调用了 removeAll）
        repaint();//强制重绘界面，确保所有更改可见。
    }


    private void createAttackPanel(JPanel panel, boolean[][] airplanes, boolean[][] hits,
                                   List<AirplaneInfo> airplaneInfos, boolean isPlayer1Attacking) {
        panel.removeAll();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));//为每个格子创建按钮，并设置大小

                //检查格子是否被发现或者飞机被发现
                //如果有飞机：检查是否是飞机头部，设置相应颜色
                //没有飞机：设置为灰色（未命中）
                //否则，设置为白色（未点击）
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

                //检查游戏是否结束、判断是否是当前正确玩家的回合、该各自是否被点击
                button.addActionListener(e -> {
                    if (!gameOver && ((isPlayer1Attacking && player1Turn) || (!isPlayer1Attacking && !player1Turn))
                            && !hits[row][col]) {

                        // 增加点击计数
                        if (isPlayer1Attacking) {
                            player1Clicks++;
                        } else {
                            player2Clicks++;
                        }

                        hits[row][col] = true;

                        //检查点击的格子是否有飞机
                        //如果是飞机，检查是否是头部
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

                                //当玩家击中飞机头部时，重新渲染攻击面板，显示被击中的飞机状态
                                if (isPlayer1Attacking) {
                                    createAttackPanel(leftPanel, player2Airplanes, player2Hits, player2AirplaneInfos, true);
                                } else {
                                    createAttackPanel(rightPanel, player1Airplanes, player1Hits, player1AirplaneInfos, false);
                                }

                                player1Turn = !player1Turn;
                                //更新游戏状态显示
                                updateStatus();

                                //检查游戏是否结束
                                if (player1Found >= airplaneCount || player2Found >= airplaneCount) {
                                    revealAllAirplanes();
                                    checkGameOver();
                                }
                            } else {
                                //命中了显示对应的颜色
                                button.setBackground(airplaneBodyColor);
                            }
                        } else {
                            //未命中，设置灰色并切换玩家
                            button.setBackground(Color.GRAY);
                            player1Turn = !player1Turn;
                            //更新游戏状态显示
                            updateStatus();
                        }
                    }
                });

                panel.add(button);
            }
        }
    }

    //显示所有飞机
    private void revealAllAirplanes() {
        //显示最终的时间
        updateTimer();
        //计时停止
        gameTimer.stop();
        //揭示玩家1的所有飞机
        for (AirplaneInfo info : player1AirplaneInfos) {
            for (Point p : info.bodyParts) {
                player1Revealed[p.x][p.y] = true;
            }
        }
        //揭示玩家2的所有飞机
        for (AirplaneInfo info : player2AirplaneInfos) {
            for (Point p : info.bodyParts) {
                player2Revealed[p.x][p.y] = true;
            }
        }
        //重新绘制攻击面板（显示所有飞机）
        createAttackPanel(leftPanel, player2Airplanes, player2Hits, player2AirplaneInfos, true);
        createAttackPanel(rightPanel, player1Airplanes, player1Hits, player1AirplaneInfos, false);
    }

    //随机选一个位置和方向
    //计算飞机每个部件的实际坐标（考虑旋转）
    //检查所有坐标是否合法（不越界、不重叠）
    //如果全部通过，放置并记录飞机信息
    private void placeAirplanes(boolean[][] grid, List<AirplaneInfo> airplaneInfos) {
        Random random = new Random();
        int[][] offsets = getPlaneOffsets();//获取飞机的形状
        int[] headOffset = offsets[0];//飞机的头部

        //放置每架飞机
        for (int count = 0; count < airplaneCount; count++) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 1000) {
                attempts++;
                int x = random.nextInt(GRID_SIZE);
                int y = random.nextInt(GRID_SIZE);
                int direction = random.nextInt(4);

                boolean canPlace = true;
                List<Point> positions = new ArrayList<>();//记录实际坐标
                Point headPos = null;//存储头部坐标

                //假设飞机的基准点是（5，5)
                //飞机部件偏移是（1，0）
                //x是向右增长，y是向下增长
                for (int[] offset : offsets) {
                    int dx = offset[0];//获取当前X的偏移量
                    int dy = offset[1];//获取当前y的偏移量
                    int newX = x, newY = y;//从飞机的基准点（x,y)开始计算

                    switch (direction) {
                        case 0:
                            newX += dx;
                            newY += dy;
                            break;//默认方向
                        case 1:
                            newX += dy;
                            newY -= dx;
                            break;//旋转90度
                        case 2:
                            newX -= dx;
                            newY -= dy;
                            break;//旋转180度
                        case 3:
                            newX -= dy;
                            newY += dx;
                            break;//旋转270度
                    }

                    //判断超出网格左界，超出网格右界，判断该位置是否飞机被占用
                    if (newX < 0 || newX >= GRID_SIZE || newY < 0 || newY >= GRID_SIZE || grid[newX][newY]) {
                        canPlace = false;//发现越界就停止检查
                        break;
                    }

                    Point p = new Point(newX, newY);
                    positions.add(p);

                    if (offset == headOffset) {
                        headPos = p;//如果是头部就单独记录
                    }
                }

                //如果上面的要求全部实现，就放置飞机
                if (canPlace && headPos != null) {
                    for (Point p : positions) {
                        grid[p.x][p.y] = true;
                    }
                    airplaneInfos.add(new AirplaneInfo(x, y, direction, positions, headPos));
                    placed = true;
                }
            }
        }
    }

    //检查游戏是否结束
    private void checkGameOver() {
        if (player1Found >= airplaneCount || player2Found >= airplaneCount) {
            gameOver = true;
            String winner = player1Found >= airplaneCount ? "玩家1" : "玩家2";
            String message = String.format("<html><b>%s获胜!</b><br>游戏时间: %s<br>点击次数: 玩家1(%d) 玩家2(%d)</html>",
                    winner,
                    timeLabel.getText().substring(3),
                    player1Clicks,
                    player2Clicks);
            JOptionPane.showMessageDialog(this, message, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
            updateStatus();
        }
    }

    //更新状态
    private void updateStatus() {
        controlPanel.removeAll();//清除面板

        String turnInfo = player1Turn ? "玩家1的回合" : "玩家2的回合";
        if (gameOver) {
            turnInfo = "游戏结束 - " + (player1Found >= airplaneCount ? "玩家1获胜!" : "玩家2获胜!");
        }

        JLabel statusLabel = new JLabel(turnInfo);
        controlPanel.add(statusLabel);

        JLabel scoreLabel = new JLabel("  找到: 玩家1(" + player1Found + "/" + airplaneCount + ") 玩家2(" + player2Found + "/" + airplaneCount + ")");
        controlPanel.add(scoreLabel);

        clicksLabel = new JLabel("  点击: 玩家1(" + player1Clicks + ") 玩家2(" + player2Clicks + ")");
        controlPanel.add(clicksLabel);

        timeLabel = new JLabel(" 时间: 00:00");
        controlPanel.add(timeLabel);

        JButton newGameButton = new JButton("新游戏");
        newGameButton.addActionListener(e -> initialize());
        controlPanel.add(newGameButton);

        controlPanel.revalidate();//重新计算布局
        controlPanel.repaint();//重绘组件
    }

    //遍历面板按钮-->过滤掉飞机部件-->定位网格-->判断是机身还是头部-->跟新颜色
    private void updateBoardColors() {
        //实现做面板颜色正确显示
        Component[] leftComponents = leftPanel.getComponents();//获取面板的所有组件
        //遍历所有组件
        for (int i = 0; i < leftComponents.length; i++) {
            if (leftComponents[i] instanceof JButton)/*过滤掉不是按钮的组件*/ {
                JButton button = (JButton) leftComponents[i];
                //只处理飞机头和机身的颜色
                if (button.getBackground() == airplaneHeadColor ||
                        button.getBackground() == airplaneBodyColor) {

                    //计算按钮对应的网格坐标
                    int row = i / GRID_SIZE;//行
                    int col = i % GRID_SIZE;//列

                    boolean isHead = false;//判断是否是飞机头部
                    for (AirplaneInfo info : player2AirplaneInfos) {
                        if (info.head.x == row && info.head.y == col) {
                            isHead = true;
                            break;
                        }
                    }

                    //若 isHead 为 true，使用 airplaneHeadColor（如红色）
                    //否则使用 airplaneBodyColor（如蓝色）
                    button.setBackground(isHead ? airplaneHeadColor : airplaneBodyColor);
                }
            }
        }

        //实现右面板颜色正确实现
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
