package com.frame;

import javax.imageio.ImageIO;//处理图像的输入和输出
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;//表示图像数据
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;//检查异常

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public class SingleMode extends JFrame {
    // 游戏状态变量
    private JButton[][] gridButtons;
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JLabel attemptsLabel;
    private int gridSize = 10;
    private List<Plane> planes = new ArrayList<>();
    private int attempts = 0;
    private Timer gameTimer;
    private int secondsElapsed = 0;
    private boolean gameOver = false;
    private int targetPlaneCount = 1;
    private int foundPlanes = 0;

    // 飞机形状配置
    private String[] planeShapes = {"机型1","机型2","机型3","机型4","机型5","机型6"};
    private String currentShape = "机型1";
    private Color headColor = new Color(255, 0, 0, 199); // 飞机头部颜色
    private Color bodyColor = new Color(0, 105, 180, 255); // 飞机身体颜色
    private Color missColor = new Color(255, 255, 255); // 未击中颜色

    // 游戏统计
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int bestTime = Integer.MAX_VALUE;
    private int bestAttempts = Integer.MAX_VALUE;

    // 飞机类，存储飞机信息和方向
    private class Plane {
        int headRow;
        int headCol;
        int direction; // 0:上, 1:右, 2:下, 3:左

        Plane(int headRow, int headCol, int direction) {
            this.headRow = headRow;
            this.headCol = headCol;
            this.direction = direction;
        }
    }

     void initialize() {
        createMainWindow();
        setupMenuBar();
        setupGamePanel();
        startNewGame();
        setVisible(true);
    }

    private void createMainWindow() {
        setTitle("单人模式");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 750);
        setLocationRelativeTo(null);
        setBackground(missColor);
        setIconImage(
                Toolkit.getDefaultToolkit().getImage(SingleMode.class.getResource("logo.jpg"))
        );
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 游戏菜单
        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGameItem = new JMenuItem("新游戏 (N)");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newGameItem.addActionListener(e -> startNewGame());

        //实现页面跳转
        JMenuItem exitItem = new JMenuItem("主界面");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        SingleMode.this,
                        "确定返回主界面吗",
                        "退出确认",
                        YES_NO_OPTION
                );

                if (choice == YES_OPTION) {
                   new MainFrame().setVisible(true);
                   SingleMode.this.dispose();
                }
            }
        });

        JMenuItem Dmbutton=new JMenuItem("双人模式");
        Dmbutton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		DoubleMode t=new DoubleMode();
        		t.setVisible(true);
        		SingleMode.this.dispose();
        	}
        });
        
        JMenuItem Dumbutton=new JMenuItem("对抗模式");
        Dumbutton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		DuelMode d=new DuelMode();
        		d.setVisible(true);
        		SingleMode.this.dispose();
        	}
        });
        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        gameMenu.add(Dmbutton);
        gameMenu.add(Dumbutton);

        // 选项菜单
        JMenu optionsMenu = new JMenu("选项");
        JMenuItem appearanceItem = new JMenuItem("更改外观");
        appearanceItem.addActionListener(e -> changeAppearance());

        JMenuItem planeCountItem = new JMenuItem("设置飞机数量");
        planeCountItem.addActionListener(e -> setPlaneCount());

        optionsMenu.add(appearanceItem);
        optionsMenu.add(planeCountItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem rulesItem = new JMenuItem("游戏规则");
        Rules r=new Rules();
        rulesItem.addActionListener(e ->r.showSRules());

        JMenuItem statsItem = new JMenuItem("游戏统计");
        statsItem.addActionListener(e -> showStatistics());
        
        JMenuItem planeType = new JMenuItem("飞机类型");
        planeType.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		//创建显示图片的窗口
        		JFrame imageFrame=new JFrame("飞机类型");
        		imageFrame.setSize(600,400);
        		imageFrame.setLocationRelativeTo(null);
        		
        		 try {
        	            // 从资源加载图片
        	            BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/com/frame/PlaneType.png"));
        	            JLabel imageLabel = new JLabel(new ImageIcon(image));
        	            imageLabel.setHorizontalAlignment(JLabel.CENTER);
        	            
        	            // 添加滚动功能（如果图片很大）
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
        helpMenu.add(statsItem);
        helpMenu.add(planeType);

        menuBar.add(gameMenu);
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void setupGamePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 状态面板
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        statusLabel = new JLabel("点击方格开始游戏");
        timeLabel = new JLabel("时间: 0秒");
        attemptsLabel = new JLabel("尝试: 0次");

        statusPanel.add(statusLabel);
        statusPanel.add(timeLabel);
        statusPanel.add(attemptsLabel);

        // 网格面板
        JPanel gridPanel = new JPanel(new GridLayout(gridSize, gridSize, 2, 2));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        gridButtons = new JButton[gridSize][gridSize];

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                gridButtons[row][col] = createGridButton(row, col);
                gridPanel.add(gridButtons[row][col]);
            }
        }

        // 控制面板
        JPanel controlPanel = createControlPanel();

        // 组装主界面
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    //创建网格按钮
    private JButton createGridButton(int row, int col) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(50,50));
        button.setFont(new Font("Arial", Font.BOLD, 14));//字体加粗
        button.setFocusPainted(false);//禁用焦点时的绘制效果
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(e -> handleGridClick(row, col));

        // 添加鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            //鼠标进入按钮区域时
            public void mouseEntered(MouseEvent e) {
                if (!gameOver && button.isEnabled()) {//若游戏未结束，则处于启用状态
                    button.setBackground(new Color(220, 220, 255));//更改按钮背景颜色
                }
            }

            @Override
            //当鼠标离开按钮区域
            public void mouseExited(MouseEvent e) {
                if (!gameOver && button.getText().isEmpty()) {//若按钮区域不属于机身
                    button.setBackground(null);//按钮背景颜色恢复为默认值
                }
            }
        });

        // 添加鼠标点击监听器
        button.addActionListener(e -> {
            if (!gameOver) {
                // 点击时启动计时器（如果未启动）
                if (gameTimer == null ||!gameTimer.isRunning()) {
                    startTimer();
                }
                handleGridClick(row, col);
            }
        });

        return button;
    }

    //用于创建并启动游戏计时器
    private void startTimer() {
        gameTimer = new Timer(1000, e -> {
            secondsElapsed++;
            timeLabel.setText("时间: " + secondsElapsed + "秒");
        });
        gameTimer.start();
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 飞机形状选择
        JPanel shapePanel = new JPanel();
        shapePanel.setAlignmentX(Component.CENTER_ALIGNMENT);//在水平上居中对齐
        JLabel shapeLabel = new JLabel("飞机形状:");
        JComboBox<String> shapeCombo = new JComboBox<>(planeShapes);//创建下拉框，选项来自plane Shape数组，该数组包含了所有可选的飞机形状
        shapeCombo.setSelectedItem(currentShape);//将下拉框的当前选中项设置为currentShape，即当前选择的飞机形状
        shapeCombo.addActionListener(e -> {//添加一个动作监听器，当用户选择不同的飞机形状，会执行监听器中的代码
            currentShape = (String) shapeCombo.getSelectedItem();//current Shape更新为用户新选择的形状
            startNewGame();//重新开始游戏
        });

        shapePanel.add(shapeLabel);
        shapePanel.add(Box.createHorizontalStrut(10));
        shapePanel.add(shapeCombo);

        // 组装控制面板
        controlPanel.add(shapePanel);
        controlPanel.add(Box.createVerticalStrut(10));
        return controlPanel;
    }

    //开始新游戏
    private void startNewGame() {
        gamesPlayed++;//记录游戏进行次数
        gameOver = false;//表示新游戏开始
        attempts = 0;//步数归零
        secondsElapsed = 0;//已用次数归零
        foundPlanes = 0;//已找到飞机数归零
        planes.clear();//清空飞机信息的收集

        // 重置计时器
        if (gameTimer != null) {
            gameTimer.stop();//停止计时器
        }

        gameTimer = null;

        // 重置网格
        resetGrid();

        // 放置新飞机
        placePlanes();

        // 更新状态
        statusLabel.setText("寻找 " + targetPlaneCount + " 架飞机 (已找到 0)");
        attemptsLabel.setText("尝试: 0次");
    }

    //重置网格
    private void resetGrid() {
        //遍历清空
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                JButton button = gridButtons[row][col];
                button.setText("");
                button.setBackground(null);
                button.setEnabled(true);
            }
        }
    }

    //实现在网格中随机放置指定数量(targetPlaneCount)的飞机
    private void placePlanes() {
        Random random = new Random();//随机数生成器初始化
        //循环放置飞机
        for (int i = 0; i < targetPlaneCount; i++) {
            int headRow, headCol, direction;//飞机头行坐标、列坐标、方向
            boolean validPosition;//位置是否有效

            // 尝试最多100次找到有效位置
            int attempts = 0;
            do {
                headRow = random.nextInt(gridSize);//生成0~gridSize-1之间的随机数作为行坐标
                headCol = random.nextInt(gridSize);//生成0~gridSize-1之间的随机数作为列坐标
                direction = random.nextInt(4); // 随机方向（0~3代表上下左右四个方向
                validPosition = isValidPlanePosition(headRow, headCol, direction);//判断位置是否有效

                attempts++;//尝试次数+1
                //当次数>100
                if (attempts > 100) {
                    //弹出提示框
                    JOptionPane.showMessageDialog(
                            this,
                            "无法放置所有飞机，请尝试减少飞机数量或增大网格",
                            "提示",
                            JOptionPane.WARNING_MESSAGE
                    );
                    targetPlaneCount = planes.size();//更新已经成功放置飞机的数量
                    return;
                }
            } while (!validPosition);
            planes.add(new Plane(headRow, headCol, direction));//创建一个新的Plane对象，并添加到planes列表中
        }
    }

    // 检查飞机是否超出边界
    private boolean isValidPlanePosition(int headRow, int headCol, int direction) {
        int[][] offsets = getRotatedOffsets(direction);//获取飞机朝向 direction 旋转后描述了飞机相对于头部位置所占据的各个格子的相对位置的偏移量数组。
        for (int[] offset : offsets) {
            int r = headRow + offset[0];//机头行坐标+行坐标偏移量
            int c = headCol + offset[1];//机头列坐标+列坐标偏移量
            //边界检查
            if (r < 0 || r >= gridSize || c < 0 || c >= gridSize) {
                return false;
            }
        }

        // 检查是否与其他飞机重叠
        for (Plane plane : planes) {
            if (planesOverlap(headRow, headCol, direction, plane.headRow, plane.headCol, plane.direction)) {
                return false;
            }
        }

        return true;
    }

    //检查两架飞机在网格中是否重叠
    private boolean planesOverlap(int row1, int col1, int dir1, int row2, int col2, int dir2) {
        // 获取两个飞机的所有格子位置（通过行坐标、列坐标、方向计算）
        List<int[]> plane1Cells = getAllPlaneCells(row1, col1, dir1);
        List<int[]> plane2Cells = getAllPlaneCells(row2, col2, dir2);

        // 检查是否有重叠
        for (int[] cell1 : plane1Cells) {
            for (int[] cell2 : plane2Cells) {
                if (cell1[0] == cell2[0] && cell1[1] == cell2[1]) {
                    return true;
                }
            }
        }
        return false;
    }


    //根据机头位置及朝向，计算机身的位置
    private List<int[]> getAllPlaneCells(int headRow, int headCol, int direction) {
        List<int[]> cells = new ArrayList<>();//用于存储机身所占格子数
        cells.add(new int[]{headRow, headCol}); // 添加头部

        // 添加机身
        int[][] offsets = getRotatedOffsets(direction);
        //循环体，对每个元素执行的操作(元素类型 元素变量 : 数组或集合)
        for (int[] offset : offsets) {
            cells.add(new int[]{headRow + offset[0], headCol + offset[1]});
        }

        return cells;
    }

//根据给定的方向direction对飞机的原始偏移量进行旋转，得到旋转后的偏移量数组
    private int[][] getRotatedOffsets(int direction) {
        int[][] originalOffsets = getPlaneOffsets();//获取原始偏移量数组
        int[][] rotatedOffsets = new int[originalOffsets.length][2];

        for (int i = 0; i < originalOffsets.length; i++) {
            int x = originalOffsets[i][0];
            int y = originalOffsets[i][1];

            // 根据方向旋转偏移量（交换行列坐标+/-偏移量）
            switch (direction) {
                case 0: // 上 (原始方向)
                    rotatedOffsets[i][0] = x;
                    rotatedOffsets[i][1] = y;
                    break;
                case 1: // 右 (顺时针90度)
                    rotatedOffsets[i][0] = -y;
                    rotatedOffsets[i][1] = x;
                    break;
                case 2: // 下 (180度)
                    rotatedOffsets[i][0] = -x;
                    rotatedOffsets[i][1] = -y;
                    break;
                case 3: // 左 (逆时针90度)
                    rotatedOffsets[i][0] = y;
                    rotatedOffsets[i][1] = -x;
                    break;
            }
        }

        return rotatedOffsets;
    }

    //处理点击网格按钮事件
    private void handleGridClick(int row, int col) {
        if (gameOver) return;
        //更新尝试次数
        attempts=attempts+1;
        attemptsLabel.setText("尝试: " + attempts + "次");
        JButton button = gridButtons[row][col];
        //禁用点击过的按钮
        button.setEnabled(false);

        // 检查是否点击了飞机头部或机身
        boolean hitHead = false;
        boolean hitBody = false;
        Plane hitPlane = null;

        for (Plane plane : planes) {
            // 检查头部
            if (row == plane.headRow && col == plane.headCol) {
                hitHead = true;
                hitPlane = plane;
                break;
            }

            // 检查机身
            int[][] offsets = getRotatedOffsets(plane.direction);
            for (int[] offset : offsets) {
                int r = plane.headRow + offset[0];
                int c = plane.headCol + offset[1];
                if (r == row && c == col) {
                    hitBody = true;//标记击中机身为true
                    break;
                }
            }
            if (hitBody) break;
        }

        //若击中机头
        if (hitHead) {
            button.setBackground(headColor);
            // 显示飞机身体
            revealPlane(hitPlane);
            // 从列表中移除已找到的飞机
            planes.remove(hitPlane);
            foundPlanes++;

            // 更新状态
            statusLabel.setText(
                    "寻找 " + targetPlaneCount + " 架飞机 (已找到 " + foundPlanes + ")"
            );

            // 检查是否找到所有飞机
            if (foundPlanes >= targetPlaneCount) {
                gameOver = true;
                gamesWon++;
                updateBestScores();
                gameTimer.stop();
                statusLabel.setText(
                        "恭喜！找到所有 " + targetPlaneCount + " 架飞机，" +
                                "用时 " + secondsElapsed + " 秒，尝试 " + attempts + " 次"
                );
                
                // 保存分数到排行榜
                String playerName = JOptionPane.showInputDialog(
                    this,
                    "请输入您的名字以保存到排行榜:",
                    "排行榜记录",
                    JOptionPane.PLAIN_MESSAGE
                );
                
                if (playerName != null && !playerName.trim().isEmpty()) {
                    RankingDB rankingDB = new RankingDB();
                    rankingDB.addScore(playerName, secondsElapsed);
                    rankingDB.close();
                }
            }
        } else if (hitBody) {
            button.setBackground(bodyColor);
            statusLabel.setText(
                    "点击到了机身！继续寻找飞机头部，" +
                            "已找到 " + foundPlanes + "/" + targetPlaneCount
            );
        } else {
            button.setBackground(missColor);
            statusLabel.setText(
                    "未击中飞机，" +
                            "已找到 " + foundPlanes + "/" + targetPlaneCount
            );
        }
    }

    //揭示飞机所在位置
    private void revealPlane(Plane plane) {
        int[][] offsets = getRotatedOffsets(plane.direction);
        for (int[] offset : offsets) {
            int r = plane.headRow + offset[0];
            int c = plane.headCol + offset[1];
            if (r >= 0 && r < gridSize && c >= 0 && c < gridSize) {
                gridButtons[r][c].setText("身");
                gridButtons[r][c].setBackground(bodyColor);
            }
        }
    }

    public int[][] getPlaneOffsets() {
        switch (currentShape) {
            case "机型1": return new int[][] {{1,0},{1,2},{1,1},{1,-1},{1,-2},{2,0},{3,0},{3,1},{3,-1}};
            case "机型2": return new int[][] {{1,-1},{1,0},{1,1},{2,-2},{2,0},{2,2},{3,-3},{3,0},{3,3},{4,-1},{4,0},{4,1}};
            case "机型3": return new int[][] {{1,0},{2,-2},{2,-1},{2,0},{2,1},{2,2},{3,0},{4,-1},{4,1}};
            case "机型4": return new int[][] {{1,-1},{1,0},{1,1},{2,-2},{2,0},{2,2},{3,-1},{3,0},{3,1}};
            case "机型5": return new int[][] {{1,-2},{1,0},{1,-1},{1,1},{1,2},{2,0},{3,-1},{3,1}};
            case "机型6": return new int[][] {{1,-1},{1,0},{1,1},{2,-2},{2,0},{2,2},{3,-1},{3,1}};
            default: return new int[][]{{1,0}, {2,0}};
        }
    }

    //更新最好成绩
    private void updateBestScores() {
        //比较用时
        if (secondsElapsed < bestTime) {
            bestTime = secondsElapsed;
        }
        //比较步数
        if (attempts < bestAttempts) {
            bestAttempts = attempts;
        }
    }

    private void showStatistics() {
        String statsText = String.format(
                "<html><div style='width:250px;'><h2>游戏统计</h2>" +
                        "<p>已玩局数: %d</p>" +
                        "<p>获胜局数: %d</p>" +
                        "<p>最佳时间: %s</p>" +
                        "<p>最少尝试: %s</p></div></html>",
                gamesPlayed,
                gamesWon,
                //使用三元运算符进行判断：如果它们的值为 Integer.MAX_VALUE，表示还没有有效的最佳成绩，显示 "N/A"；否则，将其值与单位（秒或次）拼接后显示。
                bestTime == Integer.MAX_VALUE ? "N/A" : bestTime + "秒",
                bestAttempts == Integer.MAX_VALUE ? "N/A" : bestAttempts + "次"
        );
        JOptionPane.showMessageDialog(this, statsText, "游戏统计", JOptionPane.INFORMATION_MESSAGE);
    }

    //更改外观
    private void changeAppearance() {
        JPanel appearancePanel = new JPanel(new GridLayout(2, 2, 5, 5));

        JButton headColorBtn = new JButton("飞机头部颜色");
        headColorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "选择飞机头部颜色", headColor);
            if (newColor != null) headColor = newColor;
        });

        JButton bodyColorBtn = new JButton("飞机机身颜色");
        bodyColorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "选择飞机机身颜色", bodyColor);
            if (newColor != null) bodyColor = newColor;
        });

        JButton bgColorBtn = new JButton("背景颜色");
        bgColorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "选择背景颜色", getBackground());
            if (newColor != null) missColor=newColor; /*frame.getContentPane().setBackground(newColor);*/
        });

        appearancePanel.add(headColorBtn);
        appearancePanel.add(bodyColorBtn);
        appearancePanel.add(bgColorBtn);

        JOptionPane.showMessageDialog(this, appearancePanel, "更改外观", JOptionPane.PLAIN_MESSAGE);
    }

    //展示寻找的飞机数量
    private void setPlaneCount() {
        String input = JOptionPane.showInputDialog(
        		this,
                "输入要寻找的飞机数量 (1-5):",
                targetPlaneCount
        );
        try {
            int newCount = Integer.parseInt(input);
            if (newCount >= 1 && newCount <= 5) {
                targetPlaneCount = newCount;
                startNewGame();
            } else {
                JOptionPane.showMessageDialog(
                		this,
                        "请输入1到5之间的数字",
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "请输入有效的数字",
                    "错误",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
